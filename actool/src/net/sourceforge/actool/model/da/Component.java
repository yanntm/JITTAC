package net.sourceforge.actool.model.da;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sourceforge.actool.model.ResourceMapping;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;



public class Component extends ArchitectureElement
                       implements IPropertySource {

    // ID string for Component's properties.
	public static final String ID                  = "id";
	public static final String NAME 		       = "name";
	public static final String SOURCE_CONNECTIONS  = "sourceConnections";
	public static final String TARGET_CONNECTIONS  = "targetConnections";
	public static final String MAPPINGS            = "mappings";

	
	private ArchitectureModel model;
	private String id, name;
	private ConcurrentLinkedQueue<Connector> sourceConnections      = new ConcurrentLinkedQueue<Connector>();
	private ConcurrentLinkedQueue<Connector> targetConnections	   = new ConcurrentLinkedQueue<Connector>();
	

    public static final IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[]  {
           new TextPropertyDescriptor(ID, "ID"),
           new TextPropertyDescriptor(NAME, "Name")
   };
	
	public Component(String id, String name) {
	    construct(null, id, name);
	}
	
	protected Component(ArchitectureModel model, String id, String name) {
	    construct(model, id, name);
	}

	private void construct(ArchitectureModel model, String id, String name) {
	    if (id == null || id.isEmpty())
	        throw new IllegalArgumentException();

	    this.id = id;
	    this.name = name;
	    if (model != null)
	        setModel(model);
	}

	public boolean hasModel() {
	    return this.model != null;
	}
	
	protected void setModel(ArchitectureModel model) {
	    if (this.model != null && model != null)
	        throw new IllegalStateException("Component already associated with a model!");
	    this.model = model;	    
	}
	
	public ArchitectureModel getModel() {
		return model;
	}
	
	public String getID() {
		assert id != null && !id.isEmpty();
		return id;
	}
	
	private void setID(String value) {
	    if (value == null || value.isEmpty() || id == value)
	        throw new IllegalArgumentException();
	    // Components ID must be unique within one model.
	    if (hasModel() && getModel().hasComponent(value))
	        throw new IllegalArgumentException("Components ID is not unique!");
        
        String oldValue = id;
	    id = value;
	    firePropertyChange(ID, oldValue, id);
	    if (isNameSet())
	        firePropertyChange(NAME, oldValue,id);
	}
	
	public String getName() {
		if (isNameSet())
			return getID();
		return name;
	}
	
	public void setName(String name) {
		String oldValue = getName();
		if (name != null && (name.isEmpty() || name.equals(id)))
			name = null;
		
		this.name = name;
		firePropertyChange(NAME, oldValue, getName());
	}
	
	protected boolean isNameSet() {
	    return name == null || name.isEmpty();	}
	
	protected void addSourceConnector(Connector conn) {
		sourceConnections.add(conn);
		firePropertyChange(SOURCE_CONNECTIONS, null, conn);
	}
	
	protected void removeSourceConnector(Connector conn) {
		sourceConnections.remove(conn);
		firePropertyChange(SOURCE_CONNECTIONS, conn, null);
	}
	
	public List<Connector> getSourceConnectors() {		
		return new ArrayList<Connector>(sourceConnections);
	}

    public int getNumberSourceConnectors() {
        return sourceConnections.size();
    }
	
	protected void addTargetConnector(Connector conn) {
		targetConnections.add(conn);
		firePropertyChange(TARGET_CONNECTIONS, null, conn);
	}
	
	protected void removeTargetConnector(Connector conn) {
		targetConnections.remove(conn);
		firePropertyChange(TARGET_CONNECTIONS, conn, null);
	}
	
	public int getNumberTargetConnectors() {
	    return targetConnections.size();
	}
	
	public List<Connector> getTargetConnectors() {
		return new ArrayList<Connector>(targetConnections);
	}

	public Connector getConnectorForSource(Component source) {
        return getConnectorForSource(source.getID());
    }
   
    public Connector getConnectorForSource(String sourceid) {
        Iterator<Connector> iter = targetConnections.iterator();
        while (iter.hasNext()) {
            Connector connector = iter.next();
            if (connector.getSource().getID() == sourceid)
                return connector;
        }

        return null;
    } 

    public Connector getConnectorForTarget(Component target) {
        return getConnectorForTarget(target.getID());
    }

    public Connector getConnectorForTarget(String targetId) {
        Iterator<Connector> iter = sourceConnections.iterator();
        while (iter.hasNext()) {
            Connector connector = iter.next();
            if (connector.getTarget().getID() == targetId)
                return connector;
        }

        return null;
    }
     
    /**
     * Disconnect all component's connections (source and target).
     */
    public void disconnectAllConnectors() {
        Iterator<Connector> iter;
        
        iter = (new ArrayList<Connector>(sourceConnections)).iterator();
        while (iter.hasNext())
            iter.next().disconnect();
        
        iter = (new ArrayList<Connector>(targetConnections)).iterator();
        while (iter.hasNext())
            iter.next().disconnect();
    }
  
    
    /**
     * Map given element to this component. If the mapping already exists
     * nothing is done and the old mapping is returned.
     * 
     * @param resource to map to this component.
     * @return return the mapping object
     */
    public ResourceMapping addMapping(IResource resource) {
        // Check if previous mapping exists for this component.
        ResourceMapping mapping = new ResourceMapping(resource, this);
        ResourceMapping previous = getModel().getResourceMap().getMapping(resource);
        if (previous != null) {
            if (!mapping.equals(previous)){
                // Remove this element's mapping from the other component.
                previous.getComponent().removeMapping(previous.getResource());
            } else
                return previous;
        } 
        
        getModel().getResourceMap().addMapping(resource, mapping);
        firePropertyChange(MAPPINGS, null, mapping);
        
        return mapping;
    }
    
    public ResourceMapping[] getMappings() {
    	if (hasModel())
    		return getModel().getResourceMap().getMappingsForComponent(this);
    	else
    		return new ResourceMapping[0];
    }
 
    public ResourceMapping[] removeAllMappings() {
    	ResourceMapping[] mappings = getMappings();
        for (int i = 0; i < mappings.length; ++i) 
            removeMapping(mappings[i].getResource());
        
        return mappings;
    }

    
    public ResourceMapping removeMapping(IResource resource) {
        ResourceMapping mapping = null;

        mapping = getModel().getResourceMap().getMapping(resource);
        if (mapping == null || !mapping.getComponent().equals(this))
            throw new IllegalArgumentException("Element is not mapped to this component!");

        getModel().getResourceMap().addMapping(resource, null);
        firePropertyChange(MAPPINGS, mapping, null);
        
        return mapping;
    }
    
    
    public void accept(IArchitectureModelVisitor visitor) {
    	if (!visitor.visit(this))
    		return;
    	
    	// Visit source connectors only to avoid double traversals.
    	for (Connector connector: getSourceConnectors())
    		connector.accept(visitor);
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        return propertyDescriptors;
    }

    public Object getPropertyValue(Object id) {
        if (id.equals(ID))
            return getID();
        else if (id.equals(NAME))
            return getName();
        
        return null;
    }

    public boolean isPropertySet(Object id) {
        if (id.equals(ID))
            return true; // We can not have component with no ID!
        else if (id.equals(NAME))
            return isNameSet();
        
        return false;
    }

    public void resetPropertyValue(Object id) {
    }

    public void setPropertyValue(Object id, Object value) {
        if (!(value instanceof String))
            throw new IllegalArgumentException();
        
        if (id.equals(ID))
            setID((String) value);
        else if (id.equals(NAME))
            setName((String) value);
    }
    
    public boolean equals(Object obj) {
    	if (obj instanceof Component) {
    		Component other = (Component) obj;
    		return id.equals(other.id) && model.equals(other.model);
    	} else
    		return false;
    }
    
    public String toString() {
        return getID();
    }
}
