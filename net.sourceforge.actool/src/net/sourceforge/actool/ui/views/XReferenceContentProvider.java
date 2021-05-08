package net.sourceforge.actool.ui.views;

import net.sourceforge.actool.model.da.Connector;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;



public class XReferenceContentProvider implements IStructuredContentProvider {


    public Object[] getElements(Object inputElement) {//used for arch relations view table data
        if (inputElement != null && !(inputElement instanceof Connector))
            throw new IllegalArgumentException();
        
        if (inputElement != null){
            return ((Connector) inputElement).getXReferences().toArray();
        }
        return new Object[0];
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