package com.example.bugrap.views;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.alump.distributionbar.DistributionBar;

import com.example.bugrap.constants.AssigneeSelections;
import com.example.components.LoggedInUserInfo;
import com.example.components.ProjectSelectComponent;
import com.example.components.ProjectVersionSelectComponent;
import com.example.components.ReportList;
import com.example.components.ReportListFilterer;
import com.example.components.report.EditReportComponent;
import com.example.components.report.MassEditReportsComponent;
import com.example.events.report.ReportSelectedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
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
		
		reportsView.addComponent(getViewHeader());
		reportsView.addComponent(this.getVersionSelectBar());
		reportsView.addComponent(getFilterOptionsLayout());
		reportsView.addComponent(getReportsSection());
		
		this.setFirstComponent(reportsView);
		
		eventRouter.addListener(ReportSelectedEvent.class, this, "setSelectedReport");
	}
	
	private void setViewProperties() {
		this.setSizeFull();
		this.setSplitPosition(70, Unit.PERCENTAGE);
		this.addStyleName("main-layout");
	}
	
	public void setSelectedReport(ReportSelectedEvent event) {
		if(event.getSelectedReports().size() <= 1) {
			this.replaceComponent(this.getSecondComponent(), new EditReportComponent(eventRouter, event.getSelectedReports().iterator().next(), false));
		} else {
			this.replaceComponent(this.getSecondComponent(), new MassEditReportsComponent(event.getSelectedReports()));
		}
		
	}

	private HorizontalLayout getViewHeader() {
		HorizontalLayout headerLayout = new HorizontalLayout();
		FormLayout selectProjectLayout = new FormLayout();
		LoggedInUserInfo loggedInUserComponent = new LoggedInUserInfo(loggedInUser, navigator);
		projectSelect = new ProjectSelectComponent("Select project: ", eventRouter);
		
		headerLayout.setWidth(100, Unit.PERCENTAGE);
		
		selectProjectLayout.addComponent(projectSelect);
		
		headerLayout.addComponent(selectProjectLayout);
		headerLayout.addComponent(loggedInUserComponent);
		
		headerLayout.setExpandRatio(selectProjectLayout, 3.0f);
		headerLayout.setExpandRatio(loggedInUserComponent, 1.0f);
		
		headerLayout.setComponentAlignment(selectProjectLayout, Alignment.MIDDLE_LEFT);
		headerLayout.setComponentAlignment(loggedInUserComponent, Alignment.MIDDLE_RIGHT);
		return headerLayout;
	}
	
	private VerticalLayout getReportsSection() {
		VerticalLayout reportSection = new VerticalLayout();
		ReportList reportList = new ReportList("", eventRouter);
		reportSection.addComponent(reportList);
		return reportSection;
	}
	
	private HorizontalLayout getVersionSelectBar() {
		HorizontalLayout versionBar = new HorizontalLayout();
		versionBar.setWidth(100, Unit.PERCENTAGE);
		FormLayout versionLayout = new FormLayout();
		versionBar.addComponent(versionLayout);
		versionLayout.addComponent(getVersionSelectComponent());
		DistributionBar distributionBar = new DistributionBar(3);
		distributionBar.setupPart(0, 0, "Closed");
		distributionBar.getState().getParts().get(0).setStyleName("bar-part-first");
		distributionBar.getState().getParts().get(0).setTooltip("Closed");
		distributionBar.setupPart(1, 0, "Assigned");
		distributionBar.getState().getParts().get(1).setStyleName("bar-part-second");
		distributionBar.getState().getParts().get(1).setTooltip("Assigned");
		distributionBar.setupPart(2, 0, "Unassigned");
		distributionBar.getState().getParts().get(2).setStyleName("bar-part-third");
		distributionBar.getState().getParts().get(2).setTooltip("Unassigned");
		distributionBar.setMinPartWidth(30.0);
		distributionBar.setPartSize(0, 30.0);
		distributionBar.setPartSize(1, 30.0);
		distributionBar.setPartSize(2, 30.0);
		//distributionBar.setSizeFull();
		//versionBar.addComponent(distributionBar);
		return versionBar;
	}
	
	private NativeSelect getVersionSelectComponent() {
		if(versionSelect == null) {
			versionSelect = new ProjectVersionSelectComponent("Reports for", eventRouter);
		}
		return versionSelect;
	}
	
	private HorizontalLayout getFilterOptionsLayout() {
		HorizontalLayout filterOptionsLayput = new HorizontalLayout();
		Map<String, Object> filterOptions = new HashMap<>();
		filterOptions.put(AssigneeSelections.FOR_ME.getSelectionValue(), loggedInUser.getName());
		filterOptions.put(AssigneeSelections.EVERYONE.getSelectionValue(), null);
		filterOptionsLayput.addComponent(new ReportListFilterer("Assignee", "assigned", filterOptions, eventRouter));
		return filterOptionsLayput;
	}
}
