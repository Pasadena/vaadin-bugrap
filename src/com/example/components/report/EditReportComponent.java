package com.example.components.report;

import com.example.bugrap.SingleReportUI;
import com.example.bugrap.util.HtmlUtils;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
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
		
		HorizontalLayout header = this.createHeaderRow(externalMode);
		ReportActionBar editActions = new ReportActionBar.ActionBarBuilder(this.eventRouter).withSingleReport(this.editableReport).build();
		ReportCommentListComponent commentSection = new ReportCommentListComponent(this.editableReport, this.eventRouter);
		AddCommentComponent addCommentSection = new AddCommentComponent(this.editableReport, this.eventRouter);
		
		container.addComponents(header, editActions, commentSection, addCommentSection);
		
		container.setExpandRatio(header, 0.1f);
		container.setExpandRatio(editActions, 0.2f);
		container.setExpandRatio(commentSection, 0.2f);
		container.setExpandRatio(addCommentSection, 0.5f);
		
		this.eventRouter.addListener(ReloadReportEvent.class, this, "reloadReport");

		this.setSizeUndefined();
		this.setWidth(100, Unit.PERCENTAGE);
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
		container.addStyleName("content-padding");
		this.container.setSizeFull();
	}
	
	private HorizontalLayout createHeaderRow(boolean externalMode) {
		HorizontalLayout header = new HorizontalLayout();
		if(!externalMode) {
			Link openReportLink = this.createOpenInNewWindowLink();
			header.addComponent(openReportLink);
			header.setComponentAlignment(openReportLink, Alignment.MIDDLE_CENTER);
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
