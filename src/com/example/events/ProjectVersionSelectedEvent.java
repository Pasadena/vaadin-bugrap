package com.example.events;

import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

@SuppressWarnings("serial")
public class ProjectVersionSelectedEvent extends Event {
	
	private final ProjectVersion projectVersion;
	
	public ProjectVersionSelectedEvent(final Component component, final ProjectVersion projectVersion) {
		super(component);
		this.projectVersion = projectVersion;
	}

	public ProjectVersion getProjectVersion() {
		return projectVersion;
	}

}
