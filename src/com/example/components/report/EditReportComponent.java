package com.example.components.report;

import java.util.ArrayList;
import java.util.Arrays;

import com.example.bugrap.SingleReportUI;
import com.example.bugrap.util.HtmlUtils;
import com.example.events.report.ReportUpdatedEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeFactory;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.reports.ReportPriority;
import com.vaadin.incubator.bugrap.model.reports.ReportStatus;
import com.vaadin.incubator.bugrap.model.reports.ReportType;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class EditReportComponent extends CustomComponent {
	
	public static final String REPORT_ID_PARAM = "reportId";
	
	private final VerticalLayout container;
	private HorizontalLayout actionsBar;
	private NativeSelect versionSelect;
	private NativeSelect typeSelect;
	private NativeSelect prioritySelect;
	private NativeSelect statusSelect;
	private NativeSelect assigneeSelect;
	
	private Label projectNameField;
	
	private final BeanFieldGroup<Report> fieldGroup;
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
		this.fieldGroup = new BeanFieldGroup<>(Report.class);
		this.externalMode = externalMode;

		setContainerProperties();
		
		container.addComponent(this.createHeaderRow(externalMode));
		container.addComponent(this.createActionsBar());
		container.addComponent(new ReportCommentListComponent(this.editableReport, this.eventRouter));
		container.addComponent(new AddCommentComponent(this.editableReport, this.eventRouter));
		
		this.bindValuesToForm(this.editableReport);
		
		this.eventRouter.addListener(ReloadReportEvent.class, this, "reloadReport");
		
		setSizeUndefined();
		setCompositionRoot(container);
	}
	
	public void reloadReport(ReloadReportEvent event) {
		if(!event.getComponent().equals(this)) {
			this.editableReport = event.getUpdatedEvent();
			this.bindValuesToForm(this.editableReport);
		}
	}
	
	private void setContainerProperties() {
		container.setSizeUndefined();
		container.setSpacing(true);
		container.setMargin(true);
		container.addStyleName("no-horizontal-padding");
	}
	
	private HorizontalLayout createActionsBar() {
		this.actionsBar = new HorizontalLayout();
		
		this.createActionBarSelects();
		this.createActionBarButtons();
		
		actionsBar.setSpacing(true);
		actionsBar.setSizeUndefined();
		
		for(Component child: actionsBar) {
			child.setSizeUndefined();
		}
		
		return actionsBar;
	}
	
	private HorizontalLayout createHeaderRow(boolean externalMode) {
		HorizontalLayout header = new HorizontalLayout();
		if(!externalMode) {
			header.addComponent(this.createOpenInNewWindowLink());
		}
		
		this.projectNameField = HtmlUtils.createHeader(this.editableReport.getSummary(), 3);
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
	
	private void createActionBarSelects() {
		this.prioritySelect = new NativeSelect("Priority", Arrays.asList(ReportPriority.values()));
		this.typeSelect = new NativeSelect("Type", Arrays.asList(ReportType.values()));
		this.statusSelect = new NativeSelect("Status", Arrays.asList(ReportStatus.values()));		
		this.assigneeSelect = new NativeSelect("Assigned to", FacadeUtil.getReporters());
		this.versionSelect = new NativeSelect("Version", new ArrayList<ProjectVersion>());
		actionsBar.addComponents(prioritySelect, typeSelect, statusSelect, assigneeSelect, versionSelect);
	}
	
	private void createActionBarButtons() {
		Button updateButton = new Button("Update", event -> this.saveReport());
		
		Button revertButton = new Button("Revert", event -> this.discardChanges());
		revertButton.addStyleName("danger bottom-aligned");
		updateButton.addStyleName("primary bottom-aligned");
		actionsBar.addComponents(updateButton, revertButton);
	}
	
	private void saveReport() {
		try {
			fieldGroup.commit();
			Notification.show("Report updated", Notification.Type.TRAY_NOTIFICATION);
			this.editableReport = FacadeFactory.getFacade().store(this.editableReport);
			eventRouter.fireEvent(new ReportUpdatedEvent(this, this.editableReport));
			if(this.externalMode) {
				eventRouter.fireEvent(new ReloadReportEvent(this, this.editableReport));
			}
		} catch (CommitException ce) {
			Notification.show("Something went terribly wrong! Unable to update report", Notification.Type.ERROR_MESSAGE);
		}
	}
	
	private void discardChanges() {
		fieldGroup.discard();
		Notification.show("Discarded all changes to this report", Notification.Type.TRAY_NOTIFICATION);
	}
	
	private void bindValuesToForm(final Report selectedReport) {
		fieldGroup.setItemDataSource(selectedReport);
		
		fieldGroup.bind(prioritySelect, "priority");
		fieldGroup.bind(typeSelect, "type");
		fieldGroup.bind(statusSelect, "status");
		fieldGroup.bind(assigneeSelect, "assigned");
		fieldGroup.bind(versionSelect, "version");
	}
}
