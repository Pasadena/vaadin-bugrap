package com.example.components;

import java.util.Locale;

import com.example.events.ProjectVersionSelectedEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class ReportList extends Table {
	
	public ReportList(String title, EventRouter eventRouter) {
		super(title);
		this.setWidth(80, Unit.PERCENTAGE);
		this.setContainerDataSource(getReportsContainer(null));
		eventRouter.addListener(ProjectVersionSelectedEvent.class, this, "handleProjectVersionChange");
		this.toggleTableVisibility();
		this.setConverter("assigned", new ReporterConverter());
	}
	
	private BeanItemContainer<Report> getReportsContainer(ProjectVersion version) {
		BeanItemContainer<Report> reportsContainer = new BeanItemContainer<>(Report.class);
		if(version != null) {
			reportsContainer.addAll(FacadeUtil.getReportsForVersion(version));
		}
		return reportsContainer;
	}
	
	public void handleProjectVersionChange(ProjectVersionSelectedEvent event) {
		this.setContainerDataSource(getReportsContainer(event.getProjectVersion()));
		this.toggleTableVisibility();
	}

	private void toggleTableVisibility() {
		if(this.getContainerDataSource().getItemIds().isEmpty()) {
			this.setVisible(false);
		} else {
			this.setVisible(true);
			this.setVisibleColumns("priority", "type", "summary", "assigned", "timestamp");
			this.setColumnHeaders("Priority", "Type", "Summary", "Assigned to", "Reported");
			this.sort(new Object[] { "priority" }, new boolean[] { false });
		}
	}

	private class ReporterConverter implements Converter<String, Reporter> {
	
		@Override
		public Reporter convertToModel(String value, Class<? extends Reporter> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			// TODO Auto-generated method stub
			return null;
		}
	
		@Override
		public String convertToPresentation(Reporter value, Class<? extends String> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			// TODO Auto-generated method stub
			if(value == null) return "";
			return value.getName();
		}
	
		@Override
		public Class<Reporter> getModelType() {
			return Reporter.class;
		}
	
		@Override
		public Class<String> getPresentationType() {
			return String.class;
		}
		
	}

}
