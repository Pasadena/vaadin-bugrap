package com.example.components;

import com.example.events.FilterChangedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;

@SuppressWarnings("serial")
public abstract class AbstractListFilterer extends CustomComponent {
	
	private final String filterPropertyName;
	private final EventRouter eventRouter;
	private final String caption;
	
	private final FormLayout container;
	
	public AbstractListFilterer(final String caption, final String filterPropertyName, final EventRouter eventRouter) {
		this.filterPropertyName = filterPropertyName;
		this.eventRouter = eventRouter;
		this.caption = caption;
		
		this.container = new FormLayout();
		this.container.setSizeUndefined();
		
		this.setCompositionRoot(this.container);
	}
	
	public abstract void registerFilterChangeListener();
	
	public void addContent(Component content) {
		this.container.addComponent(content);
	}
	
	public void fireFilterChangeEvent(final Object value) {
		this.eventRouter.fireEvent(new FilterChangedEvent(this, filterPropertyName, value));
	}

	public String getCaption() {
		return caption;
	}
}
