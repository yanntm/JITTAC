package net.sourceforge.actool.ui.editor.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;
import net.sourceforge.actool.ui.editor.commands.ComponentDeleteCommand;
import net.sourceforge.actool.ui.editor.commands.ConnectorCreateCommand;
import net.sourceforge.actool.ui.editor.dnd.MapEditPolicy;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.requests.ReconnectRequest;





public class ComponentEditPart extends AbstractGraphicalEditPart
							   implements NodeEditPart, PropertyChangeListener, Visibility {
	
	PropertyChangeDelegate delegate;
	private int visibility = VISIBLE;

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
	
	
	public Component getCastedModel() {
		return (Component) getModel();
	}
	
	public int getIntProperty(String key, int def) {
		if (!getCastedModel().hasModel())
			return def;

		IResource resource = getCastedModel().getModel().getResource();
		
		try {
			String value = resource.getPersistentProperty(new QualifiedName(getCastedModel().getID(), key));
			if (value != null) {
				return Integer.parseInt(value);
			}
        } catch (CoreException e) {
			e.printStackTrace();
		}
		
		return def;
	}

	public void setIntProperty(String key, int value) {
		IResource resource = getCastedModel().getModel().getResource();
		
		try {
			resource.setPersistentProperty(new QualifiedName(getCastedModel().getID(), key), Integer.toString(value));
        } catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public int getVisibility() {
		return visibility;
	}

	@SuppressWarnings("unchecked")
	public void setVisibility(int visibility) {
		if (this.visibility == visibility
		    || (visibility < INVISIBLE || visibility > FADED))
			return;
	
		
		this.visibility = visibility;
		_setVisibilityProperty(visibility);
		updateFigureVisibility(getFigure(), visibility);
		
		// Connections have to be grayed out too.
		Collection<Object>  connections = new ArrayList<Object>(getSourceConnections().size()
																+ getTargetConnections().size());
		connections.addAll(getSourceConnections());
		connections.addAll(getTargetConnections());
		for (Object obj: connections) {
			GraphicalEditPart part = (GraphicalEditPart) obj;
			part.getFigure().setVisible(visibility != INVISIBLE);
			part.refresh();
		}
		getParent().refresh();
	}
	
	
	private void updateFigureVisibility(IFigure figure, int visibility) {

		switch (visibility) {
		case INVISIBLE:
			figure.setVisible(false);
			break;

		case VISIBLE:
			figure.setForegroundColor(ColorConstants.listForeground);
			figure.setBackgroundColor(ColorConstants.titleInactiveBackground);
			figure.setVisible(true);
			break;

		case FADED:
			figure.setForegroundColor(FigureUtilities.mixColors(ColorConstants.listForeground, ColorConstants.listBackground , 0.1));
			figure.setBackgroundColor(FigureUtilities.mixColors(ColorConstants.titleInactiveBackground, ColorConstants.listBackground , 0.1));
			figure.setVisible(true);
			break;

		default:
			return;
		}
	}
	
	private Point _getLocationProperty() {
		int x = 0, y = 0;
		
		try {
			x = getIntProperty("x", 0);
			y = getIntProperty("y", 0);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return new Point(x, y);
	}
	
	protected int _getVisibilityProperty() {
		int vis = VISIBLE;
		
		try {
			vis = getIntProperty("visibility", VISIBLE);
			if (vis < INVISIBLE || vis > FADED)
				vis = VISIBLE;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return vis;
	}
		
	public void _setLocationProperty(Point loc) {
		setIntProperty("x", loc.x);
		setIntProperty("y", loc.y);
	}

	protected void _setVisibilityProperty(int visibility) {
		setIntProperty("visibility", visibility);
	}

	
	@Override
	protected void createEditPolicies() {
        installEditPolicy(MapEditPolicy.MAPPING_ROLE, new MapEditPolicy());
	    installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy() {
	        protected Command getDeleteCommand(GroupRequest request) {
	                return new ComponentDeleteCommand(getCastedModel());
	        }
	    });
	    

	    installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new GraphicalNodeEditPolicy() {

	        protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
	            request.setStartCommand(new ConnectorCreateCommand((Component) getHost().getModel()));
	            return request.getStartCommand();
	        }

	        protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {          
	            ConnectorCreateCommand command = (ConnectorCreateCommand) request.getStartCommand();
	            command.setTarget((Component) getHost().getModel());
	            return command;
	        }


            protected Command getReconnectSourceCommand(ReconnectRequest request) {
                return null;
            }
            protected Command getReconnectTargetCommand(ReconnectRequest request) {
                return null;
            }
	    });
	}
	
	protected IFigure createFigure() {
		Component component = getCastedModel();
		assert component != null;
		
		// Create the composite figure.
		RoundedRectangle figure = new RoundedRectangle();
		BorderLayout layout = new BorderLayout();
		figure.setLayoutManager(layout);
		figure.add(new Label(component.getName()), BorderLayout.CENTER);

		figure.setBackgroundColor(ColorConstants.titleInactiveBackground);
		figure.setCornerDimensions(new Dimension(8, 8));
		//figure.setAntialias(1);
		
	    // Set visibility.
		visibility = _getVisibilityProperty();
	    updateFigureVisibility(figure, visibility);

		return figure;
	}
	
	public List<Connector> getModelSourceConnections() {
		return getCastedModel().getSourceConnectors();
	}
	
	public List<Connector> getModelTargetConnections() {
		return getCastedModel().getTargetConnectors();
	}

	public void propertyChange(PropertyChangeEvent event) {
		
		if (event.getPropertyName() == Component.NAME) {
			String name = (String) event.getNewValue();
			((Label) getFigure().getChildren().get(0)).setText(name);
		} else if (event.getPropertyName() == Component.SOURCE_CONNECTIONS) {
			refreshSourceConnections();
		} else if (event.getPropertyName() == Component.TARGET_CONNECTIONS) {
			refreshTargetConnections();
		} else
			return;
		
		refreshVisuals();
	}
	
	protected void refreshVisuals() {
		
		// Set figure's size and position.
	    Point loc = _getLocationProperty();
	    Dimension size = getFigure().getPreferredSize();
	    getFigure().setBounds(new Rectangle(loc.x, loc.y, size.width + size.height, size.height * 2));
	}

	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
	    return new ChopboxAnchor(getFigure());
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
	    return new ChopboxAnchor(getFigure());
	}

	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
	    return new ChopboxAnchor(getFigure());
	}


	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
	    return new ChopboxAnchor(getFigure());
	}
}
