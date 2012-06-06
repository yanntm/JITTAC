package net.sourceforge.actool.ui.editor.commands;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;

import org.eclipse.core.resources.IResource;
import org.eclipse.gef.commands.Command;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaElementLabelProvider;




public class ComponentCreateAndMapCommand extends Command {
    private ArchitectureModel model;
    private Collection<Component> components = new Vector<Component>();
    private Map<Component, IResource> mapping = new HashMap<Component, IResource>();
    
	JavaElementLabelProvider labelProvider = new JavaElementLabelProvider();

    
	
	public ComponentCreateAndMapCommand(ArchitectureModel model, Set<IResource> resources) {
	    this.setLabel("Add Mapping");

	    
	    this.model = model;
	    Iterator<IResource> iter = resources.iterator();
	    while (iter.hasNext()) {
	        IResource resource = iter.next();
	        
	        Component component;
	        
	        // HACK: actool should not depend on JDT!
	        IJavaElement element = JavaCore.create(resource);
	        if (element != null)
	        	component = new Component(model.comuteUniqueID(), labelProvider.getText(element));
	        else
	        	component = new Component(model.comuteUniqueID(), resource.getName());
	        components.add(component);
	        mapping.put(component, resource);
	    }
	}
	

	/**
	 * Execute command.
	 */
	public void execute() {
	    
	    Iterator<Component> iter = components.iterator();
	    while (iter.hasNext()) {
	        Component component = iter.next();
	        model.addComponent(component);
	        component.addMapping(mapping.get(component));
	    }
	}

	/**
	 * Re-execute command.
	 */
	public void redo() {
	    execute();
	}

	/**
	 * Undo the execution.
	 */
	public void undo() {
        Iterator<Component> iter = components.iterator();
        while (iter.hasNext()) {
            Component component = iter.next();
            component.removeMapping(mapping.get(component));
            model.removeComponent(component);
        }
	}
}
