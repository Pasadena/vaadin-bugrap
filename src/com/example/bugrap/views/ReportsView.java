package com.example.bugrap.views;

import java.util.HashMap;
import java.util.Map;

import com.example.bugrap.VersionDistributionBar;
import com.example.bugrap.constants.AssigneeSelections;
import com.example.bugrap.constants.SessionAttributeConstants;
import com.example.bugrap.util.SessionUtils;
import com.example.components.LoggedInUserInfo;
import com.example.components.ProjectSelectComponent;
import com.example.components.ProjectVersionSelectComponent;
import com.example.components.ReportList;
import com.example.components.ReportListFilterer;
import com.example.components.ReportStatusFilterer;
import com.example.components.SummarySearch;
import com.example.components.report.EditReportComponent;
import com.example.components.report.MassEditReportsComponent;
import com.example.events.layout.CloseSelectedReportEvent;
import com.example.events.report.ReportSelectedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.projects.Project;
import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
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
		this.buildViewContent();
	}
	
	public ReportsView() {
		loggedInUser = SessionUtils.getValueFromSession(SessionAttributeConstants.LOGGED_IN_USER.getAttributeName());
		this.setViewProperties();
		
		eventRouter.addListener(ReportSelectedEvent.class, this, "setSelectedReport");
		eventRouter.addListener(CloseSelectedReportEvent.class, this, "removeSelectedReportComponent");
	}
	
	private void buildViewContent() {
		VerticalLayout reportsView = new VerticalLayout();
		reportsView.setMargin(true);
		
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
		
		viewBodyLayout.setExpandRatio(versionSelectLayout, 1);
		viewBodyLayout.setExpandRatio(tableFiltersLayout, 1);
		viewBodyLayout.setExpandRatio(reportListLayout, 8);
		
		reportsView.setExpandRatio(headerLayout, 1);
		reportsView.setExpandRatio(viewBodyLayout, 4);;

		this.setFirstComponent(reportsView);
	}
	
	private void setViewProperties() {
		this.updateSplitPosition(100, true);
	}
	
	private void updateSplitPosition(float percents, boolean locked) {
		this.setSplitPosition(percents, Unit.PERCENTAGE);
		this.setLocked(locked);
	}
	
	public void setSelectedReport(final ReportSelectedEvent event) {
		if(event.getSelectedReports().isEmpty()) {
			this.removeSelectedReportComponent(null);
		} else {
			this.addEditReportComponentToView(event);
		}
	}
		
	private void addEditReportComponentToView(final ReportSelectedEvent event) {
		this.updateSplitPosition(60, false);
		VerticalLayout reportEditontainer = new VerticalLayout();
		reportEditontainer.setMargin(true);
		reportEditontainer.setSizeUndefined();
		reportEditontainer.setWidth(100, Unit.PERCENTAGE);
		Component editReportComponent = null;
		if(event.getSelectedReports().size() <= 1) {
			editReportComponent = new EditReportComponent(eventRouter, event.getSelectedReports().iterator().next(), false);
		} else {
			editReportComponent = new MassEditReportsComponent(event.getSelectedReports(), this.eventRouter);
		}
		reportEditontainer.addComponent(editReportComponent);
		this.replaceComponent(this.getSecondComponent(), reportEditontainer);
	}
	
	public void removeSelectedReportComponent(final CloseSelectedReportEvent event) {
		if(this.getSecondComponent() != null) {
			this.removeComponent(this.getSecondComponent());
			this.updateSplitPosition(100, true);
		}
	}
	
	private GridLayout getViewHeaderLayout() {
		final GridLayout headerLayout = new GridLayout(2, 2);
		headerLayout.setWidth(100, Unit.PERCENTAGE);
		headerLayout.addStyleName("header-layout");
		headerLayout.setMargin(new MarginInfo(false, false, true, false));
		
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
		Panel listWrapper = new Panel();
		ReportList reportList = new ReportList(null, eventRouter, (ProjectVersion)versionSelect.getValue());
		listWrapper.setContent(reportList);
		reportSection.addComponent(listWrapper);
		return reportSection;
	}
	
	private HorizontalLayout getVersionSelectBar() {
		HorizontalLayout versionBar = new HorizontalLayout();
		versionBar.setWidth(100, Unit.PERCENTAGE);
		versionBar.setSpacing(true);
		
		FormLayout versionLayout = new FormLayout();
		versionLayout.addComponent(getVersionSelectComponent());
		
		VersionDistributionBar distributionBar = new VersionDistributionBar(this.eventRouter, (ProjectVersion)versionSelect.getValue());
		versionBar.addComponent(versionLayout);
		versionBar.addComponent(distributionBar);
		
		versionBar.setExpandRatio(versionLayout, 1);
		versionBar.setExpandRatio(distributionBar, 2);
		return versionBar;
	}
	
	private NativeSelect getVersionSelectComponent() {
		if(versionSelect == null) {
			versionSelect = new ProjectVersionSelectComponent("Reports for:", eventRouter, (Project)this.projectSelect.getValue());
		}
		return versionSelect;
	}
	
	private HorizontalLayout getFilterOptionsLayout() {
		HorizontalLayout filterOptionsLayput = new HorizontalLayout();
		filterOptionsLayput.setWidth(100, Unit.PERCENTAGE);
		filterOptionsLayput.setSpacing(true);
		Map<String, Object> filterOptions = new HashMap<>();
		filterOptions.put(AssigneeSelections.FOR_ME.getSelectionValue(), loggedInUser.getName());
		filterOptions.put(AssigneeSelections.EVERYONE.getSelectionValue(), null);
		
		final ReportListFilterer assigneeFilterer = new ReportListFilterer("Assignee:", "assigned", filterOptions, eventRouter); 
		filterOptionsLayput.addComponent(assigneeFilterer);

		final ReportStatusFilterer statusFilterer = new ReportStatusFilterer("Status:", "status", eventRouter); 
		filterOptionsLayput.addComponent(statusFilterer);
		
		final SummarySearch summmarySearch = new SummarySearch("", "summary", eventRouter); 
		filterOptionsLayput.addComponent(summmarySearch);
		
		filterOptionsLayput.setComponentAlignment(summmarySearch, Alignment.MIDDLE_RIGHT);

		return filterOptionsLayput;
	}
}
