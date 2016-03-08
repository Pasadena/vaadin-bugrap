package com.example.events;

import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

@SuppressWarnings("serial")
public class FilterChangedEvent extends Event {
	
	private final Object filterValue;
	private final String filterName;
	
	public FilterChangedEvent(final Component source, String filterName, Object filterValue) {
		super(source);
		this.filterValue = filterValue;
		this.filterName = filterName;
	}
	

	public String getFilterName() {
		return filterName;
	}


	public Object getFilterValue() {
		return filterValue;
	}

}
