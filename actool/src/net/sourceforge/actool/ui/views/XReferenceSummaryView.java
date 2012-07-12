/**
 * 
 */
package net.sourceforge.actool.ui.views;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.swing.RowFilter.ComparisonType;



import net.sourceforge.actool.model.ResourceMapping;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;
import net.sourceforge.actool.model.ia.IElement;
import net.sourceforge.actool.model.ia.IXReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;


/**
 * @author Sean
 *
 */
public class XReferenceSummaryView extends ViewPart implements ISelectionListener {
	private static final String METHOD = "Method:";
	private static final String CLASS = "Class:";
	private static final String FIELD = "Field:";
	private static final String CLASS_PACKAGE = "Class/Package:";
	private static TreeViewer treeViewer;
	private static Tree tree ;
//															fqns= fully qualified names
	final HashMap<String,Triplet<IXReference,Integer,Integer>> targetFqns = new HashMap<String, Triplet<IXReference,Integer,Integer>>();
	final HashMap<String,Triplet<IXReference,Integer,Integer>> sourceFqns = new HashMap<String, Triplet<IXReference,Integer,Integer>>();
	private static final Comparator<Node> nodeComparator = new Comparator<Node>() {
		
		@Override
		public int compare(Node o1, Node o2) {
			
			return o1.getName().substring(o1.getName().indexOf("[")+1,o1.getName().indexOf("]")).compareTo(o2.getName().substring(o2.getName().indexOf("[")+1,o2.getName().indexOf("]")));
		}
	};
   

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite arg0) {
		this.tree = new Tree(arg0, PROP_TITLE);
		treeViewer= new TreeViewer(tree);

	    treeViewer.setContentProvider(new MyTreeContentProvider());
	    treeViewer.setLabelProvider(new NodeLabelProvider());
	    treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            
            public void doubleClick(DoubleClickEvent event) {
           	 if(event==null)return;
           	 Node selected = (Node)((IStructuredSelection)event.getSelection()).getFirstElement();
           	 Stack<String> nodes = new Stack<String>();
           	 Node currentNode = selected;
           	 while(currentNode!=null){
           		 String temp = currentNode.getName();
           		 if(temp.contains(":")&&temp.contains("[")){
           		 nodes.add(currentNode.getName().substring(currentNode.getName().indexOf(":")+1,currentNode.getName().indexOf("[")).trim());
           		 currentNode = currentNode.getParent();
           		 }else break;
           	 }
           	 String str = "";
           	 while(!nodes.isEmpty()) str+=nodes.pop()+".";
           	 if(str.endsWith("."))str=str.substring(0,str.length()-1);
           	 IXReference xref=null;
           	 IResource resource = null;
           	 if(targetFqns.containsKey(str)){
           		 xref= targetFqns.get(str).key;
           		 resource= xref.getTarget().getResource();
           		 if (resource==null||resource.getType() != IResource.FILE)
	                 	return;
	                 
	                 try {
	                     IEditorPart editor = IDE.openEditor(getSite().getPage(), (IFile) resource, true);
	                 } catch (PartInitException e) {
	                     // TODO Auto-generated catch block
	                     e.printStackTrace();
	                 }
           	 }
           	 else if(sourceFqns.containsKey(str)){
           		 xref= sourceFqns.get(str).key;
           		 resource= xref.getSource().getResource();
           		 if(xref==null) return;
	            	
	                 
	                 if (resource==null||resource.getType() != IResource.FILE)
	                 	return;
	                 try {
	                     IEditorPart editor = IDE.openEditor(getSite().getPage(), (IFile) resource, true);
	                 } catch (PartInitException e) {
	                     // TODO Auto-generated catch block
	                     e.printStackTrace();
	                 }
           	 }
           	 
           	 
           	 
            }
        });
			
	    }

	 public void init(IViewSite site) throws PartInitException {
	        super.init(site);
	        getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
	    }

	    public void dispose() {
	        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
	        super.dispose();
	    }
		 static class Triplet<T1,T2,T3>{
			 public T1 key;
			 public T2 val;
			 public T3 segments;
			 public Triplet(T1 key,T2 val,T3 segments){
				 this.key=key;
				 this.val=val;
				 this.segments=segments;
			 }
			 
		 }
	    
	    private  void update(Connector data) {
	    	Collection<IXReference> xrefs= data.getXReferences();
	    	targetFqns.clear();
	    	sourceFqns.clear();
	    	for(IXReference xref : xrefs){
				String key = xref.getTarget().toString().trim();
				if(key.endsWith(".java"))
					key=key.replace(".java", "_java");
				if(targetFqns.containsKey(key))targetFqns.get(key).val++;
				else targetFqns.put(key,new Triplet<IXReference,Integer,Integer>(xref,Integer.valueOf(1),Integer.valueOf(countSegments(key,'.'))));
				key = xref.getSource().toString().trim();
				if(key.endsWith(".java"))
					key=key.replace(".java",  "_java");
				if(sourceFqns.containsKey(key))sourceFqns.get(key).val++;
				else sourceFqns.put(key,new Triplet<IXReference,Integer,Integer>(xref,Integer.valueOf(1),Integer.valueOf(countSegments(key,'.'))));
				
			}
	    	//target
	    	int maxSegments = 0;
	    	for(Triplet<IXReference,Integer,Integer> triplet : targetFqns.values()) maxSegments = Math.max(maxSegments, triplet.segments);
	    	String[][] targetMatrix = new String[targetFqns.keySet().size()][maxSegments];
	    	int index=0;
	    	for(String str : targetFqns.keySet()) {
	    		targetMatrix[index++] = str.split("\\.");
	    	}
	    	
	    	Vector<Node> nodes = new Vector<Node>();
	    	Node target = new Node("Target"+ " ["+targetMatrix.length+"]", null);
	    	calcTree(target,targetMatrix,targetFqns,maxSegments);
//	    	target.sortSubNodes(nodeComparator);
	    	nodes.add(target);
	    	//source
	    	index=0;
	    	maxSegments = 0;
	    	for(Triplet<IXReference,Integer,Integer> triplet : sourceFqns.values()) maxSegments = Math.max(maxSegments, triplet.segments);
	    	String[][] sourceMatrix = new String[sourceFqns.keySet().size()][maxSegments];
	    	for(String str : sourceFqns.keySet()) {
	    		sourceMatrix[index++] = str.split("\\.");
	    	}
	    	Node source = new Node("Source"+ " ["+sourceMatrix.length+"]", null);
	    	calcTree(source,sourceMatrix,sourceFqns,maxSegments);
//	    	source.sortSubNodes(nodeComparator);
	    	nodes.add(source);
	    	treeViewer.setInput(nodes);
	    	treeViewer.expandToLevel(2);
	    }
	    
	    
	    
	    private static void calcTree(Node parent, String[][] matrix,HashMap<String, Triplet<IXReference, Integer, Integer>> fqns,int maxColoums) {
//	    	Vector<Node> result= new Vector<Node>();
	    	String nodeNamePrefix = "";
			int col =0;
			while(col<maxColoums&&coloumIsEqual(matrix,col)){
				if(!nodeNamePrefix.equals("")&&!nodeNamePrefix.endsWith("."))nodeNamePrefix+=".";
				nodeNamePrefix+= getFirstColoumValue(matrix, col);
				col++;
			}
			
	    	String type=getType(nodeNamePrefix,fqns);
			Node category= col==0?parent:new Node(type+" "+nodeNamePrefix+ " ["+matrix.length+"]", parent);
//			result.add(category);
			
			Set<String> childern = getColoumValues(matrix, col);
			for(String str : childern){
				String parentFullname =  getFullname(matrix,col-1);
				type=getType(parentFullname+"."+str,fqns);
				Node child= (type!=FIELD&&type!=METHOD)?new Node(type+" "+str+ " ["+countColoumValue(matrix, col, str)+"]", category):new Node(type+" "+str, category);
//				result.add(child);
				calcTree(getSubMatrix(matrix,str,col,maxColoums),fqns,col+1,child);
			}
			
//			return result;
		}
	    
	    private static void calcTree(String[][] matrix,HashMap<String, Triplet<IXReference, Integer, Integer>> fqns,int currentcol,Node parent) {
	    	int maxColoums = 0;
	    	for(int i=0; i<matrix.length;i++)maxColoums=Math.max(maxColoums, matrix[i].length);
	    	int col = currentcol;
	    	if(!(col<maxColoums||parent==null))return;
	    	String nodeName = "";
//	    	while(col<maxColoums&&coloumIsEqual(matrix,col)){
//				if(!nodeName.equals("")&&!nodeName.endsWith("."))nodeName+=".";
//				nodeName+= getFirstColoumValue(matrix, col);
//				col++;
//			}
	    	String parentFullname="";
	    	String type ="";
			if(!nodeName.equals("")){
				parentFullname =  getFullname(matrix,col-1);
				type=getType(parentFullname+"."+nodeName,fqns);
//				nodeName= type+ " " +nodeName+ " ["+countColoumValue(matrix, col-1, nodeName)+"]";
				Node category=new Node(type+ " " +nodeName+ " ["+countColoumValue(matrix, col-1, nodeName)+"]", parent);
				Set<String> childern = getColoumValues(matrix, col);
				for(String str : childern){
					if(parentFullname.contains(nodeName))type=getType(parentFullname+"."+str,fqns);
					else type=getType(parentFullname+"."+nodeName+"."+str,fqns);
					Node child= (type!=FIELD&&type!=METHOD)?new Node(type+" "+str+ " ["+countColoumValue(matrix, col, str)+"]", category):new Node(type+" "+str, category);
	//				result.add(child);
					calcTree(getSubMatrix(matrix,str,col,maxColoums),fqns,col+1,child);
				}
			}else{
				Set<String> childern = getColoumValues(matrix, col);
				for(String str : childern){
					parentFullname =  getFullname(matrix,col-1);
					type=getType(parentFullname+"."+str,fqns);
					Node child= (type!=FIELD&&type!=METHOD)?new Node(type+" "+str+ " ["+countColoumValue(matrix, col, str)+"]", parent):new Node(type+" "+str, parent);
	//				result.add(child);
					calcTree(getSubMatrix(matrix,str,col,maxColoums),fqns,col+1,child);
				}
			}
//			
//			
		}
	    private static String getFullname(String[][] matrix, int i) {
	    	String result ="";
	    	int index=0;
	    	while(matrix[index].length<=i)index++;
			if(index<matrix.length)for(int x=0;x<i;x++) result+=matrix[index][x]+".";
			if(i>=0&&matrix[index].length>i)result+=matrix[index][i];
			return result;
		}

		private static String getType(String key,HashMap<String, Triplet<IXReference, Integer, Integer>> fqns) {
	    	
	    	
				if(key.endsWith(")")) return METHOD;
				else if(key.endsWith("_java")) return CLASS;
				else if(fqns.containsKey(key)){
					switch(fqns.get(key).key.getType()){
						case IXReference.IMPORT:return CLASS_PACKAGE;
						case IXReference.ASSIGNMENT: return FIELD;
						case IXReference.ACCESS:
						{
							IXReference xref = fqns.get(key).key;
							IElement elm= key.contains(xref.getTarget().getName())?xref.getTarget():xref.getSource();
							String elementName =elm.getName();
							String className = elm.getResource().getName();
							className = className.contains(".")?className.substring(0,className.lastIndexOf(".")):className;
							return elementName.equals(className)? CLASS : FIELD;
							
						}
						default: return "";
						
					}
					
				}
	    	
			return CLASS_PACKAGE;
		}

		private static String getFullname(Node node){
	    	String result = "";
	    	Stack<Node> stack = new Stack<Node>();
	    	Node current = node;
	    	while(current!=null){
	    		stack.push(current);
	    		current=current.getParent();
	    	}
	    	while(!stack.isEmpty())result+= stack.pop().getName()+".";
	    	result= result.endsWith(".")?result: result.substring(0,result.length()-1);
	    	return result;
	    }
	    
	    private static int countColoumValue(String[][] matrix, int col,	String nodeName) {
			// TODO Auto-generated method stub
	    	int count=0;						
	    	for(int i=0; i<matrix.length; i++)if(col<matrix[i].length&&nodeName.equals(matrix[i][col]))count++;
	    	if(hasChildern(matrix, col,	nodeName))for(int i=0; i<matrix.length; i++)if(col+1==matrix[i].length&&nodeName.equals(matrix[i][col]))count--;
			return count;
		}

		private static boolean hasChildern(String[][] matrix, int col,String nodeName) {
			for(int i=0; i<matrix.length; i++)if(col<matrix[i].length&&nodeName.equals(matrix[i][col]))if(col+1<matrix[i].length)return true;
			return false;
		}

		private static String[][] getSubMatrix(String[][] matrix, String name,int col,int maxCol) {
	    	String[][] temp= new String[matrix.length][maxCol];
	    	int count =0;
	    	for(int i=0; i<matrix.length; i++){
	    		if(col<matrix[i].length){
	    			if(matrix[i][col].equals(name)) {
	    				temp[count++]=matrix[i];
	    				
	    			}
		    	}
	    	}
	    	String[][] result=new String[count][maxCol];
	        for(int i=0; i<count;i++)result[i]=temp[i];
	    	temp=null;
	    	return result ;
		}

		private static boolean coloumIsEqual(String[][] matrix, int col){
	    	boolean result= true;
	    	String last = "";
	    	for(int i=0; i<matrix.length; i++){
	    		if(col<matrix[i].length){
	    			if(last.equals("")) last=matrix[i][col];
		    		result&= matrix[i][col].equals(last);
		    		last=matrix[i][col];
	    		}
	    	}
	    	return result;
	    	
	    }
	    
	    private static String getFirstColoumValue(String[][] matrix, int col){
	    	String result= "";
	    	for(int i=0; i<matrix.length; i++){
	    		if(col<matrix[i].length){
	    			result=matrix[i][col];
	    			break;
		    	}
	    	}
	    	return result;
	    }
	    
	    private static Set<String> getColoumValues(String[][] matrix, int col){
	    	Set<String> result = new HashSet<String>();
	    	for(int i=0; i<matrix.length; i++){
	    		if(col<matrix[i].length){
	    			result.add(matrix[i][col]);
		    	}
	    	}
	    	return result;
	    }
	    
		public static int  countSegments(String str, char delimiter){
	    	int result = 1;
	    	for(int i=0; i<str.length();i++) if(str.charAt(i)==delimiter)result++;
	    	return result;
	    }



	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();

	}
	



    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (!(selection instanceof IStructuredSelection))
            return;
        
        Object element = ((IStructuredSelection) selection).getFirstElement();
        if (element instanceof EditPart && ((EditPart) element).getModel() instanceof Connector) {
            update((Connector)((EditPart) element).getModel());
      
            
        }
    }

}

