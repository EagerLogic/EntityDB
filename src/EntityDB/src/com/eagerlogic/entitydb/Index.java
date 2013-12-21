package com.eagerlogic.entitydb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author dipacs
 */
final class Index<T> {

	private final HashMap<String, HashMap<String, TreeMap<T, List<Long>>>> index = new HashMap<>();
	private final HashMap<String, T> valueCache = new HashMap<>();

	Index() {
	}

	public void put(String kind, String attributeName, long entityId, T value) {
		remove(kind, attributeName, entityId);
		HashMap<String, TreeMap<T, List<Long>>> kindMap = getKindMap(kind);
		TreeMap<T, List<Long>> attributeMap = getAttributeMap(kindMap, attributeName);

		List<Long> entityList = attributeMap.get(value);
		if (entityList == null) {
			entityList = new LinkedList<>();
			attributeMap.put(value, entityList);
		}

		entityList.add(entityId);
		valueCache.put(kind + "-" + attributeName + "-" + entityId, value);
	}

	public void remove(String kind, String attributeName, long entityId) {
		T oldValue = valueCache.remove(kind + "-" + attributeName + "-" + entityId);
		if (oldValue == null) {
			return;
		}

		HashMap<String, TreeMap<T, List<Long>>> kindMap = getKindMap(kind);
		TreeMap<T, List<Long>> attributeMap = getAttributeMap(kindMap, attributeName);

		List<Long> entityList = attributeMap.get(oldValue);
		if (entityList == null) {
			return;
		}

		Iterator<Long> it = entityList.iterator();
		while (it.hasNext()) {
			if (it.next().longValue() == entityId) {
				it.remove();
			}
		}

	}

	public List<Long> getKeys(String kind, String attributeName, T value) {
		HashMap<String, TreeMap<T, List<Long>>> kindMap = getKindMap(kind);
		TreeMap<T, List<Long>> attributeMap = getAttributeMap(kindMap, attributeName);

		return attributeMap.get(value);
	}

	public T get(String kind, String attributeName, long entityId) {
		return valueCache.get(kind + "-" + attributeName + "-" + entityId);
	}

	public List<Long> query(String kind, AFilterItem filter) {
		if (filter == null) {
			throw new NullPointerException("The filter parameter can not be null.");
		}

		List<Long> res = new LinkedList<>();
		HashMap<String, TreeMap<T, List<Long>>> kindMap = getKindMap(kind);
		if (filter instanceof FilterGroupItem) {
			throw new IllegalArgumentException("FilterGroups can't applied on indexes.");
		} else if (filter instanceof NullFilterItem) {
			filterNull(filter, kindMap, res);
		} else if (filter instanceof LongFilterItem) {
			filterLong(filter, kindMap, res);
		} else if (filter instanceof BooleanFilterItem) {
			filterBoolean(filter, kindMap, res);
		} else if (filter instanceof StringFilterItem) {
			filterString(filter, kindMap, res);
		} else {
			throw new IllegalArgumentException("Invalid filter type: " + filter.getClass().getName());
		}
		
		return res;
	}

	private HashMap<String, TreeMap<T, List<Long>>> getKindMap(String kind) {
		HashMap<String, TreeMap<T, List<Long>>> kindMap = index.get(kind);
		if (kindMap == null) {
			kindMap = new HashMap<>();
			index.put(kind, kindMap);
		}
		return kindMap;
	}

	private TreeMap<T, List<Long>> getAttributeMap(HashMap<String, TreeMap<T, List<Long>>> kindMap, String attributeName) {
		TreeMap<T, List<Long>> attributeIndex = kindMap.get(attributeName);
		if (attributeIndex == null) {
			attributeIndex = new TreeMap<>();
			kindMap.put(attributeName, attributeIndex);
		}
		return attributeIndex;
	}

