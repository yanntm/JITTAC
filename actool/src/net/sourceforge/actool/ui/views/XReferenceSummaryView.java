/**
 * 
 */
package net.sourceforge.actool.ui.views;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;



import net.sourceforge.actool.model.ResourceMapping;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;
import net.sourceforge.actool.model.ia.IXReference;

import org.eclipse.gef.EditPart;
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
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;


/**
 * @author Sean
 *
 */
public class XReferenceSummaryView extends ViewPart implements ISelectionListener {
	private static TreeViewer treeViewer;
	private static Tree tree ;
   

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite arg0) {
		this.tree = new Tree(arg0, PROP_TITLE);
		treeViewer= new TreeViewer(tree);

	    treeViewer.setContentProvider(new MyTreeContentProvider());
	    treeViewer.setLabelProvider(new NodeLabelProvider());

			
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
	    
	    private static void update(Connector data) {
	    	Collection<IXReference> xrefs= data.getXReferences();
	    	HashMap<String,Triplet<IXReference,Integer,Integer>> fqns = new HashMap<String, Triplet<IXReference,Integer,Integer>>();
			for(IXReference xref : xrefs){
				String key = xref.getTarget().toString();
				if(fqns.containsKey(key))fqns.get(key).val++;
				else fqns.put(xref.getTarget().toString(),new Triplet<IXReference,Integer,Integer>(xref,Integer.valueOf(1),Integer.valueOf(countSegments(key,'.'))));
			}
			List<String> methods = new LinkedList<String>();
	    	List<String> feilds = new LinkedList<String>();
	    	List<String> classes = new LinkedList<String>();
	    	Iterator<String> it = fqns.keySet().iterator();
	    	while(it.hasNext()){
				String current = it.next();
				if(current.endsWith(")")) methods.add(current);
				else{
					switch(fqns.get(current).key.getType()){
						case IXReference.IMPORT:classes.add(current); break;
						case IXReference.ACCESS:
						case IXReference.ASSIGNMENT: feilds.add(current); break;
					}
					
				}
	    	}
	    	
	    	int maxSegments = 0;
	    	for(Triplet<IXReference,Integer,Integer> triplet : fqns.values()) maxSegments = Math.max(maxSegments, triplet.segments);
	    	String[][] matrix = new String[fqns.keySet().size()][maxSegments+1];
	    	int index=0;
	    	for(String str : fqns.keySet()) {
	    		matrix[index++] = str.split("\\.",maxSegments+1);
	    	}
	    	index=0;
	    	Vector<Node> nodes = calcTree(matrix,fqns,maxSegments);
	    	treeViewer.setInput(nodes);
	    	
	    
	    }
	    
	    
	    private static Vector<Node> calcTree(String[][] matrix,HashMap<String, Triplet<IXReference, Integer, Integer>> fqns,int maxColoums) {
	    	Vector<Node> result= new Vector<Node>();
	    	String nodeNamePrefix = "";
			int col =0;
			while(col<maxColoums&&coloumIsEqual(matrix,col)){
				if(!nodeNamePrefix.equals(""))nodeNamePrefix+=".";
				nodeNamePrefix+= getFirstColoumValue(matrix, col);
				col++;
			}
			Node category= col==0?new Node("root", null):new Node(nodeNamePrefix, null);
			result.add(category);
			
			Set<String> childern = getColoumValues(matrix, col);
			for(String str : childern){
				Node child= new Node(str, category);
				result.add(child);
				calcTree(getSubMatrix(matrix,str,col,maxColoums),fqns,maxColoums,col,child);
			}
			
			return result;
		}
	    
	    private static void calcTree(String[][] matrix,HashMap<String, Triplet<IXReference, Integer, Integer>> fqns,int maxColoums,int currentCol,Node parent) {
	    	if(!(currentCol<maxColoums))return;
	    	String nodeName = "";
	    	int col =currentCol;
			while(col<maxColoums&&coloumIsEqual(matrix,col)){
				if(!nodeName.equals(""))nodeName+=".";
				nodeName+= getFirstColoumValue(matrix, col);
				col++;
			}
			if(!nodeName.equals("")){
			Node category=new Node(nodeName, parent);
			
//			calcTree(String[][] matrix,HashMap<String, Triplet<IXReference, Integer, Integer>> fqns,int maxColoums,int currentCol,String columValue,Node Parent);
			}
//			
//			
		}
	    
	    private static String[][] getSubMatrix(String[][] matrix, String name,int col,int maxCol) {
	    	String[][] temp= new String[matrix.length][maxCol];
	    	int count =0;
	    	for(int i=0; i<matrix.length; i++){
	    		if(col<matrix[i].length){
	    			if(matrix[i][col].equals(name)) temp[count++]=matrix[i];
	    			break;
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
	    	int result = 0;
	    	for(int i=0; i<str.length();i++) if(str.charAt(i)==delimiter)result++;
	    	return result;
	    }
	    
	    
	    
//	private static void update(Connector data) {
//		Collection<IXReference> xrefs= data.getXReferences();
//		HashMap<String,IXReference> fqns = new HashMap<String, IXReference>();
//		for(IXReference xref : xrefs){
////			fqns.add(xref.getSource().toString());
//			fqns.put(xref.getTarget().toString(),xref);
//		}
//		
//		String longestPackageName ="";
//    	Iterator<String> it=fqns.keySet().iterator();
//    	List<String> methods = new LinkedList<String>();
//    	List<String> feilds = new LinkedList<String>();
//    	List<String> classes = new LinkedList<String>();
//    	outer:
//    	while(it.hasNext()){
////	    	
//			String current = it.next();
//			if(current.endsWith(")")) methods.add(current);
//			else if(fqns.get(current).getType()==IXReference.IMPORT)classes.add(current);
//			else if(fqns.get(current).getType()==IXReference.ACCESS||fqns.get(current).getType()==IXReference.ASSIGNMENT)feilds.add(current);
//			
//			if(longestPackageName.equals(""))	longestPackageName=current;
//			else if(longestPackageName.length()>current.length()){
//				inner:
//				for(int i=0;i<current.length();i++) 
//					if(current.charAt(i)!=longestPackageName.charAt(i)){
//						longestPackageName=current.substring(0,i);
//						if(i==0)break outer;
//						break inner;
//					}
//			}
//				
//		}
//    	
//    	
//	    Vector<Node> nodes = new Vector<Node>();
//		Node category;
//		longestPackageName=longestPackageName.substring(0,longestPackageName.lastIndexOf("."));
//		if(!longestPackageName.equals("")&&longestPackageName.contains("."))category= new Node(longestPackageName, null);
//		else category= new Node("root", null);
//	    nodes.add(category);
//	    
//	    HashMap<String,List<String>> tempMap=getChildPackages(fqns, longestPackageName);
//
//	    category = new Node("a1", category);
//	    new Node("a11", category);
//	    new Node("a12", category);
//
//	    category = new Node("B", null);
//	    nodes.add(category);
//
//	    new Node("b1", category);
//	    new Node("b2", category);
//	    
//	    
//	    treeViewer.setInput(nodes);
//	}

	private static HashMap<String,List<String>> getChildPackages(HashMap<String, IXReference> fqns,
			String longestPackageName) {
		HashMap<String,List<String>> tempMap = new HashMap<String, List<String>>();
	    int segmentIndex = longestPackageName.length()+1;
	    for(String key : fqns.keySet()){
	    	if(key.length()>segmentIndex){
	    		int nextSegEnd = key.indexOf(".", segmentIndex);
	    		if(nextSegEnd>segmentIndex){
	    			String currentPackage = key.substring(0,nextSegEnd);
	    			if(!tempMap.containsKey(currentPackage)) tempMap.put(currentPackage,new LinkedList<String>());
	    			tempMap.get(currentPackage).add(key);
	    			
	    		}
	    	}
	    	
	    }
	    return tempMap;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();

	}
	
	private  TreeItem addNode(TreeItem parentItem, Node node) {
	    TreeItem item = null;
	    if (parentItem == null)
	      item = new TreeItem(tree, SWT.NONE);
	    else
	      item = new TreeItem(parentItem, SWT.NONE);

	    item.setText(node.getName());

	    Vector subs = node.getSubCategories();
	    for (int i = 0; subs != null && i < subs.size(); i++)
	      addNode(item, (Node) subs.elementAt(i));
	    return item; 
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
	    Vector<Node> subcats = ((Node) parentElement).getSubCategories();
	    return subcats == null ? new Object[0] : subcats.toArray();
	  }

	  public Object getParent(Object element) {
	    return ((Node) element).getParent();
	  }

	  public boolean hasChildren(Object element) {
	    return ((Node) element).getSubCategories() != null;
	  }

	  public Object[] getElements(Object inputElement) {
	    if (inputElement != null && inputElement instanceof Vector) {
	      return ((Vector<?>) inputElement).toArray();
	    }
	    return new Object[0];
	  }

	  public void dispose() {
	  }

	  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	  }
	}


class Node {
	  private String name;

	  private Vector<Node> subCategories;

	  private Node parent;

	  public Node(String name, Node parent) {
	    this.name = name;
	    this.parent = parent;
	    if (parent != null)
	      parent.addSubCategory(this);
	  }

	  public Vector getSubCategories() {
	    return subCategories;
	  }

	  private void addSubCategory(Node subcategory) {
	    if (subCategories == null)
	      subCategories = new Vector<Node>();
	    if (!subCategories.contains(subcategory))
	      subCategories.add(subcategory);
	  }

	  public String getName() {
	    return name;
	  }

	  public Node getParent() {
	    return parent;
	  }
}
	
