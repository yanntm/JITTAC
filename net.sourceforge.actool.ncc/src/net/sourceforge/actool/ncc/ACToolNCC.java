package net.sourceforge.actool.ncc;

import net.sourceforge.actool.model.ModelManager;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ACToolNCC extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.sourceforge.actool.ncc";

	// The shared instance
	private static ACToolNCC plugin;
	
	/**
	 * The constructor
	 */
	public ACToolNCC() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		// HACK: Add the IA model factory before configuring projects.
		ModelManager manager = ModelManager.getDefault();
		manager.addImplementationModelFactory(new NCCModelFactory());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ACToolNCC getDefault() {
		return plugin;
	}

}
