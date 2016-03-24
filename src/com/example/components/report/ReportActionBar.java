package com.example.components.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.events.report.ReportListUpdatedEvent;
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
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;

@SuppressWarnings("serial")
public class ReportActionBar extends HorizontalLayout {
	
	public static class ActionBarBuilder {
		
		private final EventRouter eventRouter;
		private Collection<Report> selectedReports;
		private Report selectedReport;
		
		public ActionBarBuilder(final EventRouter eventRouter) {
			this.eventRouter = eventRouter;
		}
		
		public ActionBarBuilder withSingleReport(final Report selectedReport) {
			this.selectedReport = selectedReport;
			return this;
		}
		
		public ActionBarBuilder withReportCollection(final Collection<Report> reports) {
			this.selectedReports = reports;
			return this;
		}
		
		public ReportActionBar build() {
			if(this.selectedReport != null) {
				return new SingleReportActionBar(this.selectedReport, this.eventRouter);
			} else {
				return new MultiReportActionBar(this.selectedReports, this.eventRouter);
			}
		}
	}
		
		
	protected NativeSelect versionSelect;
	protected NativeSelect typeSelect;
	protected NativeSelect prioritySelect;
	protected NativeSelect statusSelect;
	protected NativeSelect assigneeSelect;
	
	protected void saveChanges() {}
	protected void discardChanges() {}
	protected List<ProjectVersion> getVersions() { return new ArrayList<ProjectVersion>(); }
	
	public ReportActionBar() {

	}
	
	private void setContent() {
		this.createActionBarSelects();
		this.createActionBarButtons();
		this.setActionBarProperties();
	}
	
	private void setActionBarProperties() {
		this.setSpacing(true);
		this.setSizeUndefined();
		
		for(Component child: this) {
			child.setSizeUndefined();
		}
	}
	
	private void createActionBarSelects() {
		this.prioritySelect = new NativeSelect("Priority", Arrays.asList(ReportPriority.values()));
		this.typeSelect = new NativeSelect("Type", Arrays.asList(ReportType.values()));
		this.statusSelect = new NativeSelect("Status", Arrays.asList(ReportStatus.values()));		
		this.assigneeSelect = new NativeSelect("Assigned to", FacadeUtil.getReporters());
		this.versionSelect = new NativeSelect("Version", this.getVersions());
		this.addComponents(prioritySelect, typeSelect, statusSelect, assigneeSelect, versionSelect);
	}
		
	private void createActionBarButtons() {
		Button updateButton = new Button("Update", event -> this.saveChanges());
		Button revertButton = new Button("Revert", event -> this.discardChanges());
		
		revertButton.addStyleName("danger bottom-aligned");
		updateButton.addStyleName("primary bottom-aligned");
		
		this.addComponents(updateButton, revertButton);
	}
	
	private static class SingleReportActionBar extends ReportActionBar {
		
		private Report editableReport;
		private final BeanFieldGroup<Report> fieldGroup;
		private final EventRouter eventRouter;
		
		public SingleReportActionBar(Report report, final EventRouter eventRouter) {
			super();
			this.editableReport = report;
			this.eventRouter = eventRouter;
			this.fieldGroup = new BeanFieldGroup<>(Report.class);
			this.fieldGroup.setItemDataSource(this.editableReport);
			
			super.setContent();
			this.bindValuesToForm(this.editableReport);
		}
		
		protected List<ProjectVersion> getVersions() { 
			return FacadeUtil.getVersions(this.editableReport.getProject());
		}
		
		@Override
		protected void saveChanges() {
			try {
				fieldGroup.commit();
				this.editableReport = FacadeFactory.getFacade().store(this.editableReport);
				eventRouter.fireEvent(new ReportUpdatedEvent(this, this.editableReport));
				/**if(this.externalMode) {
					eventRouter.fireEvent(new ReloadReportEvent(this, this.editableReport));
				}**/
				Notification.show("Report updated", Notification.Type.TRAY_NOTIFICATION);
			} catch (CommitException ce) {
				Notification.show("Something went terribly wrong! Unable to update report", Notification.Type.ERROR_MESSAGE);
			}
		}
		