	private void filterLong(AFilterItem filter, HashMap<String, TreeMap<T, List<Long>>> kindMap, List<Long> res) throws IllegalArgumentException {
		LongFilterItem longFilter = (LongFilterItem) filter;
		TreeMap<T, List<Long>> attributeMap = getAttributeMap(kindMap, longFilter.getAttributeName());
		if (attributeMap != null) {
			if (longFilter.getOperator() == LongFilterItem.EOperator.EQUALS) {
				res.addAll(attributeMap.get(longFilter.getReferenceValue()));
			} else if (longFilter.getOperator() == LongFilterItem.EOperator.GREATER) {
				for (Entry<T, List<Long>> entry : attributeMap.entrySet()) {
					long key = (long) (Long) entry.getKey();
					if (key > longFilter.getReferenceValue()) {
						res.addAll(entry.getValue());
					}
				}
			} else if (longFilter.getOperator() == LongFilterItem.EOperator.SMALLER) {
				for (Entry<T, List<Long>> entry : attributeMap.entrySet()) {
					long key = (long) (Long) entry.getKey();
					if (key < longFilter.getReferenceValue()) {
						res.addAll(entry.getValue());
					} else {
						// other keys are greater or equals
						break;
					}
				}
			} else if (longFilter.getOperator() == LongFilterItem.EOperator.GREATER) {
				for (Entry<T, List<Long>> entry : attributeMap.entrySet()) {
					long key = (long) (Long) entry.getKey();
					if (key != longFilter.getReferenceValue()) {
						res.addAll(entry.getValue());
					}
				}
			} else {
				throw new IllegalArgumentException("Illegal LongFilter operator: " + longFilter.getOperator().name());
			}
		}
	}

	private void filterString(AFilterItem filter, HashMap<String, TreeMap<T, List<Long>>> kindMap, List<Long> res) throws IllegalArgumentException {
		StringFilterItem stringFilter = (StringFilterItem) filter;
		TreeMap<T, List<Long>> attributeMap = kindMap.get(stringFilter.getAttributeName());
		if (attributeMap != null) {
			for (Entry<T, List<Long>> entry : attributeMap.entrySet()) {
				if (stringFilter.getOperator() == StringFilterItem.EOperator.CONTAINS) {
					if (((String)entry.getKey()).contains(stringFilter.getReferenceValue())) {
						res.addAll(entry.getValue());
					}
				} else if (stringFilter.getOperator() == StringFilterItem.EOperator.EQUALS) {
					if (((String)entry.getKey()).equals(stringFilter.getReferenceValue())) {
						res.addAll(entry.getValue());
					}
				} else if (stringFilter.getOperator() == StringFilterItem.EOperator.GREATER) {
					if (((String)entry.getKey()).compareTo(stringFilter.getReferenceValue()) > 0) {
						res.addAll(entry.getValue());
					}
				} else if (stringFilter.getOperator() == StringFilterItem.EOperator.NOT_EQUALS) {
					if (!((String)entry.getKey()).equals(stringFilter.getReferenceValue())) {
						res.addAll(entry.getValue());
					}
				} else if (stringFilter.getOperator() == StringFilterItem.EOperator.SMALLER) {
					if (((String)entry.getKey()).compareTo(stringFilter.getReferenceValue()) < 0) {
						res.addAll(entry.getValue());
					}
				} else {
					throw new IllegalArgumentException("Invalid StringFilterOperator: " + stringFilter.getOperator().name());
				}
			}
		}
	}

	private void filterBoolean(AFilterItem filter, HashMap<String, TreeMap<T, List<Long>>> kindMap, List<Long> res) {
		BooleanFilterItem boolFilter = (BooleanFilterItem) filter;
		TreeMap<T, List<Long>> values = kindMap.get(boolFilter.getAttributeName());
		if (values != null) {
			List<Long> r = values.get(boolFilter.getReferenceValue());
			if (r != null) {
				res.addAll(r);
			}
		}
	}

	private void filterNull(AFilterItem filter, HashMap<String, TreeMap<T, List<Long>>> kindMap, List<Long> res) {
		NullFilterItem nullFilter = (NullFilterItem) filter;
		TreeMap<T, List<Long>> attributeMap = kindMap.get(nullFilter.getAttributeName());
		if (attributeMap != null) {
			for (List<Long> keys : attributeMap.values()) {
				res.addAll(keys);
			}
		}
	}
}
