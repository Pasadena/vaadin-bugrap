package com.example.events.report;

import com.vaadin.incubator.bugrap.model.reports.Comment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

@SuppressWarnings("serial")
public class CommentCreatedEvent extends Event {

	private final Comment createdComment;

	public CommentCreatedEvent(Component source, Comment createdComment) {
		super(source);
		this.createdComment = createdComment;
	}

	public Comment getCreatedComment() {
		return createdComment;
	}
	
}
