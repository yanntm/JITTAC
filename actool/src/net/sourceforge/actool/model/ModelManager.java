package net.sourceforge.actool.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.sourceforge.actool.ACTool;
import net.sourceforge.actool.ProblemManager;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.ArchitectureModelReader;
import net.sourceforge.actool.model.ia.IImplementationModelFactory;
import net.sourceforge.actool.model.ia.IXReferenceStringFactory;
import net.sourceforge.actool.model.ia.ImplementationModel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.InstanceofExpression;

public class ModelManager {
	
	/// The one and only instance of ModelManager.
	private static ModelManager instance = null;
	
	private Collection<IImplementationModelFactory> factories = new Vector<IImplementationModelFactory>();
	private Map<String, ImplementationModel> implementations = new HashMap<String, ImplementationModel>();
	private Map<String, ArchitectureModel> models = new HashMap<String, ArchitectureModel>();
	
	
	private ModelManager() {
	}
	
	
	/**
	 * Return the ModelManager instance.
	 * 
	 * @return the one and only instance of model manager.
	 */
	public static synchronized ModelManager getDefault() {
		if (instance == null)
			instance = new ModelManager();
		
		return instance;
	}
	
	public synchronized void addImplementationModelFactory(IImplementationModelFactory factory) {
		factories.add(factory);
	}
	
    static protected ArchitectureModel _loadModel(IFile file) {
        if (file == null || !file.exists())
            throw new IllegalArgumentException();
        ArchitectureModel model = new ArchitectureModel(file);
            
        try {
            model = ArchitectureModelReader.read(file);
        } catch (CoreException ex) {
            ex.printStackTrace(); 
            
            // TODO: Do something more reasonable!
            return null;
        }
        
        return model;
    }

    
    public synchronized void _storeImplementationModel(IProject project) {
        ImplementationModel model = getImplementationModel(project);
        
        IPath path = project.getWorkingLocation(ACTool.PLUGIN_ID);
        path = path.append("/ia_model.sav");
        model._store(path);
        
    }
    
   private ImplementationModel createImplementationModel(IProject project) {
	   // HACK: Make sure that model factories before use.
	   if (factories.size() == 0)
		   ACTool.getDefault().initialiseImplementationModelFactories();
	   
	   Iterator<IImplementationModelFactory> iter = factories.iterator();
	   ImplementationModel model = null;
	   
	   while (model == null && iter.hasNext())
		   model = iter.next().createImplementationModel(project);
	   return model;
   }
	
   /**
     * Get the assigned Designed Architecture model for the given file.
     * 
     * @return
     */
	public synchronized ArchitectureModel getArchitectureModel(IFile file) {
		if (!file.exists())
			return null;

		String key = file.getProjectRelativePath().toPortableString();
		LinkedList<ImplementationModel> imodels = new LinkedList<ImplementationModel>();
		try {
			
			IProject projects[] = file.getProject().getReferencedProjects();
			for (IProject project : projects) {
				ImplementationModel im = getImplementationModel(project);
				if (im != null) {
//					model.attachToImplementation(im);
					imodels.add(im);
					if(im instanceof IXReferenceStringFactory && ArchitectureModel.xrefStringFactory== null)
					ArchitectureModel.xrefStringFactory=(IXReferenceStringFactory) im;
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		ArchitectureModel model = models.get(key);
		if (model == null) {
			ImplementationModel ia = getImplementationModel(file.getProject());
			
			
			model = _loadModel(file);
			if (model == null)
				throw new IllegalArgumentException(
						"File does not represent a valid Architecture Model");
			models.put(key, model);
			ResourcesPlugin.getWorkspace().addResourceChangeListener(model,
					IResourceChangeEvent.POST_CHANGE);

			// Attach project containing the file if implementation is
			// available.
			
			if (ia != null)
				model.attachToImplementation(ia);

			// Attach all implementation models this project depends on.
			for(ImplementationModel im :imodels) model.attachToImplementation(im);

			model.addModelListener(ProblemManager.getInstance(model));
			models.put(key, model);
		}

		return model;
	}

    public synchronized ImplementationModel getImplementationModel(IProject project) {
    	// Try the map first, this project might have been created already.
        ImplementationModel im = implementations.get(project.toString());
        
        // If not use factory to create an implementation for given project;
        if (im == null) {
        	im = createImplementationModel(project);
        	if (im == null)
        		return null;
            implementations.put(project.toString(), im);
            
            IPath path = project.getProject().getWorkingLocation(ACTool.PLUGIN_ID);
            path = path.append("/ia_model.sav");
            im._restore(path);
            
            /*
        	// HACK: Retrieve a model from first project that depends on this implementation.
        	//		 This will make sure that there is a model attached to this implementation
        	//       and create the project in turn.
        	IProject[] projectList = project.getProject().getReferencingProjects();
        	for (int i = 0; i < projectList.length; ++i) {
        		IResource model = projectList[i].findMember("model.xam");
        		if (model != null && model instanceof IFile) {
        			getArchitectureModel(((IFile) model));
        			break;
        		}
        	}
        	*/
        }
    
        return im;
    }
    
    public ImplementationModel ininialiseImplementationModel(IProject project) {
    	ImplementationModel im = getImplementationModel(project);
    	
    	// Get Projects referencing this project.
		Set<IProject> dependants = new HashSet<IProject>(Arrays.asList(project.getReferencingProjects()));
		dependants.add(project);
    	
    	for (ArchitectureModel am: models.values()) {
    		if (dependants.contains(am.getResource().getProject())){
    			am.attachToImplementation(im);
    			
    		}
    	}
    	
    	return im;
    }
}
