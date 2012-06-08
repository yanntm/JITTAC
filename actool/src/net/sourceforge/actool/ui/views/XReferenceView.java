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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
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
        viewer = new TableViewer(parent, SWT.SINGLE);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        
        // Create columns in the table.
        String[] columns = {"Source", "Target"};
        for (int i = 0; i < columns.length; ++i) {
            TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
            column.setText(columns[i]);
            column.pack();
        }
        
        viewer.setContentProvider(new XReferenceContentProvider());
        viewer.setLabelProvider(new XReferenceLabelProvider());
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
        }
    }
}

class XReferenceContentProvider implements IStructuredContentProvider {


    public Object[] getElements(Object inputElement) {//not used
        if (inputElement != null && !(inputElement instanceof Connector))
            throw new IllegalArgumentException();
        
//        if (inputElement != null)
//            return ((Connector) inputElement).getXReferences().toArray();
        
        return null;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if ((oldInput != null && !(oldInput instanceof Connector)) || (newInput != null && !(newInput instanceof Connector)))
            throw new IllegalArgumentException();
        // TODO Auto-generated method stub

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

