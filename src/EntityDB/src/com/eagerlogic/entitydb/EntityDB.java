package com.eagerlogic.entitydb;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * This class represents a database connection. You need one EntityDB instance per database.
 * 
 * @author dipacs
 */
public final class EntityDB {

	private static final byte[] FILE_HEADER = new byte[]{(byte) 0xed, (byte) 0xb0};
	private static final long HEADER_LENGTH = FILE_HEADER.length + 8;

	/**
	 * Opens the given database, or creates a newone if the given file does not exists.
	 * 
	 * @param dbFile
	 * The url of the db file.
	 * 
	 * @return 
	 * The EntityDB instance which represents a database connection.
	 */
	public static synchronized EntityDB connect(File dbFile) {
		return new EntityDB(dbFile);
	}
	private long nextId;
	private final RandomAccessFile db;
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private boolean closed = false;
	private FileLock fileLock;
	private DbFileStructure structure = new DbFileStructure(HEADER_LENGTH);
	private final HashMap<Long, Long> idCache = new HashMap<>();
	private final Index<Long> longIndex = new Index<>();
	private final Index<Boolean> boolIndex = new Index<>();
	private final Index<String> stringIndex = new Index<>();

	private EntityDB(File dbFile) {
		if (!dbFile.exists()) {
			try {
				if (!dbFile.createNewFile()) {
					throw new RuntimeException("Can't create file.");
				} else {
					FileOutputStream fos = null;
					DataOutputStream dos = null;
					try {
						fos = new FileOutputStream(dbFile);
						dos = new DataOutputStream(fos);
						dos.write(FILE_HEADER);
						dos.writeLong(-1l);
					} finally {
						if (dos != null) {
							dos.close();
						}
						if (fos != null) {
							fos.close();
						}
					}
				}
			} catch (IOException ex) {
				throw new RuntimeException("Can't create file.", ex);
			}
		} else if (dbFile.isDirectory()) {
			throw new RuntimeException("The given file is a directory.");
		}
		try {
			db = new RandomAccessFile(dbFile, "rwd");
		} catch (FileNotFoundException ex) {
			throw new RuntimeException("Can't create file.", ex);
		}
		try {
			fileLock = db.getChannel().lock();
		} catch (IOException ex) {
			throw new RuntimeException("The file is in use.", ex);
		}
		try {
			if (db.read() != (FILE_HEADER[0] & 0xff)) {
				throw new RuntimeException("Invalid database format.");
			}
			if (db.read() != (FILE_HEADER[1] & 0xff)) {
				throw new RuntimeException("Invalid database format.");
			}
		} catch (IOException ex) {
			throw new RuntimeException("Can't read database.", ex);
		}

		cacheDb();
	}

	private void cacheDb() {
		try {
			long currentOffset = FILE_HEADER.length;
			currentOffset = db.readLong();
			long maxId = 0;
			while (db.getFilePointer() < db.length()) {
				long nextOffset = db.readLong();
				ObjectInputStream ois = new ObjectInputStream(Channels.newInputStream(db.getChannel().position(currentOffset + 8)));
				Entity e = (Entity) ois.readObject();

				if (e.getId() > maxId) {
					maxId = e.getId();
				}
				structure.addPiece(currentOffset, db.getFilePointer() - currentOffset);
				idCache.put(e.getId(), currentOffset);
				cacheAttributes(e);

				if (nextOffset < 0) {
					break;
				}
				currentOffset = nextOffset;
			}
			nextId = maxId + 1;
		} catch (Throwable ex) {
			throw new RuntimeException("Error reading db file.", ex);
		}
	}

	private void cacheAttributes(Entity entity) {
		for (String attributeName : entity.getAttributeNames()) {
			longIndex.remove(entity.getKind(), attributeName, entity.getId());
			stringIndex.remove(entity.getKind(), attributeName, entity.getId());
			boolIndex.remove(entity.getKind(), attributeName, entity.getId());
			Object value = entity.getAttribute(attributeName);
			if (value instanceof Long) {
				longIndex.put(entity.getKind(), attributeName, entity.getId(), (Long) value);
			} else if (value instanceof String) {
				stringIndex.put(entity.getKind(), attributeName, entity.getId(), ((String) value).toLowerCase());
			} else if (value instanceof Boolean) {
				boolIndex.put(entity.getKind(), attributeName, entity.getId(), (Boolean) value);
			} else {
				throw new RuntimeException("Invalid value in database.");
			}
		}
	}

