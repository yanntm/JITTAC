package net.sourceforge.actool.logging;

import net.sourceforge.actool.model.da.ArchitectureModelListener;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;
import net.sourceforge.actool.model.ia.IXReference;

import org.eclipse.core.resources.IResource;

public class ModelEventListener extends ArchitectureModelListener {
	private final EventLogger logger;
	
	public ModelEventListener(EventLogger logger) {
		this.logger = logger;
	}
	
	protected EventLogger getLogger() {
		return logger;
	}
	
	
	@Override
	public void componentMappingAdded(Component component, IResource resource) {
		getLogger().logMappingAdded(resource, component);
	}

	@Override
	public void componentMappingRemoved(Component component, IResource resource) {
		getLogger().logMappingRemoved(resource, component);		
	}
	
	@Override
	public void componentSourceConnectionAdded(Component component,
			Connector connector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentSourceConnectionRemoved(Component component,
			Connector connector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentTargetConnectionAdded(Component component,
			Connector connector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentTargetConnectionRemoved(Component component,
			Connector connector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectorStateChanged(Connector connector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectorXReferenceAdded(Connector connector, IXReference xref) {
		getLogger().logXReference(EventLogger.XREFERENCE_ADDED, connector);
	}

	@Override
	public void connectorXReferenceRemoved(Connector connector, IXReference xref) {
		getLogger().logXReference(EventLogger.XREFERENCE_REMOVED, connector);
	}

	@Override
	public void modelComponentAdded(Component component) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modelComponentRemoved(Component component) {
		// TODO Auto-generated method stub

	}
}
