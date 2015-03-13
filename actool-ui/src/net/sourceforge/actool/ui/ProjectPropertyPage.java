package net.sourceforge.actool.ui;

//import net.sourceforge.actool.Config;

import net.sourceforge.actool.Config;
import net.sourceforge.actool.model.da.ModelProperties;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

public class ProjectPropertyPage extends PropertyPage {

	private static final String ENABLE_TEXT = "Enable Architecture Consistency checking...";
	private static final String VIOLATIONS_TEXT ="Report violations as:";
	private static final String UNMAPPED_TEXT ="Report unmapped resources as:";

	private Composite contents;
	private Button enableButton;
	private Combo violationsCombo;
	private Combo unmappedCombo;
	

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public ProjectPropertyPage() {
		super();
	}
	
	private void addTopSection(Composite parent) {
		// Create a check button to disable/enable
		enableButton = new Button(parent, SWT.CHECK);
		enableButton.setText(ENABLE_TEXT);
		enableButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				updateSettings();
			}		
		});
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	
	
	private void addSettingsSection(Composite parent) {
		contents = createContentsComposite(parent);
		Label label;
		
		label = new Label(contents, SWT.HORIZONTAL);
		label.setText(VIOLATIONS_TEXT);
		violationsCombo = createCombo();
		
		label = new Label(contents, SWT.HORIZONTAL);
		label.setText(UNMAPPED_TEXT);		
		unmappedCombo = createCombo();
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		addTopSection(parent);
		addSeparator(parent);
		addSettingsSection(parent);

		initSettings();
		return parent;
	}

	private static Composite createContentsComposite(Composite parent) {
		Composite contents = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		contents.setLayout(layout);

		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
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
	
	private void setCombo(Combo combo, String value) {
		if (value.equals(ModelProperties.ERROR))
			combo.select(0);
		else if (value.equals(ModelProperties.WARNING))
			combo.select(1);
		else if (value.equals(ModelProperties.INFO))
			combo.select(2);
		else
			combo.select(3);

	}
	
	private String getCombo(Combo combo) {
		switch (combo.getSelectionIndex()) {
		case 0:
			return ModelProperties.ERROR;
		case 1:
			return ModelProperties.WARNING;
		case 2:
			return ModelProperties.INFO;
			
		default:
			return ModelProperties.IGNORE;
		}
	}
	
	protected void initSettings() {
		performDefaults();
		
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		if (project != null) {
			Config prefs = Config.getInstance(project);
			
			enableButton.setSelection(prefs.getBoolean(Config.AC_ENABLE));
			setCombo(violationsCombo, prefs.getString(ModelProperties.VIOLATION_SEVERITY.toString()));
			setCombo(unmappedCombo, prefs.getString(ModelProperties.UNMAPPED_SEVERITY.toString()));
		}
		
		updateSettings();
	}
	
	protected void updateSettings() {
		boolean enabled = enableButton.getSelection();
		
		contents.setEnabled(enabled);
		for (Control control: contents.getChildren())
			control.setEnabled(enabled);
	}

	protected void performDefaults() {
		violationsCombo.select(1);
		unmappedCombo.select(2);
	}
	
	public boolean performOk() {
		IProject project = (IProject) getElement().getAdapter(IProject.class);

		if (project != null) {
			Config prefs = Config.getInstance(project);
			
			prefs.setString(ModelProperties.VIOLATION_SEVERITY.toString(), getCombo(violationsCombo));
			prefs.setString(ModelProperties.UNMAPPED_SEVERITY.toString(), getCombo(unmappedCombo));
			prefs.setBoolean(Config.AC_ENABLE, enableButton.getSelection());
			
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
				// TODO: Show a message box to the user!
				e.printStackTrace();
			}
		}

		return true;
	}

}