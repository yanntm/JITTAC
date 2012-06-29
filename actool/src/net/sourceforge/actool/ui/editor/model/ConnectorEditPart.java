package net.sourceforge.actool.ui.editor.model;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.actool.model.da.Connector;
import net.sourceforge.actool.ui.editor.commands.ConnectorDeleteCommand;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;




public class ConnectorEditPart extends AbstractConnectionEditPart
							   implements PropertyChangeListener,
							   			  Visibility {

	PropertyChangeDelegate delegate;
	private static IFigure oldFigure = null;
	private boolean selected = false;
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

	
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new ConnectionEditPolicy() {
			protected Command getDeleteCommand(GroupRequest request) {
				return new ConnectorDeleteCommand(getCastedModel());
			}
		});
	}


	protected IFigure createFigure() {
		Connector conn = getCastedModel();
		assert conn != null;
		
		PolylineConnection polyline = (PolylineConnection) super.createFigure();

        PolygonDecoration arrow = new PolygonDecoration();
        arrow.setScale(16.0, 4.0);
		polyline.setTargetDecoration(arrow);

		String tooltip = conn.getSource().getName()
						 + " -> " + conn.getTarget().getName();
		polyline.setToolTip(new Label(tooltip));
		
		int nrel = conn.getNumXReferences();
		Label label = new Label(Integer.toString(nrel));
		label.setOpaque(true);
		polyline.add(label, new ConnectionLocator(polyline, ConnectionLocator.MIDDLE));
		
	    updateConnectionType(polyline);
	    try {
			polyline.setAntialias(1);
			arrow.setAntialias(1);
	  	} catch (NoSuchMethodError ex) {
			// Handle GEF 3.4
		}
			
		return polyline;
	}
	
	protected Label getLabelFigure() {
	    return (Label) getFigure().getChildren().get(1);
	}

	private void updateConnectionType(PolylineConnection figure) {

	    switch (getCastedModel().getState()) {
	    case Connector.ABSENT:
	    	try {
		        figure.setLineStyle(SWT.LINE_CUSTOM);
		        figure.setLineDash(new float[] {2.0f, 5.0f});
	    	} catch (NoSuchMethodError ex) {
	    		// Handle GEF 3.4
	    		figure.setLineStyle(SWT.LINE_DOT);
	    	}
	        break;
	    case Connector.CONVERGENT:
	        figure.setLineStyle(SWT.LINE_SOLID);
	        break;
	    case Connector.DIVERGENT:
	    	try {
		        figure.setLineStyle(SWT.LINE_CUSTOM);
		        figure.setLineDash(new float[] {10.0f, 5.0f});
	    	} catch (NoSuchMethodError ex) {
	    		// Handle GEF 3.4
	    		figure.setLineStyle(SWT.LINE_DASH);
	    	}
	        break;
	    }
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		
		if (event.getPropertyName() == Connector.ENVISAGED) {
		    updateConnectionType(getCastedFigure());
		} else if (event.getPropertyName() == Connector.XREFERENCES) {
            // Update the number on the connection and also check the type.
            getLabelFigure().setText(Integer.toString(getCastedModel().getNumXReferences()));
            updateConnectionType(getCastedFigure());		    
		}else
			return;
		
        refreshVisuals();
	}
	


	public PolylineConnection getCastedFigure() {
	    return (PolylineConnection) getFigure();
	}
	
	public Connector getCastedModel() {
		return (Connector) getModel();
	}
	
	protected void refreshVisuals() {
		super.refreshVisuals();

		switch (getVisibility()) {
		case INVISIBLE:
			getFigure().setVisible(false);
			break;

		case VISIBLE:
			figure.setForegroundColor(ColorConstants.listForeground);
			figure.setVisible(true);
			break;

		case FADED:
			figure.setForegroundColor(FigureUtilities.mixColors(ColorConstants.listForeground, ColorConstants.listBackground , 0.1));
			figure.setVisible(true);
			break;
		}
	}

	
	public int getVisibility() {
		Visibility source = (Visibility) getSource();
		Visibility target = (Visibility) getTarget();
		
		if ((source == null || source.getVisibility() == INVISIBLE)
		    || (target == null || target.getVisibility() == INVISIBLE))
			return INVISIBLE;
		else if (source.getVisibility() == FADED || target.getVisibility() == FADED)
			return FADED;
		else
			return VISIBLE;
		
	}
	
	@Override
	protected void fireSelectionChanged() {
		super.fireSelectionChanged();
		if(oldFigure!=null) oldFigure.setForegroundColor(ColorConstants.black);
		oldFigure =this.figure;
		oldFigure.setForegroundColor(ColorConstants.blue);	
	}
	
	public static  void setOldFigure(IFigure fig){
		if(oldFigure!=null) oldFigure.setForegroundColor(ColorConstants.black);
		oldFigure =fig;
	}
	

	public void setVisibility(int visibility) {
		throw new UnsupportedOperationException("Connector's visibility is controlled by Components!");
	}
	
	
}
