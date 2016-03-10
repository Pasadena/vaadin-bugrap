package com.example.components;

import java.util.List;

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
			List<ProjectVersion> versionsForProject = FacadeUtil.getVersions(selectedProject);
			if(versionsForProject.size() > 1) {
				versionsForProject.add(0, getAllVersionsModel(selectedProject));
			}
			projectVersionContainer.addAll(versionsForProject);
		}
		return projectVersionContainer;
	}
	
	public void handleProjectChange(ProjectSelectedEvent pse) {
		this.setContainerDataSource(this.getProjectVersionsContainer(pse.getSelectedProject()));
	}
	
	private static ProjectVersion getAllVersionsModel(Project selectedProject) {
		ProjectVersion allVersionsModel = new ProjectVersion();
		allVersionsModel.setVersion("All versions");
		allVersionsModel.setId(-999L);
		allVersionsModel.setProject(selectedProject);
		return allVersionsModel;
	}

}
