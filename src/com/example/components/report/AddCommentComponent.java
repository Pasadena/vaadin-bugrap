package com.example.components.report;

import java.util.Date;

import com.example.events.report.CommentCreatedEvent;
import com.example.events.report.ReportSelectedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.reports.Comment;
import com.vaadin.incubator.bugrap.model.reports.CommentType;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddCommentComponent extends CustomComponent {
	
	private boolean isOpen;
	private VerticalLayout container;
	private TextArea commentArea;
	private Button addCommentButton;
	
	private Report reportToComment;
	private final EventRouter eventRouter;
	
	public AddCommentComponent(final Report reportToComment, final EventRouter eventRouter) {
		this.reportToComment = reportToComment;
		this.eventRouter = eventRouter;
		this.container = new VerticalLayout();
		this.container.setSizeUndefined();
		this.container.setWidth(100, Unit.PERCENTAGE);
		
		this.createCLosedLayout();
		this.createAddCommentLayout();
		
		this.toggleLayoutComponents(this.isOpen);
		
		eventRouter.addListener(ReportSelectedEvent.class, this, "setSelectedReport");
		
		setCompositionRoot(this.container);
	}
	
	public void setSelectedReport(final ReportSelectedEvent event) {
		this.reportToComment = event.getSelectedReport();
	}
	
	private void createCLosedLayout() {
		VerticalLayout commentBoxHiddenLayout = new VerticalLayout();
		commentBoxHiddenLayout.setSizeUndefined();
		Button openCommentSectionButton = new Button("Add comment", event -> this.toggleOpen());
		openCommentSectionButton.addStyleName(ValoTheme.BUTTON_LINK);
		openCommentSectionButton.setSizeUndefined();
		commentBoxHiddenLayout.addComponent(openCommentSectionButton);
		container.addComponent(commentBoxHiddenLayout);
	}
	
	private void createAddCommentLayout() {
		VerticalLayout addCommentLayout = new VerticalLayout();
		addCommentLayout.setSpacing(true);
		addCommentLayout.addComponents(this.createCommentAreaLayout(), this.createCommentActionsLayout());
		this.container.addComponent(addCommentLayout);
	}
	
	private HorizontalLayout createCommentAreaLayout() {
		HorizontalLayout commentLayout = new HorizontalLayout();
		commentLayout.setWidth(100, Unit.PERCENTAGE);
		
		this.commentArea = new TextArea("Add comment");
		commentArea.setWidth(100, Unit.PERCENTAGE);
		commentArea.setImmediate(true);
		//TODO: This should be changed to KeyPressHAndler (etc.). This requires work on client level (?), so let's do this when we doo other gwt-related stuff.
		commentArea.addValueChangeListener(event -> toggleCommentButtonEnabled(event.getProperty().getValue()));
		
		commentLayout.addComponent(commentArea);
		return commentLayout;
	}
	
	private void toggleCommentButtonEnabled(Object value) {
		if(value != null && !value.toString().trim().isEmpty()) {
			addCommentButton.setEnabled(true);
		} else {
			addCommentButton.setEnabled(false);
		}
	}
	
	private HorizontalLayout createCommentActionsLayout() {
		HorizontalLayout commentActionsLayout = new HorizontalLayout();
		commentActionsLayout.setSpacing(true);
		
		this.addCommentButton = new Button("Done", event -> this.addComment());
		addCommentButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		addCommentButton.setEnabled(false);
		
		Button cancel = new Button("Cancel", event -> this.toggleOpen());
		cancel.addStyleName(ValoTheme.BUTTON_SMALL);
		
		commentActionsLayout.addComponents(addCommentButton, cancel);
		return commentActionsLayout;
	}

	private void toggleOpen() {
		this.isOpen = !this.isOpen;
		this.toggleLayoutComponents(this.isOpen);
	}
	
	private void toggleLayoutComponents(boolean commentLayoutVisible) {
		this.container.getComponent(0).setVisible(!commentLayoutVisible);
		this.container.getComponent(1).setVisible(commentLayoutVisible);
	}
	
	private void addComment() {
		Comment comment = new Comment();
		comment.setComment(this.commentArea.getValue());
		comment.setAuthor((Reporter)VaadinService.getCurrentRequest().getWrappedSession().getAttribute("loggedInUser"));
		
		comment.setType(CommentType.COMMENT);
		comment.setTimestamp(new Date());
		comment.setReport(this.reportToComment);
		eventRouter.fireEvent(new CommentCreatedEvent(this, FacadeUtil.store(comment)));
		this.toggleOpen(); 
	}

}
