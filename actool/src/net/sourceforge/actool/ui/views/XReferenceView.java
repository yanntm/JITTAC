package net.sourceforge.actool.ui.views;


import net.sourceforge.actool.model.da.Connector;
import net.sourceforge.actool.model.ia.IXReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;



public class XReferenceView extends ViewPart
                            implements ISelectionListener {
    
    public static final String ID = "net.sourceforge.actool.ui.views.XReferenceView";
    private int[] sortdirection = new int[]{1,1};
    private int sortColumn =1;
    private TableViewer viewer;
    
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
    }

    public void dispose() {
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
        super.dispose();
    }

    public void createPartControl(Composite parent) {
        
        // Create the table control
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        viewer.setSorter(new ViewerSorter(){
        	@Override
        	public void sort(Viewer viewer, Object[] elements) {
        		// TODO change to sort by frequency then alfa.
        		super.sort(viewer, elements);
        	}
        	
        	@Override
        	public int compare(Viewer viewer, Object e1, Object e2) {
        		int[] result = new int[2];
        		result[0] = ((IXReference) e1).getSource().toString().compareTo(((IXReference) e2).getSource().toString())*sortdirection[0];
        		result[1] = ((IXReference) e1).getTarget().toString().compareTo(((IXReference) e2).getTarget().toString())*sortdirection[1];
//        		sortColumn =result[1]!=0?1:0;
        		return result[sortColumn];
        	}
        });
        
        
        // Create columns in the table.
        String[] columns = {"Source", "Target"};
       
        for (int i = 0; i < columns.length; ++i) {
            TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
            
            column.setText(columns[i]);
            column.addSelectionListener(getSelectionAdapter(column, i));
            column.setResizable(true);
    		column.setMoveable(true);
            column.pack();
          
        }
        viewer.getTable().setSortColumn(viewer.getTable().getColumn(1));
        viewer.setContentProvider(new XReferenceContentProvider());
        viewer.setLabelProvider(new XReferenceLabelProvider());
        int width = viewer.getTable().getSize().x;
        viewer.getTable().getColumn(0).setWidth(width / 2);
        viewer.getTable().getColumn(1).setWidth(width / 2);
        
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            
            public void doubleClick(DoubleClickEvent event) {
                IXReference xref = (IXReference) ((IStructuredSelection)event.getSelection()).getFirstElement();
                IResource resource = xref.getSource().getResource();
                if (resource.getType() != IResource.FILE)
                	return;
                
                try {
                    IEditorPart editor = IDE.openEditor(getSite().getPage(), (IFile) resource, true);
                    ((ITextEditor) editor).selectAndReveal(xref.getOffset(), xref.getLength());
                } catch (PartInitException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
   
    private SelectionAdapter getSelectionAdapter(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			boolean flip = false;
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.getTable().setSortDirection((sortdirection[index]*=-1)==1?SWT.UP:SWT.DOWN);
				viewer.getTable().setSortColumn(column);
				sortColumn=index;
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

    public void setFocus() {
        viewer.getControl().setFocus();
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (!(selection instanceof IStructuredSelection))
            return;
        
        Object element = ((IStructuredSelection) selection).getFirstElement();
        if (element instanceof EditPart && ((EditPart) element).getModel() instanceof Connector) {
            viewer.setInput(((EditPart) element).getModel());
            
            // Set the column size to be half of the control size.
            int width = viewer.getTable().getSize().x;
            viewer.getTable().getColumn(0).setWidth(width / 2);
            viewer.getTable().getColumn(1).setWidth(width / 2);
            
        }
    }
}



class XReferenceLabelProvider extends LabelProvider
                              implements ITableLabelProvider {

    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
    	IXReference xref = (IXReference) element;
    	
    	switch (columnIndex) {
        case 0:
            return xref.getSource().toString();
        case 1:
            return xref.getTarget().toString();
        }

        return null;
    }    
}

