package net.sourceforge.actool.ui.editor.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.actool.ui.editor.commands.ComponentVisibilityCommand;
import net.sourceforge.actool.ui.editor.model.ComponentEditPart;
import net.sourceforge.actool.ui.editor.model.Visibility;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;

public class ComponentVisibilityAction extends SelectionAction {
	public static final String ID_INVISIBLE		= "net.sourceforge.actool.ui.actions.HIDE";
	public static final String ID_VISIBLE		= "net.sourceforge.actool.ui.actions.SHOW";
	public static final String ID_FADED			= "net.sourceforge.actool.ui.actions.FADE";
	
	private final int visibility;

	public ComponentVisibilityAction(IWorkbenchPart part, int visibility) {
		super(part);
		this.visibility = visibility;
	
		switch (visibility) {
		case Visibility.INVISIBLE:
			setId(ID_INVISIBLE);
			setText("Remove");
			break;

		case Visibility.VISIBLE:
			setId(ID_VISIBLE);
			setText("Show");
			break;

		case Visibility.FADED:
			setId(ID_FADED);
			setText("Hide");
			break;

		default:
			throw new IllegalArgumentException("Invalid visibility value!");
		}

		// TODO: Set image descriptor for this action!
	}
	
	
	protected boolean calculateEnabled() {
		List<?> parts = getSelectedObjects();
		if (parts.isEmpty())
			return false;

		boolean sameVisibility = true;
		for (Object part: parts) {
			if (!(part instanceof Visibility))
				return false;
			if (sameVisibility)
				sameVisibility = ((Visibility) part).getVisibility() == visibility;
		}

		return !sameVisibility;
	}

	public void run() {
		Set<ComponentEditPart> comps = new HashSet<ComponentEditPart>();
		for (Object obj: getSelectedObjects()) {
			if (obj instanceof Visibility)
				comps.add((ComponentEditPart) obj);
			else
				return;
		}

		execute(new ComponentVisibilityCommand(comps, visibility));
	}
}
