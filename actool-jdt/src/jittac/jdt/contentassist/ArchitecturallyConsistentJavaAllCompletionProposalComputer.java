package jittac.jdt.contentassist;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

@SuppressWarnings("restriction")
public class ArchitecturallyConsistentJavaAllCompletionProposalComputer
        extends JavaAllCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private IJavaElement extractJavaElement(ICompletionProposal proposal) {
        if (proposal instanceof AbstractJavaCompletionProposal) {
            return ((AbstractJavaCompletionProposal) proposal).getJavaElement();
        }
        
        return null;
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        List<ICompletionProposal> proposals =  super.computeCompletionProposals(context, monitor);
        
        Iterator<ICompletionProposal> iter = proposals.iterator();
        while (iter.hasNext()) {
            ICompletionProposal proposal = iter.next();
            IJavaElement target = extractJavaElement(proposal);
            if (target == null) {
                continue;
            }
        }
        
        return proposals;
    }
}
