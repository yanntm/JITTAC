package net.sourceforge.actool.jdt.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import net.sourceforge.actool.model.ia.IXReference;
import net.sourceforge.actool.model.ia.IXReferenceFactory;
import net.sourceforge.actool.model.ia.ImplementationChangeListener;
import net.sourceforge.actool.model.ia.ImplementationModel;

public abstract class AbstractJavaModel extends ImplementationModel implements IXReferenceFactory  {

	public AbstractJavaModel() {
		super();
	}


	public abstract void _updateListener(ImplementationChangeListener listener);
	public abstract void addXReference(int type, IJavaElement source, IJavaElement target,
			int line, int offset, int length);
	/**
	 * Start processing a compilation unit (this unit becomes current).
	 * 
	 * @throws IllegalStateException if there is already a current unit
	 */
	public abstract void beginUnit(ICompilationUnit unit); 

	/**
	 * End processing current compilation unit.
	 */
	public abstract void endUnit(); 

	/**
	 * Return current compilation unit.
	 * 
	 * @return current compilation unit
	 * @throws IllegalStateException if there is no current unit
	 */

	public abstract void clearUnit();

	public IXReference createXReference(String xref) {
		return JavaXReference.fromString(xref);
	}

}