package net.sourceforge.actool.model.ia;


import org.eclipse.core.resources.IResource;

public interface IXReference {
	static public final int UNKNOWN			= 0;
	static public final int ACCESS			= 1;
	static public final int ASSIGNMENT		= 2;
	static public final int CALL			= 3;
	static public final int CREATION		= 4;
	static public final int IMPORT			= 5;

	
	public int getType();
	
	public IElement getSource();
	public IElement getTarget();
	
	public IResource getResource();
	public int getOffset();
	public int getLength();
	public int getLine();
}
