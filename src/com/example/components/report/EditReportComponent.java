package com.example.components.report;

import com.example.bugrap.SingleReportUI;
import com.example.bugrap.util.HtmlUtils;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class EditReportComponent extends CustomComponent {
	
	public static final String REPORT_ID_PARAM = "reportId";
	
	private final VerticalLayout container;

	private Report editableReport;
	private final boolean externalMode;
	
	private final EventRouter eventRouter;
	
	private class ReloadReportEvent extends Event {
		
		private final Report updatedEvent;

		public ReloadReportEvent(Component source, Report updatedEvent) {
			super(source);
			this.updatedEvent = updatedEvent;
		}

		public Report getUpdatedEvent() {
			return updatedEvent;
		}
	}
	
	public EditReportComponent(EventRouter eventRouter, Report selectedReport, boolean externalMode) {
		this.container = new VerticalLayout();
		this.eventRouter = eventRouter;
		this.editableReport = selectedReport;
		this.externalMode = externalMode;

		setContainerProperties();
		
		container.addComponent(this.createHeaderRow(externalMode));
		container.addComponent(new ReportActionBar.ActionBarBuilder(this.eventRouter).withSingleReport(this.editableReport).build());
		container.addComponent(new ReportCommentListComponent(this.editableReport, this.eventRouter));
		container.addComponent(new AddCommentComponent(this.editableReport, this.eventRouter));
		
		this.eventRouter.addListener(ReloadReportEvent.class, this, "reloadReport");
		
		setSizeUndefined();
		setCompositionRoot(container);
	}
	
	public void reloadReport(ReloadReportEvent event) {
		if(!event.getComponent().equals(this)) {
			this.editableReport = event.getUpdatedEvent();
		}
	}
	
	private void setContainerProperties() {
		container.setSizeUndefined();
		container.setSpacing(true);
		container.setMargin(true);
		container.addStyleName("no-horizontal-padding");
	}
	
	private HorizontalLayout createHeaderRow(boolean externalMode) {
		HorizontalLayout header = new HorizontalLayout();
		if(!externalMode) {
			header.addComponent(this.createOpenInNewWindowLink());
		}
		
		Label projectNameField = HtmlUtils.createHeader(this.editableReport.getSummary(), 3);
		projectNameField.setSizeUndefined();
		
		header.addComponent(projectNameField);
		header.setSizeUndefined();
		header.setSpacing(true);
		
		return header;
	}
	
	private Link createOpenInNewWindowLink() {
		Link newWindowLink = new Link();
		newWindowLink.setIcon(FontAwesome.EXTERNAL_LINK);
		newWindowLink.setSizeUndefined();
		
		BrowserWindowOpener opener = new BrowserWindowOpener(SingleReportUI.class);
		opener.extend(newWindowLink);
		opener.setParameter(REPORT_ID_PARAM, String.valueOf(this.editableReport.getId()));
		VaadinService.getCurrentRequest().getWrappedSession().setAttribute("eventRouter", this.eventRouter);
		return newWindowLink;
	}
}
