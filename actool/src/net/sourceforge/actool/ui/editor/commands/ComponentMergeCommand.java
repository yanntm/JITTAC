package net.sourceforge.actool.ui.editor.commands;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.actool.model.ResourceMapping;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;

import org.eclipse.gef.commands.Command;




public class ComponentMergeCommand extends Command {
    private Component component;
	private ArchitectureModel model = null;
	
	private Set<ComponentState> components;
	
	public ComponentMergeCommand(Set<Component> components) {
	    this.setLabel("Merge Components");
	    this.components = new HashSet<ComponentState>();

	    String name = "Merged (";
	    for (Component comp: components) {
	    	if (model != null) {
	    		name += ", " + comp.getName();
	    		assert model.equals(comp.getModel());
	        } else
	        	name += comp.getName();
	    
	    	this.model = comp.getModel();
	    	this.components.add(new ComponentState(comp));
	    }
	    name += ")";
	    
	    this.component = new Component(model.comuteUniqueID(), name);
	}
	
	protected ArchitectureModel getModel() {
	    return model;
	}
	
	protected Component getComponent() {
	    return component;
	}
	
	/**
	 * Execute command.
	 */
	public void execute() {
		redo();
	}

	/**
	 * Re-execute command.
	 */
	public void redo() {
		// Add the new component.
	    getModel().addComponent(getComponent());
	    
	    // Process all components beeing merged.
	    for (ComponentState state: components) {
		    // Transfer existing (envisaged) connectors.
		    for (Connector conn: state.getSourceConnectors()) {
		    	if (!conn.isEnvisaged() || getComponent().getConnectorForTarget(conn.getTarget()) != null)
		    		continue;
		    	Connector.connect(getComponent(), conn.getTarget(), true);
		    }
		    for (Connector conn: state.getTargetConnectors()) {
		    	if (!conn.isEnvisaged() || getComponent().getConnectorForSource(conn.getSource()) != null)
		    		continue;
		    	Connector.connect(conn.getTarget(), getComponent(), true);
		    }

		    // Transfer mappings to the new components.
		    for (ResourceMapping mapping: state.getMappings())
		    	getComponent().addMapping(mapping.getResource());
		    getModel().removeComponent(state.getComponent());
	    }
	}

	/**
	 * Undo the execution.
	 */
	public void undo() {
	    for (ComponentState state: components) {
		    getModel().addComponent(state.getComponent());
		    
		    // Restore connections.
		    for (Connector conn: state.getSourceConnectors())
		    	conn.connect();
		    for (Connector conn: state.getTargetConnectors())
		    	conn.connect();

		    // Restore mappings.
		    for (ResourceMapping mapping: state.getMappings())
		    	state.getComponent().addMapping(mapping.getResource());
	    }
		
		getModel().removeComponent(getComponent());
 	}
}

class ComponentState {

	private Component component;
	private List<Connector> sourceConnectors;
	private List<Connector> targetConnectors;
	private ResourceMapping mappings[];
	
	public ComponentState(Component component) {
		this.component = component;
		sourceConnectors = component.getSourceConnectors();
		targetConnectors = component.getTargetConnectors();
		mappings = component.getMappings();
	}

	public Component getComponent() {
		return component;
	}

	public List<Connector> getSourceConnectors() {
		return sourceConnectors;
	}

	public List<Connector> getTargetConnectors() {
		return targetConnectors;
	}

	public ResourceMapping[] getMappings() {
		return mappings;
	}
	
	public boolean equals(Object obj) {
		return component.equals(obj);
	}
}
