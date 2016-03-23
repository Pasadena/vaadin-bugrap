package com.example.events.report;

import java.util.Set;

import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

public class ReportSelectedEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5842155960673831959L;
	private final Set<Report> selectedReports;

	public ReportSelectedEvent(Component source, Set<Report> selectedReports) {
		super(source);
		this.selectedReports = selectedReports;
	}

	public Set<Report> getSelectedReports() {
		return selectedReports;
	}

}
