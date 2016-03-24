package com.example.components.report;

import java.util.Set;

import com.example.bugrap.util.HtmlUtils;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class MassEditReportsComponent extends CustomComponent {
	
	private final EventRouter eventRouter;
	
	public MassEditReportsComponent(final Set<Report> selectedReports, final EventRouter eventRouter) {
		this.eventRouter = eventRouter;
		VerticalLayout container = new VerticalLayout();
		container.setSizeUndefined();
		
		container.addComponent(HtmlUtils.createHeader("<b>" + selectedReports.size() + " reports selected</b> - Select single  report to view contents", 3));
		container.addComponent(new ReportActionBar.ActionBarBuilder(this.eventRouter).withReportCollection(selectedReports).build());
		
		
		setCompositionRoot(container);
	}

}
