package com.example.components;

import com.example.events.ProjectSelectedEvent;
import com.example.events.ProjectVersionSelectedEvent;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.projects.Project;
import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.ui.NativeSelect;

@SuppressWarnings("serial")
public class ProjectVersionSelectComponent extends NativeSelect {
	
	public ProjectVersionSelectComponent(final String title, final EventRouter eventRouter) {
		super(title);
		this.setImmediate(true);
		this.setContainerDataSource(getProjectVersionsContainer(null));
		eventRouter.addListener(ProjectSelectedEvent.class, this, "handleProjectChange");
		this.addValueChangeListener(event -> {
			eventRouter.fireEvent(new ProjectVersionSelectedEvent(this, (ProjectVersion)event.getProperty().getValue()));
		});
	}
	
	private Container getProjectVersionsContainer(final Project selectedProject) {
		BeanItemContainer<ProjectVersion> projectVersionContainer = new BeanItemContainer<>(ProjectVersion.class);
		if(selectedProject != null) {
			projectVersionContainer.addAll(FacadeUtil.getVersions(selectedProject));
		}
		return projectVersionContainer;
	}
	
	public void handleProjectChange(ProjectSelectedEvent pse) {
		this.setContainerDataSource(this.getProjectVersionsContainer(pse.getSelectedProject()));
	}

}
