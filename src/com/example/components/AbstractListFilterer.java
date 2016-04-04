package com.example.components;

import com.example.events.FilterChangedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;

@SuppressWarnings("serial")
public abstract class AbstractListFilterer extends CustomComponent {
	
	private final String filterPropertyName;
	private final EventRouter eventRouter;
	private final String caption;

	private final CssLayout container;
	
	public AbstractListFilterer(final String caption, final String filterPropertyName, final EventRouter eventRouter) {
		this.filterPropertyName = filterPropertyName;
		this.eventRouter = eventRouter;
		this.caption = caption;

		this.container = new CssLayout();
		this.container.setSizeUndefined();
		
		this.setCompositionRoot(this.container);
	}
	
	public abstract void registerFilterChangeListener();
	
	public void addContent(Component... content) {
		this.container.addComponents(content);
	}
	
	public AbstractLayout getContainer() {
		return this.container;
	}
	
	public void fireFilterChangeEvent(final Object value) {
		this.eventRouter.fireEvent(new FilterChangedEvent(this, filterPropertyName, value));
	}

	public String getCaption() {
		return caption;
	}
}
