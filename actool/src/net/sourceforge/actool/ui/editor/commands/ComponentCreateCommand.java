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
	
	public Component getComponent() {
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
//		Job job = new Job("Create Component") {
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
				getModel().addComponent(getComponent());
//				return Status.OK_STATUS;
//			}
//		};
//        job.setRule(model.getSchedulingRule());
//        job.schedule();
	}

	/**
	 * Undo the execution.
	 */
	public void undo() {
//		Job job = new Job("UnCreate Component") {
//			@Override
//			protected IStatus run(IProgressMonitor monitor) {
				getModel().removeComponent(getComponent());
//				return Status.OK_STATUS;
//			}
//		};
//        job.setRule(model.getSchedulingRule());
//        job.schedule();
	}
}
