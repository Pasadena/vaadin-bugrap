package com.example.components;

import java.util.Locale;

import com.example.events.FilterChangedEvent;
import com.example.events.ProjectVersionSelectedEvent;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
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
	
	private BeanItemContainer<Report> reportContainer;
	
	public ReportList(String title, EventRouter eventRouter) {
		super(title);
		this.setWidth(100, Unit.PERCENTAGE);
		this.setContainerDataSource(getReportsContainer(null));
		eventRouter.addListener(ProjectVersionSelectedEvent.class, this, "handleProjectVersionChange");
		eventRouter.addListener(FilterChangedEvent.class, this, "registerListFilter");
		this.toggleTableVisibility();
		this.setConverter("assigned", new ReporterConverter());
	}
	
	private BeanItemContainer<Report> getReportsContainer(ProjectVersion version) {
		this.reportContainer = new BeanItemContainer<>(Report.class);
		if(version != null) {
			reportContainer.addAll(FacadeUtil.getReportsForVersion(version));
		}
		return reportContainer;
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
	
	public void registerListFilter(FilterChangedEvent filterChangedEvent) {
		this.reportContainer.removeAllContainerFilters();
		Filter assigneeFilter = new AssigneeFilter(filterChangedEvent.getFilterName(), filterChangedEvent.getFilterValue()); 
		this.reportContainer.addContainerFilter(assigneeFilter);
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
	
	private class AssigneeFilter implements Container.Filter {
		
		private final Object propertyId;
		private final Object filterValue;
		
		public AssigneeFilter(final Object propertyId, final Object filterValue) {
			this.propertyId = propertyId;
			this.filterValue = filterValue;
		}

		@Override
		public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
			if(filterValue == null) {
				return true;
			}
			@SuppressWarnings("rawtypes") Property property= item.getItemProperty(this.propertyId);
			if(property == null) {
				return false;
			}
			return property.toString().contains(filterValue.toString());
		}

		@Override
		public boolean appliesToProperty(Object propertyId) {
			return propertyId != null && this.propertyId.equals(propertyId);
		}
		
	}

}
