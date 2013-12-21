package com.eagerlogic.entitydb;

/**
 *
 * @author dipacs
 */
public final class LongFilterItem extends AFilterItem {
	
	public static enum EOperator {
		SMALLER,
		GREATER,
		EQUALS,
		NOT_EQUALS
	}
	
	private final String attributeName;
	private final EOperator operator;
	private final long referenceValue;

	public LongFilterItem(String attributeName, EOperator operator, long referenceValue) {
		if (attributeName == null) {
			throw new NullPointerException("The attributeName parameter can not be null.");
		}
		if (operator == null) {
			throw new NullPointerException("The operator parameter can not be null.");
		}
		this.attributeName = attributeName;
		this.operator = operator;
		this.referenceValue = referenceValue;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public EOperator getOperator() {
		return operator;
	}

	public long getReferenceValue() {
		return referenceValue;
	}

}
