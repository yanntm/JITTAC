package net.sourceforge.actool.ui.editor.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.ui.editor.commands.ComponentMergeCommand;
import net.sourceforge.actool.ui.editor.model.ComponentEditPart;
import net.sourceforge.actool.ui.editor.model.Visibility;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;

public class ComponentMergeAction extends SelectionAction {
	public static final String ID		= "net.sourceforge.actool.ui.actions.MERGE";
	
	public ComponentMergeAction(IWorkbenchPart part) {
		super(part);

		setId(ID);
		setText("Merge");
		// TODO: Set image descriptor for this action!
	}
	
	
	protected boolean calculateEnabled() {
		List<?> parts = getSelectedObjects();
		if (parts.size() < 2)
			return false;

		for (Object part: parts) {
			if (!(part instanceof Visibility))
				return false;
		}

		return true;
	}

	public void run() {
		Set<Component> comps = new HashSet<Component>();
		for (Object obj: getSelectedObjects()) {
			if (obj instanceof ComponentEditPart)
				comps.add(((ComponentEditPart) obj).getModel());
			else
				return;
		}

		execute(new ComponentMergeCommand(comps));
	}
}
