package com.example.events.report;

import java.util.List;

import com.vaadin.incubator.bugrap.model.reports.Comment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

@SuppressWarnings("serial")
public class CommentCreatedEvent extends Event {

	private final List<Comment> createdComments;

	public CommentCreatedEvent(Component source, final List<Comment> createdComments) {
		super(source);
		this.createdComments = createdComments;
	}

	public List<Comment> getCreatedComments() {
		return createdComments;
	}
	
}
