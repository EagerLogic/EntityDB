package com.eagerlogic.entitydb;

import java.util.List;

/**
 *
 * @author dipacs
 */
public final class DB {
	
	private EntityDB entityDB;

	DB(EntityDB entityDB) {
		this.entityDB = entityDB;
	}
	
	public void put(Entity entity) {
		entityDB.put(entity);
	}
	
	public void remove(long id) {
		entityDB.remove(id);
	}
	
	public Entity get(long id) {
		return entityDB.get(id);
	}
	
	public List<Long> queryKeys(Filter filter) {
		return entityDB.queryKeys(filter);
	}
	
	public List<Entity> query(Filter filter) {
		return entityDB.query(filter);
	}

}
