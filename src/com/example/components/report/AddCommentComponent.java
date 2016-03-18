package com.example.components.report;

import java.util.Date;

import com.vaadin.incubator.bugrap.model.facade.FacadeFactory;
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
	
	private Report reportToComment;
	
	public AddCommentComponent(Report reportToComment) {
		this.reportToComment = reportToComment;
		this.container = new VerticalLayout();
		this.container.setSizeUndefined();
		this.container.setWidth(100, Unit.PERCENTAGE);
		
		this.createCLosedLayout();
		this.createAddCommentLayout();
		
		this.toggleLayoutComponents(this.isOpen);
		
		setCompositionRoot(this.container);
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
		
		commentLayout.addComponent(commentArea);
		return commentLayout;
	}
	
	private HorizontalLayout createCommentActionsLayout() {
		HorizontalLayout commentActionsLayout = new HorizontalLayout();
		commentActionsLayout.setSpacing(true);
		
		Button addCommentButton = new Button("Done", event -> this.addComment());
		addCommentButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		
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
		comment.setId(-1);
		comment.setComment(this.commentArea.getValue());
		comment.setAuthor((Reporter)VaadinService.getCurrentRequest().getWrappedSession().getAttribute("loggedInUser"));
		comment.setReport(reportToComment);
		comment.setType(CommentType.COMMENT);
		comment.setTimestamp(new Date());
		FacadeFactory.getFacade().store(comment);
		this.toggleOpen(); 
	}

}
