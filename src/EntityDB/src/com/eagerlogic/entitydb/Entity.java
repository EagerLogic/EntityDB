package com.eagerlogic.entitydb;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a persistable object.
 *
 * @author dipacs
 */
public final class Entity implements Serializable {

    private long id = -1;
    private String kind;
    private Object value;
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    Entity() {
    }

    /**
     * Creates a new Entity with the given kind.
     *
     * @param kind The kind of the entity.
     */
    public Entity(String kind) {
        this.kind = kind;
    }

    void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the id of this entity. The id is filled automatically after
     * storing it in the database first, using the <code>DB.put(Entity)</code>
     * method.
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the kind of this entity.
     *
     * @return The kind of this entity.
     */
    public String getKind() {
        return kind;
    }

    /**
     * Returns the value of this entity.
     *
     * @return The value of this entity.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of this entity.
     *
     * @param value The new value of this entity.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Puts the given long value as an attribute associated with the given key.
     *
     * @param key The key of the given attribute.
     * @param value The value of the attribute.
     */
    public void putAttribute(String key, long value) {
        attributes.put(key, value);
    }

    /**
     * Puts the given boolean value as an attribute associated with the given
     * key.
     *
     * @param key The key of the given attribute.
     * @param value The value of the attribute.
     */
    public void putAttribute(String key, boolean value) {
        attributes.put(key, value);
    }

    /**
     * Puts the given String value as an attribute associated with the given
     * key.
     *
     * @param key The key of the given attribute.
     * @param value The value of the attribute.
     */
    public void putAttribute(String key, String value) {
        attributes.put(key, value);
    }

    /**
     * Returns the attribute associated with the given key as long.
     *
     * @param key The key of the attribute.
     *
     * @return The attribute associated with the given key.
     */
    public long getLongAttribute(String key) {
        return ((Long) attributes.get(key));
    }

    /**
     * Returns the attribute associated with the given key as boolean.
     *
     * @param key The key of the attribute.
     *
     * @return The attribute associated with the given key.
     */
    public boolean getBooleanAttribute(String key) {
        return ((Boolean) attributes.get(key));
    }

    /**
     * Returns the attribute associated with the given key as String.
     *
     * @param key The key of the attribute.
     *
     * @return The attribute associated with the given key.
     */
    public String getStringAttribute(String key) {
        return ((String) attributes.get(key));
    }

    /**
     * Returns the attribute associated with the given key as an Object.
     *
     * @param key The key of the attribute.
     *
     * @return The attribute associated with the given key.
     */
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * Indicates if the given key is null (not set).
     *
     * @param key The key of the attribute.
     *
     * @return True if the given attribute is null.
     */
    public boolean isAttributeNull(String key) {
        return attributes.get(key) == null;
    }

    /**
     * Returns the name of the attributes which are associated with this Entity.
     *
     * @return The name of the attributes which are associated with this Entity.
     */
    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

}
