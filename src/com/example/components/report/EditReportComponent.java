package com.example.components.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.events.report.ReportSelectedEvent;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.event.EventRouter;
import com.vaadin.incubator.bugrap.model.facade.FacadeFactory;
import com.vaadin.incubator.bugrap.model.facade.FacadeUtil;
import com.vaadin.incubator.bugrap.model.projects.ProjectVersion;
import com.vaadin.incubator.bugrap.model.reports.Comment;
import com.vaadin.incubator.bugrap.model.reports.Report;
import com.vaadin.incubator.bugrap.model.reports.ReportPriority;
import com.vaadin.incubator.bugrap.model.reports.ReportStatus;
import com.vaadin.incubator.bugrap.model.reports.ReportType;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class EditReportComponent extends CustomComponent {
	
	private final VerticalLayout container;
	private HorizontalLayout actionsBar;
	private NativeSelect versionSelect;
	private NativeSelect typeSelect;
	private NativeSelect prioritySelect;
	private NativeSelect statusSelect;
	private NativeSelect assigneeSelect;
	
	private Label projectNameField;
	
	private final BeanFieldGroup<Report> fieldGroup;
	private Report editableReport;
	
	public EditReportComponent(EventRouter eventRouter) {
		this.container = new VerticalLayout();
		this.editableReport = null;
		this.fieldGroup = new BeanFieldGroup<>(Report.class);
		
		container.setSizeUndefined();
		container.setSpacing(true);
		container.setMargin(true);
		container.addStyleName("no-horizontal-padding");
		
		eventRouter.addListener(ReportSelectedEvent.class, this, "setSelectedReport");
		
		container.addComponent(this.createHeaderRow());
		container.addComponent(this.createActionsBar());
		container.addComponent(this.createCommentSection(null));
		container.addComponent(this.createAddCommentSection());
		
		setSizeUndefined();
		setCompositionRoot(container);
		toggleVisibility(editableReport);
	}
	
	private HorizontalLayout createActionsBar() {
		this.actionsBar = new HorizontalLayout();
		
		this.createActionBarSelects();
		this.createActionBarButtons();
		
		actionsBar.setSpacing(true);
		actionsBar.setSizeUndefined();
		
		for(Component child: actionsBar) {
			child.setSizeUndefined();
		}
		
		return actionsBar;
	}
	
	private HorizontalLayout createHeaderRow() {
		HorizontalLayout header = new HorizontalLayout();
		Link newWindowLink = new Link();
		newWindowLink.setIcon(FontAwesome.EXTERNAL_LINK);
		this.projectNameField = new Label("");
		
		newWindowLink.setSizeUndefined();
		projectNameField.setSizeUndefined();
		
		header.addComponents(newWindowLink, projectNameField);
		header.setSizeUndefined();
		header.setSpacing(true);
		
		return header;
	}
	
	private VerticalLayout createCommentSection(Report selectedReport) {
		VerticalLayout commentSection = new VerticalLayout();
		commentSection.setSizeUndefined();
		if(selectedReport == null) {
			return commentSection;
		}
		
		List<Comment> reportComments = FacadeUtil.getComments(selectedReport);
		
		for(Comment comment: reportComments) {
			VerticalLayout singleCommentLayout = new VerticalLayout();
			Label commentatorInfo = new Label(FontAwesome.USER.getHtml() + " " +comment.getAuthor().getName() + " (" + comment.getTimestamp().toString() + ")");
			
			TextArea commentArea = new TextArea();
			commentArea.setValue(comment.getComment());
			commentArea.setEnabled(false);
			
			singleCommentLayout.addComponents(commentatorInfo, commentArea);
			
			commentSection.addComponent(singleCommentLayout);
		}
		
		return commentSection;
	}
	
	public VerticalLayout createAddCommentSection() {
		VerticalLayout addCommentsLayout = new VerticalLayout();
		HorizontalLayout commentLayout = new HorizontalLayout();
		HorizontalLayout commentActionsLayout = new HorizontalLayout();
		TextArea newCommentArea = new TextArea("Add comment");
		Button addCommentButton = new Button("Done", event -> {
			
		});
		addCommentsLayout.setSizeUndefined();
		addCommentButton.setSizeUndefined();
		newCommentArea.setSizeUndefined();
		
		commentLayout.addComponent(newCommentArea);
		commentActionsLayout.addComponent(addCommentButton);
		addCommentsLayout.addComponents(commentLayout, commentActionsLayout);
		
		return addCommentsLayout;
	}
	
	private void createActionBarSelects() {
		this.prioritySelect = new NativeSelect("Priority", Arrays.asList(ReportPriority.values()));
		this.typeSelect = new NativeSelect("Type", Arrays.asList(ReportType.values()));
		this.statusSelect = new NativeSelect("Status", Arrays.asList(ReportStatus.values()));		
		this.assigneeSelect = new NativeSelect("Assigned to", FacadeUtil.getReporters());
		this.versionSelect = new NativeSelect("Version", new ArrayList<ProjectVersion>());
		actionsBar.addComponents(prioritySelect, typeSelect, statusSelect, assigneeSelect, versionSelect);
	}
	
	private void createActionBarButtons() {
		Button updateButton = new Button("Update", event -> this.saveReport());
		
		Button revertButton = new Button("Revert", event -> this.discardChanges());
		revertButton.addStyleName("danger bottom-aligned");
		updateButton.addStyleName("primary bottom-aligned");
		actionsBar.addComponents(updateButton, revertButton);
	}
	
	private void saveReport() {
		try {
			fieldGroup.commit();
			FacadeFactory.getFacade().store(this.editableReport);
			Notification.show("Report updated", Notification.Type.TRAY_NOTIFICATION);
		} catch (CommitException ce) {
			Notification.show("Something went terribly wrong! Unable to update report", Notification.Type.ERROR_MESSAGE);
		}
	}
	
	private void discardChanges() {
		fieldGroup.discard();
		Notification.show("Discarded all changes to this report", Notification.Type.TRAY_NOTIFICATION);
	}
	
	private void toggleVisibility(Report report) {
		this.setVisible(report != null);
	}
	
	public void setSelectedReport(ReportSelectedEvent event) {
		this.editableReport = event.getSelectedReport();
		this.updateVersionList(this.editableReport);
		this.projectNameField.setValue(this.editableReport.getSummary());
		this.bindValuesToForm(this.editableReport);
		toggleVisibility(editableReport);
	}
	
	private void updateVersionList(final Report selectedReport) {
		this.versionSelect.clear();
		this.versionSelect.addItems(FacadeUtil.getVersions(selectedReport.getProject()));
	}
	
	private void bindValuesToForm(final Report selectedReport) {
		fieldGroup.setItemDataSource(selectedReport);
		
		fieldGroup.bind(prioritySelect, "priority");
		fieldGroup.bind(typeSelect, "type");
		fieldGroup.bind(statusSelect, "status");
		fieldGroup.bind(assigneeSelect, "assigned");
		fieldGroup.bind(versionSelect, "version");
	}
}
