package com.example.components;

import java.util.List;

import com.example.bugrap.constants.SessionAttributeConstants;
import com.example.bugrap.util.SessionUtils;
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
	
	public ProjectVersionSelectComponent(final String title, final EventRouter eventRouter, final Project selectedProject) {
		super(title);
		
		this.setComponentProperties();
		this.setContainerDataSource(getProjectVersionsContainer(selectedProject));
		
		eventRouter.addListener(ProjectSelectedEvent.class, this, "handleProjectChange");
		this.addValueChangeListener(event -> {
			ProjectVersion selectedVersion = (ProjectVersion)event.getProperty().getValue();
			SessionUtils.storeValueToSession(SessionAttributeConstants.SELECTED_VERSION.getAttributeName(), selectedVersion);
			eventRouter.fireEvent(new ProjectVersionSelectedEvent(this, selectedVersion));
		});
		this.selectStoredOrFirst();
	}
	
	private void selectStoredOrFirst() {
		if(SessionUtils.containsKey(SessionAttributeConstants.SELECTED_VERSION.getAttributeName())) {
			this.select(SessionUtils.getValueFromSession(SessionAttributeConstants.SELECTED_VERSION.getAttributeName()));
		} else {
			this.selectFirstValue();
		}
	}
	
	private void setComponentProperties() {
		this.setImmediate(true);
		this.setNullSelectionAllowed(false);
	}
	
	private void selectFirstValue() {
		this.select(this.getItemIds().iterator().next());
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
		this.selectFirstValue();
	}
	
	private static ProjectVersion getAllVersionsModel(Project selectedProject) {
		ProjectVersion allVersionsModel = new ProjectVersion();
		allVersionsModel.setVersion("All versions");
		allVersionsModel.setId(-999L);
		allVersionsModel.setProject(selectedProject);
		return allVersionsModel;
	}

}
