package net.sourceforge.actool.ui;

//import net.sourceforge.actool.Config;

import java.util.ArrayList;
import java.util.Arrays;

import net.sourceforge.actool.ProblemManager;
import net.sourceforge.actool.model.ModelManager;
import net.sourceforge.actool.model.da.ArchitectureModel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.ide.IDE;

public class ModelPropertyPage extends PropertyPage {

	private static final String PROJECTS_TEXT ="Select projectList controlled by this model...";
	private static final String VIOLATIONS_TEXT ="Report violations as:";
	private static final String UNMAPPED_TEXT ="Report unmapped resources as:";

	private Composite contents;
	private Combo violationsCombo;
	//private Combo unmappedCombo;
	private CheckboxTableViewer projectList;
	
	
	/**
	 * Constructor for SamplePropertyPage.
	 */
	public ModelPropertyPage() {
		super();
	}
	
	public ArchitectureModel getModel() {
		return ModelManager.getDefault()
			.getArchitectureModel((IFile) getElement().getAdapter(IFile.class));
	}

	public ProblemManager getProblemManager() {
		return ProblemManager.getInstance(getModel());
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}
	
	private void addErrorsWarningsSection(Composite parent) {
		contents = createComposite(parent, 2, false);
		Label label;
		
		label = new Label(contents, SWT.HORIZONTAL);
		label.setText(VIOLATIONS_TEXT);
		violationsCombo = createCombo();
		
		//label = new Label(contents, SWT.HORIZONTAL);
		//label.setText(UNMAPPED_TEXT);		
		//unmappedCombo = createCombo();
	}

	private void addProjectsSection(Composite parent) {
        Composite composite = createComposite(parent, 1, true);
        initializeDialogUnits(composite);
        
		Label label = new Label(composite, SWT.NONE);
		label.setText(PROJECTS_TEXT);
  
        projectList = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
        projectList.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        projectList.setLabelProvider(new LabelProvider() {
        	@Override
        	public Image getImage(Object element) {
        		return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
        	}

        	@Override
        	public String getText(Object element) {
        		return ((IProject) element).getName();
        	}
        });
		
        // Filter out closed projects!
        ArrayList<IProject> projects = new ArrayList<IProject>();
        for (IProject project: ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
        	if (project.isOpen())
        		projects.add(project);
        }
        projectList.setContentProvider(new ArrayContentProvider());
        projectList.setInput(projects);

        Dialog.applyDialogFont(composite);
	}

	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		addErrorsWarningsSection(parent);
		addSeparator(parent);
		addProjectsSection(parent);
		
		initSettings();
		return parent;
	}

	private static Composite createComposite(Composite parent, int columns, boolean expand) {
		Composite contents = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = columns;
		contents.setLayout(layout);

		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = expand;
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		contents.setLayoutData(data);

		return contents;
	}
	
	private Combo createCombo() {
		Combo combo = new Combo(contents, SWT.READ_ONLY);
		combo.setItems(new String[] {
			"Error",
			"Warning",
			"Info",
			"Ignore"
		});
		return combo;
	}
	
	private void setCombo(Combo combo, int value) {
		switch (value) {
		case IMarker.SEVERITY_ERROR:
			combo.select(0);
			break;
		case IMarker.SEVERITY_WARNING:
			combo.select(1);
			break;
		case IMarker.SEVERITY_INFO:
			combo.select(2);
			break;
		default:
			combo.select(3);
		}
	}
	
	private int getCombo(Combo combo) {
		switch (combo.getSelectionIndex()) {
		case 0:
			return IMarker.SEVERITY_ERROR;
		case 1:
			return IMarker.SEVERITY_WARNING;
		case 2:
			return IMarker.SEVERITY_INFO;
			
		default:
			return -1;
		}
	}
	
	private IProject[] getControlledProjects() {
		Object[] elements = projectList.getCheckedElements();
		IProject[] projects = new IProject[elements.length];
		for (int i = 0; i < elements.length; ++i)
			projects[i] = (IProject) elements[i];
		
		return projects;
	}
	
	private void setControlledProjects(IProject[] projects) {
		projectList.setCheckedElements(projects);
	}
	
	protected void initSettings() {
		performDefaults();
		
		ProblemManager manager = getProblemManager();
		setCombo(violationsCombo, manager.getViolationSeverity());
		//setCombo(unmappedCombo, manager.getUnmappedSeverity());
		setControlledProjects(manager.getControlledProjects());
	}

	protected void performDefaults() {
		violationsCombo.select(1);
		//unmappedCombo.select(2);
	}
	
	public boolean performOk() {		
		ProblemManager manager = getProblemManager();
		manager.setViolationSeverity(getCombo(violationsCombo));
		//manager.setUnmappedSeverity(getCombo(unmappedCombo));
		manager.setControlledProjects(getControlledProjects());

		return true;
	}

}