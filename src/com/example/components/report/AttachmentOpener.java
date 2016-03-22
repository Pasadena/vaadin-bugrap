package com.example.components.report;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.vaadin.incubator.bugrap.model.reports.Comment;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Link;

@SuppressWarnings("serial")
public class AttachmentOpener extends Link {
	
	private final Comment attachmentData;
	
	public AttachmentOpener(final Comment attachmentData) {
		this.attachmentData = attachmentData;
		
		this.setCaption(attachmentData.getAttachmentName());
		
		StreamSource attachmentSource = this.createAttachmentStreamSource(this.attachmentData);
		StreamResource attachmentResource = new StreamResource(attachmentSource, attachmentData.getAttachmentName()); 
		
		this.extendWithOpener(attachmentResource);
	}

	private StreamSource createAttachmentStreamSource(final Comment attachmentData) {
		return new StreamSource() {
			
			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(attachmentData.getAttachment());
			}
		};
	}
	
	private void extendWithOpener(StreamResource attachmentResource) {
		BrowserWindowOpener opener = new BrowserWindowOpener(attachmentResource);
		opener.extend(this);
	}

}
