package jittac.jdt.commands;

import static java.util.Collections.emptyList;
import static jittac.jdt.util.Projects.extractProjectsWithJavaNature;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.resources.IProject;

abstract class BaseHandler extends AbstractHandler {
    
    /**
     * Extracts projects from selection for on which this action can be applied.
     * 
     * All other objects are ignored.
     * 
     * @param context evaluation context representing the selection
     * @return collection of projects, never {@code null}
     */
    protected Collection<IProject> extractApplicableProjects(EvaluationContext context) {
        Object variable = context.getDefaultVariable();
        if (variable instanceof Collection) {
            return extractProjectsWithJavaNature((Collection<?>) variable);
        }

        return emptyList();
    }
    
	protected void fireHadlerChanged(boolean enabledChanged, boolean handledChanged) {
	    fireHandlerChanged(new HandlerEvent(this, enabledChanged, handledChanged));
	}
}
