package com.example.components.report;

import java.util.Set;

import com.example.bugrap.util.HtmlUtils;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class MassEditReportsComponent extends CustomComponent {
	
	public MassEditReportsComponent(final Set<Report> selectedReports) {
		
		VerticalLayout container = new VerticalLayout();
		container.setSizeUndefined();
		
		container.addComponent(HtmlUtils.createHeader("<b>" + selectedReports.size() + " reports selected</b> - Select single  report to view contents", 3));
		
		setCompositionRoot(container);
	}

}
