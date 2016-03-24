package com.example.components;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.example.events.FilterChangedEvent;
import com.example.events.ProjectVersionSelectedEvent;
import com.example.events.report.ReportListUpdatedEvent;
import com.example.events.report.ReportSelectedEvent;
import com.example.events.report.ReportUpdatedEvent;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.EventRouter;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.incubator.bugrap.model.facade.FacadeFactory;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.projects.Project;
import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class ReportList extends Table {
	
	private BeanItemContainer<Report> reportContainer;
	private final EventRouter eventRouter;
	
	private ProjectVersion selectedVersion;
	
	public ReportList(String title, EventRouter eventRouter) {
		super(title);
		this.eventRouter = eventRouter;
		this.setTableProperties();
		this.setContainerDataSource(getReportsContainer(null));
		this.setConverter("assigned", new ReporterConverter());
		
		this.registerListeners();
		this.toggleTableVisibility();
	}
	
	private void setTableProperties() {
		//TODO: Set selection mode to multi
		this.setWidth(90, Unit.PERCENTAGE);
		this.setHeightUndefined();
		
		this.setSelectable(true);
		this.setImmediate(true);
		this.setMultiSelect(true);
		this.addValueChangeListener(event -> {
			@SuppressWarnings("unchecked")Set<Report> selectedValues = (Set<Report>)event.getProperty().getValue();
			eventRouter.fireEvent(new ReportSelectedEvent(this, selectedValues));
		});
	}
	
	private BeanItemContainer<Report> getReportsContainer(ProjectVersion version) {
		this.reportContainer = new BeanItemContainer<>(Report.class);
		if(version == null) {
			return reportContainer;
		}
		if(version.getId() > 0) {
			reportContainer.addAll(FacadeUtil.getReportsForVersion(version));
		} else {
			//TODO: In this case add version-column to list
			reportContainer.addAll(getAllReportsForProject(version.getProject()));
		}
		return reportContainer;
	}
	
	private List<Report> getAllReportsForProject(Project project) {
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("proj", project);
		return FacadeFactory.getFacade()
        .list("SELECT r FROM Report r WHERE r.project = :proj",
                params);
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
	
	public void handleProjectVersionChange(ProjectVersionSelectedEvent event) {
		this.setContainerDataSource(getReportsContainer(event.getProjectVersion()));
		this.selectedVersion = event.getProjectVersion();
		this.toggleTableVisibility();
	}
	
	public void registerListFilter(FilterChangedEvent filterChangedEvent) {
		this.reportContainer.removeAllContainerFilters();
		Filter assigneeFilter = new AssigneeFilter(filterChangedEvent.getFilterName(), filterChangedEvent.getFilterValue()); 
		this.reportContainer.addContainerFilter(assigneeFilter);
	}
	
	public void updateRow(final ReportUpdatedEvent event) {
		Report updatedReport = event.getUpdatedReport();
		if(updatedReport.getVersion().equals(this.selectedVersion)) {
			this.refreshRowCache();			
		} else {
			this.reportContainer.removeItem(updatedReport);
		}
	}
	
	public void updateRows(final ReportListUpdatedEvent event) {
		for(Report report: event.getUpdatedReports()) {
			if(!report.getVersion().equals(this.selectedVersion)) {
				this.reportContainer.removeItem(report);
			}
		}
		this.refreshRowCache();
	}
	
	private void registerListeners() {
		eventRouter.addListener(ProjectVersionSelectedEvent.class, this, "handleProjectVersionChange");
		eventRouter.addListener(FilterChangedEvent.class, this, "registerListFilter");
		eventRouter.addListener(ReportUpdatedEvent.class, this, "updateRow");
		eventRouter.addListener(ReportListUpdatedEvent.class, this, "updateRows");
		this.addItemCLickListeners();
		this.addKeyboardEventListeners();
	}
	
	private void addItemCLickListeners() {
		this.addItemClickListener(event -> {
			if(event.isDoubleClick()) {
				//eventRouter.fireEvent(new ReportSelectedEvent(this, (Report)event.getItemId()));
			}
		});
	}

	private void addKeyboardEventListeners() {
		this.addShortcutListener(new ShortcutListener("", KeyCode.ENTER, new int[10]) {
			@Override
			public void handleAction(Object sender, Object target) {
				//eventRouter.fireEvent(new ReportSelectedEvent(ReportList.this, (Report)getValue()));
			}
		});
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
