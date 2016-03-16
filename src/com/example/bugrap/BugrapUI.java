package com.example.bugrap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.example.bugrap.views.LoginView;
import com.example.bugrap.views.ReportsView;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.incubator.bugrap.DBTools;
import com.vaadin.incubator.bugrap.model.facade.FacadeFactory;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@Theme("bugrap")
public class BugrapUI extends UI {
	
	private Navigator navigator;

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = BugrapUI.class, widgetset = "com.example.bugrap.widgetset.BugrapWidgetset")
	public static class Servlet extends VaadinServlet {
		
		@Override
	    protected void servletInitialized() throws ServletException {
	        super.servletInitialized();
	        FacadeFactory.registerFacade("default", true);
	        /**try {
				DBTools.main(new String[0]);
			} catch (Exception e) {
			}**/
		}
		
	}

	@Override
	protected void init(VaadinRequest request) {
		getPage().setTitle("Bugreport: the alpha version");
		navigator = new Navigator(this, this);
		
		
		navigator.addView("login", LoginView.class);
		navigator.addView("main", ReportsView.class);
		if(VaadinService.getCurrentRequest().getWrappedSession().getAttribute("loggedInUser") == null) {
			navigator.navigateTo("login");
		}
	}
 
}