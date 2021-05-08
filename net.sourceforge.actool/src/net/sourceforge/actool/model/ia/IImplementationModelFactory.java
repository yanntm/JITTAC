package net.sourceforge.actool.model.ia;


import org.eclipse.core.resources.IProject;

public interface IImplementationModelFactory {
	
	ImplementationModel createImplementationModel(IProject project);

}
