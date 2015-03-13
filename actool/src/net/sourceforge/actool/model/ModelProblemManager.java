package net.sourceforge.actool.model;

import static net.sourceforge.actool.model.ModelManager.MODELS_KEY;
import static org.eclipse.core.runtime.Path.fromPortableString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.actool.defaults;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.ArchitectureModelListener;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;
import net.sourceforge.actool.model.da.ModelProperties;
import net.sourceforge.actool.model.ia.IXReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;


public class ModelProblemManager extends ArchitectureModelListener {
	private final static Map<IResource, ModelProblemManager> instances = new HashMap<IResource, ModelProblemManager>();
	
	private final ArchitectureModel model;
	private Set<IProject> projects = new HashSet<IProject>();
	
    //private Map<String, Long> unmapped		= new HashMap<String, Long>();
    private Map<String, Long> violations	= new HashMap<String, Long>();


    protected ModelProblemManager(ArchitectureModel model) {
    	this.model = model;
    }
    
	public static ModelProblemManager problemManager(ArchitectureModel model) {
    	synchronized (instances) {
    		ModelProblemManager instance = instances.get(model.getResource());
    		if (instance == null) {
    			instance = new ModelProblemManager(model);
    			instances.put(model.getResource(), instance);
    		}
    		
        	return instance;
    	}
    }
    
    private ModelProperties getModelProperties() {
        return model.getModelProperties();
    }
    
    protected void validateSeverity(int severity) {
    	switch (severity) {
       	case IMarker.SEVERITY_ERROR:
       	case IMarker.SEVERITY_WARNING:
       	case IMarker.SEVERITY_INFO:
       	case -1:
       		break;
       	    	
    	default:
    		throw new IllegalArgumentException("Illegal Severity");
    	}
    }

    public int getUnmappedSeverity() {
    	return getModelProperties().getUnmappedSeverity();
    }
    
    public void setUnmappedSeverity(int severity) {
    	validateSeverity(severity);
    	getModelProperties().setUnmappedSeverity(severity);
    }
    
    public boolean ignoreUnmapped() {
    	return getUnmappedSeverity() == -1;
    }

    public int getViolationSeverity() {
    	return getModelProperties().getViolationSeverity();
    }
 
    public void setViolationSeverity(int severity) {
    	validateSeverity(severity);
    	int oldSeverity = getViolationSeverity();
    
    	if (severity != oldSeverity) {
    		getModelProperties().setViolationSeverity(severity);
    	
    		for (IProject project: projects) {
				try {
					if (oldSeverity == -1) {
						model.accept(new ModelProblemCollector(this, project));
					} else if (severity != -1) {
						for (IMarker marker: project.findMarkers(defaults.MARKER_TYPE,
																 true, IResource.DEPTH_INFINITE))
							marker.setAttribute(IMarker.SEVERITY, severity);
					} else
						project.deleteMarkers(defaults.MARKER_TYPE,
											  true, IResource.DEPTH_INFINITE);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}  				
    	}
    }
    
    protected boolean ignoreViolations() {
    	return getViolationSeverity() == -1;
    }
    
    protected boolean isAttachedToProject(IProject project) {
    	return projects.contains(project);
    }
    
    protected void attachToProject(IProject project) {
    	if (!projects.add(project))
    		return;
    	// This will add the errors originating from this project.
    	model.accept(new ModelProblemCollector(this, project));
    } 
  
    protected void detachFromProject(IProject project) {
    	if (!projects.remove(project))
    		return;
    	
    	try {
			project.deleteMarkers(defaults.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } 
    
    public static void initialiseProject(IProject project) {
    	if (!project.isOpen())
    		throw new IllegalArgumentException("Project is not open!");

    	
    	ModelManager manager = ModelManager.modelManager();
    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    	try {
   			String property = project.getPersistentProperty(MODELS_KEY);
   			if (property == null || property.trim().equals(""))
   				return;
   			for (String path: property.split(";")) {
   				IResource resource = root.findMember(fromPortableString(path));
   				if (!(resource instanceof IFile)){
   					continue;
   				}

//   				EventLogger.getInstance().logModelInitBegin(resource);
   				ArchitectureModel model = manager.getArchitectureModel((IFile) resource);
//   				model.addModelListener(new ModelEventListener(EventLogger.getInstance()));
//   				EventLogger.getInstance().logModelInitEnd(resource);
   				

   				// This will crate an instance of problem manager...
   				ModelProblemManager pm = problemManager(model);
   				pm.attachToProject(project);
   			}
   		
		} catch (CoreException e) {
			e.printStackTrace();
		}
    }
    
    @Override
    public void connectorXReferenceAdded(final Connector connector, final IXReference xref) {
//    	Thread thread = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
				if (connector.isEnvisaged() || ignoreViolations()
						|| !isAttachedToProject(xref.getSource().getResource().getProject()))
						return;
					
			        IResource resource = xref.getSource().getResource(); 

			        try {
			            IMarker marker = resource.createMarker(defaults.MARKER_TYPE);
			            marker.setAttribute(IMarker.SEVERITY, getViolationSeverity());
			            marker.setAttribute(IMarker.MESSAGE, "Element " + xref.getTarget().getName() 
			                                                  + " should not be accessed in this context "
			                                                  + "(" + connector.getSource().getName() + "->" 
			                                                  + connector.getTarget().getName() + ")");
			            
			            marker.setAttribute(IMarker.CHAR_START, xref.getOffset());
			            marker.setAttribute(IMarker.CHAR_END, xref.getOffset() + xref.getLength());
			            marker.setAttribute(IMarker.LINE_NUMBER, xref.getLine());
			            marker.setAttribute(defaults.MODEL, model.getResource().getFullPath());
			            marker.setAttribute(defaults.CONNECTOR_ID, connector.toString());
			            violations.put(ArchitectureModel.xrefStringFactory.toString(xref), marker.getId());
			        } catch (CoreException ex) {
			            // TODO: Do something better here;
			            ex.printStackTrace();
			        }
				
//			}
//		});thread.start();
		
	}

	@Override
	public void connectorXReferenceRemoved(Connector connector, final IXReference xref) {		
//        Thread thread = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
				IResource resource = xref.getSource().getResource();
		        
		        try {
		        	String strId = ArchitectureModel.xrefStringFactory.toString(xref);
					Long id = violations.get(strId);
		        	if (id == null)
		        		return;

		            IMarker marker = resource.findMarker(id.longValue());
		            if (marker != null)
		            	marker.delete();
		            violations.remove(strId);
		        } catch (CoreException e) {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		        }
				
//			}
//		});thread.start();
		
	}
	
	@Override
    public void connectorStateChanged(final Connector connector) {
        for (IXReference xref:  connector.getXReferences()) {;
            if (connector.isEnvisaged()) {
                connectorXReferenceRemoved(connector, xref);
            } else {
                connectorXReferenceAdded(connector, xref);
            }

        }
    }

	@Override
	public void componentMappingAdded(Component component, IResource resource) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMappingRemoved(Component component, IResource resource) {
		// TODO Auto-generated method stub
		
	}
	
   

}
