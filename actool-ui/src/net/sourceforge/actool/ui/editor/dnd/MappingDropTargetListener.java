package net.sourceforge.actool.ui.editor.dnd;

import net.sourceforge.actool.ui.editor.model.ArchitectureModelEditPart;
import net.sourceforge.actool.ui.editor.model.ComponentEditPart;

import org.eclipse.core.resources.IResource;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.ui.part.ResourceTransfer;



public class MappingDropTargetListener extends AbstractTransferDropTargetListener {

    public MappingDropTargetListener(EditPartViewer viewer) {
        super(viewer, ResourceTransfer.getInstance());
    }
    
    protected Request createTargetRequest() {
        return new MapElementeRequest();
    }
  
    protected void updateTargetEditPart() {
        super.updateTargetEditPart();

        EditPart part = getTargetEditPart();
        if (part instanceof ComponentEditPart) 
            getCurrentEvent().detail = DND.DROP_LINK;
        if (part instanceof ArchitectureModelEditPart)
            getCurrentEvent().detail = DND.DROP_COPY;  
    }
      
    protected void updateTargetRequest() {
        DropTargetEvent event = getCurrentEvent();
        if (event.data != null) {
            ((MapElementeRequest) getTargetRequest())
                .setResources((IResource[]) event.data);
            ((MapElementeRequest) getTargetRequest())
                .setLocation(getDropLocation());
        }
    }
}
