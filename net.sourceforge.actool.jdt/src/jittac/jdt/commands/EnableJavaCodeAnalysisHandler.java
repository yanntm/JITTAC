package jittac.jdt.commands;

import static com.google.common.collect.ObjectArrays.concat;
import static net.sourceforge.actool.jdt.ACToolJDT.errorStatus;

import java.util.ArrayList;
import java.util.Collection;

import jittac.jdt.JavaAC;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class EnableJavaCodeAnalysisHandler extends BaseHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Collection<IProject> projects = new ArrayList<>();
		ISelection selected = HandlerUtil.getActiveMenuSelection(event);
		if (selected instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selected;
			for (Object elt : ssel) {
				if (elt instanceof IJavaProject) {
					projects.add( ((IJavaProject) elt).getProject());
				}
			}
		}
//		Collection<IProject> projects
//		        = extractApplicableProjects((EvaluationContext) event.getApplicationContext());
		
		for (IProject project: projects) {
        	try {
        	    if (!project.isOpen() 
        	        || !project.hasNature(JavaCore.NATURE_ID)
        	        || project.hasNature(JavaAC.NATURE_ID)) {
        	        continue;
        	    }

                IProjectDescription desc = project.getDescription();
                desc.setNatureIds(concat(desc.getNatureIds(), JavaAC.NATURE_ID));
                project.setDescription(desc, null);
        	} catch (CoreException e) {
        	    errorStatus("Errora adding JITTAC nature: " + JavaAC.NATURE_ID, e);
        	}
		}

		return null;
	}
}
