package com.example.components.report;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.events.report.CommentCreatedEvent;
import com.example.events.report.ReportSelectedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.reports.Comment;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ReportCommentListComponent extends CustomComponent {
	
	private Report report;
	
	private final VerticalLayout commentListLayout;
	
	public ReportCommentListComponent(final Report report, final EventRouter eventRouter) {
		this.report = report;
		this.commentListLayout = new VerticalLayout();
		this.commentListLayout.setWidth(100, Unit.PERCENTAGE);

		this.refreshCommentList(this.report);
		
		eventRouter.addListener(CommentCreatedEvent.class, this, "addCommentToList");
		eventRouter.addListener(ReportSelectedEvent.class, this, "setSelectedReport");
		
		setCompositionRoot(commentListLayout);
	}
	
	public void setSelectedReport(final ReportSelectedEvent event) {
		this.report = event.getSelectedReport();
		this.refreshCommentList(this.report);
	}
	
	private void refreshCommentList(final Report selectedReport) {
		this.commentListLayout.removeAllComponents();
		List<VerticalLayout> comments = this.createComments(selectedReport);
		this.commentListLayout.addComponents(comments.toArray(new VerticalLayout[comments.size()]));
	}
	
	private List<VerticalLayout> createComments(Report selectedReport) {
		List<VerticalLayout> comments = new ArrayList<>();
		if(selectedReport == null) {
			return comments;
		}
		//TODO: These probably need to be sorted
		List<Comment> reportComments = FacadeUtil.getComments(selectedReport);
		
		for(Comment comment: reportComments) {
			VerticalLayout singleCommentLayout = this.createSingleCommentLayout(comment);
			comments.add(singleCommentLayout);
		}
		
		return comments;
	}
	
	private VerticalLayout createSingleCommentLayout(final Comment comment) {
		VerticalLayout singleCommentLayout = new VerticalLayout();
		singleCommentLayout.addStyleName("comment-layout");
		
		Label commenterInfo = new Label(FontAwesome.USER.getHtml() + " " +comment.getAuthor().getName() + " (" + this.getTimeSincePostedString(comment.getTimestamp()) + ")");
		commenterInfo.setContentMode(ContentMode.HTML);
		commenterInfo.addStyleName("header");
		
		Label commentArea = new Label();
		commentArea.setValue(comment.getComment());
		commentArea.setContentMode(ContentMode.HTML);
		commentArea.addStyleName("body");
		
		singleCommentLayout.addComponents(commenterInfo, commentArea);
		return singleCommentLayout;
	}
	
	private String getTimeSincePostedString(final Date timeOfComment) {	
		ZonedDateTime zonedTimeOfComment = timeOfComment.toInstant().atZone(ZoneId.systemDefault());
		LocalDate commentTime = zonedTimeOfComment.toLocalDate();
		Period difference = Period.between(commentTime, LocalDate.now());
		long dayDifference = difference.getDays();
		if(dayDifference == 0) {
			return getHoursBetweenDatesString(zonedTimeOfComment);
		} else {
			return String.format("%s %s ago", dayDifference, dayDifference > 1 ? "days" : "day");
		}
	}
	
	private String getHoursBetweenDatesString(final ZonedDateTime timeOfComment) {
		LocalDateTime commentTime = timeOfComment.toLocalDateTime();
		Duration timeSinceComment = Duration.between(commentTime, LocalDateTime.now());
		long hoursSinceComment = timeSinceComment.toHours();
		switch(Long.valueOf(hoursSinceComment).intValue()) {
			case 0:
				return String.valueOf("Less than hour ago");
			case 1:
				return String.valueOf("One hour ago");
			default:
				return String.format("%s hours ago", hoursSinceComment);
		}
	}
	
	public void addCommentToList(CommentCreatedEvent event) {
		VerticalLayout commentLayout = this.createSingleCommentLayout(event.getCreatedComment());
		this.commentListLayout.addComponent(commentLayout, 0);
	}

}
