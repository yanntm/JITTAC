package net.sourceforge.actool.ui.editor.commands;


import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;

import org.eclipse.gef.commands.Command;




public class ComponentCreateCommand extends Command {
	ArchitectureModel model;
    Component component;
		

	public ComponentCreateCommand(ArchitectureModel model) {
	    this.model = model;
	    component = new Component(model.comuteUniqueID(), "");
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
	public synchronized  void redo() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				getModel().addComponent(getComponent());
			}
		});t.start();
	}

	/**
	 * Undo the execution.
	 */
	public synchronized void undo() {
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				getModel().removeComponent(getComponent());
				
			}
		});t.start();
	   
	}
}
