package net.sourceforge.actool.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import net.sourceforge.actool.model.ResourceMapping;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;
import net.sourceforge.actool.ui.editor.commands.ComponentDeleteCommand;
import net.sourceforge.actool.ui.editor.commands.ConnectorDeleteCommand;
import net.sourceforge.actool.ui.editor.commands.MappingDeleteCommand;
import net.sourceforge.actool.ui.editor.dnd.MapEditPolicy;
import net.sourceforge.actool.ui.editor.model.PropertyChangeDelegate;

import org.eclipse.core.resources.IResource;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPageSite;

public class ArchitectureOutlinePage extends ContentOutlinePage {
	private ArchitectureEditor editor;

	public ArchitectureOutlinePage(ArchitectureEditor editor) {
		super(new TreeViewer());
		this.editor = editor;
	}

	public void createControl(Composite parent) {
		super.createControl(parent);

		ContextMenuProvider provider = new ArchitectureEditorContextMenuProvider(
				getViewer(), getEditor().getActionRegistry());
		getSite()
				.registerContextMenu(
						"net.sourceforge.actool.ui.views.ArchitectureOutline.ContextMenu",
						provider, getSite().getSelectionProvider());
		getViewer().setContextMenu(provider);

		getViewer().setEditDomain(new DefaultEditDomain(getEditor()));
		getViewer().setEditPartFactory(new OutlinePartFactory());
		getViewer().setContents(getEditor().getModel());

		getEditor().addViewerSynchronisation(getViewer());
	}

	protected ArchitectureEditor getEditor() {
		return editor;
	}

	public void init(IPageSite pageSite) {
		super.init(pageSite);
		ActionRegistry registry = getEditor().getActionRegistry();
		IActionBars bars = pageSite.getActionBars();
		String id = ActionFactory.UNDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.REDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.DELETE.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
	}
}

class ArchitectureOutlinePart extends AbstractTreeEditPart implements
		PropertyChangeListener {
	PropertyChangeDelegate delegate = new PropertyChangeDelegate(this);

	public ArchitectureOutlinePart(ArchitectureModel model) {
		super(model);
	}

	public void activate() {
		if (isActive())
			return;

		super.activate();
		getCastedModel().addPropertyChangeListener(delegate);
	}

	public void deactivate() {
		if (!isActive())
			return;

		getCastedModel().removePropertyChangeListener(delegate);
		delegate = null;
		super.deactivate();
	}

	private ArchitectureModel getCastedModel() {
		assert getModel() instanceof ArchitectureModel;
		return (ArchitectureModel) getModel();
	}

	protected List<Component> getModelChildren() {
		return getCastedModel().getComponents();
	}

	public void propertyChange(PropertyChangeEvent event) {

		if (event.getPropertyName() == ArchitectureModel.COMPONENTS) {
			refreshChildren();
		} else
			return;
	}
}

class ComponentOutlinePart extends AbstractTreeEditPart implements
		PropertyChangeListener {
	PropertyChangeDelegate delegate = new PropertyChangeDelegate(this);

	public ComponentOutlinePart(Component model) {
		super(model);
	}

	public void activate() {
		if (isActive())
			return;

		super.activate();
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
		installEditPolicy(MapEditPolicy.MAPPING_ROLE, new MapEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy() {
			protected Command getDeleteCommand(GroupRequest request) {
				return new ComponentDeleteCommand(getCastedModel());
			}
		});
	}

	private Component getCastedModel() {
		assert getModel() instanceof Component;
		return (Component) getModel();
	}

	protected Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}

	protected List<OutlinePlaceholder> getModelChildren() {
		Vector<OutlinePlaceholder> result = new Vector<OutlinePlaceholder>();
		result.add(new OutlinePlaceholder("Connectors", new ArrayList<Object>(
				getCastedModel().getSourceConnectors())));
		result.add(new OutlinePlaceholder("Mappings", new ArrayList<Object>(
				Arrays.asList(getCastedModel().getMappings()))));

		return result;
	}

	protected String getText() {
		return getCastedModel().getName();
	}

	public void propertyChange(PropertyChangeEvent event) {

		if (event.getPropertyName() == Component.NAME) {
			// refreshVisuals() will do the job!
		} else if (event.getPropertyName() == Component.SOURCE_CONNECTIONS) {
			refreshChildren();
		} else if (event.getPropertyName() == Component.TARGET_CONNECTIONS) {
			refreshChildren();
		} else if (event.getPropertyName() == Component.MAPPINGS) {
			refreshChildren();
		} else
			return;

		refreshVisuals();
	}
}

class OutlinePlaceholder extends AbstractTreeEditPart {
	private String name;
	List<Object> children;

	public OutlinePlaceholder(String name, List<Object> children) {
		this.name = name;
		this.children = new ArrayList<Object>(children);
	}

	protected Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD);
	}

	public String getText() {
		return this.name;
	}

	protected List<Object> getModelChildren() {
		return children;
	}
}

class ConnectorOutlinePart extends AbstractTreeEditPart {
	public ConnectorOutlinePart(Connector model) {
		super(model);
	}

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ROLE,
				new ConnectionEditPolicy() {
					protected Command getDeleteCommand(GroupRequest request) {
						return new ConnectorDeleteCommand(getCastedModel());
					}
				});
	}

	private Connector getCastedModel() {
		assert getModel() instanceof Component;
		return (Connector) getModel();
	}

	protected Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}

	protected String getText() {
		return getCastedModel().getTarget().getName();
	}
}

class MappingOutlinePart extends AbstractTreeEditPart {
	JavaElementLabelProvider labelProvider = new JavaElementLabelProvider();
	
	public MappingOutlinePart(ResourceMapping model) {
		super(model);
		labelProvider.turnOn(JavaElementLabelProvider.SHOW_SMALL_ICONS);
		labelProvider.turnOn(JavaElementLabelProvider.SHOW_ROOT);
	}

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy() {
			protected Command getDeleteCommand(GroupRequest request) {
				return new MappingDeleteCommand(getCastedModel());
			}
		});
	}

	private ResourceMapping getCastedModel() {
		assert getModel() instanceof ResourceMapping;
		return (ResourceMapping) getModel();
	}

	protected Image getImage() {
		
		// HACK: This should not be dependent on JDT!
		IJavaElement element = JavaCore.create(getCastedModel().getResource());
		if (element != null) {
			return labelProvider.getImage(element);
		} else		
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}

	protected String getText() {
		IResource resource = getCastedModel().getResource();
	
		// HACK: This should not be dependent on JDT!
		IJavaElement element = JavaCore.create(getCastedModel().getResource());
		if (element != null)
			return labelProvider.getText(element);
		else
			return resource.getName() + " ["  + resource.getProject().getName() + ']';
	}
}

final class OutlinePartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		if (model instanceof ArchitectureModel) {
			return new ArchitectureOutlinePart((ArchitectureModel) model);
		} else if (model instanceof Component) {
			return new ComponentOutlinePart((Component) model);
		} else if (model instanceof Connector) {
			return new ConnectorOutlinePart((Connector) model);
		} else if (model instanceof ResourceMapping) {
			return new MappingOutlinePart((ResourceMapping) model);
		} else if (model instanceof OutlinePlaceholder) {
			return ((OutlinePlaceholder) model);
		}

		return null;
	}

}