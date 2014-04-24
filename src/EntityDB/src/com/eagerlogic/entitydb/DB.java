package com.eagerlogic.entitydb;

import java.util.List;

/**
 * This class represents a Database session.
 * 
 * @author dipacs
 */
public final class DB {
	
	private EntityDB entityDB;

	DB(EntityDB entityDB) {
		this.entityDB = entityDB;
	}
	
	/**
	 * Puts the given entity in to the database. If the given entity is already stored in the database, than the entity
	 * is updated, else the entity is stored as a new entity, and a new id is generated for it.
	 * 
	 * @param entity 
	 * The entity which will be stored.
	 */
	public void put(Entity entity) {
		entityDB.put(entity);
	}
	
	/**
	 * Removes an entity from the database by it's id.
	 * 
	 * @param id 
	 * The id of the database which will be removed.
	 */
	public void remove(long id) {
		entityDB.remove(id);
	}
	
	/**
	 * Reads an entity from the database by it's id.
	 * 
	 * @param id
	 * The id of the entity.
	 * 
	 * @return 
	 * The entity which has the given id.
	 */
	public Entity get(long id) {
		return entityDB.get(id);
	}
	
	/**
	 * Queryes the database using the given filter.
	 * 
	 * @param filter
	 * The filter which will be used to query the database.
	 * 
	 * @return 
	 * The keys of the matching entities.
	 */
	public List<Long> queryKeys(Filter filter) {
		return entityDB.queryKeys(filter);
	}
	
	/**
	 * Queryes the database using the given filter.
	 * 
	 * @param filter
	 * The filter which will be used to query the database.
	 * 
	 * @return 
	 * The matching entities.
	 */
	public List<Entity> query(Filter filter) {
		return entityDB.query(filter);
	}

	/**
	 * Executes a query based on the given filter and returns the only one result returned by the query.
	 * The query must return exectly one element, otherwise this method throws a RuntimeException.
	 * 
	 * @param filter
	 * The filter.
	 * 
	 * @return 
	 * The only one result returned by the query.
	 * 
	 * @throws RuntimeException if more or less than one entity is returned by the query.
	 */
	public Entity querySingleton(Filter filter) {
		return entityDB.querySingleton(filter);
	}

	/**
	 * Executes a query based on the given filter and returns the first result returned by the query.
	 * This method returns null if no results are found by the query.
	 * 
	 * @param filter
	 * The filter.
	 * 
	 * @return 
	 * The first result from the result set, or null if no result is returned by the query.
	 */
	public Entity queryFirst(Filter filter) {
		return entityDB.queryFirst(filter);
	}

	/**
	 * Executes a query based on the given filter and returns the index of the only one result returned by the query.
	 * The query must return exectly one element, otherwise this method throws a RuntimeException.
	 * 
	 * @param filter
	 * The filter.
	 * 
	 * @return 
	 * The key of the only one result returned by the query.
	 * 
	 * @throws RuntimeException if more or less than one entity is returned by the query.
	 */
	public long querySingletonKey(Filter filter) {
		return entityDB.querySingletonKey(filter);
	}

	/**
	 * Executes a query based on the given filter and returns the index of the first result returned by the query.
	 * This method returns null if no results are found by the query.
	 * 
	 * @param filter
	 * The filter.
	 * 
	 * @return 
	 * The first result from the result set, or -1 if no result is returned by the query.
	 */
	public long queryFirstKey(Filter filter) {
		return entityDB.queryFirstKey(filter);
	}

}
