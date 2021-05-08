package net.sourceforge.actool;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class Config {
	private static final String QUALIFIER				= ACTool.PLUGIN_ID;


	public static final String AC_ENABLE				= "net.sourceforge.actool.enabled";

	private final IScopeContext[] contexts;
	private final IPreferencesService service 	= Platform.getPreferencesService();
	
	protected Config() {
		contexts = new IScopeContext[] {
				new InstanceScope()// do not change 
				,new ConfigurationScope()// do not change 
				,new DefaultScope()// do not change 
		};
	}
	
	protected Config(IProject project) {
		contexts = new IScopeContext[] {
				new ProjectScope(project)
				,new InstanceScope()// do not change 
				,new ConfigurationScope()// do not change 
				,new DefaultScope()// do not change 
		};
	}

	public static Config getInstance(IResource resource) {
		return new Config(resource.getProject());
	}
	
	
	protected IEclipsePreferences getStoreNode() {
		return contexts[0].getNode(QUALIFIER);
	}
	
	public void addPreferenceChangeListener(IPreferenceChangeListener listener) {
		getStoreNode().addPreferenceChangeListener(listener);
	}

	public void removePreferenceChangeListener(IPreferenceChangeListener listener) {
		getStoreNode().removePreferenceChangeListener(listener);
	}
	
	public boolean getBoolean(String key) {
		return service.getBoolean(QUALIFIER, key, false, contexts);
	}
	
	public void setBoolean(String key, boolean value) {
		getStoreNode().putBoolean(key, value);
	}

	public float getFloat(String key) {
		return service.getFloat(QUALIFIER, key, 0.0f, contexts);
	}

	public void setFloat(String key, float value) {
		getStoreNode().putFloat(key, value);
	}
	
	public int getInteger(String key) {
		return service.getInt(QUALIFIER, key, 0, contexts);
	}

	public void setInteger(String key, int value) {
		getStoreNode().putInt(key, value);
	}

	public String getString(String key) {
		return service.getString(QUALIFIER, key, null, contexts);
	}

	public void setString(String key, String value) {
		getStoreNode().put(key, value);
	}
	
	public void flush() throws BackingStoreException {
		getStoreNode().flush();
	}
}
