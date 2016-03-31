package com.example.bugrap.views;

import java.util.HashMap;
import java.util.Map;

import com.example.bugrap.VersionDistributionBar;
import com.example.bugrap.constants.AssigneeSelections;
import com.example.components.LoggedInUserInfo;
import com.example.components.ProjectSelectComponent;
import com.example.components.ProjectVersionSelectComponent;
import com.example.components.ReportList;
import com.example.components.ReportListFilterer;
import com.example.components.SummarySearch;
import com.example.components.report.EditReportComponent;
import com.example.components.report.MassEditReportsComponent;
import com.example.events.report.ReportSelectedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

@SuppressWarnings("serial")
public class ReportsView extends VerticalSplitPanel implements View {

	private Navigator navigator;
	private Reporter loggedInUser; 
	
	private NativeSelect versionSelect;
	private ProjectSelectComponent projectSelect;
	
	private final EventRouter eventRouter = new EventRouter();
	
	@Override
	public void enter(ViewChangeEvent event) {
		navigator = event.getNavigator();
	}
	
	public ReportsView() {
		loggedInUser = (Reporter)VaadinService.getCurrentRequest().getWrappedSession().getAttribute("loggedInUser");
		this.setViewProperties();
		
		VerticalLayout reportsView = new VerticalLayout();
		
		GridLayout headerLayout = this.getViewHeaderLayout();
		
		VerticalLayout viewBodyLayout = new VerticalLayout();
		viewBodyLayout.addStyleName("body-layout");
		
		HorizontalLayout versionSelectLayout = this.getVersionSelectBar();
		HorizontalLayout tableFiltersLayout = this.getFilterOptionsLayout();
		VerticalLayout reportListLayout = this.getReportsSection();
		
		reportsView.addComponent(headerLayout);
		reportsView.addComponent(viewBodyLayout);
		
		
		viewBodyLayout.addComponent(versionSelectLayout);
		viewBodyLayout.addComponent(tableFiltersLayout);
		viewBodyLayout.addComponent(reportListLayout);
		
		reportsView.setExpandRatio(headerLayout, 0.5f);
		viewBodyLayout.setExpandRatio(versionSelectLayout, 1f);;
		
		this.setFirstComponent(reportsView);
		
		eventRouter.addListener(ReportSelectedEvent.class, this, "setSelectedReport");
	}
	
	private void setViewProperties() {
		this.setSizeFull();
		this.setSplitPosition(100, Unit.PERCENTAGE);
		this.setLocked(true);
		this.addStyleName("main-layout");
	}
	
	public void setSelectedReport(ReportSelectedEvent event) {
		this.setSplitPosition(60, Unit.PERCENTAGE);
		this.setLocked(false);
		if(event.getSelectedReports().size() <= 1) {
			this.replaceComponent(this.getSecondComponent(), new EditReportComponent(eventRouter, event.getSelectedReports().iterator().next(), false));
		} else {
			this.replaceComponent(this.getSecondComponent(), new MassEditReportsComponent(event.getSelectedReports(), this.eventRouter));
		}
		
	}
	
	private GridLayout getViewHeaderLayout() {
		final GridLayout headerLayout = new GridLayout(2, 2);
		headerLayout.setWidth(99, Unit.PERCENTAGE);
		headerLayout.addStyleName("header-layout");
		
		LoggedInUserInfo userInfo = new LoggedInUserInfo(loggedInUser, navigator);
		userInfo.setSizeUndefined();

		HorizontalLayout linksLayout = this.createHeaderLinksLayout();
		
		headerLayout.addComponent(this.createProjectSelectLayout(), 0, 0);
		headerLayout.addComponent(userInfo, 1, 0);
		headerLayout.addComponent(linksLayout, 0, 1);
		
		headerLayout.setComponentAlignment(userInfo, Alignment.MIDDLE_RIGHT);
		headerLayout.setRowExpandRatio(0, 0.7f);
		headerLayout.setRowExpandRatio(1, 0.3f);
		
		return headerLayout;
	}
	
	private HorizontalLayout createHeaderLinksLayout() {
		HorizontalLayout linksLayout = new HorizontalLayout();
		linksLayout.setWidth(70, Unit.PERCENTAGE);
		
		Link reportBugLink = this.createHeaderLink("Report a bug", FontAwesome.BUG);
		Link requestFeatureLink = this.createHeaderLink("Report a feature", FontAwesome.LIGHTBULB_O);
		Link manageProjectLink = this.createHeaderLink("Manage project", FontAwesome.COG);
		
		linksLayout.addComponents(reportBugLink, requestFeatureLink, manageProjectLink);
		return linksLayout;
	}
	
	private Link createHeaderLink(final String caption, final FontAwesome icon) {
		Link headerLink = new Link();
		headerLink.setCaption(caption);
		headerLink.setIcon(icon);
		return headerLink;
	}
	
	private FormLayout createProjectSelectLayout() {
		FormLayout selectProjectLayout = new FormLayout();
		projectSelect = new ProjectSelectComponent("Select project: ", eventRouter);
		selectProjectLayout.addComponent(projectSelect);
		selectProjectLayout.addStyleName("no-padding");
		return selectProjectLayout;
	}
	
	private VerticalLayout getReportsSection() {
		VerticalLayout reportSection = new VerticalLayout();
		ReportList reportList = new ReportList("", eventRouter);
		reportSection.addComponent(reportList);
		return reportSection;
	}
	
	private HorizontalLayout getVersionSelectBar() {
		//TODO set spacing and grow  widget with expand ratio
		HorizontalLayout versionBar = new HorizontalLayout();
		versionBar.setWidth(100, Unit.PERCENTAGE);
		versionBar.setSpacing(true);
		
		FormLayout versionLayout = new FormLayout();
		versionLayout.addComponent(getVersionSelectComponent());
		
		VersionDistributionBar distributionBar = new VersionDistributionBar(this.eventRouter);
		versionBar.addComponent(versionLayout);
		versionBar.addComponent(distributionBar);
		
		versionBar.setExpandRatio(versionLayout, 1);
		versionBar.setExpandRatio(distributionBar, 2);
		return versionBar;
	}
	
	private NativeSelect getVersionSelectComponent() {
		if(versionSelect == null) {
			versionSelect = new ProjectVersionSelectComponent("Reports for:", eventRouter);
		}
		return versionSelect;
	}
	
	private HorizontalLayout getFilterOptionsLayout() {
		HorizontalLayout filterOptionsLayput = new HorizontalLayout();
		Map<String, Object> filterOptions = new HashMap<>();
		filterOptions.put(AssigneeSelections.FOR_ME.getSelectionValue(), loggedInUser.getName());
		filterOptions.put(AssigneeSelections.EVERYONE.getSelectionValue(), null);
		filterOptionsLayput.addComponent(new ReportListFilterer("Assignee", "assigned", filterOptions, eventRouter));

		filterOptionsLayput.addComponent(new SummarySearch("", "summary", eventRouter));

		return filterOptionsLayput;
	}
}
