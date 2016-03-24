package com.example.events.report;

import java.util.Collection;

import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

@SuppressWarnings("serial")
public class ReportListUpdatedEvent extends Event {
	
	private final Collection<Report> updatedReports;

	public ReportListUpdatedEvent(Component source, Collection<Report> updatedReports) {
		super(source);
		this.updatedReports = updatedReports;
	}

	public Collection<Report> getUpdatedReports() {
		return updatedReports;
	}

}
