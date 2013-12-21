package com.eagerlogic.entitydb;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author dipacs
 */
public final class Entity implements Serializable {
	
	private long id = -1;
	private String kind;
	private Object value;
	private final Map<String, Object> attributes = new HashMap<String, Object>();
	
	Entity() {}
	
	public Entity(String kind) {
		this.kind = kind;
	}

	void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getKind() {
		return kind;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public void putAttribute(String key, long value) {
		attributes.put(key, value);
	}
	
	public void putAttribute(String key, boolean value) {
		attributes.put(key, value);
	}
	
	public void putAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	public long getLongAttribute(String key) {
		return ((Long) attributes.get(key));
	}
	
	public boolean getBooleanAttribute(String key) {
		return ((Boolean) attributes.get(key));
	}
	
	public String getStringAttribute(String key) {
		return ((String) attributes.get(key));
	}
	
	public <T> T getAttribute(String key) {
		return (T) attributes.get(key);
	}
	
	public boolean isAttributeNull(String key) {
		return attributes.get(key) == null;
	}
	
	public Set<String> getAttributeNames() {
		return attributes.keySet();
	}

}
