package com.eagerlogic.entitydb;

/**
 *
 * @author dipacs
 */
public final class BooleanFilterItem extends AFilterItem {
	
	private final String attributeName;
	private final boolean referenceValue;

	public BooleanFilterItem(String attributeName, boolean referenceValue) {
		if (attributeName == null) {
			throw new NullPointerException("The attributeName parameter can not be null.");
		}
		this.attributeName = attributeName;
		this.referenceValue = referenceValue;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public boolean getReferenceValue() {
		return referenceValue;
	}

}
