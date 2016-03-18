package com.example.events.report;

import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

@SuppressWarnings("serial")
public class ReportUpdatedEvent extends Event {
	
	private final Report updatedReport;

	public ReportUpdatedEvent(Component source, Report updatedReport) {
		super(source);
		this.updatedReport = updatedReport;
	}

	public Report getUpdatedReport() {
		return updatedReport;
	}
}
