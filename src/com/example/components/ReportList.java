package com.example.components;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.vaadin.incubator.bugrap.model.reports.ReportType;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class ReportList extends Table {
	
	private final Object[] normalColumnProperties = new Object[] {"source.priority", "source.type", "source.summary", "source.assigned", "source.status", "source.resolution", "source.timestamp"};
	private final String[] normalColumnHeaderNames = new String[] {"Priority", "Type", "Summary", "Assigned to", "Status", "Resolution", "Reported"};
	
	private final Object[] columnPropertiesWithVersion = new Object[] {"versionName", "source.priority", "source.type", "source.summary", "source.assigned", "source.status", "source.resolution", "source.timestamp"};
	private final String[] columnHeaderNamesWithVersion = new String[] {"Version", "Priority", "Type", "Summary", "Assigned to", "Status", "Resolution", "Reported"};
	
	private final String PRIORITY_PROPERTY_NAME = "source.priority";
	private final String VERSION_PROPERTY_NAME = "versionName";
	
	private BeanItemContainer<WrappedReport> reportContainer;
	private final EventRouter eventRouter;
	
	private ProjectVersion selectedVersion;
	
	public ReportList(String title, EventRouter eventRouter, final ProjectVersion activeVersion) {
		super(title);
		this.eventRouter = eventRouter;
		this.selectedVersion = activeVersion;
		
		this.setTableProperties();
		
		this.setContainerDataSource(getReportsContainer(activeVersion));
		this.setConverter("source.timestamp", new DateToFinnishStringConverter());
		
		this.registerListeners();
		this.toggleTableVisibility(activeVersion);
		this.addGeneratedColumns();
		this.setTableSorting(this.areMultipleVersionsSelected(activeVersion));
	}
	
	private void setTableProperties() {
		this.setWidth(100, Unit.PERCENTAGE);
		this.setSelectable(true);
		this.setImmediate(true);
		this.setMultiSelect(true);
		this.addValueChangeListener(event -> {
			@SuppressWarnings("unchecked")Set<WrappedReport> selectedWrappers = (Set<WrappedReport>)event.getProperty().getValue();
			Set<Report> selectedValues = WrappedReport.getSourcesFromWrappers(selectedWrappers);
			eventRouter.fireEvent(new ReportSelectedEvent(this, selectedValues));
		});
	}
	
	private void setTableColumnProperties(boolean includeVersion) {
		if(includeVersion) {
			this.setVisibleColumns(columnPropertiesWithVersion);
			this.setColumnHeaders(columnHeaderNamesWithVersion);
		} else {
			this.setVisibleColumns(normalColumnProperties);
			this.setColumnHeaders(normalColumnHeaderNames);
		}
	}
	
	private void setTableSorting(boolean includeVersion) {
		if(includeVersion) {
			this.sort(new Object[] { VERSION_PROPERTY_NAME, PRIORITY_PROPERTY_NAME}, new boolean[] { true, false });
		} else {
			this.sort(new Object[] { PRIORITY_PROPERTY_NAME }, new boolean[] { false });
		}
	}
	
	private void addGeneratedColumns() {
		this.addGeneratedColumn(PRIORITY_PROPERTY_NAME, new Table.ColumnGenerator() {
			
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				return new Label(getImageMatchingPriority((WrappedReport)itemId), ContentMode.HTML);
			}
		});
	}
	
	private boolean areMultipleVersionsSelected(final ProjectVersion projectVersion) {
		return projectVersion != null && projectVersion.getId() <= 0;
	}
	
	private String getImageMatchingPriority(final WrappedReport report) {
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
	
	private BeanItemContainer<WrappedReport> getReportsContainer(ProjectVersion version) {
		this.reportContainer = new BeanItemContainer<>(WrappedReport.class);
		this.setNestedContainerProperties();
		if(version == null) {
			return reportContainer;
		}
		if(version.getId() > 0) {
			reportContainer.addAll(WrappedReport.buildListFromSource(FacadeUtil.getReportsForVersion(version)));
		} else {
			reportContainer.addAll(WrappedReport.buildListFromSource(getAllReportsForProject(version.getProject())));
		}
		reportContainer.setItemSorter(new ReportPrioritySorter(this.reportContainer, new Object[] {PRIORITY_PROPERTY_NAME}, new boolean[] { false }));
		return reportContainer;
	}
	
	private void setNestedContainerProperties() {
		this.reportContainer.addNestedContainerProperty("source.priority");
		this.reportContainer.addNestedContainerProperty("source.type");
		this.reportContainer.addNestedContainerProperty("source.summary");
		this.reportContainer.addNestedContainerProperty("source.assigned");
		this.reportContainer.addNestedContainerProperty("source.status");
		this.reportContainer.addNestedContainerProperty("source.resolution");
		this.reportContainer.addNestedContainerProperty("source.timestamp");
	}
	
	private List<Report> getAllReportsForProject(Project project) {
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("proj", project);
		return FacadeFactory.getFacade()
        .list("SELECT r FROM Report r WHERE r.project = :proj",
                params);
	}

	private void toggleTableVisibility(final ProjectVersion projectVersion) {
		if(this.getContainerDataSource().getItemIds().isEmpty()) {
			this.setVisible(false);
		} else {
			this.setVisible(true);
			this.setTableColumnProperties(this.areMultipleVersionsSelected(projectVersion));
		}
	}
	
	public void handleProjectVersionChange(ProjectVersionSelectedEvent event) {
		this.setContainerDataSource(getReportsContainer(event.getProjectVersion()));
		this.selectedVersion = event.getProjectVersion();
		this.setTableSorting(this.areMultipleVersionsSelected(this.selectedVersion));
		this.toggleTableVisibility(event.getProjectVersion());
	}
	
	public void registerListFilter(FilterChangedEvent filterChangedEvent) {
		this.reportContainer.removeContainerFilters(filterChangedEvent.getFilterName());
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
		if(this.selectedVersion != null && !updatedReport.getVersion().equals(this.selectedVersion)) {
			this.reportContainer.removeItem(updatedReport);			
		}
		this.refreshTableAfterUpdate();
	}
	
	public void updateRows(final ReportListUpdatedEvent event) {
		for(Report report: event.getUpdatedReports()) {
			if(report.getVersion() != null && !report.getVersion().equals(this.selectedVersion)) {
				this.reportContainer.removeItem(report);
			}
		}
		this.refreshTableAfterUpdate();
	}
	
	private void refreshTableAfterUpdate() {
		this.setContainerDataSource(this.getReportsContainer(this.selectedVersion));
		this.setTableSorting(this.areMultipleVersionsSelected(this.selectedVersion));
		this.toggleTableVisibility(this.selectedVersion);
		this.refreshRowCache();
		this.setValue(null);
		this.sort();
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
				WrappedReport clickedReport = (WrappedReport)event.getItemId();
				this.openSelectedReportInNewWindow(clickedReport.getSource());
			}
		});
	}

	private void addKeyboardEventListeners() {
		this.addShortcutListener(new ShortcutListener("", KeyCode.ENTER, new int[10]) {
			@Override
			public void handleAction(Object sender, Object target) {
				@SuppressWarnings("unchecked")
				Set<WrappedReport> selectedReports = (Set<WrappedReport>)getValue();
				if(selectedReports.size() == 1) {
					openSelectedReportInNewWindow(selectedReports.iterator().next().getSource());
				}
			}
		});
	}
	
	private void openSelectedReportInNewWindow(Report clickedReport) {
		getUI().getPage().open(getUI().getPage().getLocation().getPath() + "popup/SingleReportUI?reportId=" +clickedReport.getId(), getUI().getPage().getWindowName() +clickedReport.getId());
	}
	
	public static class WrappedReport {
		
		public static List<WrappedReport> buildListFromSource(final List<Report> sources) {
			List<WrappedReport> result = new ArrayList<ReportList.WrappedReport>();
			for(Report report: sources) {
				result.add(new WrappedReport(report));
			}
			return result;
		}
		
		public static Set<Report> getSourcesFromWrappers(final Set<WrappedReport> wrappers) {
			Set<Report> result = new HashSet<Report>();
			for(WrappedReport wrapper: wrappers) {
				result.add(wrapper.getSource());
			}
			return result;
		}
		
		private final Report source;
		
		public WrappedReport(final Report source) {
			this.source = source;
		}
		
		public Report getSource() {
			return source;
		}
		
		public String getVersionName() {
			return source.getVersion() != null ? source.getVersion().getVersion() : null;
		}
		
		public ReportType getType() {
	        return source.getType();
	    }
		
		public String getSummary() {
	        return source.getSummary();
	    }
		
		public ReportPriority getPriority() {
	        return source.getPriority();
	    }
		
		public Reporter getAssigned() {
	        return source.getAssigned();
	    }
		
		public ReportStatus getStatus() {
	        return source.getStatus();
	    }
		
		public ReportResolution getResolution() {
	        return source.getResolution();
	    }
		
		public Date getTimestamp() {
	        return source.getTimestamp();
	    }
	}
	
	public static class ReportPrioritySorter extends DefaultItemSorter {

		public ReportPrioritySorter(Container.Sortable container, Object[] properties, boolean[] ascending) {
			super(new ReportPriorityComparator());
			this.setSortProperties(container, properties, ascending);
		}
		
		public static class ReportPriorityComparator implements Comparator<Object> {

			@Override
			public int compare(Object firstObject, Object secondObject) {
				if(firstObject instanceof String || secondObject instanceof String) {
					return compareObjects((String)firstObject, (String)secondObject, 1);
				} else {
					//Hack hack, multiply with -1 in order to reverse normal enum compare (priorities are declared from worst to easiest, which is opposite that we want here).
					//Correct fix would be to include custom attribute to enum, which tells the severity of the value). But this cannot be done, so let's suffer this.
					return compareObjects((ReportPriority)firstObject, (ReportPriority)secondObject, -1);
				}
				
			}
			
			
			@SuppressWarnings("rawtypes" )
			private int compareObjects(final Comparable first, final Comparable second, int multiplier) {
				if(first == null && second != null) {
					return -1;
				}
				if(second == null && first != null) {
					return 1;
				}
				if(first == null && second == null) {
					return 0;
				}
				@SuppressWarnings("unchecked")
				int compareResult = first.compareTo(second);
				return compareResult * multiplier;
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
			Reporter reporter = (Reporter)property.getValue();
			return reporter != null && reporter.getName().contains(filterValue.toString());
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
			WrappedReport itemBean = (WrappedReport)itemId;
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
	
	private class DateToFinnishStringConverter implements Converter<String, Date> {
		
		private final SimpleDateFormat formatter;
		
		public DateToFinnishStringConverter() {
			this.formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		}

		@Override
		public Date convertToModel(String value, Class<? extends Date> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			return null;
		}

		@Override
		public String convertToPresentation(Date value, Class<? extends String> targetType, Locale locale)
				throws com.vaadin.data.util.converter.Converter.ConversionException {
			// TODO Auto-generated method stub
			return formatter.format(value);
		}

		@Override
		public Class<Date> getModelType() {
			return Date.class;
		}

		@Override
		public Class<String> getPresentationType() {
			return String.class;
		}
		
	}

}
