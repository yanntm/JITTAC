package net.sourceforge.actool.ui.editor.dnd;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Request;

public class MapElementeRequest extends Request {
    static public final String REQ_MAP = "map";
   
    private Point location; 
    private Set<IResource> resources =  new HashSet<IResource>();
    
    public MapElementeRequest() {
        super(REQ_MAP);
    }
    
    
    public Point getLocation() {
        return location;
    }


    protected void setLocation(Point location) {
        this.location = location;
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
