package net.sourceforge.actool.ui.editor.model;

import java.util.LinkedList;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;



public class ArchitectureEditPartFactory implements EditPartFactory {	
	private static LinkedList<IViolationHighlighter> highlighters = new LinkedList<IViolationHighlighter>();
	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part;
		
		// Select appropriate edit part.
		if (model instanceof ArchitectureModel) {
			part = new ArchitectureModelEditPart();
			highlighters.add((IViolationHighlighter)part);
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
	
	public static void highlightViolation(String id) {
		for(IViolationHighlighter vh : highlighters)vh.highlightViolation(id);
	}
	
}
