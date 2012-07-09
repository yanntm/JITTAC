package net.sourceforge.actool.ui.editor.commands;


import net.sourceforge.actool.ui.editor.model.ComponentEditPart;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;






public class ComponentSetConstraintCommand extends Command {
	private final ComponentEditPart part;
	
	private final Rectangle newBounds;
	private final Rectangle oldBounds;

	public ComponentSetConstraintCommand(ComponentEditPart part, Rectangle bounds) {
		this.setLabel("Move Component");
		this.part = part;

		newBounds = bounds;
		oldBounds = part.getFigure().getBounds().getCopy();
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
		
		part.getFigure().setBounds(newBounds);
		part._setLocationProperty(newBounds.getTopLeft());
	}

	/**
	 * Undo the execution.
	 */
	public void undo() {
		part.getFigure().setBounds(oldBounds);
		part._setLocationProperty(oldBounds.getTopLeft());
	}
}
