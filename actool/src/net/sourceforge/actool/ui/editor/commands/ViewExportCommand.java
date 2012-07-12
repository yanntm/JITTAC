package net.sourceforge.actool.ui.editor.commands;

import java.util.Iterator;
import java.util.Set;

import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.ui.ImageSaveUtil;
import net.sourceforge.actool.ui.editor.model.ArchitectureModelEditPart;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.widgets.Display;
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
		Job job = new Job("export image") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						ImageSaveUtil.save(comp);
					}
				}); 
				
				 return Status.OK_STATUS;
			}
		};job.schedule();
		  
		   

		
	}

}
