package net.sourceforge.actool.jdt.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import net.sourceforge.actool.jdt.ACToolJDT;
import net.sourceforge.actool.jdt.model.AbstractJavaModel;
import net.sourceforge.actool.jdt.util.ProjectTracker;
import net.sourceforge.actool.logging.EventLogger;
import net.sourceforge.actool.model.ModelManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;



/**
 * @since 0.1
 */
public class ImplementationModelBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "net.sourceforge.actool.jdt.build.ImplementationModelBuilder";

	class CompilationUnitCollector implements IResourceVisitor, IResourceDeltaVisitor {
		Collection<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
		
		// List of extensions we can handle.
		Collection<String> extensions = Arrays.asList(JavaCore.getJavaLikeExtensions());

		private AbstractJavaModel model;
		
		public CompilationUnitCollector(AbstractJavaModel model) {
			this.model = model;
		}
		
		public void clear() {
			units.clear();
		}
		
		public Collection<ICompilationUnit> getCompilationUnits() {
			return units;
		}
		
		public boolean visit(IResource resource) {
			// We are only interested in disk files.
			if (resource instanceof IFile && extensions.contains(resource.getFileExtension())) {
				units.add(JavaCore.createCompilationUnitFrom((IFile) resource));
				
				// No need to visit children.
				return false;
			}

			return true;
		}
		
		public boolean visit(IResourceDelta delta) {
			
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
			case IResourceDelta.CHANGED:
				return visit(delta.getResource());
				
			case IResourceDelta.REMOVED:
				IResource resource = delta.getResource();
				if (!(resource instanceof IFile && extensions.contains(resource.getFileExtension())))
					return true;
				ICompilationUnit unit = JavaCore.createCompilationUnitFrom((IFile) resource);
				
				// This will remove all the x-references from that file!
				model.beginUnit(unit);
				model.endUnit();
				break;
			}
			
			return true;
		}
	}
	

	class Requestor extends ASTRequestor {	

		public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
			AbstractJavaModel model = ((AbstractJavaModel) ModelManager.getDefault().getImplementationModel(getProject()));
		    ModelBuilder builder = new ModelBuilder(model);
			
		    try {
		    	EventLogger.getInstance().logBuildBegin(source);
				ast.accept(builder);
		    	EventLogger.getInstance().logBuildEnd(source);
		    } finally {
		    	// Clear unit so next compile session can proceed!
		    	model.clearUnit();
		    }
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		ModelManager manager = ModelManager.getDefault();
		if (!(manager.getImplementationModel(getProject()) instanceof AbstractJavaModel)) {
			throw new IllegalStateException("AC JDT Builder run on non-Java project!");
		}
		

		// Create a list of all affected compilation units.
		CompilationUnitCollector collector = new CompilationUnitCollector((AbstractJavaModel) manager.getImplementationModel(getProject()));
		IResourceDelta delta = getDelta(getProject());
		if (kind == FULL_BUILD || delta == null)
			getProject().accept(collector);
		else
			delta.accept(collector);
		
		// Create and configure the parser.
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setProject(JavaCore.create(getProject()));
		parser.setResolveBindings(true);
		
		Requestor requestor = new Requestor();
		ICompilationUnit units[] = (ICompilationUnit[])
			collector.getCompilationUnits().toArray(new ICompilationUnit[0]);
		try {
			parser.createASTs(units, new String [0], requestor, monitor);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		manager._storeImplementationModel(getProject());
		
		ProjectTracker tracker = ACToolJDT.getDefault().getTracker(getProject());
		if (tracker != null)
			tracker.scanForUnmappedResources();
		
		return null;
	}
}
