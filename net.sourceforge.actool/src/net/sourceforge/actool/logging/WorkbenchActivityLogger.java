package net.sourceforge.actool.logging;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public class WorkbenchActivityLogger implements IWindowListener, IPageListener, IPartListener {
	private final EventLogger logger;
	
	public WorkbenchActivityLogger(EventLogger logger) {
		this.logger = logger;
	}
	
	public void register(IWorkbench workbench) {
		workbench.addWindowListener(this);
		for (IWorkbenchWindow window: workbench.getWorkbenchWindows()) {
			window.addPageListener(this);
			for (IWorkbenchPage page: window.getPages()) {
				page.addPartListener(this);
			}
		}
	}
	

	public void partActivated(IWorkbenchPart part) {
		logger.logPartActivated(part);
	}

	public void partDeactivated(IWorkbenchPart part) {
		logger.logPartDeactivated(part);
	}
	
	public void windowOpened(IWorkbenchWindow window) {
		window.addPageListener(this);
	}

	public void pageOpened(IWorkbenchPage page) {
		page.addPartListener(this);
	}

	
	public void partBroughtToTop(IWorkbenchPart arg0) {}
	public void partClosed(IWorkbenchPart arg0) {}
	public void partOpened(IWorkbenchPart arg0) {}

	public void windowClosed(IWorkbenchWindow arg0) {}
	public void windowActivated(IWorkbenchWindow arg0) {}
	public void windowDeactivated(IWorkbenchWindow arg0) {}
	
	public void pageClosed(IWorkbenchPage arg0) {}
	public void pageActivated(IWorkbenchPage arg0) {}
}
