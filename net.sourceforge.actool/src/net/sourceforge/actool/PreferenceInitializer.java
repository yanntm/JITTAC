package net.sourceforge.actool;

import static jittac.Preferences.IGNORE_INTRAPROJECT_REFERENCES;
import static jittac.Preferences.IGNORE_LIBRARY_REFERENCES;
import static jittac.Preferences.preferenceStore;
import net.sourceforge.actool.model.da.ModelProperties;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		// do not change 
		IEclipsePreferences preferences = DefaultScope.INSTANCE.getNode(ACTool.PLUGIN_ID);
		// General Settings
		preferences.putBoolean(Config.AC_ENABLE, true);
		//preferences.putBoolean(Config.MODE_RETARDED, false);

		// Errors/Warnings
		preferences.put(ModelProperties.VIOLATION_SEVERITY.toString(), ModelProperties.INFO);
		preferences.put(ModelProperties.UNMAPPED_SEVERITY.toString(), ModelProperties.INFO);
		
        preferenceStore().setDefault(IGNORE_LIBRARY_REFERENCES, true);
        preferenceStore().setDefault(IGNORE_INTRAPROJECT_REFERENCES, false);
	}

}
