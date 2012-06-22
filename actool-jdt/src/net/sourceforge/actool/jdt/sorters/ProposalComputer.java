package net.sourceforge.actool.jdt.sorters;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.HippieProposalComputer;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.AbstractProposalSorter;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Color;

public class ProposalComputer implements IJavaCompletionProposalComputer {
	 
	private static JavaCompletionProposalComputer hpc = new JavaCompletionProposalComputer();
	private static ArcitechtualComparator arcitechtualComparator = new ArcitechtualComparator();
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		List<ICompletionProposal> result =hpc.computeCompletionProposals(context, monitor);
		context.getViewer().setTextColor(new Color(null, 50, 0, 0));
		
		Collections.sort(result,arcitechtualComparator);
		return result;
	}

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor){
		// TODO Auto-generated method stub
		return hpc.computeContextInformation(context, monitor);
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return hpc.getErrorMessage();
	}

	@Override
	public void sessionEnded() {
		hpc.sessionEnded();

	}

	@Override
	public void sessionStarted() {
		hpc.sessionStarted();

	}

}
