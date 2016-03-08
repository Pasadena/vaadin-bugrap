package com.example.bugrap.constants;

public enum AssigneeSelections {
	
	FOR_ME("Only me"),
	EVERYONE("Everyone");
	
	private String selectionValue;
	
	private AssigneeSelections(final String value) {
		this.selectionValue = value;
	}

	public String getSelectionValue() {
		return selectionValue;
	}
}
