package jittac;

import net.sourceforge.actool.ACTool;

import org.eclipse.jface.preference.IPreferenceStore;

public abstract class Preferences {
    public static final String IGNORE_LIBRARY_REFERENCES = "ignoreLibraryPreferences";
    public static final String IGNORE_INTRAPROJECT_REFERENCES = "ignoreIntraProjectPreferences";


    public static IPreferenceStore preferenceStore() {
        return ACTool.getDefault().getPreferenceStore();
    }
}