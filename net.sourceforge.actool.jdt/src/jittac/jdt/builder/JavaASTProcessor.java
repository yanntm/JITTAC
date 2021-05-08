package jittac.jdt.builder;

import static com.google.common.base.Preconditions.checkNotNull;
import static jittac.jdt.JavaAC.error;
import static jittac.jdt.JavaAC.warn;
import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
import static org.eclipse.jdt.core.IPackageFragmentRoot.K_BINARY;

import java.util.Iterator;
import java.util.Stack;

import net.sourceforge.actool.jdt.model.AbstractJavaModel;
import net.sourceforge.actool.jdt.model.JavaXReference;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
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
public class JavaASTProcessor extends ASTVisitor {

    private boolean ignoreLibraryReferences = false;
    private boolean ignoreIntraProjectReferences = false;
 
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

    public JavaASTProcessor(AbstractJavaModel model) {
        this.model = checkNotNull(model);
    }

    public boolean isIgnoreLibraryReferences() {
        return ignoreLibraryReferences;
    }

    public void setIgnoreLibraryReferences(boolean value) {
        this.ignoreLibraryReferences = value;
    }


    public boolean isIgnoreIntraProjectReferences() {
        return ignoreIntraProjectReferences;
    }


    public void setIgnoreIntraProjectReferences(boolean value) {
        this.ignoreIntraProjectReferences = value;
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
        IJavaElement element = node.getJavaElement();
        //IResource resource = element.getResource();
        _stack.push(new NodeBinding(node, element));
      
        model.beginUnit((ICompilationUnit) node.getJavaElement());
        
        return true;
    }

    public void endVisit(CompilationUnit node) {
        popNodeAndBinding();

        // Finish resource processing and flush all the changes.
        model.endUnit();
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
        IMethodBinding binding = node.resolveMethodBinding();
        if (binding == null) {
            unhandledBinding(node);
            return false;
        }

        IJavaElement target = binding.getJavaElement();
        if (target == null && binding.getDeclaringClass().isEnum()) {
           // In case of enums some standard methods (like 'values()' will not resolve.
           // Reference the enum itself then..
           target = binding.getDeclaringClass().getJavaElement();
        }
        handleXReference(JavaXReference.CALL, currentBinding(), target, node);
        
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
        // TODO: Investigate why resolveConstructorBinding() doesn't work.
        ITypeBinding binding = node.getType().resolveBinding();
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
        IMethodBinding binding = node.resolveConstructorBinding();
        if (binding == null) {
            unhandledBinding(node);
            return false;
        }
        
        IJavaElement target = binding.getJavaElement();
        if (target == null 
            && binding.isConstructor()
            && node.arguments().size() == 0) {
            // In case of a default constructor reference the Class itself...
            target = binding.getDeclaringClass().getJavaElement();
        }
        handleXReference(JavaXReference.CALL, currentBinding(), target, node);
        
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
        boolean handledLeftHand = false;
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
                Name qualifier = fa.getQualifier();
                if (qualifier != null) {
                    qualifier.accept(this);
                }
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
            } else if (expr instanceof MethodInvocation
                       || expr instanceof SuperMethodInvocation) {
                expr.accept(this);
                handledLeftHand = true;
                break;
            }

            warn("Unable to handle left-hand side of assignment expression: {0}[''{1}'']",
                 expr.getClass().getSimpleName(), expr.toString());
            break;
        }
        
        if (!handledLeftHand) {
            if (binding == null) {
                unhandledBinding(node);
            }
            handleXReference(JavaXReference.ASSIGNMENT, currentBinding(),
                             binding.getJavaElement(), node);
        }
        
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
            // TODO: Sometimes some binding may fail to resolve into java elements. Fix it!
            error("Failed to resolve Java element for {0}[''{1}'']",
                  node.getClass().getSimpleName(), node.toString());
            return;
        }
        
        IResource sourceResource = source.getResource();
        IResource targetResource = target.getResource();
        
        // Ignore all references within the same resource (compilation unit).
        if (sourceResource.equals(targetResource)) {
            return;
        }

        
        // Ignore references within ghe same project.
        if (ignoreIntraProjectReferences && targetResource != null
            && sourceResource.getProject().equals(targetResource.getProject())) {
            return;
        } 
        
        // Ignore all references to the types contained in libraries (jar or zip files)
        if (ignoreLibraryReferences) {
            try {
                IPackageFragmentRoot root = (IPackageFragmentRoot) target.getAncestor(PACKAGE_FRAGMENT_ROOT);
                if (root == null || root.getKind() == K_BINARY) {
                    return;
                }
            } catch (JavaModelException ex) {
                error(ex, "Unexpected exception in AST Parser");
            }
        }

        model.addXReference(type, source, target,
                            ((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition()),
                            node.getStartPosition(), node.getLength());
    }   
    
    private void unhandledBinding(ASTNode node) {
        warn("Unhandled Binding ({0}): {1}", node.getClass().getSimpleName(), node.toString());
    }

    private void unhandledNode(ASTNode node){
        warn("Unhandled Node: {0}",  node.toString());
    }
}

