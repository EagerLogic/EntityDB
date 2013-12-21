package com.eagerlogic.entitydb;

/**
 *
 * @author dipacs
 */
public final class Filter {
	
	private final String kind;
	private final AFilterItem filterItem;

	public Filter(String kind, AFilterItem filterItem) {
		if (kind == null) {
			throw new NullPointerException("The kind parameter can not be null.");
		}
		this.kind = kind;
		
		if (filterItem == null) {
			throw new NullPointerException("The filterItem parameter can not be null.");
		}
		this.filterItem = filterItem;
	}

	public String getKind() {
		return kind;
	}

	public AFilterItem getFilterItem() {
		return filterItem;
	}

}
