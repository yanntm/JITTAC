package jittac.jdt.contentassist;

import static com.google.common.collect.Lists.newArrayList;
import static jittac.jdt.contentassist.ContentAssistUtils.getControllingModelsComponents;
import static jittac.jdt.contentassist.ContentAssistUtils.getRelationStatus;
import static jittac.jdt.contentassist.ContentAssistUtils.getSourceElement;
import static jittac.jdt.contentassist.ContentAssistUtils.getTargetElement;
import static jittac.jdt.contentassist.RelationStatus.VIOLATION;

import java.util.List;
import java.util.Map;

import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

@SuppressWarnings("restriction")
public class ArchitecturallyConsistentJavaAllCompletionProposalComputer
        extends JavaAllCompletionProposalComputer implements IJavaCompletionProposalComputer {
 
    @Override
    public List<ICompletionProposal> computeCompletionProposals(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        List<ICompletionProposal> proposals =  super.computeCompletionProposals(context, monitor);
        
        IJavaElement sourceElement = getSourceElement(context);
        if (sourceElement != null) {
            Map<ArchitectureModel, Component> sourceComponents 
                    = getControllingModelsComponents(sourceElement);
       

            List<ICompletionProposal> processed = newArrayList();
            for (ICompletionProposal proposal: proposals) {
                IJavaElement targetElement = getTargetElement(proposal);
                if (targetElement != null) {
                    RelationStatus status = getRelationStatus(sourceComponents, targetElement);
                    if (status == VIOLATION) {
                        continue;
                    }
                }
                processed.add(proposal);
            }
            
            return processed;
        }
        
        return proposals;
    }
}
