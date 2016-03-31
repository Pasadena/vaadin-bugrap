package com.example.bugrap.widgetset.client.versiondistributionbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class VersionDistributionBarWidget extends Composite {

	public static final String CLASSNAME = "versiondistributionbar";

	private FlowPanel container;
	private Label closedLabel;
	private Label openAndAssignedLabel;
	private Label unassignedLabel; 

	public VersionDistributionBarWidget() {

		this.container = new FlowPanel();
		this.container.addStyleName(CLASSNAME);
		this.container.setWidth("100%");
		
		this.closedLabel = this.createBarLabel("closed-bar");
		this.openAndAssignedLabel = this.createBarLabel("open-bar");
		this.unassignedLabel = this.createBarLabel("unassigned-bar");
		
		this.setDefaultBarWidths();
		
		this.container.add(this.closedLabel);
		this.container.add(this.openAndAssignedLabel);
		this.container.add(this.unassignedLabel);
		
		initWidget(container);
	}
	
	private Label createBarLabel(final String styleName) {
		Label barLabel = new Label("0");
		barLabel.addStyleName(styleName);
		return barLabel;
	}
	
	private void setDefaultBarWidths() {
		this.closedLabel.setWidth("33.33%");
		this.openAndAssignedLabel.setWidth("33.33%");
		this.unassignedLabel.setWidth("33.33%");
	}
	
	public void updateDistributions(Map<ReportDistributionGroup, Long> distributions) {
		this.updateLabel(this.closedLabel, distributions, ReportDistributionGroup.CLOSED);
		this.updateLabel(this.openAndAssignedLabel, distributions, ReportDistributionGroup.OPEN_ASSIGNED);
		this.updateLabel(this.unassignedLabel, distributions, ReportDistributionGroup.OPEN_UNASSIGNED);
		
		this.updateWidths(distributions);
	}
	
	private void updateLabel(final Label label, final Map<ReportDistributionGroup, Long> distributions, final ReportDistributionGroup key) {
		label.setText(this.getValueOrZero(distributions, key).toString());
	}
	
	private Long getValueOrZero(final Map<ReportDistributionGroup, Long> valueMap, final ReportDistributionGroup key) {
		Long valueForKey = valueMap.get(key);
		return valueForKey != null ? valueForKey : 0L;
	}
	
	private void updateWidths(Map<ReportDistributionGroup, Long> distributions) {
		Long allReportsCount = 0L;
		for(Long groupValue: distributions.values()) {
			allReportsCount += groupValue;
		}
		
		if(allReportsCount == 0L) {
			this.setDefaultBarWidths();
			return;
		}
		double totalWidthWithoutMargins = 100.0 - (2.0 / (this.container.getOffsetWidth() + 2) * 100);
		double thirtyPixelsAsPercents = 30.0 / (this.container.getOffsetWidth()) * totalWidthWithoutMargins;

		List<ValueBar> labelElements = new ArrayList<>();
		labelElements.add(this.calculateValueBar(this.closedLabel, distributions.get(ReportDistributionGroup.CLOSED), allReportsCount.doubleValue(), thirtyPixelsAsPercents, totalWidthWithoutMargins));
		labelElements.add(this.calculateValueBar(this.openAndAssignedLabel, distributions.get(ReportDistributionGroup.OPEN_ASSIGNED), allReportsCount.doubleValue(), thirtyPixelsAsPercents, totalWidthWithoutMargins));
		labelElements.add(this.calculateValueBar(this.unassignedLabel, distributions.get(ReportDistributionGroup.OPEN_UNASSIGNED), allReportsCount.doubleValue(), thirtyPixelsAsPercents, totalWidthWithoutMargins));
		
		if(this.getTotalWidthFromElements(labelElements) <= 100.0) {
			this.updateLabelSizes(labelElements);
		} else {
			this.updateLabelsWithAdjustedSizes(labelElements, thirtyPixelsAsPercents);
		}
	}
	
	private double getTotalWidthFromElements(List<ValueBar> elements) {
		double totalWidth = 0.0;
		for(ValueBar element: elements) {
			totalWidth += element.getPercent();
		}
		return totalWidth;
	}
	
	private ValueBar calculateValueBar(final Label label, final Long reportCountForLabel, final Double totalNumberOfReports, final double minimumWidthAsPercents, final double totalWidth) {
		double labelLengthAsPercents = (reportCountForLabel/ totalNumberOfReports) * totalWidth;
		double barPercentValue = labelLengthAsPercents <= minimumWidthAsPercents ? minimumWidthAsPercents: labelLengthAsPercents;
		ValueBar valueBar = new ValueBar(label, barPercentValue);
		return valueBar;
	}
	
	private void updateLabelSizes(List<ValueBar> barValues) {
		for(ValueBar valueBar: barValues) {
			valueBar.getLabel().setWidth(valueBar.getPercent() + "%");
		}
	}
	
	private void updateLabelsWithAdjustedSizes(List<ValueBar> barValues, final double minimumWidthAsPercents) {
		List<ValueBar> minimumWidthElements = new ArrayList<>();
		List<ValueBar> regularElements = new ArrayList<>();
		for(ValueBar valueBar: barValues) {
			if(valueBar.getPercent() > minimumWidthAsPercents) {
				regularElements.add(valueBar);
			} else {
				minimumWidthElements.add(valueBar);
			}
		}

		this.updateLabelSizes(minimumWidthElements);
		
		for(ValueBar valueBar: regularElements) {
			double adjustedWidth = valueBar.getPercent() - (minimumWidthAsPercents * minimumWidthElements.size() / regularElements.size());
			valueBar.getLabel().setWidth(adjustedWidth + "%");
		}
	}
	
	public class ValueBar {
		
		private final Label label;
		private final double percent;

		public ValueBar(Label label, double percent) {
			this.label = label;
			this.percent = percent;
		}

		public Label getLabel() {
			return label;
		}

		public double getPercent() {
			return percent;
		}
		
	}

}