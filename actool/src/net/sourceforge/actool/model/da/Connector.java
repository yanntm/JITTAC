package net.sourceforge.actool.model.da;

import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.actool.model.ia.IXReference;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;



public class Connector extends ArchitectureElement {

    public static final int ABSENT          = 0;
    public static final int CONVERGENT      = 1;
    public static final int DIVERGENT       = 2;
    
    public static final String ENVISAGED    = "ENVISAGED";
    public static final String STATE        = "STATE";
    public static final String SOURCE       = "SOURCE";
    public static final String TARGET       = "TARGET";
    public static final String COMMENT      = "COMMENT";
    public static final String XREFERENCES  = "XREFERENCES";
    
    public static final IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[]  {
        new PropertyDescriptor(STATE, "Type"),
        new PropertyDescriptor(SOURCE, "Source"),
        new PropertyDescriptor(TARGET, "Target"),
        new TextPropertyDescriptor(COMMENT, "Comment")
    };


    private Component source, target;
    private boolean envisaged = true, connected = false;
    private String comment = "";

    private LinkedList<IXReference> xrefs = new LinkedList<IXReference>();
    
    protected Connector(Component source, Component target) {
    	if (source == null || target == null || target.equals(source))
        	throw new IllegalArgumentException();
        construct(source, target, true);
    }

    protected Connector(Component source, Component target, boolean connect) {
    	if (source == null || target == null || target.equals(source))
        	throw new IllegalArgumentException();
        construct(source, target, connect);
    }

    private void construct(Component source, Component target, boolean connect) {   	
        this.source = source;
        this.target = target;
        if (connect)
            connect();
    }
    
    public static Connector connect(Component source, Component target, boolean envisaged) {      
        Connector conn =  new Connector(source, target);
        conn.setEnvisaged(envisaged);
        return conn;
    }
 
    public boolean isEnvisaged() {
        return envisaged;
    }
    
    public void setEnvisaged(boolean envisaged) {
        if (isEnvisaged() == envisaged)
            return;

        this.envisaged = envisaged;
        firePropertyChange(ENVISAGED, !envisaged, envisaged);
    }

    public boolean isConnected() {
        return connected;
    }
    
    public void connect() {
        assert connected = false;
        getSource().addSourceConnector(this);
        getTarget().addTargetConnector(this);
        connected = true;
    }

    public void disconnect() {
        assert connected == true;
        getSource().removeSourceConnector(this);
        getTarget().removeTargetConnector(this);
        connected = false;
    }
    
    public Component getSource() {
        return source;
    }

    public Component getTarget() {
        return target;
    }
    
    public int getState() {
        if (isEnvisaged()) {
            if (xrefs.isEmpty())
                return ABSENT;
            else
                return CONVERGENT;
        } else
            return DIVERGENT;
    }
    
    public boolean inState(int state) {
        if (state < ABSENT || state > DIVERGENT)
            throw new IllegalArgumentException();
        return getState() == state;
    }
    
    public ArchitectureModel getModel() {
        if (!source.getModel().equals(target.getModel()))
            throw new IllegalStateException("Connector can not connect components from different models!");
        return source.getModel();
    }
    
    public boolean hasXReferences() {
        return !xrefs.isEmpty();
    }

    public int getNumXReferences() {
        return xrefs.size();
    }
  
    public void addXReference(IXReference xref) {
        xrefs.add(xref);       
        firePropertyChange(XREFERENCES,  null, xref);
    }

    public void addXReferences(Collection<IXReference> xrefs) {
    	this.xrefs.addAll(xrefs);
    	for (IXReference xref: xrefs)
    		firePropertyChange(XREFERENCES, null, xref);
    }
    
    public void removeXReference(IXReference xref) {
        xrefs.remove(xref);
        firePropertyChange(XREFERENCES, xref, null);
    }
    
    public Collection<IXReference> getXReferences() {
        return new LinkedList<IXReference>(xrefs);
    }
   
    public void accept(IArchitectureModelVisitor visitor) {
    	visitor.visit(this);
    }
    
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return propertyDescriptors;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (id.equals(STATE)) {
            switch (getState()) {
            case ABSENT:
                return "Absence";
            case CONVERGENT:
                return "Convergence";
            case DIVERGENT:
                return "Divergence";
            }
        } else if (id.equals(SOURCE)) {
            return source.getName();
        } else if (id.equals(TARGET)) {
            return target.getName();
        } else if (id.equals(COMMENT)) {
            return comment;
        }
        
        return null;
    }

    @Override
    public boolean isPropertySet(Object id) {
        if (id.equals(STATE) || id.equals(SOURCE) || id.equals(TARGET)) {
            return true;
        } else if (id.equals(COMMENT)) {
            return comment != null && !comment.isEmpty();
        }
        return false;
    }

    @Override
    public void resetPropertyValue(Object id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (id.equals(COMMENT))
            comment = (String) value;
    }
    
    public String toString() {
        return source.toString() + "->" + target.toString();
    }
}
