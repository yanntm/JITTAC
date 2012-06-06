package net.sourceforge.actool.ui.editor.dnd;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.gef.Request;

public class MapElementeRequest extends Request {
    static public final String REQ_MAP = "map";
    
    private Set<IResource> resources =  new HashSet<IResource>();
    
    public MapElementeRequest() {
        super(REQ_MAP);
    }
    
    protected void setResources(IResource[] data) {
        resources.clear();
        for (int i = 0; i < data.length; i++)
            resources.add(data[i]);
    }
    
    public Set<IResource> getResources() {
        return new HashSet<IResource>(resources);
    }
}
