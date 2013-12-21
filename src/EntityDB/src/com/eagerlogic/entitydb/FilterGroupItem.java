package com.eagerlogic.entitydb;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author dipacs
 */
public final class FilterGroupItem extends AFilterItem {
	
	public static enum EOperator {
		AND,
		OR
	}
	
	private final EOperator operator;
	private final List<AFilterItem> filters;

	public FilterGroupItem(EOperator operator, AFilterItem filter1, AFilterItem filter2, AFilterItem[] otherFilters) {
		if (operator == null) {
			throw new NullPointerException("The operator parameter can not be null.");
		}
		this.operator = operator;
		if (filter1 == null) {
			throw new NullPointerException("The filter1 parameter can not be null.");
		}
		if (filter2 == null) {
			throw new NullPointerException("The filter2 parameter can not be null.");
		}
		this.filters = new LinkedList<>();
		if (otherFilters != null) {
			for (AFilterItem filter : otherFilters) {
				this.filters.add(filter);
			}
		}
	}

	public EOperator getOperator() {
		return operator;
	}

	List<AFilterItem> getFilters() {
		return filters;
	}
	
}
