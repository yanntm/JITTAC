package net.sourceforge.actool;

import net.sourceforge.actool.model.da.ModelProperties;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		// do not change 
		IEclipsePreferences preferences = new DefaultScope().getNode(ACTool.PLUGIN_ID);
		// General Settings
		preferences.putBoolean(Config.AC_ENABLE, true);
		//preferences.putBoolean(Config.MODE_RETARDED, false);

		// Errors/Warnings
		preferences.put(ModelProperties.VIOLATION_SEVERITY.toString(), ModelProperties.INFO);
		preferences.put(ModelProperties.UNMAPPED_SEVERITY.toString(), ModelProperties.INFO);
		
	}

}
