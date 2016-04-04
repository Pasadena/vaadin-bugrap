package com.example.components;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.example.components.ReportStatusFilterer.StatusFiltererDTO;
import com.example.events.FilterChangedEvent;
import com.example.events.ProjectVersionSelectedEvent;
import com.example.events.report.ReportListUpdatedEvent;
import com.example.events.report.ReportSelectedEvent;
import com.example.events.report.ReportUpdatedEvent;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.EventRouter;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.incubator.bugrap.model.facade.FacadeFactory;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.projects.Project;
import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.reports.ReportPriority;
import com.vaadin.incubator.bugrap.model.reports.ReportResolution;
import com.vaadin.incubator.bugrap.model.reports.ReportStatus;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class ReportList extends Table {
	
	private BeanItemContainer<Report> reportContainer;
	private final EventRouter eventRouter;
	
	private ProjectVersion selectedVersion;
	
	public ReportList(String title, EventRouter eventRouter, final ProjectVersion activeVersion) {
		super(title);
		this.eventRouter = eventRouter;
		
		this.setTableProperties();
		
		this.setContainerDataSource(getReportsContainer(activeVersion));
		this.setConverter("assigned", new ReporterConverter());

		this.registerListeners();
		this.toggleTableVisibility();
		this.addGeneratedColumns();
	}
	
	private void setTableProperties() {
		this.setWidth(100, Unit.PERCENTAGE);
		this.setHeightUndefined();
		
		this.setSelectable(true);
		this.setImmediate(true);
		this.setMultiSelect(true);
		this.addValueChangeListener(event -> {
			@SuppressWarnings("unchecked")Set<Report> selectedValues = (Set<Report>)event.getProperty().getValue();
			eventRouter.fireEvent(new ReportSelectedEvent(this, selectedValues));
		});
	}
	
	private void setTableColumnProperties() {
		this.setVisibleColumns("priority", "type", "summary", "assigned", "status", "resolution", "timestamp");
		this.setColumnHeaders("Priority", "Type", "Summary", "Assigned to", "Status", "Resolution", "Reported");
		this.sort(new Object[] { "priority" }, new boolean[] { false });
	}
	
	private void addGeneratedColumns() {
		this.addGeneratedColumn("priority", new Table.ColumnGenerator() {
			
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				return new Label(getImageMatchingPriority((Report)itemId), ContentMode.HTML);
			}
		});
	}
	
	private String getImageMatchingPriority(final Report report) {
		if(report.getPriority() == null) {
			return "";
		}
		switch (report.getPriority()) {
			case BLOCKER:
				return FontAwesome.BATTERY_4.getHtml();
			case CRITICAL:
				return FontAwesome.BATTERY_3.getHtml();
			case MAJOR:
				return FontAwesome.BATTERY_2.getHtml();
			case NORMAL:
				return FontAwesome.BATTERY_1.getHtml();
			case MINOR:
				return FontAwesome.BATTERY_0.getHtml();
			case TRIVIAL:
				return FontAwesome.BATTERY_EMPTY.getHtml();
			default:
				return FontAwesome.BATTERY_EMPTY.getHtml();
		}
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
		reportContainer.setItemSorter(new ReportPrioritySorter());
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
			this.setTableColumnProperties();
		}
	}
	
	public void handleProjectVersionChange(ProjectVersionSelectedEvent event) {
		this.setContainerDataSource(getReportsContainer(event.getProjectVersion()));
		this.selectedVersion = event.getProjectVersion();
		this.toggleTableVisibility();
	}
	
	public void registerListFilter(FilterChangedEvent filterChangedEvent) {
		this.reportContainer.removeAllContainerFilters();
		if(filterChangedEvent.getFilterName().equals("assigned")) {
			this.reportContainer.addContainerFilter(new AssigneeFilter(filterChangedEvent.getFilterName(), filterChangedEvent.getFilterValue()));
		} else if(filterChangedEvent.getFilterName().equals("status")) {
			this.reportContainer.addContainerFilter(new StatusFilter(filterChangedEvent.getFilterName(), filterChangedEvent.getFilterValue()));
		} else {
			this.reportContainer.addContainerFilter(new SimpleStringFilter(filterChangedEvent.getFilterName(), filterChangedEvent.getFilterValue().toString(), true, true));
		}
	}
	
	public void updateRow(final ReportUpdatedEvent event) {
		Report updatedReport = event.getUpdatedReport();
		if(this.selectedVersion == null || updatedReport.getVersion().equals(this.selectedVersion)) {
			this.refreshRowCache();			
		} else {
			this.reportContainer.removeItem(updatedReport);
		}
		this.setValue(null);
	}
	
	public void updateRows(final ReportListUpdatedEvent event) {
		for(Report report: event.getUpdatedReports()) {
			if(!report.getVersion().equals(this.selectedVersion)) {
				this.reportContainer.removeItem(report);
			}
		}
		this.refreshRowCache();
		this.setValue(null);
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
			return null;
		}
	
		@Override
		public String convertToPresentation(Reporter value, Class<? extends String> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
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
	
	public static class ReportPrioritySorter extends DefaultItemSorter {

		public ReportPrioritySorter() {
			super(new ReportPriorityComparator());
		}
		
		public static class ReportPriorityComparator implements Comparator<Object> {

			@Override
			public int compare(Object firstObject, Object secondObject) {
				ReportPriority firstPriority = (ReportPriority)firstObject;
				ReportPriority secondPriority = (ReportPriority)secondObject;
				if(firstPriority == null && secondPriority != null) {
					return -1;
				}
				if(secondPriority == null && firstPriority != null) {
					return 1;
				}
				if(firstPriority == null && secondPriority == null) {
					return 0;
				}
				int compareResult = firstPriority.compareTo(secondPriority);
				//Hack hack, multiply with -1 in order to reverse normal enum compare (priorities are declared from worst to easiest, which is opposite that we want here).
				//Correct fix would be to include custom attribute to enum, which tells the severity of the value). But this cannot be done, so let's suffer this.
				return compareResult * -1;
			}
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
	
	private class StatusFilter implements Container.Filter {
		
		private final Object propertyId;
		private final StatusFiltererDTO filterValue;

		public StatusFilter(Object propertyId, Object filterValue) {
			this.propertyId = propertyId;
			this.filterValue = this.getFiltererDTO(filterValue);
		}
		
		private StatusFiltererDTO getFiltererDTO(Object statusFiltererObject) {
			try {
				return (StatusFiltererDTO)statusFiltererObject;
			} catch(ClassCastException cce) {
				throw new RuntimeException("Cannot create Status filerer object from filter event's value");
			}
		}

		@Override
		public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
			if(this.filterValue == null || !hasFilterValue()) {
				return true;
			}
			Report itemBean = (Report)itemId;
			ReportStatus selectedStatus = this.filterValue.getSelectedStatus();
			Set<ReportResolution> selectedResolutions = this.filterValue.getSelectedResolutions();
			
			if(selectedStatus != null && selectedStatus != itemBean.getStatus()) {
				return false;
			}
			return selectedResolutions != null ? selectedResolutions.contains(itemBean.getResolution()) : true;
		}
		
		private boolean hasFilterValue() {
			return this.filterValue.getSelectedStatus() != null || (this.filterValue.getSelectedResolutions() != null && !this.filterValue.getSelectedResolutions().isEmpty());
		}

		@Override
		public boolean appliesToProperty(Object propertyId) {
			return propertyId != null && propertyId.equals(this.propertyId);
		}
		
	}

}
