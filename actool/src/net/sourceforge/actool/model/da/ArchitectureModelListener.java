package net.sourceforge.actool.model.da;

import java.beans.PropertyChangeEvent;

import net.sourceforge.actool.model.ResourceMapping;
import net.sourceforge.actool.model.ia.IXReference;

public abstract class ArchitectureModelListener implements IArchitectureModelListener {

	private void handleModelChange(ArchitectureModel model, PropertyChangeEvent event) {
		if (event.getPropertyName() == ArchitectureModel.COMPONENTS) {
			if (event.getNewValue() != null && event.getOldValue() == null) {
				Component component = (Component) event.getNewValue();
				
				component.addPropertyChangeListener(this);
				modelComponentAdded(component);
			} else if (event.getNewValue() == null && event.getOldValue() != null) {
				Component component = (Component) event.getOldValue();
				
				component.removePropertyChangeListener(this);
				modelComponentRemoved(component);
			}
		}
	}
	
	
	private void handleComponentChange(Component component, PropertyChangeEvent event) {
		if (event.getPropertyName() == Component.SOURCE_CONNECTIONS
		    || event.getPropertyName() == Component.TARGET_CONNECTIONS) {
			
			if (event.getNewValue() != null && event.getOldValue() == null) {
				Connector connector = (Connector) event.getNewValue();
				
				if (event.getPropertyName() == Component.SOURCE_CONNECTIONS) {
					connector.addPropertyChangeListener(this);
					componentSourceConnectionAdded(component, connector);
				} else
					componentTargetConnectionAdded(component, connector);
			} else if (event.getNewValue() == null && event.getOldValue() != null) {
				Connector connector = (Connector) event.getOldValue();
				
				if (event.getPropertyName() == Component.SOURCE_CONNECTIONS) {
					connector.removePropertyChangeListener(this);
					componentSourceConnectionRemoved(component, connector);
				} else
					componentTargetConnectionRemoved(component, connector);
			}
		} else if (event.getPropertyName() == Component.MAPPINGS) {
			if (event.getNewValue() != null && event.getOldValue() == null)
				componentMappingRemoved(component, ((ResourceMapping) event.getNewValue()).getResource());
			else if (event.getNewValue() == null && event.getOldValue() != null)
				componentMappingRemoved(component, ((ResourceMapping) event.getOldValue()).getResource());
		}
	}

	private void handleConnectorChange(Connector connector, PropertyChangeEvent event) {
		if (event.getPropertyName() == Connector.XREFERENCES) {
			if (event.getNewValue() != null && event.getOldValue() == null) {
				IXReference xref = (IXReference) event.getNewValue();

				connectorXReferenceAdded(connector, xref);
			} else if (event.getNewValue() == null && event.getOldValue() != null) {
				IXReference xref = (IXReference) event.getOldValue();
				
				connectorXReferenceRemoved(connector, xref);
			}
		} else if (event.getPropertyName() == Connector.ENVISAGED) {
			connectorStateChanged(connector);
		}
	}
	
	@Override
	public final void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() instanceof Connector) {
			handleConnectorChange((Connector) event.getSource(), event);
		} else if (event.getSource() instanceof Component) {
			handleComponentChange((Component) event.getSource(), event);
		} else if (event.getSource() instanceof ArchitectureModel) {
			handleModelChange((ArchitectureModel) event.getSource(), event);
		}
	}
	
	public void modelComponentAdded(Component component) {}
	public void modelComponentRemoved(Component component) {}
	
	public void componentSourceConnectionAdded(Component component, Connector connector) {}
	public void componentSourceConnectionRemoved(Component component, Connector connector) {}
	
	public void componentTargetConnectionAdded(Component component, Connector connector) {}
	public void componentTargetConnectionRemoved(Component component, Connector connector) {}

	
	public void connectorStateChanged(Connector connector) {}
	
	public void connectorXReferenceAdded(Connector connector, IXReference xref) {}
	public void connectorXReferenceRemoved(Connector connector, IXReference xref) {}
}
