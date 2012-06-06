package net.sourceforge.actool.ui.editor.model;

import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;



public class ArchitectureEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part;
		
		// Select appropriate edit part.
		if (model instanceof ArchitectureModel) {
			part = new ArchitectureModelEditPart();
		} else if (model instanceof Component) {
			part = new ComponentEditPart();
		} else if (model instanceof Connector) {
			part = new ConnectorEditPart();
		} else
			return null;
		
		// Associate part with the model.
		part.setModel(model);
		return part;
	}
}
