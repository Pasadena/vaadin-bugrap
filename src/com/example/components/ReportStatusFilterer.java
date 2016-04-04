package com.example.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.vaadin.event.EventRouter;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.incubator.bugrap.model.reports.ReportResolution;
import com.vaadin.incubator.bugrap.model.reports.ReportStatus;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class ReportStatusFilterer extends AbstractListFilterer {
	
	private OptionGroup filterOptions;
//	private PopupView customSelectionView;
	private final Map<String, ReportStatus> options;
	
	private static final Map<ReportResolution, String> resolutionLabelMap = new LinkedHashMap<>();
	
	static {
		resolutionLabelMap.put(ReportResolution.FIXED, "Fixed");
		resolutionLabelMap.put(ReportResolution.INVALID, "Invalid");
		resolutionLabelMap.put(ReportResolution.WONTFIX, "Wont't fix");
		resolutionLabelMap.put(ReportResolution.CANTFIX, "Can't fix");
		resolutionLabelMap.put(ReportResolution.DUPLICATE, "Duplicate");
		resolutionLabelMap.put(ReportResolution.WORKSFORME, "Works for me");
		resolutionLabelMap.put(ReportResolution.NEEDMOREINFO, "Needs more information");
	}
	
	private static final String OPEN_SELECTION_LABEL = "Open";
	private static final String ALL_SELECTION_LABEL = "All kinds";
	private static final String CUSTOM_SELECTION_LABEL = "Custom";
	
	private final Set<ReportResolution> selectedResolutions;
	private ReportStatus selectedStatus;
	
	private int popupPositionX;
	private int popupPositionY;
	
	public ReportStatusFilterer(final String caption, final String filterPropertyName, final EventRouter eventRouter) {
		super(caption, filterPropertyName, eventRouter);
		this.options = this.getOptionList();
		this.selectedResolutions = new HashSet<>();
		
		//this.customSelectionView = this.getCustomSelectionView();
//		this.addContent(this.getFiltererContent(), customSelectionView);
		this.addContent(this.getFiltererContent());
		
		this.registerFilterChangeListener();
	}

	@Override
	public void registerFilterChangeListener() {
		this.filterOptions.addValueChangeListener(event -> {
			if(!event.getProperty().getValue().equals(CUSTOM_SELECTION_LABEL)) {
				fireFilterChangeEvent(new StatusFiltererDTO(options.get(event.getProperty().getValue()), null));
			} else {
				this.addPopUpView();
			}
		});
		this.filterOptions.addFocusListener(event -> {
			if(this.filterOptions.getValue() != null && this.filterOptions.getValue().equals(CUSTOM_SELECTION_LABEL)) {
				this.addPopUpView();
			}
		});
		CssLayout container = (CssLayout)this.getContainer();
		container.addLayoutClickListener(event -> {
			this.popupPositionX = event.getClientX() + Float.valueOf(this.filterOptions.getHeight()).intValue();
			this.popupPositionY = event.getClientY();
		});
	}

	private void addPopUpView() {
		//this.customSelectionView.setPopupVisible(true);
		//this.customSelectionView.setVisible(true);
		StatusPopup popup = new StatusPopup(selectedStatus, selectedResolutions, this.popupPositionX, this.popupPositionY);
		UI.getCurrent().addWindow(popup);
		UI.getCurrent().addClickListener(popup);
	}
	
	private Map<String, ReportStatus> getOptionList() {
		Map<String, ReportStatus> options = new HashMap<>();
		options.put(OPEN_SELECTION_LABEL, ReportStatus.OPEN);
		options.put(ALL_SELECTION_LABEL, null);
		options.put(CUSTOM_SELECTION_LABEL, null);
		return options;
	}
	
	public Component getFiltererContent() {
		this.filterOptions = new OptionGroup(getCaption());
		filterOptions.addItems(Arrays.asList(OPEN_SELECTION_LABEL, ALL_SELECTION_LABEL, CUSTOM_SELECTION_LABEL));
		
		this.filterOptions.setSizeUndefined();
		this.filterOptions.addStyleName("horizontal");
		
		return this.filterOptions;
	}
	
	/**private PopupView getCustomSelectionView() {
		final VerticalLayout popupContent = new VerticalLayout();
		popupContent.setWidth(100, Unit.PERCENTAGE);
		popupContent.addComponent(new Label("Status:"));
		
		CheckBox openStatusBox = new CheckBox(OPEN_SELECTION_LABEL);
		openStatusBox.addStyleName("status-checkbox");
		openStatusBox.addValueChangeListener(event -> this.selectedStatus = openStatusBox.getValue() ? ReportStatus.OPEN : null);
		popupContent.addComponent(openStatusBox);
		
		for(Map.Entry<ReportResolution, String> resolutionEntry: resolutionLabelMap.entrySet()) {
			CheckBox resolutionBox = new CheckBox(resolutionEntry.getValue());
			resolutionBox.addValueChangeListener(event -> {
				if(resolutionBox.getValue()) {
					this.selectedResolutions.add(resolutionEntry.getKey());
				} else {
					this.selectedResolutions.remove(resolutionEntry.getKey());
				}
			});
			popupContent.addComponent(resolutionBox);
		}

		PopupView popupView = new PopupView(new PopupView.Content() {
			
			@Override
			public Component getPopupComponent() {
				return popupContent;
			}
			
			@Override
			public String getMinimizedValueAsHTML() {
				return null;
			}
		});

		popupView.addStyleName("status-popup");
		popupView.setHideOnMouseOut(false);
		popupView.setVisible(false);
		popupView.addPopupVisibilityListener(event -> {
			event.getComponent().setVisible(false);
			if(!event.isPopupVisible()) {
				this.fireFilterChangeEvent(new StatusFiltererDTO(this.selectedStatus, selectedResolutions));
			}
		});
		return popupView;
	}**/
	
	private class StatusPopup extends Window implements ClickListener {
		
		private ReportStatus selectedStatus;
		private Set<ReportResolution> selectedResolutions;
		private int xAxisPosition;
		private int yAxisPosition;
		public StatusPopup(ReportStatus selectedStatus, Set<ReportResolution> selectedResolutions, int xPosition, int yPosition) {
			super();
			this.selectedStatus = selectedStatus;
			this.selectedResolutions = selectedResolutions;
			this.xAxisPosition = xPosition;
			this.yAxisPosition = yPosition;
			
			this.setWindowContent();
			this.setProperties();
		}
		
		private void setProperties() {
			this.setPosition(xAxisPosition, yAxisPosition);
			this.addStyleName("popup-status");
		}
		
		private void setWindowContent() {
			final VerticalLayout popupContent = new VerticalLayout();
			popupContent.setWidth(100, Unit.PERCENTAGE);
			popupContent.addComponent(new Label("Status:"));
			
			CheckBox openStatusBox = new CheckBox(OPEN_SELECTION_LABEL);
			openStatusBox.addStyleName("status-checkbox");
			openStatusBox.addValueChangeListener(event -> this.selectedStatus = openStatusBox.getValue() ? ReportStatus.OPEN : null);
			popupContent.addComponent(openStatusBox);
			
			for(Map.Entry<ReportResolution, String> resolutionEntry: resolutionLabelMap.entrySet()) {
				CheckBox resolutionBox = new CheckBox(resolutionEntry.getValue());
				resolutionBox.addValueChangeListener(event -> {
					if(resolutionBox.getValue()) {
						this.selectedResolutions.add(resolutionEntry.getKey());
					} else {
						this.selectedResolutions.remove(resolutionEntry.getKey());
					}
				});
				popupContent.addComponent(resolutionBox);
			}
			this.setContent(popupContent);
		}
		
		@Override
		public void click(ClickEvent event) {
			fireFilterChangeEvent(new StatusFiltererDTO(this.selectedStatus, selectedResolutions));
			this.close();
			UI.getCurrent().removeClickListener(this);
		}
	}
	
	public static class StatusFiltererDTO {
		
		private ReportStatus selectedStatus;
		
		private Set<ReportResolution> selectedResolutions;

		public StatusFiltererDTO(ReportStatus selectedStatus, Set<ReportResolution> selectedResolutions) {
			this.selectedStatus = selectedStatus;
			this.selectedResolutions = selectedResolutions;
		}

		public ReportStatus getSelectedStatus() {
			return selectedStatus;
		}

		public Set<ReportResolution> getSelectedResolutions() {
			return selectedResolutions;
		}
		
	}

}
