package com.example.events;

import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

public class ReportSelectedEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5842155960673831959L;
	private final Report selectedReport;

	public ReportSelectedEvent(Component source, Report selectedReport) {
		super(source);
		this.selectedReport = selectedReport;
	}

	public Report getSelectedReport() {
		return selectedReport;
	}

}