		@Override
		protected void discardChanges() {
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

	private static class MultiReportActionBar extends ReportActionBar {
		
		private final Collection<Report> editableReports;
		private final EventRouter eventRouter;
		
		private final BeanFieldGroup<ReportListValueHolder> fieldGroup;
		private ReportListValueHolder valueHolder;
		
		
		public MultiReportActionBar(final Collection<Report> editableReports, final EventRouter eventRouter) {
			super();
			this.editableReports = editableReports;
			this.eventRouter = eventRouter;
			this.valueHolder = new ReportListValueHolder(editableReports);
			this.fieldGroup = new BeanFieldGroup<>(ReportListValueHolder.class);
			this.fieldGroup.setItemDataSource(this.valueHolder);
			
			super.setContent();
			this.bindValuesToForm();
		}
		
		protected List<ProjectVersion> getVersions() { 
			return this.editableReports.isEmpty() ? new ArrayList<>() : FacadeUtil.getVersions(this.editableReports.iterator().next().getProject());
		}
		
		@Override
		public void saveChanges() {
			try {
				fieldGroup.commit();
				List<Report> savedReports = new ArrayList<>();
				for(Report report: this.editableReports) {
					savedReports.add(FacadeUtil.store(this.populateReportData(report)));
				}

				eventRouter.fireEvent(new ReportListUpdatedEvent(this, savedReports));
				Notification.show("Reports updated", Notification.Type.TRAY_NOTIFICATION);
			} catch (CommitException ce) {
				Notification.show("Something went terribly wrong! Unable to proceed with the update!", Notification.Type.ERROR_MESSAGE);
			}
		}
		
		private Report populateReportData(final Report target) {
			target.setPriority(this.valueHolder.getSharedPriority());
			target.setType(this.valueHolder.getSharedType());
			target.setStatus(this.valueHolder.getSharedStatus());
			target.setAssigned(this.valueHolder.getSharedAssignee());
			target.setVersion(this.valueHolder.getSharedVersion());
			return target;
		}
		
		@Override
		protected void discardChanges() {
			Notification.show("Discarded all changes to this report", Notification.Type.TRAY_NOTIFICATION);
		}
		
		private void bindValuesToForm() {
			fieldGroup.bind(prioritySelect, "sharedPriority");
			fieldGroup.bind(typeSelect, "sharedType");
			fieldGroup.bind(statusSelect, "sharedStatus");
			fieldGroup.bind(assigneeSelect, "sharedAssignee");
			fieldGroup.bind(versionSelect, "sharedVersion");
		}
		
		public class ReportListValueHolder {
			private ReportPriority sharedPriority;
			private ReportType sharedType;
			private ReportStatus sharedStatus;
			private Reporter sharedAssignee;
			private ProjectVersion sharedVersion;
			
			@SuppressWarnings("unused") private ReportListValueHolder() {}
			
			public ReportListValueHolder(final Collection<Report> reports) {
				this.sharedPriority = this.getCommonPriorityOrNull(reports);
				this.sharedType = this.getCommonTypeOrNull(reports);
				this.sharedStatus = this.getCommonStatusOrNull(reports);
				this.sharedAssignee = this.getCommonAssigneeOrNull(reports);
				this.sharedVersion = this.getCommonVersionOrNull(reports);
			}
			
			private ReportPriority getCommonPriorityOrNull(final Collection<Report> reports) {
				Set<ReportPriority> uniquePriorities = reports.stream().map(report -> report.getPriority()).collect(Collectors.toSet());
				return getOnlyElementOrNull(uniquePriorities);
			}
			
			private ReportType getCommonTypeOrNull(final Collection<Report> reports) {
				Set<ReportType> uniqueTypes = reports.stream().map(report -> report.getType()).collect(Collectors.toSet());
				return getOnlyElementOrNull(uniqueTypes);
			}
			
			private ReportStatus getCommonStatusOrNull(final Collection<Report> reports) {
				Set<ReportStatus> uniqueStatuses = reports.stream().map(report -> report.getStatus()).collect(Collectors.toSet());
				return getOnlyElementOrNull(uniqueStatuses);
			}
			
			private Reporter getCommonAssigneeOrNull(final Collection<Report> reports) {
				Set<Reporter> uniqueAssignees = reports.stream().map(report -> report.getAssigned()).collect(Collectors.toSet());
				return getOnlyElementOrNull(uniqueAssignees);
			}
			
			private ProjectVersion getCommonVersionOrNull(final Collection<Report> reports) {
				Set<ProjectVersion> uniqueVersions = reports.stream().map(report -> report.getVersion()).collect(Collectors.toSet());
				return getOnlyElementOrNull(uniqueVersions);
			}
			
			private <T> T getOnlyElementOrNull(Set<T> elements) {
				if(!elements.isEmpty() && elements.size() < 2) {
					return elements.iterator().next();
				}
				return null;
			}

			public ReportPriority getSharedPriority() {
				return sharedPriority;
			}

			public ReportType getSharedType() {
				return sharedType;
			}

			public ReportStatus getSharedStatus() {
				return sharedStatus;
			}

			public Reporter getSharedAssignee() {
				return sharedAssignee;
			}

			public ProjectVersion getSharedVersion() {
				return sharedVersion;
			}
			
			/**
			 * Setters, used via fieldGroup's method invocations.
			 * @param sharedPriority
			 */
			
			@SuppressWarnings("unused")
			public void setSharedPriority(ReportPriority sharedPriority) {
				this.sharedPriority = sharedPriority;
			}

			@SuppressWarnings("unused")
			public void setSharedType(ReportType sharedType) {
				this.sharedType = sharedType;
			}

			@SuppressWarnings("unused")
			public void setSharedStatus(ReportStatus sharedStatus) {
				this.sharedStatus = sharedStatus;
			}

			@SuppressWarnings("unused")
			public void setSharedAssignee(Reporter sharedAssignee) {
				this.sharedAssignee = sharedAssignee;
			}

			@SuppressWarnings("unused")
			public void setSharedVersion(ProjectVersion sharedVersion) {
				this.sharedVersion = sharedVersion;
			}
			
		}
	}

}