class NodeLabelProvider implements ILabelProvider {
	  public String getText(Object element) {
	    return ((Node) element).getName();
	  }

	  public Image getImage(Object arg0) {
	    return null;
	  }

	  public void addListener(ILabelProviderListener arg0) {
	  }

	  public void dispose() {
	  }

	  public boolean isLabelProperty(Object arg0, String arg1) {
	    return false;
	  }

	  public void removeListener(ILabelProviderListener arg0) {
	  }
	}

class MyTreeContentProvider implements ITreeContentProvider{
	  public Object[] getChildren(Object parentElement) {
	    LinkedList<Node> subcats = ((Node) parentElement).getSubNodes();
	    return subcats == null ? new Object[0] : subcats.toArray();
	  }

	  public Object getParent(Object element) {
	    return ((Node) element).getParent();
	  }

	  public boolean hasChildren(Object element) {
	    return ((Node) element).getSubNodes() != null;
	  }

	  public Object[] getElements(Object inputElement) {
	    if (inputElement != null && inputElement instanceof Vector) {
	      return ((Vector<?>) inputElement).toArray();
	    }
	    return new Object[1];
	  }

	  public void dispose() {
	  }

	  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	  }
	}


class Node {
	  private String name;

	  private LinkedList<Node> subNodes;

	  private Node parent;

	  public Node(String name, Node parent) {
	    this.name = name;
	    this.parent = parent;
	    if (parent != null)
	      parent.addSubNodes(this);
	  }

	  public LinkedList<Node> getSubNodes() {
	    return subNodes;
	  }
	  
	  public void sortSubNodes(Comparator<Node> comparator){
		  Collections.sort(subNodes,comparator);
		  for(Node n :subNodes) n.sortSubNodes(comparator);
	  }

	  private void addSubNodes(Node subcategory) {
	    if (subNodes == null)
	      subNodes = new LinkedList<Node>();
	    if (!subNodes.contains(subcategory))
	      subNodes.add(subcategory);
	  }

	  public String getName() {
	    return name;
	  }

	  public Node getParent() {
	    return parent;
	  }
}
	
