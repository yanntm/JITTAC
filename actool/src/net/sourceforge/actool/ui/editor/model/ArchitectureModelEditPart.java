/**
 * 
 */
package net.sourceforge.actool.ui.editor.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.ui.editor.commands.ComponentCreateCommand;
import net.sourceforge.actool.ui.editor.commands.ComponentSetConstraintCommand;
import net.sourceforge.actool.ui.editor.dnd.MapEditPolicy;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FanRouter;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;



/**
 * @author jrosik
 *
 */
public class ArchitectureModelEditPart extends AbstractGraphicalEditPart
                                       implements PropertyChangeListener, IViolationHighlighter {
    PropertyChangeDelegate delegate;
    
    public void activate() {
        if (isActive())
            return;
        
        super.activate();       
        delegate = new PropertyChangeDelegate(this);
        getCastedModel().addPropertyChangeListener(delegate);
    }
    
    public void deactivate() {
        if (!isActive())
            return;
        
        getCastedModel().removePropertyChangeListener(delegate);
        delegate = null;        
        super.deactivate();
    }
    
    
    public ArchitectureModel getCastedModel() {
        return (ArchitectureModel) getModel();
    }
    
    /*
	 */
	protected IFigure createFigure() {
		Figure figure = new FreeformLayer();
		figure.setBorder(new MarginBorder(4));
		figure.setLayoutManager(new FreeformLayout());

		ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
		
		// Connections should be under the nodes (z-order).
		LayeredPane pane = (LayeredPane) connLayer.getParent();
		pane.removeLayer(LayerConstants.CONNECTION_LAYER);
		pane.addLayerBefore(connLayer, LayerConstants.CONNECTION_LAYER, LayerConstants.PRIMARY_LAYER);
		
		// Create the static router for the connection layer		
		connLayer.setConnectionRouter(new FanRouter());
		((FanRouter) connLayer.getConnectionRouter()).setSeparation(16);
		
		return figure;
	}

	/* 
	 */
	protected void createEditPolicies() {
		// The root element cannot be removed nor selected.
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
		
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ModelLayoutEditPolicy());
		
        installEditPolicy(MapEditPolicy.MAPPING_ROLE, new MapEditPolicy());
	}
	
	
	protected List<Component> getModelChildren() {
		return getCastedModel().getComponents();
	}

	
	@SuppressWarnings("unchecked")
	protected void refreshChildren() {
		super.refreshChildren();
		
		// Reorder the children so that the faded ones are at the beginning.
		Collection<ComponentEditPart> parts = new ArrayList<ComponentEditPart>(getChildren());
		for (ComponentEditPart part: parts) {
			if (part.getVisibility() != Visibility.VISIBLE)
				reorderChild(part, 0);
		}
	}
	
	
	public void propertyChange(PropertyChangeEvent event) {

	    if (event.getPropertyName() == ArchitectureModel.COMPONENTS) {
	        refreshChildren();
	    } else
	        return;
	}
	/* (non-Javadoc)
	 * @see net.sourceforge.actool.ui.editor.model.IViolationHighlighter#highlightViolation(java.lang.String)
	 */
	@Override
	public void highlightViolation(String id) {
//		List<ComponentEditPart> temp =this.getSource().getParent().getChildren();
		List<ComponentEditPart> temp =this.getChildren();
		ConnectorEditPart connector=null;
		outer:
		for(ComponentEditPart componentParts :temp){
			for(ConnectorEditPart con :(List<ConnectorEditPart>)componentParts.getSourceConnections()){
				if(con.toString().contains(id)){
					connector=con;
					break outer;
				}
			}
//			for(ConnectorEditPart con :(List<ConnectorEditPart>)componentParts.getTargetConnections()){
//				if(con.toString().equals(id))
//				break outer;
//			}
		}
		
		if(connector!=null){
			ConnectorEditPart.setOldFigure(connector.getFigure());
			connector.getFigure().setForegroundColor(ColorConstants.red);
		}
	} 
}


class ModelLayoutEditPolicy extends XYLayoutEditPolicy {
	
	protected Command getCreateCommand(CreateRequest request) {
	    //Rectangle bounds = (Rectangle) getConstraintFor(request);
	    // TODO: Pass the bounds to the EditPart.
		return new ComponentCreateCommand(((ArchitectureModelEditPart) getHost()).getCastedModel());
	}

	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		if (child instanceof ComponentEditPart && constraint instanceof Rectangle) {
			return new ComponentSetConstraintCommand(((ComponentEditPart) child), (Rectangle) constraint);
		}
		return null;
	}
	
	protected EditPolicy createChildEditPolicy(EditPart child) {
		return new NonResizableEditPolicy();
	}
}