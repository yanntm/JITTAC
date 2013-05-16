package net.sourceforge.actool.jdt.model;

import net.sourceforge.actool.model.ia.AbstractXReference;
import net.sourceforge.actool.model.ia.IElement;
import net.sourceforge.actool.model.ia.IXReference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * @since 0.1
 */
public class JavaXReference extends AbstractXReference {
	
	private class _JavaElement implements IElement {
		private IJavaElement element;
		
		public _JavaElement(IJavaElement element) {
			this.element = element;
		}
		
		public IJavaElement getJavaElement() {
			return element;
		}

		public String getName() {
			return getJavaElement().getElementName();
		}
		
		public int getLength() {
			try {
				if (element instanceof ISourceReference)
					return ((ISourceReference) element).getSourceRange().getLength();
			} catch (JavaModelException ex) {}
				
			return -1;
		}

		public int getOffset() {
			try {
				if (element instanceof ISourceReference)
					return ((ISourceReference) element).getSourceRange().getOffset();
			} catch (JavaModelException ex) {}
				
			return -1;
		}

		public IProject getProject() {
			return getResource().getProject();
		}

		public IResource getResource() {
			return getJavaElement().getResource();
		}
		
		private String paramString(String[] types) {
			if (types.length == 0)
				return "";

			String params = Signature.toString(types[0]);
			for (int i = 1; i < types.length; ++i) {
				params += ", " + Signature.toString(types[i]);
			}

			return params;
		}

		private String toString(IJavaElement elem) {
			switch (elem.getElementType()) {
			case IJavaElement.COMPILATION_UNIT:
				return ((ICompilationUnit) elem).getElementName();
			case IJavaElement.TYPE:
				return ((IType) elem).getFullyQualifiedName();
			case IJavaElement.FIELD:
				return toString(elem.getParent()) + "."
						+ elem.getElementName();
			case IJavaElement.METHOD:
				return toString(elem.getParent()) + "."
						+ elem.getElementName() + "("
						+ paramString(((IMethod) elem).getParameterTypes())
						+ ")";
			}

			return elem.toString();
		}
		
		public String toString() {
			return toString(element);
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof _JavaElement) {
				return element.equals(((_JavaElement) obj).getJavaElement());
			} else
				return false;
		}
	};

	static private final String SEPARATOR	= "\t"; 
	
	_JavaElement source, target;
	
	public JavaXReference(int type, IJavaElement source, IJavaElement target, int line, int offset, int length) {
		super(type, offset, length, length);
		
		this.source = new _JavaElement(source);
		this.target = new _JavaElement(target);
	}

	static public JavaXReference fromString(String xref) {
		String[] entries = xref.split(SEPARATOR);
		
		assert entries.length == 6;	
		int type=IXReference.UNKNOWN ;
		try{type= Integer.parseInt(entries[0]);}
		catch (Exception e) {
			e.getMessage();
		}
		return new JavaXReference(type,
								  JavaCore.create(entries[1]),
								  JavaCore.create(entries[2]),
								  Integer.parseInt(entries[3]),
								  Integer.parseInt(entries[4]),
								  Integer.parseInt(entries[5]));
		
	}

	public String toString() {
		return getType()
			   + SEPARATOR + source.getJavaElement().getHandleIdentifier() 
			   + SEPARATOR + target.getJavaElement().getHandleIdentifier()
		  	   + SEPARATOR + getLine() + SEPARATOR + getOffset() + SEPARATOR + getLength();
	}
	
	
	
	@Override
    public int hashCode() {
        return getOffset();
    }

	@Override
    public boolean equals(Object obj) {
	    if (!(obj instanceof JavaXReference))
	        return false;
	    JavaXReference other = (JavaXReference) obj;
	    return getOffset() == other.getOffset()
	    	   && source.equals(other.source)
	           && target.equals(other.target)
	           && getLine() == other.getLine()
	           && getLength() == other.getLength()
	           && getType() == other.getType();
	}
	

	public IElement getSource() {
		return source;
	}
	
	public IJavaElement getJavaSource() {
		return source.getJavaElement();
	}
	
	public IElement getTarget() {
		return target;
	}

	public IJavaElement getJavaTarget() {
		return target.getJavaElement();
	}
}
