package net.sourceforge.actool.ui.editor.commands;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.actool.ui.editor.model.ComponentEditPart;
import net.sourceforge.actool.ui.editor.model.Visibility;

import org.eclipse.gef.commands.Command;


public class ComponentVisibilityCommand extends Command {
	private Map<ComponentEditPart, Integer> components = new HashMap<ComponentEditPart, Integer>();
	private int visibility;

	public ComponentVisibilityCommand(Set<ComponentEditPart> components, int visibility) {
	    this.setLabel("Change Visibility");
	    this.visibility = visibility;
	    
	    for (ComponentEditPart comp: components)
	    	this.components.put(comp, comp.getVisibility());
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
	    for (Visibility comp: components.keySet())
			comp.setVisibility(visibility);
	}

	/**
	 * Undo the execution.
	 */
	public void undo() {
	    for (Visibility comp: components.keySet())
			comp.setVisibility(components.get(comp).intValue());
	}
}
