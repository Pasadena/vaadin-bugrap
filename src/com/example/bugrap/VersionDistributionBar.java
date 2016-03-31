package com.example.bugrap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.bugrap.widgetset.client.versiondistributionbar.ReportDistributionGroup;
import com.example.bugrap.widgetset.client.versiondistributionbar.VersionDistributionBarState;
import com.example.events.ProjectVersionSelectedEvent;
import com.example.events.report.ReportListUpdatedEvent;
import com.example.events.report.ReportUpdatedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.reports.ReportStatus;

@SuppressWarnings("serial")
public class VersionDistributionBar extends com.vaadin.ui.AbstractComponent {
	
	private Map<ReportDistributionGroup, Long> distributions;

	public VersionDistributionBar(final EventRouter eventRouter) {
		this.distributions = new HashMap<>();
		this.getState().setDistributions(this.distributions);
		
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
		this.updateVersionDistributions(event.getUpdatedReport().getVersion());
	}
	
	public void handleUpdatedReports(final ReportListUpdatedEvent event) {
		this.updateVersionDistributions(event.getUpdatedReports().iterator().next().getVersion());
	}
	
	private void updateVersionDistributions(ProjectVersion projectVersion) {
		this.distributions.clear();
		List<Report> reportsForVersion = FacadeUtil.getReportsForVersion(projectVersion);
		
		this.distributions.put(ReportDistributionGroup.CLOSED, Long.valueOf(reportsForVersion.stream().filter(report -> report.getStatus() == ReportStatus.CLOSED).count()));
		this.distributions.put(ReportDistributionGroup.OPEN_ASSIGNED, Long.valueOf(reportsForVersion.stream().filter(report -> report.getStatus() == ReportStatus.OPEN && report.getAssigned() != null).count()));
		this.distributions.put(ReportDistributionGroup.OPEN_UNASSIGNED, Long.valueOf(reportsForVersion.stream().filter(report -> report.getStatus() == ReportStatus.OPEN && report.getAssigned() == null).count()));
		
		this.getState().setDistributions(this.distributions);
	}
}
