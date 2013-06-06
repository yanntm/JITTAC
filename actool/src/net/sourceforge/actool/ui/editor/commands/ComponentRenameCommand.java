package net.sourceforge.actool.ui.editor.commands;


import net.sourceforge.actool.model.da.Component;

import org.eclipse.gef.commands.Command;




public class ComponentRenameCommand extends Command {
    final Component component;
    final String newName, oldName;
		

	public ComponentRenameCommand(Component component, String name) {
        this.setLabel("Rename Component");

	    this.component = component;
        this.oldName = component.getName();
	    this.newName = name;
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
				component.setName(newName);
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
                component.setName(oldName);
//				return Status.OK_STATUS;
//			}
//		};
//        job.setRule(model.getSchedulingRule());
//        job.schedule();
	}
}
