package com.example.components;

import com.example.bugrap.constants.SessionAttributeConstants;
import com.example.bugrap.util.SessionUtils;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class LoggedInUserInfo extends CustomComponent {
	
	public LoggedInUserInfo(final Reporter loggedInUser, final Navigator navigator) {
		
		GridLayout componentLayout = new GridLayout(2, 1);
		componentLayout.setSpacing(true);
		
		Label currentUserImage = new Label("Logged in as: " + FontAwesome.USER.getHtml() + " " +loggedInUser.getName());
		currentUserImage.setContentMode(ContentMode.HTML);
		currentUserImage.addStyleName("button-aligned-label");

		final Button logoutButton = new Button("Logout", clickEvent -> {
			SessionUtils.storeValueToSession(SessionAttributeConstants.LOGGED_IN_USER.getAttributeName(), null);
			navigator.navigateTo("login");
		});
		logoutButton.setIcon(FontAwesome.KEY);
		logoutButton.addStyleName(BaseTheme.BUTTON_LINK);

		componentLayout.addComponent(currentUserImage);
		componentLayout.addComponent(logoutButton);
		
		componentLayout.setSizeUndefined();
		currentUserImage.setSizeUndefined();
		logoutButton.setSizeUndefined();
		
		setCompositionRoot(componentLayout);
	}

}
