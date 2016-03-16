package com.example.components.report;

import java.util.ArrayList;
import java.util.Arrays;

import com.example.events.ReportSelectedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.reports.ReportPriority;
import com.vaadin.incubator.bugrap.model.reports.ReportStatus;
import com.vaadin.incubator.bugrap.model.reports.ReportType;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class EditReportComponent extends CustomComponent {
	
	private final VerticalLayout container;
	private HorizontalLayout actionsBar;
	private NativeSelect versionSelect;
	private NativeSelect typeSelect;
	private NativeSelect prioritySelect;
	private NativeSelect statusSelect;
	private NativeSelect assigneeSelect;
	
	private Report editableReport;
	
	public EditReportComponent(EventRouter eventRouter) {
		this.container = new VerticalLayout();
		this.editableReport = null;
		
		container.setSizeUndefined();
		container.setSpacing(true);
		container.setMargin(true);
		container.addStyleName("no-horizontal-padding");
		
		eventRouter.addListener(ReportSelectedEvent.class, this, "setSelectedReport");
		
		container.addComponent(this.createActionsBar());
		
		setCompositionRoot(container);
		toggleVisibility(editableReport);
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
	
	private void createActionBarSelects() {
		this.prioritySelect = new NativeSelect("Priority", Arrays.asList(ReportPriority.values()));
		this.typeSelect = new NativeSelect("Type", Arrays.asList(ReportType.values()));
		this.statusSelect = new NativeSelect("Status", Arrays.asList(ReportStatus.values()));		
		this.assigneeSelect = new NativeSelect("Assigned to", FacadeUtil.getReporters());
		this.versionSelect = new NativeSelect("Version", new ArrayList<ProjectVersion>());
		actionsBar.addComponents(prioritySelect, typeSelect, statusSelect, assigneeSelect, versionSelect);
	}
	
	private void createActionBarButtons() {
		Button updateButton = new Button("Update", event -> {
			Notification.show("It seems that this one is not implemented yet chief");
		});
		
		Button revertButton = new Button("Revert", event -> {
			Notification.show("It seems that this one is not implemented yet chief");
		});
		revertButton.addStyleName("danger");
		updateButton.addStyleName("primary");
		actionsBar.addComponents(updateButton, revertButton);
	}
	
	private void toggleVisibility(Report report) {
		this.setVisible(report != null);
	}
	
	public void setSelectedReport(ReportSelectedEvent event) {
		this.editableReport = event.getSelectedReport();
		this.updateVersionList(this.editableReport);
		this.container.setCaption(this.editableReport.getProject().getName());
		this.updateComponentValues(this.editableReport);
		toggleVisibility(editableReport);
	}
	
	private void updateVersionList(final Report selectedReport) {
		this.versionSelect.clear();
		this.versionSelect.addItems(FacadeUtil.getVersions(selectedReport.getProject()));
	}
	
	private void updateComponentValues(final Report selectedReport) {
		this.prioritySelect.setValue(selectedReport.getPriority());
		this.typeSelect.setValue(selectedReport.getType());
		this.statusSelect.setValue(selectedReport.getStatus());
		this.assigneeSelect.setValue(selectedReport.getAssigned());
		this.versionSelect.setValue(selectedReport.getVersion());
	}
}
