package net.sourceforge.actool.model.da;

import net.sourceforge.actool.ACTool;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class ModelProperties {
	public static final QualifiedName UNMAPPED_SEVERITY	= new QualifiedName(ACTool.PLUGIN_ID, "unmappedSeverity");
	public static final QualifiedName VIOLATION_SEVERITY = new QualifiedName(ACTool.PLUGIN_ID, "violationSeverity");
	public static final QualifiedName CONTROLLED_PROJECTS = new QualifiedName(ACTool.PLUGIN_ID, "controlledProjects");

	public static final String ERROR 					= "error";
	public static final String WARNING 					= "warning";
	public static final String INFO 					= "info";
	public static final String IGNORE 					= "ignore";

	private final IResource resource;
	
	
	public ModelProperties(IResource resource) {
		this.resource = resource;
	}
	
	public IResource getResource() {
		return resource;
	}
	
    protected static int stringToSeverity(String severity) {
    	if (ERROR.equals(severity))
    		return IMarker.SEVERITY_ERROR;
    	else if (WARNING.equals(severity))
    		return IMarker.SEVERITY_WARNING;
    	else if (INFO.equals(severity))
    		return IMarker.SEVERITY_INFO;
    	else
    		return -1;
   } 

    protected static String severityToString(int severity) {
    	switch (severity) {
    	case IMarker.SEVERITY_ERROR:
			return ERROR;
    	case IMarker.SEVERITY_WARNING:
			return WARNING;
    	case IMarker.SEVERITY_INFO:
			return INFO;
    	default:
    		return IGNORE;
    	}
    } 
	
	protected String getProperty(QualifiedName key) {
		try {
			String result =getResource().getPersistentProperty(key);
			if(result==null){
				result= new DefaultScope().getNode(ACTool.PLUGIN_ID).get(key.toString(), null);
				setProperty(key, result);
			}
			return result;
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected void setProperty(QualifiedName key, String value) {
		try {
			getResource().setPersistentProperty(key, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
//		IEclipsePreferences preferences = new DefaultScope().getNode(ACTool.PLUGIN_ID);
//		preferences.put(key.toString(), value);
	}
	
	public  int getUnmappedSeverity() {
		return stringToSeverity(getProperty(UNMAPPED_SEVERITY));
	}
	
	public  void setUnmappedSeverity(int severity) {
		setProperty(UNMAPPED_SEVERITY, severityToString(severity));
	}

	public  int getViolationSeverity() {
		return stringToSeverity(getProperty(VIOLATION_SEVERITY));
	}
	
	public  void setViolationSeverity(int severity) {
		setProperty(VIOLATION_SEVERITY, severityToString(severity));
	}
	
	public  String getControlledProjects() {
		return getProperty(CONTROLLED_PROJECTS);
	}
	
	public  void setControlledProjects(String projects) {
		setProperty(CONTROLLED_PROJECTS, projects);
	}
}
