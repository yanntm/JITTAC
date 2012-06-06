package net.sourceforge.actool.ui.editor.dnd;

import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.ui.editor.commands.ComponentCreateAndMapCommand;
import net.sourceforge.actool.ui.editor.commands.ComponentMapCommand;
import net.sourceforge.actool.ui.editor.model.ArchitectureModelEditPart;
import net.sourceforge.actool.ui.editor.model.ComponentEditPart;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.AbstractEditPolicy;



public class MapEditPolicy extends AbstractEditPolicy {
    public static final String MAPPING_ROLE = "mapping";

    @Override
    public Command getCommand(Request request) {
        if (MapElementeRequest.REQ_MAP.equals(request.getType())){
            if (getHost() instanceof ComponentEditPart) {
                return new ComponentMapCommand(((Component) getHost().getModel()),
                        ((MapElementeRequest) request).getResources());
            } else if (getHost() instanceof ArchitectureModelEditPart) {
                return new ComponentCreateAndMapCommand(((ArchitectureModel) getHost().getModel()),
                        ((MapElementeRequest) request).getResources());
            }
        }
        
        return super.getCommand(request);
    }

    @Override
    public EditPart getTargetEditPart(Request request) {
        if (MapElementeRequest.REQ_MAP.equals(request.getType()))
            return getHost();
        return super.getTargetEditPart(request);
    }
}
