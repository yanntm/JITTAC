package net.sourceforge.actool.jdt.proposals;



import org.eclipse.jdt.ui.text.java.AbstractProposalSorter;
import org.eclipse.jface.text.contentassist.ICompletionProposal;


public class ArcitechtualConsistencySorter extends AbstractProposalSorter {
	private static ArcitechtualComparator arcitechtualComparator = new ArcitechtualComparator();
	@Override
	public int compare(ICompletionProposal arg0, ICompletionProposal arg1) {
		return arcitechtualComparator.compare(arg0, arg1);
		
	}
	
	
	

	

}
