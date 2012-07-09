package net.sourceforge.actool.ui.editor.commands;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import net.sourceforge.actool.model.ResourceMapping;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.widgets.Display;




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
	public synchronized void redo() {
	    // Remove the component, this will also remove connectors and mappings.
		Job job = new Job("Delete Component: "+component.getName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				getModel().removeComponent(getComponent());
				return Status.OK_STATUS;
			}
		};job.schedule();
//	   Thread t = new Thread(new Runnable() { equivalent but dose not add item to progrss job list
//		
//			@Override
//			public void run() {
//				getModel().removeComponent(getComponent());
//				
//			}
//		});t.start();
		
	}

	/**
	 * Undo the execution.
	 */
	public synchronized void undo() {
		Job job = new Job("UnDelete Component: "+component.getName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
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
			    return Status.OK_STATUS;
			}
		};job.schedule(); 
	}
}
