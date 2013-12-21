package com.eagerlogic.entitydb;

import java.util.Iterator;
import java.util.TreeMap;

/**
 *
 * @author dipacs
 */
final class DbFileStructure implements Iterable<DbFileStructure.Piece> {

	public static class Piece {

		private final long offset;
		private final long length;

		public Piece(long start, long length) {
			this.offset = start;
			this.length = length;
		}
	}
	private final TreeMap<Long, Piece> pieces = new TreeMap<>();
	private final long headerSize;

	DbFileStructure(long headerSize) {
		this.headerSize = headerSize;
	}

	public void addPiece(long offset, long length) {
		pieces.put(offset, new Piece(offset, length));
	}

	public void removePiece(long offset) {
		if (pieces.remove(offset) == null) {
			throw new RuntimeException("Internal error. Can't find piece with offset: " + offset);
		}
	}

	public Piece getPiece(long offset) {
		return pieces.get(offset);
	}

	public long[] getEmptySpace(long size) {
		long lastFreeByte = headerSize;
		long prevOffset = -1;
		long nextOffset = -1;
		for (Piece piece : pieces.values()) {
			if (piece.offset - lastFreeByte >= size) {
				nextOffset = piece.offset;
				break;
			}
			prevOffset = piece.offset;
			lastFreeByte = piece.offset + piece.length;
		}
		return new long[]{prevOffset, lastFreeByte, nextOffset};
	}

	public long[] getPieceSurround(long offset) {
		long prevOffset = -1;
		long exactOffset = -1;
		long nextOffset = -1;
		for (Piece piece : pieces.values()) {
			if (piece.offset < offset) {
				prevOffset = piece.offset;
			} else if (piece.offset > offset) {
				nextOffset = piece.offset;
				break;
			} else {
				exactOffset = piece.offset;
			}
		}

		if (exactOffset < 0) {
			throw new IllegalArgumentException("No piece can be found with the given offset: " + offset);
		}
		return new long[] {prevOffset, exactOffset, nextOffset};
	}

	public Iterator<Piece> iterator() {
		return pieces.values().iterator();
	}
}
