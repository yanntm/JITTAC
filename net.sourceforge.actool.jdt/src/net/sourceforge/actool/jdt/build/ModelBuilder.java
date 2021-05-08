package net.sourceforge.actool.jdt.build;

import java.util.Iterator;
import java.util.Stack;

import net.sourceforge.actool.jdt.model.AbstractJavaModel;
import net.sourceforge.actool.jdt.model.JavaXReference;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;



/**
 * @since 0.1
 */
public class ModelBuilder extends ASTVisitor {

	private AbstractJavaModel model = null;

	class NodeBinding {
		final ASTNode node;
		final IJavaElement element;
		
		public NodeBinding(ASTNode node, IJavaElement element) {
			this.node = node;
			this.element = element;
		}
	}
	private Stack<NodeBinding> _stack = new Stack<NodeBinding>();

	public ModelBuilder(AbstractJavaModel model) {
		this.model = model;
	}
	
	protected IJavaElement currentBinding() {
		return _stack.peek().element;
	}
	
	protected boolean pushNodeAndBinding(ASTNode node, IBinding binding) {
		if (binding == null) {
			_stack.push(new NodeBinding(node, null));
			unhandledBinding(node);
			return false;
		} else
			_stack.push(new NodeBinding(node, binding.getJavaElement()));

		return true;
	}
	
	protected void popNodeAndBinding() {
		_stack.pop();
	}
	
	public boolean visit(CompilationUnit node) {
		// TODO: This may not have a resource associated (unlikely though)!
		IJavaElement element = node.getJavaElement();
		//IResource resource = element.getResource();
		_stack.push(new NodeBinding(node, element));
		
		// Delete all relations associated with given resource and start new processing.
		// Changes will not be propagated until flushed or `endResource' is called.
//		try {
//			resource.deleteMarkers(TobagoBuilder.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
//		} catch (CoreException ex) {
//			// TODO Auto-generated catch block
//			ex.printStackTrace();
//		}
		
		//unit = resource;
		model.beginUnit((ICompilationUnit) node.getJavaElement());
		
		return true;
	}

	public void endVisit(CompilationUnit node) {
		// TODO: This may not have a resource associated (unlikely though)!
		//IResource resource = (IResource) node.getJavaElement().getResource();
		popNodeAndBinding();

		// Finish resource processing and flush all the changes.
		//model._endResource(resource);
		model.endUnit();
		//unit = null;
	}
	
	public boolean visit(AnonymousClassDeclaration node) {
		return pushNodeAndBinding(node, node.resolveBinding());
	}
	
	public void endVisit(AnonymousClassDeclaration node) {
		popNodeAndBinding();
	}
	
	public boolean visit(MethodDeclaration node) {
		return pushNodeAndBinding(node, node.resolveBinding());
	}
	
	public void endVisit(MethodDeclaration node) {
		popNodeAndBinding();
	}
	
	
	public boolean visit(TypeDeclaration node) {
		return pushNodeAndBinding(node, node.resolveBinding());
	}
	
