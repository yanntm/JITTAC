package jittac.jdt.util;

import static com.google.common.collect.Collections2.filter;
import static net.sourceforge.actool.jdt.ACToolJDT.errorStatus;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.JavaCore;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public abstract class Projects {

    public static Predicate<Object> isProjectWithNature(final String nature) {
        return new Predicate<Object>() {

            @Override
            public boolean apply(Object obj) {
                if (obj instanceof IAdaptable) {
                    IProject project = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
                    try {
                        return project.isOpen() && project.hasNature(nature);
                    } catch (CoreException e) {
                        errorStatus("Error checking natures of Project: " + project.getName(), e);
                    }
                }

                return false;
            }
        };
    }

    /**
     * Extracts instances of {@link IProject} instances with the given nature.
     * 
     * All object which do not have the right nature or are not instances of
     * {@link IProject} are silently ignored.
     * 
     * @param collection
     *            or object to be processed
     * @param nature
     *            string representing the ID of the required nature
     * @return collection of projects with the given nature, never {@code null}
     */
    public static Collection<IProject> extractProjectsWithNature(
            Collection<?> collection, String nature) {

        return Collections2.transform(
                filter(collection, isProjectWithNature(nature)),
                new Function<Object, IProject>() {
                    @Override
                    public IProject apply(Object obj) {
                        return (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
                    }
                });
    }

    /**
     * Extracts instances of {@link IProject} instances with the Java nature.
     * 
     * All object which do not have the Java nature or are not instances of
     * {@link IProject} are silently ignored.
     * 
     * @param collection
     *            or object to be processed
     * @return collection of projects with java nature, never {@code null}
     */
    public static Collection<IProject> extractProjectsWithJavaNature(
            Collection<?> collection) {
        return extractProjectsWithNature(collection, JavaCore.NATURE_ID);
    }
}
