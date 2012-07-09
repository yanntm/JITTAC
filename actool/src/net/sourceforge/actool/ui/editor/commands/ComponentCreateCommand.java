package net.sourceforge.actool.ui.editor.commands;


import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
	public void redo() {
		Job job = new Job("Create Component") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				getModel().addComponent(getComponent());
				return Status.OK_STATUS;
			}
		};job.schedule();
	}

	/**
	 * Undo the execution.
	 */
	public void undo() {
		Job job = new Job("UnCreate Component") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				getModel().removeComponent(getComponent());
				return Status.OK_STATUS;
			}
		};job.schedule();
	}
}
