package jittac.jdt.contentassist;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static jittac.jdt.contentassist.ContentAssistUtils.envisagedStyler;
import static jittac.jdt.contentassist.ContentAssistUtils.getControllingModelsComponents;
import static jittac.jdt.contentassist.ContentAssistUtils.getRelationStatus;
import static jittac.jdt.contentassist.ContentAssistUtils.getSourceElement;
import static jittac.jdt.contentassist.ContentAssistUtils.getTargetElement;
import static jittac.jdt.contentassist.ContentAssistUtils.unknownStyler;
import static jittac.jdt.contentassist.ContentAssistUtils.violationStyler;
import static jittac.jdt.contentassist.RelationStatus.UNKNOWN;

import java.util.Comparator;
import java.util.Map;

import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.text.java.AbstractProposalSorter;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.viewers.StyledString;

public abstract class AbstractConsistencyProposalSorter extends AbstractProposalSorter {
    private final Comparator<ICompletionProposal> proposalComparator;
    
    private boolean applyStyles = true;
    
    private Map<ICompletionProposal, RelationStatus> statusCache = newHashMap();
    private Map<ArchitectureModel, Component> sourceComponents = newHashMap();
    private IJavaElement sourceElement;

    public AbstractConsistencyProposalSorter(Comparator<ICompletionProposal> comparator) {
        this.proposalComparator = checkNotNull(comparator);
    }
 
    @Override
    public void beginSorting(ContentAssistInvocationContext context) {
        super.beginSorting(context);

        sourceElement = getSourceElement(context);
        if (sourceElement != null) {
            sourceComponents.putAll(getControllingModelsComponents(sourceElement));
        }
    }

    @Override
    public void endSorting() {
        super.endSorting();
        
        statusCache.clear();
        sourceElement = null;
        sourceComponents.clear();
    }
    
    protected void styleProposal(ICompletionProposal proposal, RelationStatus status) {
        if (!(proposal instanceof ICompletionProposalExtension6)) {
            return;
        }
        
        StyledString string = ((ICompletionProposalExtension6) proposal).getStyledDisplayString();
        switch (status) {
        case UNKNOWN:
            string.setStyle(0, string.length(), unknownStyler());
            break;
        case ENVISAGED:
            string.setStyle(0, string.length(), envisagedStyler());
            break;
        case VIOLATION:
            string.setStyle(0, string.length(), violationStyler());
            break;
        }
    }
    
    protected RelationStatus computeRelationStatus(ICompletionProposal proposal) {
        RelationStatus status = statusCache.get(proposal);
        if (status == null) {
            IJavaElement target = getTargetElement(proposal);
            if (target != null) {
                status = getRelationStatus(sourceComponents, target);
            } else {
                status = UNKNOWN;
            }
            
            statusCache.put(proposal, status);
            if (applyStyles) {
                styleProposal(proposal, status);
            }
        }
        
        return status;
    }

    @Override
    public int compare(ICompletionProposal p1, ICompletionProposal p2) {
        int status = computeRelationStatus(p1).compareTo(computeRelationStatus(p2));
        if (status == 0) {
            return proposalComparator.compare(p1, p2);
        }

        return status;
    }
}
