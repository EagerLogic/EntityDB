package com.eagerlogic.entitydb;

/**
 *
 * @author dipacs
 */
public final class StringFilterItem extends AFilterItem {
	
	public static enum EOperator {
		SMALLER,
		GREATER,
		EQUALS,
		NOT_EQUALS,
		CONTAINS
	}
	
	private final String attributeName;
	private final EOperator operator;
	private final String referenceValue;

	public StringFilterItem(String attributeName, EOperator operator, String referenceValue) {
		if (attributeName == null) {
			throw new NullPointerException("The attributeName parameter can not be null.");
		}
		if (operator == null) {
			throw new NullPointerException("The operator parameter can not be null.");
		}
		this.attributeName = attributeName;
		this.operator = operator;
		if (referenceValue == null) {
			throw new NullPointerException("The referenceValue can not be null. Use NullFilterItem instead.");
		}
		this.referenceValue = referenceValue;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public EOperator getOperator() {
		return operator;
	}

	public String getReferenceValue() {
		return referenceValue;
	}

}
