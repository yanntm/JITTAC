package jittac.jdt.contentassist;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class JavaCompletionProposalWrapper
        implements ICompletionProposal, IJavaCompletionProposal, 
                   ICompletionProposalExtension, ICompletionProposalExtension2,
                   ICompletionProposalExtension3, ICompletionProposalExtension4,
                   ICompletionProposalExtension5, ICompletionProposalExtension6 {
    private ICompletionProposal proposal;
    
    public JavaCompletionProposalWrapper(ICompletionProposal proposal) {
        this.proposal = checkNotNull(proposal);
    }
    
    protected boolean instanceOf(Class<?> clazz) {
        return clazz.isInstance(proposal);
    }
    
    protected <T> T as(Class<T> clazz) {
        checkState(clazz.isInstance(proposal),
                   "Completion Proposal does not implement: " + clazz.getName());
        return clazz.cast(proposal);
    }

    @Override
    public int getRelevance() {
        return as(IJavaCompletionProposal.class).getRelevance();
    }

    @Override
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        return as(ICompletionProposalExtension5.class).getAdditionalProposalInfo(monitor);
    }

    @Override
    public boolean isAutoInsertable() {
        return as(ICompletionProposalExtension4.class).isAutoInsertable();
    }

    @Override
    public IInformationControlCreator getInformationControlCreator() {
        return as(ICompletionProposalExtension3.class).getInformationControlCreator();
    }

    @Override
    public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
        return as(ICompletionProposalExtension3.class).getPrefixCompletionText(document, completionOffset);
    }

    @Override
    public int getPrefixCompletionStart(IDocument document, int completionOffset) {
        return as(ICompletionProposalExtension3.class).getPrefixCompletionStart(document, completionOffset);
    }

    @Override
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
        as(ICompletionProposalExtension2.class).apply(viewer, trigger, stateMask, offset);
    }

    @Override
    public void selected(ITextViewer viewer, boolean smartToggle) {
        as(ICompletionProposalExtension2.class).selected(viewer, smartToggle);
    }

    @Override
    public void unselected(ITextViewer viewer) {
        as(ICompletionProposalExtension2.class).unselected(viewer);
    }

    @Override
    public boolean validate(IDocument document, int offset, DocumentEvent event) {
        return as(ICompletionProposalExtension2.class).validate(document, offset, event);
    }

    @Override
    public void apply(IDocument document, char trigger, int offset) {
        as(ICompletionProposalExtension.class).apply(document, trigger, offset);
    }

    @Override
    public boolean isValidFor(IDocument document, int offset) {
        return as(ICompletionProposalExtension.class).isValidFor(document, offset);
    }

    @Override
    public char[] getTriggerCharacters() {
        return as(ICompletionProposalExtension.class).getTriggerCharacters();
    }

    @Override
    public int getContextInformationPosition() {
        return as(ICompletionProposalExtension.class).getContextInformationPosition();
    }

    @Override
    public StyledString getStyledDisplayString() {
        return as(ICompletionProposalExtension6.class).getStyledDisplayString();
    }

    @Override
    public void apply(IDocument document) {
        proposal.apply(document);
    }

    @Override
    public Point getSelection(IDocument document) {
        return proposal.getSelection(document);
    }

    @Override
    public String getAdditionalProposalInfo() {
        return proposal.getAdditionalProposalInfo();
    }

    @Override
    public String getDisplayString() {
        return proposal.getDisplayString();
    }

    @Override
    public Image getImage() {
        return proposal.getImage();
    }

    @Override
    public IContextInformation getContextInformation() {
        return proposal.getContextInformation();
    }

}
