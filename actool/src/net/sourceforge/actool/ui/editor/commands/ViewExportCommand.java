package net.sourceforge.actool.ui.editor.commands;

import java.util.Iterator;
import java.util.Set;

import net.sourceforge.actool.ui.ImageSaveUtil;
import net.sourceforge.actool.ui.editor.model.ArchitectureModelEditPart;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IEditorPart;

public class ViewExportCommand extends Command {

	
	ArchitectureModelEditPart comp=null;
	
	public ViewExportCommand(ArchitectureModelEditPart comp) {
		this.comp = comp;

	}

	@Override
	public boolean canExecute() {
		// TODO Auto-generated method stub
		return comp!=null;
	}
	
	/**
	 * Execute command.
	 */
	public void execute() {
	   
		   ImageSaveUtil.save(comp);
		   

		
	}

}
