package net.sourceforge.actool.ui.editor.commands;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.actool.model.da.Component;

import org.eclipse.core.resources.IResource;
import org.eclipse.gef.commands.Command;




public class ComponentMapCommand extends Command {
    private Component target;
    private HashSet<IResource> resources;
    
	
	public ComponentMapCommand(Component target, Set<IResource> resources) {
	    this.setLabel("Add Mapping");
	    
	    this.target = target;
	    this.resources = new HashSet<IResource>(resources);
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
	   Iterator<IResource> iter = resources.iterator();
	    while (iter.hasNext())
	        target.addMapping(iter.next());
	}

	/**
	 * Undo the execution.
	 */
	public void undo() {
		Iterator<IResource> iter = resources.iterator();
		while (iter.hasNext())
			target.removeMapping(iter.next());
	}
}