	public void endVisit(TypeDeclaration node) {
		popNodeAndBinding();
	}
	
	
	public boolean visit(ImportDeclaration node) {
		IBinding binding = node.resolveBinding();
		if (binding == null) {
			unhandledBinding(node);
			return false;
		}
		
		handleXReference(JavaXReference.IMPORT, currentBinding(),
						 binding.getJavaElement(), node.getName());
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean visit(MethodInvocation node) {
		IBinding binding = node.resolveMethodBinding();
		if (binding == null) {
			unhandledBinding(node);
			return false;
		}
		
		handleXReference(JavaXReference.CALL, currentBinding(),
						 binding.getJavaElement(),node);
		
		// Process the expression which give the element
		// on which the method is called. 
		if (node.getExpression() != null)
			node.getExpression().accept(this);
		
		// Process type arguments (for templates).
		Iterator<ASTNode> types = node.typeArguments().iterator();
		while (types.hasNext())
			types.next().accept(this);
		
		// And process all the arguments.
		Iterator<ASTNode> args = node.arguments().iterator();
		while (args.hasNext())
			args.next().accept(this);

		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean visit(SuperMethodInvocation node) {
		IBinding binding = node.resolveMethodBinding();
		if (binding == null) {
			unhandledBinding(node);
			return false;
		}
		
		handleXReference(JavaXReference.CALL, currentBinding(), binding.getJavaElement(),node);
		
		// Process type arguments (for templates).
		Iterator<ASTNode> types = node.typeArguments().iterator();
		while (types.hasNext())
			types.next().accept(this);
		
		// And process all the arguments.
		Iterator<ASTNode> args = node.arguments().iterator();
		while (args.hasNext())
			args.next().accept(this);

		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean visit(ClassInstanceCreation node) {
		
		// TODO: This should use node.resolveConstructorBinding()!
		IBinding binding = node.getType().resolveBinding();
		if (binding == null) {
			unhandledBinding(node);
			return false;
		}
			
		handleXReference(JavaXReference.ACCESS,currentBinding(),
				         binding.getJavaElement(), node);
		
		// Process the expression which give the element
		// on which the method is called.
		if (node.getExpression() != null)
			node.getExpression().accept(this);
		
		// Process type arguments (for templates).
		Iterator<ASTNode> types = node.typeArguments().iterator();
		while (types.hasNext())
			types.next().accept(this);
		
		// And process all the arguments.
		Iterator<ASTNode> args = node.arguments().iterator();
		while (args.hasNext())
			args.next().accept(this);

		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean visit(SuperConstructorInvocation node) {
		IBinding binding = node.resolveConstructorBinding();
		if (binding == null) {
			unhandledBinding(node);
			return false;
		}
		
		handleXReference(JavaXReference.CALL, currentBinding(),
						 binding.getJavaElement(), node);
		
		// Process the expression which give the element
		// on which the method is called.
		if (node.getExpression() != null)
			node.getExpression().accept(this);
		
		// Process type arguments (for templates).
		Iterator<ASTNode> types = node.typeArguments().iterator();
		while (types.hasNext())
			types.next().accept(this);
		
		// And process all the arguments.
		Iterator<ASTNode> args = node.arguments().iterator();
		while (args.hasNext())
			args.next().accept(this);

		return false;
	}	
	

	public boolean visit(Assignment node) {
		// Get the thing we assign to...
		IBinding binding = null;
		Expression expr = node.getLeftHandSide();
		while (expr != null) {
			if (expr instanceof Name) {
				binding = ((Name) expr).resolveBinding();
				break;
			} else if (expr instanceof FieldAccess) {
				FieldAccess fa = (FieldAccess) expr;
				fa.getExpression().accept(this);
				binding = fa.resolveFieldBinding();
				break;
			} else if (expr instanceof SuperFieldAccess) {
				SuperFieldAccess fa = (SuperFieldAccess) expr;
				fa.getQualifier().accept(this);
				binding = fa.resolveFieldBinding();
				break;
			} else if (expr instanceof ArrayAccess) {
				ArrayAccess aa = (ArrayAccess) expr;
				aa.getIndex().accept(this);
				expr = aa.getArray();
				continue;
			} else if (expr instanceof ParenthesizedExpression) {
				expr = ((ParenthesizedExpression) expr).getExpression();
				continue;
			}
			
			throw new IllegalStateException();
		}
		
		if (binding == null) {
			unhandledBinding(node);
			return false;
		}
		handleXReference(JavaXReference.ASSIGNMENT, currentBinding(),
						 binding.getJavaElement(), node);
		
		// Process right hand side expression (value).
		node.getRightHandSide().accept(this);
		return false;
	}
	
	public boolean visit(SimpleName node) {
		handleName(node);
		return false;
	}

	public boolean visit(QualifiedName node) {
		handleName(node);
		return false;
	}


	private boolean handleName(Name node) {
		return handleName(node, JavaXReference.ACCESS);
	}

    private boolean handleName(Name node, int type) {
		IBinding binding = node.resolveBinding();
		if (binding == null) {
			unhandledBinding(node);
			return false;
		}

		switch (binding.getKind()) {
		case IBinding.METHOD:
		case IBinding.TYPE:
		case IBinding.VARIABLE:
			// Ignore local variables and self references.
			IJavaElement dependant = (IJavaElement) currentBinding();
			IJavaElement dependency = binding.getJavaElement();
			if (dependency == null || dependant == null) {
				// This seems to happen mostly for array length parameter.				
				// TODO: Investigate a little more.
				return false;
			}
			if (dependency.getElementType() == IJavaElement.LOCAL_VARIABLE
				|| dependant.equals(dependency))
				break;
			handleXReference(type, dependant, dependency, node);
			break;
			
		case IBinding.PACKAGE:
			// IGNORE THESE
			break;

		default:
			unhandledNode(node);
		}
		
		return true;
	}

	
	public void handleXReference(int type, IJavaElement source, IJavaElement target, ASTNode node) {
		if (source == null || target == null) {
			// TODO: Sometimes during the first build some bindings may fail to  resolve.
			// TODO: Do something with it!
			return;
		}
		model.addXReference(type, source, target,
							((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition()),
							node.getStartPosition(), node.getLength());
	}	
	
	private void unhandledBinding(ASTNode node) {
		System.err.println("UNHANDLED BINDING: " + node.getClass().getSimpleName() + ": " + node.toString());
	}

	private void unhandledNode(ASTNode node){
		System.err.println("UNHANDLED NODE: " + node.getClass().toString());
	}
}
