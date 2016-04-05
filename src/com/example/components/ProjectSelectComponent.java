package com.example.components;

import com.example.bugrap.constants.SessionAttributeConstants;
import com.example.bugrap.util.SessionUtils;
import com.example.events.ProjectSelectedEvent;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.projects.Project;
import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.ui.NativeSelect;

@SuppressWarnings("serial")
public class ProjectSelectComponent extends NativeSelect {
	
	public ProjectSelectComponent(final String title, final EventRouter eventRouter) {
		super(title);
		this.setContainerDataSource(getProjectContainer());
		this.addValueChangeListener(event -> {
			eventRouter.fireEvent(new ProjectSelectedEvent(this, (Project)event.getProperty().getValue()));
		});
		this.selectStoredValueOrFirst();
	}
	
	private void selectStoredValueOrFirst() {
		if(SessionUtils.containsKey(SessionAttributeConstants.SELECTED_VERSION.getAttributeName())) {
			ProjectVersion storedValue = SessionUtils.getValueFromSession(SessionAttributeConstants.SELECTED_VERSION.getAttributeName());
			this.setValue(storedValue.getProject());
		} else {
			this.selectFirstValue();
		}
	}
	
	private void selectFirstValue() {
		this.setNullSelectionAllowed(false);
		this.select(this.getItemIds().iterator().next());
	}
	
	
	private Container getProjectContainer() {
		BeanItemContainer<Project> projectsContainer = new BeanItemContainer<Project>(Project.class);
		projectsContainer.addAll(FacadeUtil.getProjects());
		return projectsContainer;
	}

}
