package com.example.bugrap.constants;

public enum SessionAttributeConstants {
	
	LOGGED_IN_USER("loggedInUser"),
	SELECTED_VERSION("selectedVersion");
	
	private String attributeName;
	
	private SessionAttributeConstants(final String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeName() {
		return attributeName;
	}

}
