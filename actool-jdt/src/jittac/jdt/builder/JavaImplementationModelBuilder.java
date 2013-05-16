package jittac.jdt.builder;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.Math.min;
import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static jittac.jdt.JavaAC.checkSupportedProject;
import static jittac.jdt.JavaAC.error;
import static jittac.jdt.JavaAC.javaIAModel;
import static jittac.jdt.JavaAC.warn;
import static net.sourceforge.actool.model.ModelManager.defaultModelManager;
import static org.eclipse.core.resources.IResource.FILE;
import static org.eclipse.core.resources.IResource.FOLDER;
import static org.eclipse.core.resources.IResource.PROJECT;
import static org.eclipse.core.resources.IResourceDelta.ADDED;
import static org.eclipse.core.resources.IResourceDelta.CHANGED;
import static org.eclipse.core.resources.IResourceDelta.REMOVED;
import static org.eclipse.jdt.core.IPackageFragmentRoot.K_SOURCE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jittac.util.DummyProgressMonitor;
import net.sourceforge.actool.jdt.ACToolJDT;
import net.sourceforge.actool.jdt.model.AbstractJavaModel;
import net.sourceforge.actool.jdt.util.ProjectTracker;

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
    public static final Integer DEFAULT_MAX_BATCH_SIZE = 256;

    /** Unique ID of this builder.  */
    public static final String ID = "jittac.jdt.javaimbuilder";
    
    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;
    private boolean compactPackagesOnFullBuild = false;

    
    protected void checkCancelled(IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }
    
    protected Collection<ICompilationUnit[]> batchCompilationUnits(ICompilationUnit[] units) {
        if (units.length > maxBatchSize) {
            Collection<ICompilationUnit[]> batches = newLinkedList();

            for (int processed = 0; processed < units.length; processed += maxBatchSize) {
                ICompilationUnit[] batch = 
                        new ICompilationUnit[min(units.length - processed, maxBatchSize)];
                arraycopy(units, processed, batch, 0, batch.length);
                batches.add(batch);
            }

            return batches;
        } else if (units.length > 0) {
            return singleton(units);
        } else {
            return emptyList();
        }
    }
    
    protected Collection<ICompilationUnit[]> collectCompilationUnits(IJavaProject project,
                                                                     IProgressMonitor monitor)
            throws JavaModelException {
        monitor.beginTask("", project.getAllPackageFragmentRoots().length);

        try {
            List<ICompilationUnit[]> packageBatches = newLinkedList();

            // Collect compilation units from all fragments (packages).
            for (IPackageFragmentRoot root: project.getAllPackageFragmentRoots()) {
                monitor.subTask("[JITTAC] Scanning for Java sources in '" 
                                + project.getElementName() + "' project: " + root.getPath().toPortableString());

                // Only collect compilation units from actual source (java) files.
                if (root.getKind() != K_SOURCE) {
                    continue;
                }
                
                // Make sure that we do not include compilation units from other projects
                // as these were already processed when those projects were processed.
                if (!root.getJavaProject().equals(project)) {
                    continue;
                }

                for (IJavaElement element: root.getChildren()) {
                    if (!(element instanceof IPackageFragment)) {
                        warn("Ignoring element (not a package fragment) ''{2}''"
                              + " while analysing sources in ''{1}'' of project ''{0}''.",
                             project.getElementName(), root.getElementName(), element.getElementName());
                        continue;
                    }
                    
                    ICompilationUnit[] units = ((IPackageFragment) element).getCompilationUnits();
                    packageBatches.addAll(batchCompilationUnits(units));
                }

                monitor.worked(1);
                checkCancelled(monitor);
            }
            
    
            // Amalgamate units from all packages into a single array.
            if (compactPackagesOnFullBuild) {
                List<ICompilationUnit[]> batches = newLinkedList();
                List<ICompilationUnit> batch = newArrayList();
                
                for (ICompilationUnit[] packageBatch: packageBatches) {
                    // Process the units of this package,
                    // batch them in case package is larger than maxBatchSize
                    for (ICompilationUnit[] units: batchCompilationUnits(packageBatch)) {
                        if (batch.size() + units.length > maxBatchSize) {
                            batches.add(batch.toArray(new ICompilationUnit[batch.size()]));
                            batch.clear();
                        }
                        batch.addAll(asList(units));
                    }
                }
                
                // Add the last batch to the list...
                if (!batch.isEmpty()) {
                    batches.add(batch.toArray(new ICompilationUnit[batch.size()]));
                }
                       
               return batches;
            }

            return packageBatches;
        } finally {
            monitor.done();
        }
    }

    protected Collection<ICompilationUnit[]> collectCompilationUnits(IResourceDelta delta,
                                                                     final IProgressMonitor monitor)
            throws CoreException {
        final ArrayList<ICompilationUnit> units = newArrayList();
        final IJavaProject project = JavaCore.create(checkSupportedProject(getProject()));
        final AbstractJavaModel model = javaIAModel(project);

        monitor.beginTask("", 1);
        try {
            checkCancelled(monitor);
            monitor.subTask("[JITTAC] Scanning for modified Java sources in '"
                            + project.getElementName() + "' project");
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

        return batchCompilationUnits(units.toArray(new ICompilationUnit[units.size()]));
    }

    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
            throws CoreException {
       final IJavaProject project = JavaCore.create(checkSupportedProject(getProject()));
       final String projectName = project.getElementName();


        monitor.beginTask("", 100 + 2000 + 50);
        try {
            final Collection<ICompilationUnit[]> batches;
            IProgressMonitor collectionMonitor = new SubProgressMonitor(monitor, 100);

            // Collect all the compilation units that should be analysed.
            if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
                batches = collectCompilationUnits(getDelta(getProject()), collectionMonitor);
            } else if (kind == FULL_BUILD) {
                batches = collectCompilationUnits(project, collectionMonitor);
            } else {
                error("Invalid build kind ({0}) when invoking ''{2}'' on project ''{1}''; exiting...",
                       kind, project.getElementName(), this.getCommand().getBuilderName());
                return null;
            }
            checkCancelled(monitor);

            // Create and configure the AST parser.
            final IProgressMonitor processingMonitor = new SubProgressMonitor(monitor, 2000);

            // Get total number of units to be processed.
            int totalUnits = 0;
            for (ICompilationUnit[] units: batches) {
                totalUnits += units.length;
            }

            processingMonitor.beginTask("", totalUnits);
            processingMonitor.subTask("[JITTAC] Initialising Java IA extraction for '"
                                      + projectName + "' project...");
            try {
                ASTParser parser = ASTParser.newParser(AST.JLS4);
                parser.setResolveBindings(true);
                parser.setProject(project);
                checkCancelled(monitor);
                
                IProgressMonitor dummy = new DummyProgressMonitor(processingMonitor);
                JavaASTHandler handler = new JavaASTHandler(javaIAModel(project), 
                                                            totalUnits, processingMonitor);
                // Do the actual AST processing...
                for (ICompilationUnit[] units: batches) {
                    parser.createASTs(units, new String[0], handler, dummy);
                }
            } finally {
                processingMonitor.done();
            }
            
            monitor.subTask("[JITTAC] Storing IA model of project '" + project.getElementName() + "'");
            defaultModelManager()._storeImplementationModel(getProject());
            monitor.worked(50);

            // TODO: Move into a separate process/task.
            ProjectTracker tracker = ACToolJDT.getDefault().getTracker(getProject());
            if (tracker != null)  {
                tracker.scanForUnmappedResources();
            }
        } finally {
            monitor.done();
        }

        return null;
    }
}