	private void uncacheAttribute(Entity entity) {
		for (String attributeName : entity.getAttributeNames()) {
			longIndex.remove(entity.getKind(), attributeName, entity.getId());
			stringIndex.remove(entity.getKind(), attributeName, entity.getId());
			boolIndex.remove(entity.getKind(), attributeName, entity.getId());
		}
	}

	synchronized long getNextId() {
		return nextId++;
	}

	/**
	 * Returns a new {@link DB} instance which can be used to operate with the database.
	 * 
	 * @return 
	 * The new {@link DB} instance which can be used to operate with the database. 
	 */
	public synchronized DB getDB() {
		if (closed) {
			throw new IllegalStateException("This db is closed.");
		}

		return new DB(this);
	}

	void put(Entity entity) {
		if (closed) {
			throw new IllegalStateException("This db is closed.");
		}

		WriteLock lock = readWriteLock.writeLock();
		lock.lock();
		try {
			long id = entity.getId();
			if (entity.getId() > -1) {
				remove(entity.getId());
			}
			if (id < 0) {
				id = getNextId();
				entity.setId(id);
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(entity);
			oos.flush();
			byte[] bytes = baos.toByteArray();

			long[] offsets = structure.getEmptySpace(bytes.length + 8);
			long prevOffset = offsets[0];
			long freeOffset = offsets[1];
			long nextOffset = offsets[2];

			db.seek(freeOffset);
			db.writeLong(nextOffset);
			db.write(bytes);
			if (prevOffset > -1) {
				db.seek(prevOffset);
				db.writeLong(freeOffset);
			} else {
				db.seek(FILE_HEADER.length);
				db.writeLong(freeOffset);
			}

			structure.addPiece(freeOffset, bytes.length + 8);

			idCache.put(id, freeOffset);
			cacheAttributes(entity);
		} catch (IOException ex) {
			throw new RuntimeException("Error writing database.", ex);
		} finally {
			lock.unlock();
		}
	}

	void remove(long id) {
		if (closed) {
			throw new IllegalStateException("This db is closed.");
		}

		WriteLock lock = readWriteLock.writeLock();
		lock.lock();
		try {

			Long offset = idCache.get(id);
			if (offset == null) {
				throw new IllegalArgumentException("No entity can be found with id: " + id);
			}
			long[] surround = structure.getPieceSurround(offset);
			long prevOffset = surround[0];
			long currOffset = surround[1];
			long nextOffset = surround[2];

			Entity entity = read(id);

			if (prevOffset > -1) {
				db.seek(prevOffset);
				db.writeLong(nextOffset);
			} else {
				db.seek(FILE_HEADER.length);
				db.writeLong(currOffset);
			}

			structure.removePiece(currOffset);

			uncacheAttribute(entity);
		} catch (IOException ex) {
			throw new RuntimeException("Error writing database.", ex);
		} finally {
			lock.unlock();
		}
	}

	Entity get(long id) {
		if (closed) {
			throw new IllegalStateException("This db is closed.");
		}

		ReadLock lock = readWriteLock.readLock();
		lock.lock();
		try {
			return read(id);
		} finally {
			lock.unlock();
		}
	}

	private Entity read(long id) {
		Long offset = idCache.get(id);
		if (offset == null) {
			return null;
		}
		try {
			db.seek(offset + 8);
			ObjectInputStream ois = new ObjectInputStream(Channels.newInputStream(db.getChannel().position(offset + 8)));
			Entity res = (Entity) ois.readObject();
			return res;
		} catch (Throwable ex) {
			throw new RuntimeException("Error reading db file.", ex);
		}

	}

	List<Long> queryKeys(Filter filter) {
		if (closed) {
			throw new IllegalStateException("This db is closed.");
		}

		if (filter == null) {
			throw new NullPointerException("The filter parameter can not be null.");
		}

		ReadLock lock = readWriteLock.readLock();
		lock.lock();
		try {
			return queryIndex(filter.getKind(), filter.getFilterItem());
		} finally {
			lock.unlock();
		}
	}
	
	List<Entity> query(Filter filter) {
		List<Long> keys = queryKeys(filter);
		List<Entity> res = new LinkedList<>();
		for (Long key : keys) {
			res.add(read(key));
		}
		return res;
	}
	
	Entity querySingleton(Filter filter) {
		List<Entity> results = query(filter);
		if (results.size() > 1) {
			throw new RuntimeException("More than one results are returned by the query.");
		} 
		
		if (results.size() < 1) {
			throw new RuntimeException("No results are returned by the query.");
		}
		
		return results.get(0);
	}
	
	Entity queryFirst(Filter filter) {
		List<Entity> results = query(filter);
		if (results.size() < 1) {
			return null;
		}
		
		return results.get(0);
	}
	
	long querySingletonKey(Filter filter) {
		List<Long> results = queryKeys(filter);
		if (results.size() > 1) {
			throw new RuntimeException("More than one results are returned by the query.");
		} 
		
		if (results.size() < 1) {
			throw new RuntimeException("No results are returned by the query.");
		}
		
		return results.get(0);
	}
	
	long queryFirstKey(Filter filter) {
		List<Long> results = queryKeys(filter);
		if (results.size() < 1) {
			return -1;
		}
		
		return results.get(0);
	}

	List<Long> queryIndex(String kind, AFilterItem filter) {
		HashMap<Long, Object> res = new HashMap<>();
		if (filter instanceof FilterGroupItem) {
			FilterGroupItem filterGroup = (FilterGroupItem) filter;
			if (filterGroup.getOperator() == FilterGroupItem.EOperator.AND) {
				// and
				for (AFilterItem filterItem : filterGroup.getFilters()) {
					List<Long> itemRes = queryIndex(kind, filterItem);
					if (res.size() < 1) {
						for (Long l : itemRes) {
							res.put(l, Boolean.TRUE);
						}
					} else {
						Iterator<Long> it = itemRes.iterator();
						while (it.hasNext()) {
							Long item = it.next();
							if (res.get(item) == null) {
								it.remove();
							}
						}
						res.clear();
						for (Long l : itemRes) {
							res.put(l, Boolean.TRUE);
						}
					}
				}
			} else {
				// or
				for (AFilterItem filterItem : filterGroup.getFilters()) {
					List<Long> itemRes = queryIndex(kind, filterItem);
					for (Long l : itemRes) {
						res.put(l, Boolean.TRUE);
					}
				}

			}
		} else if (filter instanceof NullFilterItem) {
			List<Long> resPart = longIndex.query(kind, filter);
			resPart.addAll(boolIndex.query(kind, filter));
			resPart.addAll(stringIndex.query(kind, filter));
			return resPart;
		} else if (filter instanceof LongFilterItem) {
			List<Long> resPart = longIndex.query(kind, filter);
			return resPart;
		} else if (filter instanceof StringFilterItem) {
			List<Long> resPart = stringIndex.query(kind, filter);
			return resPart;
		} else if (filter instanceof BooleanFilterItem) {
			List<Long> resPart = boolIndex.query(kind, filter);
			return resPart;
		}
		List<Long> result = new LinkedList<>();
		for (Long l : res.keySet()) {
			result.add(l);
		}

		return result;
	}

	public boolean isClosed() {
		return closed;
	}

	/**
	 * Closes the database. This method first calls EntityDB.close(false) than if it's fails it calls EntityDB.close(true).
	 * 
	 * @return 
	 * True if the database is force closed, otherwise false.
	 */
	public synchronized boolean close() {
		try {
			close(false);
			return false;
		} catch (Throwable t) {
		}
		try {
			close(true);
		} catch (Throwable t) {
		}
		return true;
	}

	/**
	 * Closes this database.
	 * 
	 * @param force 
	 * Defines if this database needs to be force closed or not. If this parameter is true, than this method tries to 
	 * close the database anyway, no exception is thrown. If this parameter is false, than this method throws a RuntimeException
	 * if closing is failed by any reason.
	 */
	public synchronized void close(boolean force) {
		if (closed) {
			throw new IllegalStateException("This db is closed.");
		}

		try {
			db.close();
		} catch (IOException ex) {
			if (!force) {
				throw new RuntimeException("Can't close database.", ex);
			}
		}
		try {
			fileLock.release();
		} catch (IOException ex) {
			if (!force) {
				throw new RuntimeException("Can't release file lock.", ex);
			}
		}

		closed = true;
	}
}
