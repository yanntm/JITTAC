package net.sourceforge.actool.jdt;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.actool.jdt.model.JavaModelFactory;
import net.sourceforge.actool.jdt.util.ProjectTracker;
import net.sourceforge.actool.model.ModelManager;
import net.sourceforge.actool.model.da.ArchitectureModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
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

		ModelManager manager = ModelManager.getDefault();		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		
		// The code below doesn't seam to be requited at the moment.
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

}
