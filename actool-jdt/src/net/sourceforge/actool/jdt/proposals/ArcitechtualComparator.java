package net.sourceforge.actool.jdt.proposals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
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

@SuppressWarnings("restriction")
public class ArcitechtualComparator implements Comparator<ICompletionProposal>{
	
	private static RelevanceSorter rSort= new RelevanceSorter();
	
	private static AlphabeticSorter alphaSorter = new AlphabeticSorter();
	private static JavaContentAssistInvocationContext context= null;
	private static Component parentComp =null;
//	private static LinkedList<String> projectNameSpaces = new LinkedList<String>();
	
	@Override
	public int compare(ICompletionProposal arg0, ICompletionProposal arg1) {
		int vc0, vc1;
		vc0=vc1=0;
		if(arg0 instanceof AbstractJavaCompletionProposal && arg1 instanceof AbstractJavaCompletionProposal){
//		Map<ICompletionProposal,Boolean> isInProject = new HashMap<ICompletionProposal, Boolean>();
//		vc0=(violationChecker(arg0,isInProject));
//		vc1=(violationChecker(arg1,isInProject));
//		colourProposal(arg0,vc0,isInProject);
//		colourProposal(arg1,vc1,isInProject);
			vc0=(violationChecker(arg0));
			vc1=(violationChecker(arg1));
			colourProposal(arg0,vc0);
			colourProposal(arg1,vc1);
		}
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

//	private void colourProposal(ICompletionProposal pro,int validationState, Map<ICompletionProposal, Boolean> isInProject ){
	private void colourProposal(ICompletionProposal pro,int validationState){
		AbstractJavaCompletionProposal current = (AbstractJavaCompletionProposal) pro;
		StyledString ss = current.getStyledDisplayString();
		StyledString.Styler styler;
//		if(isInProject.get(pro)){
		
		switch(validationState){
		case -1:
			styler = new StyledString.Styler() {	
				
			    public void applyStyles(TextStyle t) {
			    	t.background =new Color(null, 250, 250, 250);
			    	t.foreground =new Color(null, 0, 175, 0);
			    }
			};
			break;
		case 1:
			styler = new StyledString.Styler() {	
			
		    public void applyStyles(TextStyle t) {
		    		t.background =new Color(null, 250, 250, 250);
		    	   	t.foreground =new Color(null, 200, 0, 0);
		    }
		};break;
		default:
		case 0:
			styler = new StyledString.Styler() {	
				public void applyStyles(TextStyle t) {
					t.background =new Color(null, 250, 250, 250);
		    	   	t.foreground =new Color(null, 200, 160, 0);
				}
			};	break;
		
		}
//		}else {
//			styler = new StyledString.Styler() {	
//			
//				public void applyStyles(TextStyle t) {
//					t.background =new Color(null, 250, 250, 250);
//		    	   	t.foreground =new Color(null, 0,0, 0);
//				}
//			};
//		}
		ss.setStyle(0, ss.getString().length(), styler);
		
	}
	
	private int violationChecker(ICompletionProposal pro){
//		private int violationChecker(ICompletionProposal pro, Map<ICompletionProposal, Boolean> isInProject){
		// return  -1 mapped non violation 0 unmapped 1 violation
//		isInProject.put(pro,false);
		if(parentComp == null ) {
			update() ;
		}
		if(parentComp != null ) {
			AbstractJavaCompletionProposal current = (AbstractJavaCompletionProposal) pro;
			String qualifiedProposalName ="";
			if(current.getSortString().startsWith("class :")){
				qualifiedProposalName= current.getSortString();
				qualifiedProposalName= qualifiedProposalName.substring(qualifiedProposalName.indexOf("<")+1,qualifiedProposalName.lastIndexOf(">"));
			}
			else if(current.getDisplayString().equalsIgnoreCase("this")){
				return 0;
				
			}
			else{
				try {// hack to get fully qualified name of the proposal via reflection. java and eclipse specific
					Method protectedMethod = AbstractJavaCompletionProposal.class.getDeclaredMethod("getProposalInfo", null);
					protectedMethod.setAccessible(true);
					MemberProposalInfo returnValue = (MemberProposalInfo) protectedMethod.invoke(current, null);
					if(returnValue!=null){
						Field privateField = MemberProposalInfo.class.getDeclaredField("fProposal");
						privateField.setAccessible(true);
						InternalCompletionProposal compPro = (InternalCompletionProposal) privateField.get(returnValue);
						Object temp=null;
						privateField = InternalCompletionProposal.class.getDeclaredField("completionKind");
						privateField.setAccessible(true);
						temp=privateField.get(compPro);
						if(current.getReplacementString().length()!=0){
							privateField = InternalCompletionProposal.class.getDeclaredField("declarationPackageName");
							privateField.setAccessible(true);
							temp =privateField.get(compPro);
							if(temp!=null&&temp instanceof char[] )
								qualifiedProposalName +=new String((char[])temp)+".";
							privateField = InternalCompletionProposal.class.getDeclaredField("declarationTypeName");
							privateField.setAccessible(true);
							temp =privateField.get(compPro);
							if(temp!=null&&temp instanceof char[] )
								qualifiedProposalName +=new String((char[])temp)+".";
							if(qualifiedProposalName.equals("")){
								privateField = InternalCompletionProposal.class.getDeclaredField("declarationSignature");
								privateField.setAccessible(true);
								temp =privateField.get(compPro);
								if(temp!=null&&temp instanceof char[] ){
									qualifiedProposalName +=new String((char[])temp);
									if(new String(compPro.getCompletion()).contains(new String((char[])temp))) 
										 qualifiedProposalName="";
									else qualifiedProposalName+=".";
											
										
										
								}
							}
							qualifiedProposalName +=new String(compPro.getCompletion());
							if(qualifiedProposalName.lastIndexOf(".")==qualifiedProposalName.length()-1) 
								qualifiedProposalName= qualifiedProposalName.substring(0, qualifiedProposalName.length()-1);
							
						}
					}else{
						qualifiedProposalName= current.getDisplayString();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}    
			}	
				
//			for(String str : projectNameSpaces)if(qualifiedProposalName.startsWith(str)){isInProject.put(pro,true);break;}
			Component proposedComponent = ArchitectureModel.getComponentByFQN(qualifiedProposalName);		
			if(proposedComponent == null) 
				return 0;
			if(parentComp.equals(proposedComponent)) return -1;
			List<Connector> conns = parentComp.getSourceConnectors();
			for(Connector c : conns) if(c.isEnvisaged()&&c.getTarget().equals(proposedComponent))return -1;
			return 1;
		}
		else return 0;
	}

	

	public static void setContext(JavaContentAssistInvocationContext jcontext) {
		context =jcontext;
		update();
	}

	private static void update()  {
//		projectNameSpaces.clear();
//		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
//		for(IProject p :projects) projectNameSpaces.add(ResourceMapping.getNamspaceFromProjectPath(p.getLocation()));
		if(context!=null){
			try {
			ICompilationUnit cunit =context.getCompilationUnit();
	    	int offset = context.getInvocationOffset();
			IJavaElement element= cunit.getElementAt(offset);
			parentComp = ArchitectureModel.getComponentByIJavaElement(element);
			} catch (JavaModelException e) {
				Logger.getAnonymousLogger().warning(e.getMessage());
			}
		}
		
		
	}
}