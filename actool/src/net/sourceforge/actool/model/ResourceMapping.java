package net.sourceforge.actool.model;

import net.sourceforge.actool.model.da.Component;

import org.eclipse.core.resources.IResource;


public class ResourceMapping {
    private IResource resource;
    private Component target;
    
    public ResourceMapping(IResource resource, Component target) {
        this.resource = resource;
        this.target = target;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof ResourceMapping) {
            ResourceMapping other = (ResourceMapping) obj;
            return resource.equals(other.resource) && target.equals(other.target);
        } else
            return false;
    }
    
    public IResource getResource() {
        return this.resource;
    }
    
    public Component getComponent() {
        return this.target;
    }
    
    public boolean matches(IResource other) {
        
        // In order to match, this mapping's resource. 
        // must be an ancestor of the other object.
        do {
            if (resource.equals(other))
                return true;
            other = other.getParent();
        } while (other != null);
        
        return false;
    }
        
    public void setTarget(Component component) {
        this.target = component;
    }
}

