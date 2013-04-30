package net.sourceforge.actool.jdt.model;

import static com.google.common.base.Preconditions.checkNotNull;
import net.sourceforge.actool.model.ia.IXReference;
import net.sourceforge.actool.model.ia.IXReferenceStringFactory;
import net.sourceforge.actool.model.ia.ImplementationChangeListener;
import net.sourceforge.actool.model.ia.ImplementationModel;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

/**
 * @since 0.2
 */
public abstract class AbstractJavaModel extends ImplementationModel implements IXReferenceStringFactory  {

	public AbstractJavaModel(IJavaProject project) {
		super(checkNotNull(project).getProject());
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
	public String toString(IXReference xref){
		if(xref instanceof JavaXReference)return((JavaXReference)xref).toString();
		else return xref.toString();
	}

}