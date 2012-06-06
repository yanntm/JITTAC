package net.sourceforge.actool.ncc;

import net.sourceforge.actool.model.ia.IImplementationModelFactory;
import net.sourceforge.actool.model.ia.ImplementationModel;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class NCCModelFactory implements IImplementationModelFactory {

	public ImplementationModel createImplementationModel(IProject project) {
		try {
			if (!project.hasNature(CProjectNature.C_NATURE_ID))
				return null;
			
			IFile file = project.getFile("model.nccout");
			if (!file.exists())
				return null;

			NCCModelReader reader = new NCCModelReader(CoreModel.getDefault().create(project));
			return reader.read(file);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
}
