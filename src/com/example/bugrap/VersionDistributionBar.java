package com.example.bugrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.bugrap.widgetset.client.versiondistributionbar.ReportDistributionGroup;
import com.example.bugrap.widgetset.client.versiondistributionbar.VersionDistributionBarState;
import com.example.events.ProjectVersionSelectedEvent;
import com.example.events.report.ReportListUpdatedEvent;
import com.example.events.report.ReportUpdatedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeFactory;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.reports.ReportStatus;

@SuppressWarnings("serial")
public class VersionDistributionBar extends com.vaadin.ui.AbstractComponent {
	
	private Map<ReportDistributionGroup, Long> distributions;
	private ProjectVersion selectedVersion;

	public VersionDistributionBar(final EventRouter eventRouter, final ProjectVersion selectedVersion) {
		this.distributions = new HashMap<>();
		this.selectedVersion = selectedVersion;
		this.updateVersionDistributions(selectedVersion);
		
		eventRouter.addListener(ProjectVersionSelectedEvent.class, this, "handleProjectVersionChange");
		eventRouter.addListener(ReportUpdatedEvent.class, this, "handleUpdatedReport");
		eventRouter.addListener(ReportListUpdatedEvent.class, this, "handleUpdatedReports");
	}

	@Override
	public VersionDistributionBarState getState() {
		return (VersionDistributionBarState) super.getState();
	}
	
	public void handleProjectVersionChange(final ProjectVersionSelectedEvent event) {
		this.updateVersionDistributions(event.getProjectVersion());
	}
	
	public void handleUpdatedReport(final ReportUpdatedEvent event) {
		this.updateVersionDistributions(this.selectedVersion);
	}
	
	public void handleUpdatedReports(final ReportListUpdatedEvent event) {
		this.updateVersionDistributions(this.selectedVersion);
	}
	
	private void updateVersionDistributions(final ProjectVersion projectVersion) {
		this.distributions.clear();
		List<Report> reportsForVersion = this.loadVersionReports(projectVersion);
		
		this.distributions.put(ReportDistributionGroup.CLOSED, Long.valueOf(reportsForVersion.stream().filter(report -> report.getStatus() == ReportStatus.CLOSED).count()));
		this.distributions.put(ReportDistributionGroup.OPEN_ASSIGNED, Long.valueOf(reportsForVersion.stream().filter(report -> report.getStatus() == ReportStatus.OPEN && report.getAssigned() != null).count()));
		this.distributions.put(ReportDistributionGroup.OPEN_UNASSIGNED, Long.valueOf(reportsForVersion.stream().filter(report -> report.getStatus() == ReportStatus.OPEN && report.getAssigned() == null).count()));
		
		this.getState().setDistributions(this.distributions);
	}
	
	private List<Report> loadVersionReports(final ProjectVersion projectVersion) {
		if(projectVersion == null) {
			return new ArrayList<>();
		}
		if(projectVersion.getId() < 0) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("proj", projectVersion.getProject());
			return FacadeFactory.getFacade().list("SELECT r FROM Report r WHERE r.project = :proj", parameters);
		} else {
			return FacadeUtil.getReportsForVersion(projectVersion);
		}
	}
}
