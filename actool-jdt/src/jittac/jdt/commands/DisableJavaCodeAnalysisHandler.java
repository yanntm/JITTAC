package jittac.jdt.commands;

import static com.google.common.collect.Lists.newArrayList;
import static net.sourceforge.actool.jdt.ACToolJDT.errorStatus;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.actool.jdt.ACNatureJDT;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

public class DisableJavaCodeAnalysisHandler extends BaseHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Collection<IProject> projects
		        = extractApplicableProjects((EvaluationContext) event.getApplicationContext());
		
        for (IProject project: projects) {
            try {
                if (!project.isOpen() 
                    || !project.hasNature(JavaCore.NATURE_ID)
                    || !project.hasNature(ACNatureJDT.NATURE_ID)) {
                    continue;
                }

                IProjectDescription desc = project.getDescription();
                
                ArrayList<String> natures = newArrayList();
                for (String nature: desc.getNatureIds()) {
                    if (!ACNatureJDT.NATURE_ID.equals(nature)){
                        natures.add(nature);
                    }
                }
                
                desc.setNatureIds(natures.toArray(new String[natures.size()]));
                project.setDescription(desc, null);
            } catch (CoreException e) {
                errorStatus("Error removing JITTAC nature: " + ACNatureJDT.NATURE_ID, e);
            }
        }

        return null;
	}
}
