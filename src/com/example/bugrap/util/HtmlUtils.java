package com.example.bugrap.util;

import java.text.MessageFormat;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class HtmlUtils {
	
	public static Label createHeader(String headerContent, int headerLevel) {
		Label headerComponent = new Label();
		headerComponent.setValue(createHeaderHtml(headerContent, headerLevel));
		headerComponent.setContentMode(ContentMode.HTML);
		return headerComponent;
	}
	
	private static String createHeaderHtml(String contentInHeader, int headerLevel) {
		return MessageFormat.format("<h{0}>{1}</h{0}>", headerLevel, contentInHeader);
	}
 
}
