package net.sourceforge.actool.model.da;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.ui.views.properties.IPropertySource;

public abstract class ArchitectureElement  implements IPropertySource  {
	
	private PropertyChangeSupport delegate = new PropertyChangeSupport(this);
	
	/**
	 * Returns the reference to the model containing this element.
	 * 
	 * @return reference to the top level element (root).
	 */
	public abstract ArchitectureModel getModel();
	
	/**
	 * Accept a visitor on this element.
	 * 
	 * @param visitor to accept.
	 */
	public abstract void accept(IArchitectureModelVisitor visitor);
	
	/**
	 * Notify all the listener of the property value change.
	 */
	protected void firePropertyChange(String property, Object oldValue, Object newValue) {
	    // Do nothing if the value has not really changed.
	    if (oldValue != null && oldValue.equals(newValue))
	        return;
	    
	    synchronized (delegate) {
    		if (delegate.hasListeners(property))
    			delegate.firePropertyChange(property, oldValue, newValue);
	    }
	}

	@Deprecated
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		assert listener != null;
		synchronized (delegate) {
		    delegate.addPropertyChangeListener(listener);
		}
	}
	
	@Deprecated
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		assert listener != null;
		synchronized (delegate) {
		    delegate.removePropertyChangeListener(listener);
		}
	}
	

    public Object getEditableValue() {
        return this;
    }
}
