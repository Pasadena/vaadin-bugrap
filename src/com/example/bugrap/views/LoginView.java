package com.example.bugrap.views;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.bugrap.constants.SessionAttributeConstants;
import com.example.bugrap.util.SessionUtils;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.incubator.bugrap.model.facade.AbstractEntity;
import com.vaadin.incubator.bugrap.model.facade.FacadeFactory;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class LoginView extends VerticalLayout implements View {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Navigator navigator;

	public LoginView() {
		super();
	}

	@Override
	public void enter(ViewChangeEvent event) {
		navigator = event.getNavigator();
		
		this.setMargin(true);
		this.addComponent(createLoginForm());
		
		
	}
	
	private FormLayout createLoginForm() {
		final FormLayout loginForm = new FormLayout();
		
		final TextField usernameField = new TextField("Username");
		usernameField.focus();
		final PasswordField passWordField = new PasswordField("Password");
		
		loginForm.addComponent(usernameField);
		loginForm.addComponent(passWordField);
		
		Button button = new Button("Log in", event -> {
			final String userName =  usernameField.getValue();
			final String password = passWordField.getValue().trim();
			Map<String, Object> searchParameters = Collections.unmodifiableMap(Stream.of(new AbstractMap.SimpleEntry<>("user", userName)).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
			List<AbstractEntity> matchingUsers = FacadeFactory.getFacade().list("SELECT r FROM Reporter r WHERE r.name = :user" , searchParameters);
			
			//final Reporter user = FacadeUtil.getUser(userName, password);
			//TODO: Figure out why passwordgenerator generated different hash for inputted password.
			if(matchingUsers.isEmpty()) {
				Notification.show("Cannot find user with provided login information!", Notification.TYPE_ERROR_MESSAGE);
			} else {
				SessionUtils.storeValueToSession(SessionAttributeConstants.LOGGED_IN_USER.getAttributeName(), (Reporter)matchingUsers.get(0));
				navigator.navigateTo("main");
			}
		});
		button.setClickShortcut(KeyCode.ENTER);
		button.addStyleName(ValoTheme.BUTTON_PRIMARY);
		loginForm.addComponent(button);
		return loginForm;
	}

}
