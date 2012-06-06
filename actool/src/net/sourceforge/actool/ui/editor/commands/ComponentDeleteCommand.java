package net.sourceforge.actool.ui.editor.commands;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import net.sourceforge.actool.model.ResourceMapping;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;

import org.eclipse.gef.commands.Command;




public class ComponentDeleteCommand extends Command {
	ArchitectureModel model;
    Component component;
	
    Collection<Connector> sourceConnectors = new ArrayList<Connector>();
    Collection<Connector> targetConnectors = new ArrayList<Connector>();
    Collection<ResourceMapping> mappings;
	

	public ComponentDeleteCommand(Component component) {
	    this.model = component.getModel();
	    this.component = component;
	    this.setLabel("Delete Component");
	}
	
	protected ArchitectureModel getModel() {
	    return model;
	}
	
	protected Component getComponent() {
	    return component;
	}

	/*private void pruneConnectors() {
        Iterator<Connector> iter;

        iter = sourceConnectors.iterator();
        while (iter.hasNext()) {
            if (!iter.next().isEnvisaged())
                iter.remove();
        }
        
        iter = targetConnectors.iterator();
        while (iter.hasNext()) {
            if (!iter.next().isEnvisaged())
                iter.remove();
        }
	}*/

	/**
	 * Execute command.
	 */
	public void execute() {
	    sourceConnectors = getComponent().getSourceConnectors();
	    targetConnectors = getComponent().getTargetConnectors();
	    mappings = Arrays.asList(getComponent().getMappings());

	    redo();
	}

	/**
	 * Re-execute command.
	 */
	public void redo() {
	    // Remove the component, this will also remove connectors and mappings.
	    getModel().removeComponent(getComponent());
	}

	/**
	 * Undo the execution.
	 */
	public void undo() {
	    getModel().addComponent(getComponent());

	    Iterator<Connector> iter;	    
	    iter = sourceConnectors.iterator();
	    while (iter.hasNext())
	        iter.next().connect();
	    iter = targetConnectors.iterator();
	    while (iter.hasNext())
	        iter.next().connect();
	    
	    Iterator<ResourceMapping> miter = mappings.iterator();
	    while (miter.hasNext())
	        getComponent().addMapping(miter.next().getResource());
	}
}
