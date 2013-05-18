package net.sourceforge.actool.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.actool.model.da.Component;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class ResourceMap {
    private QualifiedName key;
    private Map<IResource, ResourceMapping> mappings = new HashMap<IResource, ResourceMapping>();
    
    public ResourceMap(QualifiedName key) {
        this.key = key;
    }
        
    
    public void addMapping(IResource resource, ResourceMapping mapping) {
        try {
            mappings.put(resource, mapping);
            resource.setSessionProperty(this.key, mapping);
        } catch (CoreException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }
    
    public Iterator<ResourceMapping> iterator() {
        return mappings.values().iterator();
    }
    
    
    public ResourceMapping getAncestorMapping(ResourceMapping mapping) {
        IResource resource = mapping.getResource().getParent();
        while (resource != null) {
            try {
                ResourceMapping ancestor = (ResourceMapping) resource.getSessionProperty(key);
                if (ancestor != null)
                    return ancestor;
            } catch (CoreException e) {
                e.printStackTrace();
                return null;
            }
            
            resource = resource.getParent();
        }
        
        return null;
    }
    
    public ResourceMapping getMapping(IResource resource) {
        try {
            return (ResourceMapping) resource.getSessionProperty(this.key);
        } catch (CoreException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public ResourceMapping[] getMappingsForComponent(Component component) {
    	Collection<ResourceMapping> mappings = new Vector<ResourceMapping>();
    	MappingCollector collector = new MappingCollector(this, component, mappings);
    	
    	/// Go through all projectList in the workspace to collect resources.
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; ++i) {
			try {
			    if (projects[i].isOpen()) {
			        projects[i].accept(collector);
			    }
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
    	
    	return mappings.toArray(new ResourceMapping[mappings.size()]);
    }
    
    public ResourceMapping removeMapping(IResource resource) {
        ResourceMapping mapping = getMapping(resource);
        
        try {
            mappings.remove(resource);
            resource.getSessionProperties().remove(key);
        } catch (CoreException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
        
        return mapping;
    }
    
    public Component resolveMapping(IResource resource) {

        while (resource != null) {            
            try {
                ResourceMapping mapping = (ResourceMapping) resource.getSessionProperty(key);
                if (mapping != null)
                    return mapping.getComponent();
            } catch (CoreException e) {
                e.printStackTrace();
                return null;
            }
            
            resource = resource.getParent();
        }

        return null;
    }
}

class MappingCollector implements IResourceVisitor {

	private ResourceMap map;
	private Component component;
	private Collection<ResourceMapping> container;


	protected MappingCollector(ResourceMap map, Component component, Collection<ResourceMapping> container) {
		this.map = map;
		this.component = component;
		this.container = container;
	}


	public boolean visit(IResource resource) throws CoreException {
		ResourceMapping mapping = map.getMapping(resource);
		if (mapping == null)
			return true;
		if (component.equals(mapping.getComponent()))
			container.add(mapping);
		
		return true;
	}
	
}
