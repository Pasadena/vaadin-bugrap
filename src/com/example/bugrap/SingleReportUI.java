package com.example.bugrap;

import com.example.bugrap.util.HtmlUtils;
import com.example.components.report.EditReportComponent;
import com.vaadin.annotations.Theme;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("bugrap")
public class SingleReportUI extends UI {

	@Override
	protected void init(VaadinRequest request) {
		
		String reportId = (String)request.getParameter(EditReportComponent.REPORT_ID_PARAM);
		Report selectedReport = FacadeUtil.getReport(Long.valueOf(reportId));
		EventRouter currentRouter = (EventRouter)VaadinService.getCurrentRequest().getWrappedSession().getAttribute("eventRouter");
		EditReportComponent singleReportView = new EditReportComponent(currentRouter, selectedReport, true);
		HorizontalLayout breadCrumbs = this.createBreadcrumb(selectedReport);
		
		VerticalLayout content = new VerticalLayout();
		content.addStyleName("main-layout");
		
		content.addComponents(breadCrumbs, singleReportView);
		
		content.setExpandRatio(breadCrumbs, 0.1f);
		content.setExpandRatio(singleReportView, 0.9f);
		
		setContent(content);
		
	}
	
	private HorizontalLayout createBreadcrumb(Report report) {
		HorizontalLayout breadCrumbLayout = new HorizontalLayout();
		breadCrumbLayout.setSpacing(true);
		
		Label projectNameField = HtmlUtils.createHeader(report.getProject().getName(), 2);
		Label versionNameField = HtmlUtils.createHeader(report.getVersion().getVersion(), 2);
		Label arrow = HtmlUtils.createHeader(FontAwesome.ARROW_RIGHT.getHtml(), 2);
		
		breadCrumbLayout.addComponents(projectNameField, arrow, versionNameField);
		
		return breadCrumbLayout;
	}

}
