package com.example.components.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.example.events.report.CommentCreatedEvent;
import com.example.events.report.ReportSelectedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.reports.Comment;
import com.vaadin.incubator.bugrap.model.reports.CommentType;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinService;
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
		
		eventRouter.addListener(ReportSelectedEvent.class, this, "setSelectedReport");
		eventRouter.addListener(UploadStatusComponent.UploadInterruptedException.class, this, "cancelUpload");
		eventRouter.addListener(UploadStatusComponent.UploadFinishedEvent.class, this, "uploadSucceeded");
		
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
		addCommentLayout.addComponents(this.createCommentAreaLayout(), this.createAttachmentListLayout(), this.createCommentActionsLayout());
		this.container.addComponent(addCommentLayout);
	}
	
	private HorizontalLayout createCommentAreaLayout() {
		HorizontalLayout commentLayout = new HorizontalLayout();
		commentLayout.setWidth(100, Unit.PERCENTAGE);
		
		this.commentArea = new TextArea("Add comment");
		commentArea.setWidth(100, Unit.PERCENTAGE);
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
	
	private Link createUploadedFileLink(final Comment comment) {
		Link attachmentLink = new AttachmentOpener(comment);
		return attachmentLink;
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
		
		Button cancel = new Button("Cancel", event -> this.toggleOpen());
		cancel.addStyleName(ValoTheme.BUTTON_SMALL);
		
		commentActionsLayout.addComponents(addCommentButton, attachmentUpload, cancel);
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
		List<Comment> savedComments = new ArrayList<>();
		if(!this.reportAttachments.isEmpty()) {
			for(Comment comment: this.reportAttachments) {
				savedComments.add(FacadeUtil.store(comment));
			}
		}
		if(!StringUtils.isBlank(this.commentArea.getValue())) {
			Comment comment = this.createSkeletonComment(CommentType.COMMENT);
			comment.setComment(this.commentArea.getValue());
			savedComments.add(FacadeUtil.store(comment));
		}
		
		eventRouter.fireEvent(new CommentCreatedEvent(this, savedComments));
		this.toggleOpen(); 
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
