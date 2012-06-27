package net.sourceforge.actool.jdt.sorters;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.actool.jdt.model.JavaXReference;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.AlphabeticSorter;
import org.eclipse.jdt.internal.ui.text.java.MemberProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.RelevanceSorter;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.jdt.core.CompletionProposal;


public class ArcitechtualComparator implements Comparator<ICompletionProposal>{
	private static RelevanceSorter rSort= new RelevanceSorter();
	private static AlphabeticSorter alphaSorter = new AlphabeticSorter();
	private  JavaContentAssistInvocationContext context= null;
	private  Component parentComp =null;
	@Override
	public int compare(ICompletionProposal arg0, ICompletionProposal arg1) {
		int vc0, vc1;
		vc0=(violationChecker(arg0));
		vc1=(violationChecker(arg1));
		if(vc0<vc1)
			return -1;
		else if(vc0>vc1)
			return 1;
		else{
			int relevance = rSort.compare(arg0, arg1);
			if(relevance==0){
				return alphaSorter.compare(arg0, arg1);
			}
			else return relevance;
		}
	
	
	}

	private int violationChecker(ICompletionProposal pro){
		// -1 mapped non violation 0 unmapped 1 violation
		if(this.parentComp == null ) {
			update() ;
		}
		if(this.parentComp != null ) {
			AbstractJavaCompletionProposal current = (AbstractJavaCompletionProposal) pro;
			String qualifiedProposalName ="";
			try {// hack to get fully qualified name of the proposal via reflection. java and eclipse specific
			    Method protectedMethod = AbstractJavaCompletionProposal.class.getDeclaredMethod("getProposalInfo", null);
				protectedMethod.setAccessible(true);
				MemberProposalInfo returnValue = (MemberProposalInfo) protectedMethod.invoke(current, null);
				Field privateField = MemberProposalInfo.class.getDeclaredField("fProposal");
				privateField.setAccessible(true);
				InternalCompletionProposal compPro = (InternalCompletionProposal) privateField.get(returnValue);
				privateField = InternalCompletionProposal.class.getDeclaredField("declarationPackageName");
				privateField.setAccessible(true);
				qualifiedProposalName +=new String((char[])privateField.get(compPro));
				privateField = InternalCompletionProposal.class.getDeclaredField("declarationTypeName");
				privateField.setAccessible(true);
				qualifiedProposalName +="."+new String((char[])privateField.get(compPro));
				qualifiedProposalName +="."+new String(compPro.getCompletion());
			} catch (Exception e) {
				e.printStackTrace();
			}    
			Component proposedComponent = ArchitectureModel.getComponentByFQN(qualifiedProposalName);		
			StyledString ss = current.getStyledDisplayString();
			ss.setStyle(0, ss.getString().length(), new StyledString.Styler() {
			    public void applyStyles(TextStyle t) {
			    	t.foreground =new Color(null, 50, 0, 0);
			    }
			});
			current.setStyledDisplayString(ss);
			if(proposedComponent == null) return 0;
			if(this.parentComp.equals(proposedComponent)) return -1;
			List<Connector> conns = parentComp.getSourceConnectors();
			for(Connector c : conns) if(c.isEnvisaged()&&c.getTarget().equals(proposedComponent))return -1;
			return 1;
		}
		else return 0;
	}

	

	public void setContext(JavaContentAssistInvocationContext jcontext) {
		this.context =jcontext;
		update();
	}

	private void update()  {
		if(context!=null){
			try {
			ICompilationUnit cunit =context.getCompilationUnit();
	    	int offset = context.getInvocationOffset();
			IJavaElement element= cunit.getElementAt(offset);
			parentComp = ArchitectureModel.getComponentByIJavaElement(element);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
}