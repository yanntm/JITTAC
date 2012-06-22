package net.sourceforge.actool.jdt.sorters;

import java.util.Comparator;

import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.AlphabeticSorter;
import org.eclipse.jdt.internal.ui.text.java.RelevanceSorter;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;

public class ArcitechtualComparator implements Comparator<ICompletionProposal>{
	private static RelevanceSorter rSort= new RelevanceSorter();
	private static AlphabeticSorter alphaSorter = new AlphabeticSorter();
		
	@Override
	public int compare(ICompletionProposal arg0, ICompletionProposal arg1) {
		int vc0, vc1;
		vc0=(violationChecker(arg0));
		vc1=(violationChecker(arg1));
		if(vc0<vc1)return -1;
		else if(vc0>vc1)return 1;
		else{
			int relevance = rSort.compare(arg0, arg1);
			if(relevance==0){
				return alphaSorter.compare(arg0, arg1);
			}
			else return relevance;
		}
	
	
	}

	private int violationChecker(ICompletionProposal pro){
		pro.getDisplayString();
		AbstractJavaCompletionProposal current = (AbstractJavaCompletionProposal) pro;
		
		StyledString ss = current.getStyledDisplayString();
		ss.setStyle(0, ss.getString().length(), new StyledString.Styler() {
		    public void applyStyles(TextStyle t) {
		    	t.foreground =new Color(null, 50, 0, 0);
		    }
		});
		current.setStyledDisplayString(ss);
		
		//String xref = (new JavaXReference(current.getJavaElement().getElementType(), null, current.getJavaElement(), 0, current.getReplacementOffset(), current.getReplacementLength())).toString();
		
		// -1 mapped non violation 0 unmapped 1 violation
	//	String xref = (new JavaXReference(type, source, target, line, offset, length)).toString();
	//	model.addXReference(type, source, target,
	//			((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition()),
	//			node.getStartPosition(), node.getLength());
		return 0;
	}
}