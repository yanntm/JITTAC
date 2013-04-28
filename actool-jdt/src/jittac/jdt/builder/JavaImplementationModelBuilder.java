package jittac.jdt.builder;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.System.arraycopy;
import static jittac.jdt.JavaAC.checkSupportedProject;
import static jittac.jdt.JavaAC.error;
import static jittac.jdt.JavaAC.javaIAModel;
import static jittac.jdt.JavaAC.warn;
import static org.eclipse.core.resources.IResource.FILE;
import static org.eclipse.core.resources.IResource.FOLDER;
import static org.eclipse.core.resources.IResource.PROJECT;
import static org.eclipse.core.resources.IResourceDelta.ADDED;
import static org.eclipse.core.resources.IResourceDelta.CHANGED;
import static org.eclipse.core.resources.IResourceDelta.REMOVED;
import static org.eclipse.jdt.core.IPackageFragmentRoot.K_SOURCE;

import java.util.ArrayList;
import java.util.Map;

import jittac.util.DummyProgressMonitor;
import net.sourceforge.actool.jdt.model.AbstractJavaModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

public class JavaImplementationModelBuilder extends IncrementalProjectBuilder {
    /** Unique ID of this builder.  */
    public static final String ID = "jittac.jdt.javaimbuilder";

    
    protected void checkCancelled(IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }
    
    protected ICompilationUnit[] collectCompilationUnits(IJavaProject project,
                                                         IProgressMonitor monitor)
            throws JavaModelException {
        monitor.beginTask("", project.getAllPackageFragmentRoots().length);

        try {
            ArrayList<ICompilationUnit[]> packages = newArrayList();
            int count = 0;

            // Collect compilation units from all fragments (packages).
            for (IPackageFragmentRoot root: project.getAllPackageFragmentRoots()) {
                monitor.subTask("[JITTAC] Scanning for sources (" + project.getElementName() + "): "
                                + root.getPath().toPortableString());

                // Only collec compilation units from actual source (java) files.
                if (root.getKind() != K_SOURCE) {
                    continue;
                }

                for (IJavaElement element: root.getChildren()) {
                    if (!(element instanceof IPackageFragment)) {
                        warn("Ignoring element (not a package fragment) '{2}'"
                              + " while analysing sources in '{1}' of project '{0}'.",
                             project.getElementName(), root.getElementName(), element.getElementName());
                        continue;
                    }
                    
                    ICompilationUnit[] units = ((IPackageFragment) element).getCompilationUnits();
                    count += units.length;
                    packages.add(units);
                }

                monitor.worked(1);
                checkCancelled(monitor);
            }
    
            // Amalgamate units from all packages into a single array.
            int position = 0;
            ICompilationUnit[] units = new ICompilationUnit[count];
            for (ICompilationUnit[] pacakge: packages) {
                arraycopy(pacakge, 0, units, position, pacakge.length);
                position += pacakge.length;
            }
    
            return units;
        } finally {
            monitor.done();
        }
    }

    protected ICompilationUnit[] collectCompilationUnits(IResourceDelta delta,
                                                         final IProgressMonitor monitor)
            throws CoreException {
        final ArrayList<ICompilationUnit> units = newArrayList();
        final IJavaProject project = JavaCore.create(checkSupportedProject(getProject()));
        final AbstractJavaModel model = javaIAModel(project);

        monitor.beginTask("", 1);
        try {
            checkCancelled(monitor);
            monitor.subTask("[JITTAC] Scanning for modified sources ("
                            + project.getElementName() + ")");
            delta.accept(new IResourceDeltaVisitor() {
                @Override
                public boolean visit(IResourceDelta delta) throws CoreException {
                    checkCancelled(monitor);

                    IResource resource = delta.getResource();
                    int resourceType = resource.getType();
                    if (resourceType == FOLDER || resourceType == PROJECT) {
                        return true;
                    } else if (resourceType != FILE) {
                        return false;
                    }
    
                    IJavaElement element = JavaCore.create(resource);
                    if (element instanceof ICompilationUnit) {
                        // Check that this java file is contained in a package fragment,
                        // this will only happen if it is on the build path. Ignore otherwise...
                        IJavaElement parent = JavaCore.create(resource.getParent());
                        if (!(parent instanceof IPackageFragment
                              || parent instanceof IPackageFragmentRoot)) {
                            return false;
                        }
    
                        switch (delta.getKind()) {
                        case ADDED:
                        case CHANGED:
                            units.add((ICompilationUnit) element);          
                            return false;
    
                        case REMOVED:
                            // This will remove all the x-references 
                            // from the given compilation unit.
                            model.beginUnit((ICompilationUnit) element);
                            model.endUnit();
                        }
                    }
                    
                    return false;
                }
            });
            monitor.worked(1);
        } finally {
            monitor.done();
        }

        return units.toArray(new ICompilationUnit[units.size()]);
    }

    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
            throws CoreException {
       final IJavaProject project = JavaCore.create(checkSupportedProject(getProject()));
       final String projectName = project.getElementName();


        monitor.beginTask("[JITTAC] Java AST processing on project '"
                          + project.getElementName() +"'...", 100 + 2000);
        try {
            final ICompilationUnit[] units;
            IProgressMonitor collectionMonitor = new SubProgressMonitor(monitor, 100);

            // Collect all the compilation units that should be analysed.
            if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
                units = collectCompilationUnits(getDelta(getProject()), collectionMonitor);
            } else if (kind == FULL_BUILD) {
                units = collectCompilationUnits(project, collectionMonitor);
            } else {
                error("Invalid build kind ({0}) when invoking '{2}' on project '{1}'; exiting...",
                       kind, project.getElementName(), this.getCommand().getBuilderName());
                return null;
            }
            checkCancelled(monitor);

            // Create and configure the AST parser.
            final IProgressMonitor processingMonitor = new SubProgressMonitor(monitor, 2000);

            processingMonitor.beginTask("", units.length);
            processingMonitor.subTask("[JITTAC] Initialising Java AST processing for"
            		         + " project '" + projectName + "'...");
            try {
                ASTParser parser = ASTParser.newParser(AST.JLS4);
                parser.setResolveBindings(true);
                parser.setProject(project);
                checkCancelled(monitor);

                // Do the actual AST processing...
                parser.createASTs(units, new String[0], 
                        new JavaASTHandler(javaIAModel(project), units.length, processingMonitor),
                        new DummyProgressMonitor(processingMonitor));
            } finally {
                processingMonitor.done();
            }
        } finally {
            monitor.done();
        }

        return null;
    }
}
