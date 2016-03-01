package com.example.events;

import com.vaadin.incubator.bugrap.model.projects.Project;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

@SuppressWarnings("serial")
public class ProjectSelectedEvent extends Event {
	
	private final Project selectedProject;
	
	public ProjectSelectedEvent(Component source, Project project) {
		super(source);
		this.selectedProject = project;
	}

	public Project getSelectedProject() {
		return selectedProject;
	}

}
