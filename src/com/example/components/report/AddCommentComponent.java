package com.example.components.report;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.example.events.report.CommentCreatedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.reports.Comment;
import com.vaadin.incubator.bugrap.model.reports.CommentType;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class AddCommentComponent extends CustomComponent {

	private static final String COMMENT_SAVE_FAILED_MESSAGE = "Failed to save comment! Please try again later";
	private static final String ATTACHMENT_SAVE_FAILED_MESSAGE = "Failed to save attachment %s!";
	
	private boolean isOpen;
	
	private VerticalLayout container;
	private TextArea commentArea;
	private Button addCommentButton;
	private Upload attachmentUpload;
	private HorizontalLayout attachmentList;
	
	private Report reportToComment;
	private final EventRouter eventRouter;

	private List<Comment> reportAttachments = new ArrayList<Comment>();
	
	public AddCommentComponent(final Report reportToComment, final EventRouter eventRouter) {
		this.reportToComment = reportToComment;
		this.eventRouter = eventRouter;
		this.container = new VerticalLayout();
		this.container.setSizeUndefined();
		this.container.setWidth(100, Unit.PERCENTAGE);
		
		this.createCLosedLayout();
		this.createAddCommentLayout();
		
		this.toggleLayoutComponents(this.isOpen);

		eventRouter.addListener(UploadStatusComponent.UploadInterruptedException.class, this, "cancelUpload");
		eventRouter.addListener(UploadStatusComponent.UploadFinishedEvent.class, this, "uploadSucceeded");
		
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
		addCommentLayout.addComponents(this.createCommentAreaLayout(), this.createAttachmentListLayout(), this.createCommentActionsLayout());
		this.container.addComponent(addCommentLayout);
	}
	
	private HorizontalLayout createCommentAreaLayout() {
		HorizontalLayout commentLayout = new HorizontalLayout();
		commentLayout.setWidth(100, Unit.PERCENTAGE);
		
		this.commentArea = new TextArea("Add comment");
		this.commentArea.setInputPrompt("Add a comment...");
		commentArea.setWidth(100, Unit.PERCENTAGE);
		commentArea.setRows(10);
		commentArea.setImmediate(true);
		commentArea.addTextChangeListener(event -> toggleCommentButtonEnabled(event.getText()));
		commentLayout.addComponent(commentArea);
		return commentLayout;
	}
	
	private void toggleCommentButtonEnabled(String value) {
		if(!StringUtils.isBlank(value)) {
			addCommentButton.setEnabled(true);
		} else {
			addCommentButton.setEnabled(false);
		}
	}
	
	private HorizontalLayout createAttachmentListLayout() {
		this.attachmentList = new HorizontalLayout();
		this.attachmentList.setSpacing(true);
		return attachmentList;
	}
	
	private HorizontalLayout createUploadedFileLink(final Comment comment) {
		HorizontalLayout uploadedFileLayout = new HorizontalLayout();
		uploadedFileLayout.setSpacing(true);
		uploadedFileLayout.setId("uploadedFile");
		Link attachmentLink = new AttachmentOpener(comment);
		Button deleteAttachmentButton = new Button("", event -> {
			this.reportAttachments.remove(comment);
			this.attachmentList.removeComponent(uploadedFileLayout);
			this.attachmentUpload.setEnabled(!this.reportAttachments.isEmpty());
		});
		deleteAttachmentButton.setDescription("Delete attachment");
		deleteAttachmentButton.setIcon(FontAwesome.REMOVE);
		deleteAttachmentButton.addStyleName(ValoTheme.BUTTON_LINK);
		deleteAttachmentButton.addStyleName(ValoTheme.BUTTON_SMALL);
		uploadedFileLayout.addComponents(attachmentLink, deleteAttachmentButton);
		uploadedFileLayout.setComponentAlignment(attachmentLink, Alignment.MIDDLE_LEFT);
		return uploadedFileLayout;
	}
	
	private void startUpload(final String fileName) {
		UI.getCurrent().setPollInterval(200);
		
		UploadStatusComponent uploadStatus = new UploadStatusComponent(fileName, eventRouter, this.createSkeletonComment(CommentType.ATTACHMENT), (AttachmentReceiver)attachmentUpload.getReceiver());
		this.attachmentList.addComponent(uploadStatus);

		attachmentUpload.addFailedListener(uploadStatus);
		attachmentUpload.addProgressListener(uploadStatus);
		attachmentUpload.addSucceededListener(uploadStatus);
	}
	
	public void uploadSucceeded(UploadStatusComponent.UploadFinishedEvent event) {
		UI.getCurrent().setPollInterval(-1);
		Component component = event.getComponent();

		this.attachmentUpload.removeProgressListener((UploadStatusComponent)component);
		this.attachmentUpload.removeFailedListener((UploadStatusComponent)component);
		this.attachmentUpload.removeSucceededListener((UploadStatusComponent)component);
		
		this.attachmentList.replaceComponent(component, this.createUploadedFileLink(event.getComment()));
		this.reportAttachments.add(event.getComment());
		addCommentButton.setEnabled(true);
		Notification.show("File " +event.getComment().getAttachmentName() + " uploaded!", Notification.Type.TRAY_NOTIFICATION);
	}
	
	public void cancelUpload(UploadStatusComponent.UploadInterruptedException event) {
		this.attachmentUpload.interruptUpload();
		this.attachmentList.removeComponent(event.getComponent());
	}
	
	private HorizontalLayout createCommentActionsLayout() {
		HorizontalLayout commentActionsLayout = new HorizontalLayout();
		commentActionsLayout.setSpacing(true);
		
		this.addCommentButton = new Button("Done", event -> this.addComment());
		addCommentButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		addCommentButton.setEnabled(false);
		
		this.attachmentUpload = new Upload();
		attachmentUpload.setButtonCaption("Attachment...");
		attachmentUpload.setImmediate(true);
		attachmentUpload.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		this.attachmentUpload.setReceiver(new AttachmentReceiver());
		attachmentUpload.addStartedListener(event -> startUpload(event.getFilename()));
		
		Button cancel = new Button("Cancel", event -> this.cancelEdit());
		cancel.addStyleName(ValoTheme.BUTTON_SMALL);
		
		commentActionsLayout.addComponents(addCommentButton, attachmentUpload, cancel);
		return commentActionsLayout;
	}
	
	private void cancelEdit() {
		this.toggleOpen();
		this.commentArea.setValue("");
		this.addCommentButton.setEnabled(false);
		this.reportAttachments.clear();
		this.attachmentList.removeAllComponents();
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
		List<Comment> savedComments = new ArrayList<>();
		List<Comment> failedComments = new ArrayList<>();
		if(!this.reportAttachments.isEmpty()) {
			for(Comment comment: this.reportAttachments) {
				tryCommentSave(comment, savedComments, failedComments);
			}
		}
		if(!StringUtils.isBlank(this.commentArea.getValue())) {
			Comment comment = this.createSkeletonComment(CommentType.COMMENT);
			comment.setComment(this.commentArea.getValue());
			tryCommentSave(comment, savedComments, failedComments);
		}
		
		this.addSaveNotifications(savedComments, failedComments);
		eventRouter.fireEvent(new CommentCreatedEvent(this, savedComments));
		this.commentArea.setValue("");
		this.toggleOpen(); 
	}
	
	private void tryCommentSave(Comment toBeSavedEntity, List<Comment> successEntitites, List<Comment> failedEntities) {
		Comment savedEntity = FacadeUtil.store(toBeSavedEntity);
		if(savedEntity.getId() < 1) {
			failedEntities.add(savedEntity);
		} else {
			successEntitites.add(savedEntity);
		}
	}
	
	private void addSaveNotifications(List<Comment> successEntities, List<Comment> failedEntities) {
		failedEntities.forEach(item -> this.addSaveFailedNotifications(item));
		Set<CommentType> savedCommentTypes = successEntities.stream().map(item -> item.getType()).collect(Collectors.toSet());
		Map<CommentType, Long> commentsByType = successEntities.stream().collect(Collectors.groupingBy(item -> item.getType(), Collectors.counting()));
		if(commentsByType.keySet().size() > 1) {
			Notification.show("Comment and attachments saved succesfully!", Notification.Type.TRAY_NOTIFICATION);
		} else {
			Notification.show(getFormattedSingleCommentTypeMessage(commentsByType.entrySet().iterator().next()), Notification.Type.TRAY_NOTIFICATION);
		}
	}
	
	private String getFormattedSingleCommentTypeMessage(Map.Entry<CommentType, Long> commentTypeEntry) {
		String commentTypeName = commentTypeEntry.getKey() == CommentType.ATTACHMENT ? "Attachment" : "Comment";
		if(commentTypeEntry.getValue() > 1) {
			commentTypeName += "s";
		}
		return String.format("%s saved succesfully!", commentTypeName);
	}
	
	private void addSaveFailedNotifications(final Comment comment) {
		if(comment.getType() == CommentType.ATTACHMENT) {
			Notification.show(String.format(ATTACHMENT_SAVE_FAILED_MESSAGE, comment.getAttachmentName()), Notification.Type.ERROR_MESSAGE);
		} else {
			Notification.show(COMMENT_SAVE_FAILED_MESSAGE, Notification.Type.ERROR_MESSAGE);	
		}
	}
	
	private Comment createSkeletonComment(final CommentType commentType) {
		Comment comment = new Comment();
		comment.setAuthor((Reporter)VaadinService.getCurrentRequest().getWrappedSession().getAttribute("loggedInUser"));
		comment.setType(commentType);
		comment.setReport(this.reportToComment);
		return comment;
	}
	
	private class UploadStatusComponent extends CustomComponent implements SucceededListener, FailedListener, ProgressListener {
		
		private class UploadInterruptedException extends Event {

			public UploadInterruptedException(Component source) {
				super(source);
			}
		}
		
		private class UploadFinishedEvent extends Event {
			
			private final Comment comment;

			public UploadFinishedEvent(final Component source, final Comment comment) {
				super(source);
				this.comment = comment;
			}

			public Comment getComment() {
				return comment;
			}
			
		}
		
		private final HorizontalLayout container;
		private final Label fileNameField;
		private final ProgressBar attachmentProgress;
		private final EventRouter eventRouter;
		private final Comment comment;
		private final AttachmentReceiver receiver;
		
		public UploadStatusComponent(final String fileName, final EventRouter eventRouter, final Comment comment, final AttachmentReceiver receiver) {
			this.container = new HorizontalLayout();
			this.container.setSpacing(true);
			this.attachmentProgress = new ProgressBar(Float.valueOf(0.0f));
			this.attachmentProgress.addStyleName("bottom-aligned");
			this.eventRouter = eventRouter;
			this.fileNameField = new Label(fileName);
			this.comment = comment;
			this.receiver = receiver;
			
			this.comment.setAttachmentName(fileName);
			
			Button cancelUploadLink = new Button("", event -> eventRouter.fireEvent(new UploadInterruptedException(this)));
			cancelUploadLink.setIcon(FontAwesome.REMOVE);
			cancelUploadLink.addStyleName(ValoTheme.BUTTON_LINK);
			
			container.addComponents(fileNameField, attachmentProgress, cancelUploadLink);
			
			this.setCompositionRoot(this.container);
		}

		@Override
		public void uploadFailed(FailedEvent event) {
			UI.getCurrent().setPollInterval(-1);
			Notification.show("Upload interrupted", Notification.Type.TRAY_NOTIFICATION);
		}

		@Override
		public void uploadSucceeded(SucceededEvent event) {
			this.comment.setAttachment(receiver.getUploadStream().toByteArray());
			eventRouter.fireEvent(new UploadFinishedEvent(this, this.comment));
		}

		@Override
		public void updateProgress(long readBytes, long contentLength) {
			float progress = (Long.valueOf(readBytes).floatValue() / Long.valueOf(contentLength).floatValue());
			this.attachmentProgress.setValue(progress);
			/**try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {}**/
			
		}
	}
	
	private class AttachmentReceiver implements Upload.Receiver {
		
		private ByteArrayOutputStream uploadStream;
		
		public AttachmentReceiver() {
			this.uploadStream = new ByteArrayOutputStream();
		}

		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			return uploadStream;
		}

		public ByteArrayOutputStream getUploadStream() {
			return uploadStream;
		}
	}

}
