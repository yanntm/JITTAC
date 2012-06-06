package net.sourceforge.actool.model.da;

import java.beans.PropertyChangeListener;

import net.sourceforge.actool.model.ia.IXReference;

import org.eclipse.core.resources.IResource;


public interface IArchitectureModelListener extends PropertyChangeListener {
	
	public void modelComponentAdded(Component component);
	public void modelComponentRemoved(Component component);
	
	public void componentMappingAdded(Component component, IResource resource);
	public void componentMappingRemoved(Component component, IResource resource);
	
	public void componentSourceConnectionAdded(Component component, Connector connector);
	public void componentSourceConnectionRemoved(Component component, Connector connector);
	
	public void componentTargetConnectionAdded(Component component, Connector connector);
	public void componentTargetConnectionRemoved(Component component, Connector connector);

	
	public void connectorStateChanged(Connector connector);
	
	public void connectorXReferenceAdded(Connector connector, IXReference xref);
	public void connectorXReferenceRemoved(Connector connector, IXReference xref);
}
