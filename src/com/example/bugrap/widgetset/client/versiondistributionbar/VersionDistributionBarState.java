package com.example.bugrap.widgetset.client.versiondistributionbar;

import java.util.Map;

public class VersionDistributionBarState extends com.vaadin.shared.AbstractComponentState {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3178146779344191344L;
	private Map<ReportDistributionGroup, Long> distributions;

	public Map<ReportDistributionGroup, Long> getDistributions() {
		return distributions;
	}

	public void setDistributions(Map<ReportDistributionGroup, Long> distributions) {
		this.distributions = distributions;
	}

}