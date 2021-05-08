package net.sourceforge.actool.ui.editor.commands;

import net.sourceforge.actool.model.ResourceMapping;
import net.sourceforge.actool.model.da.Component;

import org.eclipse.core.resources.IResource;
import org.eclipse.gef.commands.Command;

public class MappingDeleteCommand extends Command {
	private Component component;
	private IResource resource;

	public MappingDeleteCommand(ResourceMapping mapping) {
		this.setLabel("Delete Mapping");
		component = mapping.getComponent();
		resource = mapping.getResource();
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
		component.removeMapping(resource);
	}

	/**
	 * Undo the execution.
	 */
	public void undo() {
		component.addMapping(resource);
	}
}
