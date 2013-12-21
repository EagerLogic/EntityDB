package com.eagerlogic.entitydb;

/**
 *
 * @author dipacs
 */
public final class NullFilterItem extends AFilterItem {
	
	public static enum EOperator {
		NOT_EQUALS
	}
	
	private final String attributeName;
	private final EOperator operator;

	public NullFilterItem(String attributeName, EOperator operator) {
		if (attributeName == null) {
			throw new NullPointerException("The attributeName parameter can not be null.");
		}
		this.attributeName = attributeName;
		this.operator = operator;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public EOperator getOperator() {
		return operator;
	}
	
}
