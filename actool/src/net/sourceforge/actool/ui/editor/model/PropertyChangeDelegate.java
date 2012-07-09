package net.sourceforge.actool.ui.editor.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.widgets.Display;






public  class PropertyChangeDelegate implements PropertyChangeListener {
	class PropertyChangeRunner implements Runnable {
	    private PropertyChangeListener listener;
	    private PropertyChangeEvent event;
	    
	    PropertyChangeRunner(PropertyChangeListener listener, PropertyChangeEvent event) {
	        this.listener = listener;
	        this.event = event;
	    }
	    
	    public void run() { 
	    	if(listener!=null)
	        listener.propertyChange(event);
	    }
	}

	private PropertyChangeListener listener;
	
	public PropertyChangeDelegate(PropertyChangeListener listener) {
		this.listener = listener;
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		Display.getDefault().asyncExec(new PropertyChangeRunner(listener, event));
	}
}