package jittac.jdt;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static net.sourceforge.actool.jdt.ACToolJDT.PLUGIN_ID;
import static net.sourceforge.actool.model.ModelManager.defaultModelManager;
import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.WARNING;
import static org.eclipse.jdt.core.JavaCore.isJavaLikeFileName;
import static org.eclipse.ui.statushandlers.StatusManager.getManager;
import net.sourceforge.actool.jdt.model.AbstractJavaModel;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.google.common.base.Predicate;

public class JavaAC {
    public static final String NATURE_ID = JavaACNature.ID;

    /**
     * Check that the given project is a Java project, throw exception otherwise.
     * 
     * @param project to be checked
     * @throws InvalidArgumentException if the project is not opened or is not a Java project
     */
    public static IProject checkSupportedProject(IProject project) throws CoreException {
        checkArgument(project.isOpen(), "Given Project is not open");
        checkArgument(project.hasNature(JavaCore.NATURE_ID),
                      "Given Project is not a Java [JDT] Project");
        return project;
    }

    /**
     * Return a predicate which returns true when the given resource is supported by this plug-in,
     * that is when the resource represents a java compilation unit.
     * 
     * @return the predicate
     */
    public static Predicate<IResource> supportedResource() {
        return new Predicate<IResource>() {
            @Override
            public boolean apply(IResource resource) {
                return isJavaLikeFileName(resource.getName());
            }
        };
    }
    
    public static AbstractJavaModel javaIAModel(IJavaProject project) {
        return (AbstractJavaModel) defaultModelManager().getImplementationModel(project.getProject());
    }

    private static void log(int status, String message, Object... args) {
        getManager().handle(new Status(status, PLUGIN_ID, format(message, args)));
    }

    private static void log(Throwable t, int status, String message, Object... args) {
        getManager().handle(new Status(status, PLUGIN_ID, format(message, args), t));
    }
    
    public static void error(String message, Object... args) {
        log(ERROR, message, args);
    }
    
    public static void error(Throwable t, String message, Object... args) {
        log(t, ERROR, message, args);
    }

    public static void warn(String message, Object... args) {
        log(WARNING, message, args);
    }
    
    public static void warn(Throwable t, String message, Object... args) {
        log(t, WARNING, message, args);
    }
}
