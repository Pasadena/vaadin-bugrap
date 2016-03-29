package com.example.components;

import java.util.Map;

import com.vaadin.event.EventRouter;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;

@SuppressWarnings("serial")
public class ReportListFilterer extends AbstractListFilterer {
	
	private final Map<String, Object> options;
	private OptionGroup filterOptions;
	
	public ReportListFilterer(final String caption, final String filterPropertyName, final Map<String, Object> options, final EventRouter eventRouter) {
		super(caption, filterPropertyName, eventRouter);
		this.options = options;
		
		this.addContent(this.getFiltererContent());
		
		this.registerFilterChangeListener();
	}

	public Component getFiltererContent() {
		this.filterOptions = new OptionGroup(getCaption());
		filterOptions.addItems(options.keySet());
		
		this.filterOptions.setSizeUndefined();
		this.filterOptions.addStyleName("horizontal");
		return this.filterOptions;
	}

	@Override
	public void registerFilterChangeListener() {
		this.filterOptions.addValueChangeListener(event -> fireFilterChangeEvent(options.get(event.getProperty().getValue())));
		
	}

}
