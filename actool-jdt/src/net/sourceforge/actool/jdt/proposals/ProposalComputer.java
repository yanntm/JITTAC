package net.sourceforge.actool.jdt.proposals;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;

import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;

public class ProposalComputer implements IJavaCompletionProposalComputer {
	 
	private static JavaAllCompletionProposalComputer hpc = new JavaAllCompletionProposalComputer();
//	private static ArcitechtualComparator arcitechtualComparator = new ArcitechtualComparator();
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		if(context instanceof JavaContentAssistInvocationContext) {
		      JavaContentAssistInvocationContext jcontext = (JavaContentAssistInvocationContext) context;
		      ArcitechtualComparator.setContext(jcontext);
		}
		   
		
		
		List<ICompletionProposal> result =hpc.computeCompletionProposals(context, monitor);
//		for(ICompletionProposal current: result){
//			AbstractJavaCompletionProposal pro = (AbstractJavaCompletionProposal)current;
//			
//			StyledString ss = pro.getStyledDisplayString();
//			ss.setStyle(0, ss.getString().length(), new StyledString.Styler() {	
//		    public void applyStyles(TextStyle t) {
//		    	t.foreground =new Color(null, 255, 100, 0);
//		    }
//		});
//			pro.setStyledDisplayString(ss);	
//		}
//		Collections.sort(result,arcitechtualComparator);
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
