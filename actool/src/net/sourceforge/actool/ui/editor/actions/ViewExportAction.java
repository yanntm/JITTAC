package net.sourceforge.actool.ui.editor.actions;

import net.sourceforge.actool.ui.editor.commands.ViewExportCommand;
import net.sourceforge.actool.ui.editor.model.ArchitectureModelEditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;

public class ViewExportAction extends SelectionAction {
	public static final String EXPORT		= "net.sourceforge.actool.ui.actions.export";
	

	public ViewExportAction(IWorkbenchPart part) {
		super(part);
		setId(EXPORT);
		setText("Export as image");
	
	}

	

	@Override
	protected boolean calculateEnabled() {
		return !getSelectedObjects().isEmpty();
	}
	
	public void run() {
		ArchitectureModelEditPart comp = null;
		for (Object obj: getSelectedObjects()) {
			if (obj instanceof ArchitectureModelEditPart){
				comp = (ArchitectureModelEditPart) obj;
				break;
			}
			else
				return;
		}

		execute(new ViewExportCommand(comp));
	}

}
