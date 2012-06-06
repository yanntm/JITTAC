package net.sourceforge.actool.ui.editor;

import net.sourceforge.actool.ui.editor.actions.ComponentMergeAction;
import net.sourceforge.actool.ui.editor.actions.ComponentVisibilityAction;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

/**
 * @author Jacek Rosik
 */
class ArchitectureEditorContextMenuProvider extends ContextMenuProvider {

	/** The editor's action registry. */
	private ActionRegistry actionRegistry;

	public ArchitectureEditorContextMenuProvider(EditPartViewer viewer,
			ActionRegistry registry) {
		super(viewer);
		if (registry == null) {
			throw new IllegalArgumentException();
		}
		actionRegistry = registry;
	}

	
	private void appendAction(IMenuManager manager, String group, String id) {
		IAction action = getAction(id);
		if (action.isEnabled())
			manager.appendToGroup(group, action);
	}
	
	public void buildContextMenu(IMenuManager menu) {
		// Add standard action groups to the menu
		GEFActionConstants.addStandardActionGroups(menu);

		// Add actions to the menu
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO, // target group id
				getAction(ActionFactory.UNDO.getId())); // action to add
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO,
				getAction(ActionFactory.REDO.getId()));
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
				getAction(ActionFactory.DELETE.getId()));
		
		//appendAction(menu, ComponentVisibilityAction.ID_INVISIBLE);
		appendAction(menu,GEFActionConstants.GROUP_VIEW, ComponentVisibilityAction.ID_VISIBLE);
		appendAction(menu,GEFActionConstants.GROUP_VIEW, ComponentVisibilityAction.ID_FADED);

		appendAction(menu, GEFActionConstants.GROUP_EDIT, ComponentMergeAction.ID);
	}
	

	private IAction getAction(String actionId) {
		return actionRegistry.getAction(actionId);
	}

}
