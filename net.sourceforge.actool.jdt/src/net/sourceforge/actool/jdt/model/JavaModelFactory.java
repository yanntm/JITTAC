package net.sourceforge.actool.jdt.model;

import jittac.jdt.JavaAC;
import net.sourceforge.actool.model.ia.IImplementationModelFactory;
import net.sourceforge.actool.model.ia.ImplementationModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

/**
 * @since 0.1
 */
public class JavaModelFactory implements
		IImplementationModelFactory {

	public ImplementationModel createImplementationModel(IProject project) {
		try {
			if (project.hasNature(JavaAC.NATURE_ID))
              return new JavaModel(JavaCore.create(project));
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

}
