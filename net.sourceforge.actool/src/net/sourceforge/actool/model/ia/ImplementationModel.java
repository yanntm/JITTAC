package net.sourceforge.actool.model.ia;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;


public abstract class ImplementationModel {
	// A set to hold all the model change listeners.
	private Set<ImplementationChangeListener> listeners = new HashSet<ImplementationChangeListener>();
	private IProject project;
	
	public ImplementationModel(IProject project) {
	    this.project = checkNotNull(project);
	}

	public IProject getProject() {
        return project;
    }

    protected void fireModelChange(ImplementationChangeDelta delta) {
	
		// Iterate over all listener and invoke the event change handler.
		Iterator<ImplementationChangeListener> iter = listeners.iterator();
		while (iter.hasNext())
			iter.next().implementationChangeEvent(delta);
	}
	
	public boolean addImplementationChangeListener(ImplementationChangeListener listener) {
		return listeners.add(listener);
	}
	
	public boolean removeImplementationChangeListener(ImplementationChangeListener listener) {
		return listeners.remove(listener);
	}
	
	public abstract void _updateListener(ImplementationChangeListener listener);
	
	public abstract void _restore(IPath path);
	public abstract void _store(IPath path);
}
