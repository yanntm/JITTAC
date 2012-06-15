package net.sourceforge.actool.ui.editor.quickfix;


import net.sourceforge.actool.defaults;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class OpenArchitecturalModelQuickFix implements IMarkerResolution {
	      String label;
	      OpenArchitecturalModelQuickFix(String label) {
	         this.label = label;
	      }
	      
	      public String getLabel() {
	         return label;
	      }
	      
	      @Override
	      public void run(IMarker marker) {
	         try {
	         IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	         IFile file = root.getFile( (IPath)marker.getAttribute(defaults.MODEL));
	         IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	         
				IDE.openEditor(page, file);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	         
	      }
		
		
	   
}
