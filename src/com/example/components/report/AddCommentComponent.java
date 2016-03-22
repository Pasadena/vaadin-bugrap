package com.example.components.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.bugrap.BugrapUI;
import com.example.events.report.CommentCreatedEvent;
import com.example.events.report.ReportSelectedEvent;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.reports.Comment;
import com.vaadin.incubator.bugrap.model.reports.CommentType;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.users.Reporter;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractOrderedLayout;
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
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
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
		//TODO: This should be changed to KeyPressHAndler (etc.). This requires work on client level (?), so let's do this when we doo other gwt-related stuff. Hint: TextChangedListener
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
	
	private HorizontalLayout createAttachmentListLayout() {
		this.attachmentList = new HorizontalLayout();
		this.attachmentList.setSpacing(true);
		return attachmentList;
	}
	
	private Link createUploadedFileLink(final Comment comment) {
		Link attachmentLink = new Link();
		attachmentLink.setCaption(comment.getAttachmentName());
		
		StreamSource attachmentSource = new StreamSource() {
			
			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(comment.getAttachment());
			}
		};
		StreamResource attachmentResource = new StreamResource(attachmentSource, comment.getAttachmentName()); 
		
		BrowserWindowOpener opener = new BrowserWindowOpener(attachmentResource);
		opener.extend(attachmentLink);
		return attachmentLink;
	}
	
	private void startUpload(final String fileName) {
		UploadStatusComponent uploadStatus = new UploadStatusComponent(fileName, eventRouter, new Comment());
		this.attachmentList.addComponent(uploadStatus);
		UI.getCurrent().setPollInterval(100);
		attachmentUpload.setReceiver(uploadStatus);
		attachmentUpload.addFailedListener(uploadStatus);
		attachmentUpload.addProgressListener(uploadStatus);
		attachmentUpload.addSucceededListener(uploadStatus);
	}
	
	public void uploadSucceeded(UploadStatusComponent.UploadFinishedEvent event) {
		UI.getCurrent().setPollInterval(-1);
		Component component = event.getComponent();
		
		attachmentUpload.setReceiver(null);
		this.attachmentUpload.removeProgressListener((UploadStatusComponent)component);
		this.attachmentUpload.removeFailedListener((UploadStatusComponent)component);
		this.attachmentUpload.removeSucceededListener((UploadStatusComponent)component);
		
		this.attachmentList.replaceComponent(component, this.createUploadedFileLink(event.getComment()));
		
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
		attachmentUpload.addStartedListener(event -> {
			startUpload(event.getFilename());
			/**new Thread(() -> startUpload(event.getFilename())).start();
			TestThread thred = new TestThread(attachmentList);
			
			attachmentUpload.addSucceededListener(thred);
			attachmentUpload.setReceiver(thred);
			
			thred.start(); **/
		});
		
		Button cancel = new Button("Cancel", event -> this.toggleOpen());
		cancel.addStyleName(ValoTheme.BUTTON_SMALL);
		
		commentActionsLayout.addComponents(addCommentButton, attachmentUpload, cancel);
		return commentActionsLayout;
	}
	
	public void cancelUpload(UploadStatusComponent.UploadInterruptedException event) {
		this.attachmentUpload.interruptUpload();
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
		comment.setReport(this.reportToComment);
		eventRouter.fireEvent(new CommentCreatedEvent(this, FacadeUtil.store(comment)));
		this.toggleOpen(); 
	}
	
	private class UploadStatusComponent extends CustomComponent implements Upload.Receiver, SucceededListener, FailedListener, ProgressListener {
		
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
		private ByteArrayOutputStream uploadStream;
		
		public UploadStatusComponent(final String fileName, final EventRouter eventRouter, final Comment comment) {
			this.container = new HorizontalLayout();
			this.container.setSpacing(true);
			this.attachmentProgress = new ProgressBar(Float.valueOf(0.0f));
			this.eventRouter = eventRouter;
			this.fileNameField = new Label(fileName);
			this.comment = comment;
			
			Button cancelUploadLink = new Button("", event -> eventRouter.fireEvent(new UploadInterruptedException(this)));
			cancelUploadLink.setIcon(FontAwesome.REMOVE);
			cancelUploadLink.addStyleName(ValoTheme.BUTTON_LINK);
			
			container.addComponents(fileNameField, attachmentProgress, cancelUploadLink);
			
			this.setCompositionRoot(this.container);
		}
		
		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			this.comment.setAttachmentName(filename);
			this.uploadStream = new ByteArrayOutputStream(); 
			return uploadStream;
		}

		@Override
		public void uploadFailed(FailedEvent event) {
			UI.getCurrent().setPollInterval(-1);
			Notification.show("Upload failed! Please try again", Notification.Type.ERROR_MESSAGE);
		}

		@Override
		public void uploadSucceeded(SucceededEvent event) {
			this.comment.setAttachment(uploadStream.toByteArray());
			eventRouter.fireEvent(new UploadFinishedEvent(this, this.comment));
		}

		@Override
		public void updateProgress(long readBytes, long contentLength) {
			float progress = (Long.valueOf(readBytes).floatValue() / Long.valueOf(contentLength).floatValue());
			this.attachmentProgress.setValue(progress);
			try {
				Thread.sleep(200);
			} catch (InterruptedException ie) {}
			
		}
	}
	
	private class AttachmentReceiver implements Upload.Receiver, SucceededListener, FailedListener {

		private ByteArrayOutputStream uploadStream;
		private final Comment comment;
		
		public AttachmentReceiver(final Comment comment) {
			this.comment = comment;
		}
		
		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			this.comment.setAttachmentName(filename);
			this.uploadStream = new ByteArrayOutputStream(); 
			return uploadStream;
		}
		
		@Override
		public void uploadSucceeded(SucceededEvent event) {
			this.comment.setAttachment(uploadStream.toByteArray());
		}
		
		@Override
		public void uploadFailed(FailedEvent event) {
			UI.getCurrent().setPollInterval(-1);
			Notification.show("Upload failed! Please try again", Notification.Type.ERROR_MESSAGE);
		}
	}
	
	private class UploadThread extends Thread {
		
		volatile float progress = 0.0f;
		private final AbstractField<Float> updateIndicator;
		
		public UploadThread(final AbstractField<Float> updatableIndicator) {
			this.updateIndicator = updatableIndicator;
		}
		
		@Override
		public void run() {
			
			while(progress <= 100) {
				progress += progress + 0.1f;
				
				try {
					sleep(50);
				} catch (InterruptedException ie) {}
				
				UI.getCurrent().access(new Runnable() {
					
					@Override
					public void run() {
						updateIndicator.setValue(progress);
					}
				});
			}
			
		}
	}
	
	private class TestThread extends Thread implements SucceededListener, Upload.Receiver {
		
		private AbstractOrderedLayout target;
		
		public TestThread(AbstractOrderedLayout target) {
			this.target = target;
		}
		
		@Override
		public void run() {
			try {
				sleep(2000);
			} catch (InterruptedException e) {}
		}

		@Override
		public void uploadSucceeded(SucceededEvent event) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {}
			UI.getCurrent().access(new Runnable() {
				
				@Override
				public void run() {
					target.addComponent(new Label("foo"));
				}
			});
			
		}
		
		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			return new ByteArrayOutputStream(); 
		}
		
	}

}
