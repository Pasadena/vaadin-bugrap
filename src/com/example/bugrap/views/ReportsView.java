package com.example.bugrap.views;

import org.vaadin.alump.distributionbar.DistributionBar;

import com.example.components.ProjectSelectComponent;
import com.example.components.ProjectVersionSelectComponent;
import com.example.components.ReportList;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class ReportsView extends VerticalLayout implements View {

	private Navigator navigator;
	private Reporter loggedInUser; 
	
	private NativeSelect versionSelect;
	private ProjectSelectComponent projectSelect;
	
	private final EventRouter eventRouter = new EventRouter();
	
	@Override
	public void enter(ViewChangeEvent event) {
		navigator = event.getNavigator();
	}
	
	public ReportsView() {
		loggedInUser = (Reporter)VaadinService.getCurrentRequest().getWrappedSession().getAttribute("loggedInUser");
		this.setViewProperties();
		this.addComponent(getViewHeader());
		this.addComponent(this.getVersionSelectBar());
		this.addComponent(getFilterOptionsLayout());
		this.addComponent(getReportsSection());
		
	}
	
	private void setViewProperties() {
		setSizeFull();
		setSpacing(true);
		setMargin(true);
	}

	private HorizontalLayout getViewHeader() {
		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setWidth(100, Unit.PERCENTAGE);
		projectSelect = new ProjectSelectComponent("", eventRouter);
		HorizontalLayout loggedInUserComponent = getLoggedInUserInfo();
		headerLayout.addComponent(projectSelect);
		headerLayout.setComponentAlignment(projectSelect, Alignment.MIDDLE_LEFT);
		headerLayout.addComponent(loggedInUserComponent);
		headerLayout.setExpandRatio(projectSelect, 3.0f);
		headerLayout.setExpandRatio(loggedInUserComponent, 1.0f);
		headerLayout.setComponentAlignment(loggedInUserComponent, Alignment.MIDDLE_RIGHT);
		return headerLayout;
	}
	
	private HorizontalLayout getLoggedInUserInfo() {
		HorizontalLayout loggedInUserSection = new HorizontalLayout();
		HorizontalLayout userInfoLayout = new HorizontalLayout();
		loggedInUserSection.setWidth(100, Unit.PERCENTAGE);
		Label currentUserImage = new Label(FontAwesome.USER.getHtml());
		Label currentUserName = new Label(loggedInUser.getName());
		currentUserImage.setContentMode(ContentMode.HTML);
		final Button logoutButton = new Button("Logout", clickEvent -> {
			getUI().getSession().setAttribute(Reporter.class, null);
			navigator.navigateTo("login");
		});
		logoutButton.setIcon(FontAwesome.KEY);
		logoutButton.addStyleName(BaseTheme.BUTTON_LINK);
		userInfoLayout.addComponent(currentUserImage);
		userInfoLayout.addComponent(currentUserName);
		loggedInUserSection.addComponent(userInfoLayout);
		loggedInUserSection.setComponentAlignment(userInfoLayout, Alignment.MIDDLE_LEFT);
		loggedInUserSection.addComponent(logoutButton);
		loggedInUserSection.setComponentAlignment(logoutButton, Alignment.MIDDLE_RIGHT);
		return loggedInUserSection;
	}
	
	private VerticalLayout getReportsSection() {
		VerticalLayout reportSection = new VerticalLayout();
		reportSection.setSizeFull();
		ReportList reportList = new ReportList("", eventRouter);
		reportSection.addComponent(reportList);
		return reportSection;
	}
	
	
	
	private HorizontalLayout getVersionSelectBar() {
		HorizontalLayout versionBar = new HorizontalLayout();
		versionBar.setWidth(100, Unit.PERCENTAGE);
		FormLayout versionLayout = new FormLayout();
		versionBar.addComponent(versionLayout);
		versionLayout.addComponent(getVersionSelectComponent());
		DistributionBar distributionBar = new DistributionBar(3);
		distributionBar.setupPart(0, 0, "Closed");
		distributionBar.getState().getParts().get(0).setStyleName("bar-part-first");
		distributionBar.getState().getParts().get(0).setTooltip("Closed");
		distributionBar.setupPart(1, 0, "Assigned");
		distributionBar.getState().getParts().get(1).setStyleName("bar-part-second");
		distributionBar.getState().getParts().get(1).setTooltip("Assigned");
		distributionBar.setupPart(2, 0, "Unassigned");
		distributionBar.getState().getParts().get(2).setStyleName("bar-part-third");
		distributionBar.getState().getParts().get(2).setTooltip("Unassigned");
		distributionBar.setMinPartWidth(30.0);
		distributionBar.setPartSize(0, 30.0);
		distributionBar.setPartSize(1, 30.0);
		distributionBar.setPartSize(2, 30.0);
		//distributionBar.setSizeFull();
		versionBar.addComponent(distributionBar);
		return versionBar;
	}
	
	private NativeSelect getVersionSelectComponent() {
		if(versionSelect == null) {
			versionSelect = new ProjectVersionSelectComponent("Reports for", eventRouter);
		}
		return versionSelect;
	}
	
	private HorizontalLayout getFilterOptionsLayout() {
		HorizontalLayout filterOptionsLayput = new HorizontalLayout();
		filterOptionsLayput.addComponent(new ReportListFilterer("Assignee", new Object[] {"Only me", "Everyone"}, eventRouter));
		return filterOptionsLayput;
	}
	
	public class FilterChangedEvent extends Event {
		
		private final Object filterValue;
		
		public FilterChangedEvent(final Component source, Object filterValue) {
			super(source);
			this.filterValue = filterValue;
		}

		public Object getFilterValue() {
			return filterValue;
		}
		
	}
	
	public class ReportListFilterer extends CustomComponent {
		
		public ReportListFilterer(final String caption, final Object[] options, final EventRouter eventRouter) {
			FormLayout filtererContainer = new FormLayout();
			OptionGroup filterOptions = new OptionGroup(caption);
			filterOptions.addItems(options);
			filterOptions.addValueChangeListener(event -> {
				eventRouter.fireEvent(new FilterChangedEvent(this, event.getProperty().getValue()));
			});
			filtererContainer.addComponent(filterOptions);
			
			filtererContainer.setSizeUndefined();
			filterOptions.setSizeUndefined();
			
			setCompositionRoot(filtererContainer);
		}
		
	}
}
