package com.example.components;

import com.vaadin.event.EventRouter;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
public class SummarySearch extends AbstractListFilterer {
	
	private TextField searchField;

	public SummarySearch(String caption, String filterPropertyName, EventRouter eventRouter) {
		super(caption, filterPropertyName, eventRouter);
		
		this.addContent(this.creteFiltererContent());
		
		this.registerFilterChangeListener();
	}
	
	private Component creteFiltererContent() {
		this.searchField = new TextField(this.getCaption());
		this.searchField.setSizeUndefined();
		this.searchField.setInputPrompt("Search reports...");
		
		//No icon for this field, see https://dev.vaadin.com/ticket/18668
		//this.searchField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
		//this.searchField.setIcon(FontAwesome.SEARCH);
		return this.searchField;
	}

	@Override
	public void registerFilterChangeListener() {
		this.searchField.addTextChangeListener(event -> fireFilterChangeEvent(event.getText()));
		
	}

}
