package com.example.bugrap.widgetset.client.versiondistributionbar;

import com.example.bugrap.VersionDistributionBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(VersionDistributionBar.class)
public class VersionDistributionBarConnector extends AbstractComponentConnector {

	public VersionDistributionBarConnector() {
	}

	@Override
	protected Widget createWidget() {
		return GWT.create(VersionDistributionBarWidget.class);
	}

	@Override
	public VersionDistributionBarWidget getWidget() {
		return (VersionDistributionBarWidget) super.getWidget();
	}

	@Override
	public VersionDistributionBarState getState() {
		return (VersionDistributionBarState) super.getState();
	}

	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);
		getWidget().updateDistributions(this.getState().getDistributions());
	}

}

