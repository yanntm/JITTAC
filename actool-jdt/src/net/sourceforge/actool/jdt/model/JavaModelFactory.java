package net.sourceforge.actool.jdt.model;

import net.sourceforge.actool.jdt.ACNatureJDT;
import net.sourceforge.actool.model.ia.IImplementationModelFactory;
import net.sourceforge.actool.model.ia.ImplementationModel;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

public class JavaModelFactory implements
		IImplementationModelFactory {

	public ImplementationModel createImplementationModel(IProject project) {
		try {
			if (project.hasNature(ACNatureJDT.NATURE_ID))
				return new JavaModel();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

}
