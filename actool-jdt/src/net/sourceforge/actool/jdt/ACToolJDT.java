package net.sourceforge.actool.jdt;

import static org.eclipse.core.runtime.IStatus.ERROR;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.actool.jdt.util.ProjectTracker;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * @since 0.1
 */
public class ACToolJDT extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.sourceforge.actool.jdt";

	// The shared instance
	private static ACToolJDT plugin;
	
	private Map<IProject, ProjectTracker> trackers = new HashMap<IProject, ProjectTracker>();
	
	/**
	 * The constructor
	 */
	public ACToolJDT() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// The code below doesn't seam to be requited at the moment.
//		ModelManager manager = ModelManager.getDefault();		
//		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//		IProject[] projects = root.getProjects();
		
		
//		for (int i = 0; i < projects.length; ++i) {
//			// Only process java projectList.
//			if (!projects[i].hasNature(JavaCore.NATURE_ID))
//				continue;
//			IJavaProject project = JavaCore.create(projects[i]);
//			
//			// Attach a controlling model to the project if specified.
//			/*ArchitectureModel model = manager.getArchitectureModelForProject(project.getProject());
//			if (model != null) {
//				ProjectTracker tracker = new ProjectTracker(project.getProject(), model);
//				trackers.put(project.getProject(), tracker);
//				
//				tracker.scanForUnmappedResources();
//			}*/
//		}
	}
	
	public ProjectTracker getTracker(IProject project) {
		return trackers.get(project);
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
	public static ACToolJDT getDefault() {
		return plugin;
	}
	
	public static void errorStatus(String message, Throwable t) {
		StatusManager.getManager().handle(new Status(ERROR, PLUGIN_ID, message, t));
	}

}
