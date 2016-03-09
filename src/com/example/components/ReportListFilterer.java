package com.example.components;

import java.util.Map;

import com.example.events.FilterChangedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.OptionGroup;

@SuppressWarnings("serial")
public class ReportListFilterer extends CustomComponent {
	
	public ReportListFilterer(final String caption, final String filterPropertyName, final Map<String, Object> options, final EventRouter eventRouter) {
		FormLayout filtererContainer = new FormLayout();
		OptionGroup filterOptions = new OptionGroup(caption);
		filterOptions.addItems(options.keySet());
		filterOptions.addValueChangeListener(event -> {
			eventRouter.fireEvent(new FilterChangedEvent(this, filterPropertyName, options.get(event.getProperty().getValue())));
		});
		filtererContainer.addComponent(filterOptions);
		
		filtererContainer.setSizeUndefined();
		filterOptions.setSizeUndefined();
		filterOptions.addStyleName("horizontal");
		
		setCompositionRoot(filtererContainer);
	}

}
