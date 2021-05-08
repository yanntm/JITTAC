package net.sourceforge.actool.model.ia;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public interface IElement {

	public String getName();
	
	public IProject getProject();
	
	public IResource getResource();
	public int getOffset();
	public int getLength();
}
