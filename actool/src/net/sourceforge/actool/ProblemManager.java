package net.sourceforge.actool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.actool.logging.EventLogger;
import net.sourceforge.actool.logging.ModelEventListener;
import net.sourceforge.actool.model.ModelManager;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;


public class ProblemManager extends ArchitectureModelListener {
	private final static QualifiedName MODELS_KEY = new QualifiedName(ACTool.PLUGIN_ID, "controllingModels");
	private final static Map<IResource, ProblemManager> instances = new ConcurrentHashMap<IResource, ProblemManager>(defaults.MAX_THREADS);
	
	private final ArchitectureModel model;
	private Set<IProject> projects = new HashSet<IProject>();
	
    //private Map<String, Long> unmapped		= new HashMap<String, Long>();
    private Map<String, Long> violations	= new HashMap<String, Long>();


    protected ProblemManager(ArchitectureModel model) {
    	this.model = model;
    }
    
	public static ProblemManager getInstance(ArchitectureModel model) {
    	synchronized (instances) {
    		ProblemManager instance = instances.get(model.getResource());
    		if (instance == null) {
    			instance = new ProblemManager(model);
    			instances.put(model.getResource(), instance);
    		}
    		
        	return instance;
    	}
    }
    
    protected ArchitectureModel getModel() {
    	return model;
    }
    
    protected ModelProperties getModelProperties() {
    	return new ModelProperties(getModel().getResource());
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
						getModel().accept(new ProblemCollector(this, project));
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
    
    protected boolean isEnabledForProject(IProject project) {
    	return projects.contains(project);
    }
    
    protected void attachToProject(IProject project) {
    	if (!projects.add(project))
    		return;
    	// This will add the errors originating from this project.
    	getModel().accept(new ProblemCollector(this, project));
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

    	
    	ModelManager manager = ModelManager.getDefault();
    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    	try {
   			String property = project.getPersistentProperty(MODELS_KEY);
   			if (property == null || property.trim().equals(""))
   				return;
   			for (String path: property.split(";")) {
   				IResource resource = root.findMember(Path.fromPortableString(path));
   				if (!(resource instanceof IFile)){
   					continue;
   				}

   				EventLogger.getInstance().logModelInitBegin(resource);
   				ArchitectureModel model = manager.getArchitectureModel((IFile) resource);
   				model.addModelListener(new ModelEventListener(EventLogger.getInstance()));
   				EventLogger.getInstance().logModelInitEnd(resource);
   				

   				// This will crate an instance of problem manager...
   				ProblemManager pm = getInstance(model);
   				pm.attachToProject(project);
   			}
   		
		} catch (CoreException e) {
			e.printStackTrace();
		}
    }
    
    private void addModelToProject(IProject project) {
    	if (!project.isOpen())
    		throw new IllegalArgumentException("Project is not open!");

    	String element = getModel().getResource().getFullPath().toPortableString() + ";";
    	try {
   			String property = project.getPersistentProperty(MODELS_KEY);
   			if (property != null)
   				property += element;
   			else
   				property = element;
   			
   			project.setPersistentProperty(MODELS_KEY, property);
   			attachToProject(project);
		} catch (CoreException e) {
			e.printStackTrace();
		}
    }
    
    private void removeModelFromProject(IProject project) {
    	if (!project.isOpen())
    		throw new IllegalArgumentException("Project is not open!");

    	String path = getModel().getResource().getFullPath().toPortableString();
    	StringBuilder builder = new StringBuilder();
    	try {
   			String property = project.getPersistentProperty(MODELS_KEY);
   			if (property == null || property.trim().equals(""))
   				return;

   			for (String element: property.split(";")) {
   				if (!element.equals(path))
   					builder.append(element + ";");
   				
   			}
   			
   			project.setPersistentProperty(MODELS_KEY, builder.toString());
   			detachFromProject(project);
		} catch (CoreException e) {
			e.printStackTrace();
		}
    }
    
    public IProject[] getControlledProjects() {
    	String paths = getModelProperties().getControlledProjects();
    	if (paths == null || paths.trim().equals(""))
    		return new IProject[0];

    	ArrayList<IProject> projects = new ArrayList<IProject>();
    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    	for (String path: paths.split(";")) {
    		IResource project = root.findMember(Path.fromPortableString(path));
    		if (project != null && project instanceof IProject)
    			projects.add((IProject) project);
    	}

    	return projects.toArray(new IProject[projects.size()]);
    }
    
    public void setControlledProjects(IProject[] projects) {
    	Set<IProject> newSet = new HashSet<IProject>(Arrays.asList(projects));
    	Set<IProject> oldSet = new HashSet<IProject>(Arrays.asList(getControlledProjects()));
    	
    	// Add model to newly added projects.
    	for (IProject project: newSet) {
    		if (project.isOpen() && !oldSet.contains(project))
    			addModelToProject(project);
    	}
    	// Remove model from the removed projects.
    	for (IProject project: oldSet) {
    		if (project.isOpen() && !newSet.contains(project))
    			removeModelFromProject(project);
    	}
    	
    	// Store the projects property.
    	StringBuilder builder = new StringBuilder();
    	for (IProject project: projects) 
    		builder.append(project.getFullPath().toPortableString() + ";");
    	getModelProperties().setControlledProjects(builder.toString());
    }
    
    @Override
    public void connectorXReferenceAdded(final Connector connector, final IXReference xref) {
    	Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (connector.isEnvisaged() || ignoreViolations()
						|| !isEnabledForProject(xref.getSource().getResource().getProject()))
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
				
			}
		});thread.start();
		
	}

	@Override
	public void connectorXReferenceRemoved(Connector connector, final IXReference xref) {		
        Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
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
				
			}
		});thread.start();
		
	}
	
	@Override
	public void connectorStateChanged(Connector connector) {
		Iterator<IXReference> iter = connector.getXReferences().iterator();
		if (connector.isEnvisaged()) {
			while (iter.hasNext())
				connectorXReferenceRemoved(connector, iter.next());
			//this method runs on a background thread
		} else {
			while (iter.hasNext())
				connectorXReferenceAdded(connector, iter.next());
			//this method runs on a background thread
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
