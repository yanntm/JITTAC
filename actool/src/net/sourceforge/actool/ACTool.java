package net.sourceforge.actool;

import java.io.File;
import java.io.FileOutputStream;

import net.sourceforge.actool.logging.EventLogger;
import net.sourceforge.actool.logging.WorkbenchActivityLogger;
import net.sourceforge.actool.model.ModelManager;
import net.sourceforge.actool.model.ia.IImplementationModelFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;



/**
 * The activator class controls the plug-in life cycle
 */
public class ACTool extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.sourceforge.actool";
	
	public static final String EXTID_IA_MODEL = "net.sourceforge.actool.model.ia";

	// The shared instance
	private static ACTool core;
	
	private EventLogger logger;
	
	/**
	 * The constructor
	 */
	public ACTool() {
	}
	
    public static EventLogger getEventLogger() {
    	return core.logger;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		core = this;
		
		// Open the log file.
		File file = getStateLocation().append("event.log").toFile();
		file.createNewFile();
		
		// Create and initialise logger.
		logger = new EventLogger(Platform.getPreferencesService().getString(PLUGIN_ID, "act.uid", "000000", null),
								 new FileOutputStream(file, true));
		logger.logStartup();
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		new WorkbenchActivityLogger(logger).register(workbench);
		
		for (IProject project: ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isOpen())
				ProblemManager.initialiseProject(project);
		}
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		logger.logShutdown();
		logger.close();
		
		core = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ACTool getDefault() {
		return core;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/**
	 * Load all plugins providing implementation model factories.
	 */
	public void  initialiseImplementationModelFactories() {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(ACTool.EXTID_IA_MODEL);

		for (IConfigurationElement element : config) {
			try {
				Object obj = element.createExecutableExtension("class");
				
				if (obj instanceof IImplementationModelFactory)
					// HACK: This will load the JDT plugin and the plugin will register it's factory.
					ModelManager.defaultModelManager().addImplementationModelFactory((IImplementationModelFactory) obj);
			} catch (CoreException ex) {
				ex.printStackTrace();
				continue;
			}
		}
	}
}
