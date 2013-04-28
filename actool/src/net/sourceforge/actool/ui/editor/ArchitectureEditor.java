/**
 * 
 */
package net.sourceforge.actool.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import net.sourceforge.actool.model.ModelManager;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.ArchitectureModelWriter;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.ui.editor.actions.ComponentMergeAction;
import net.sourceforge.actool.ui.editor.actions.ComponentVisibilityAction;
import net.sourceforge.actool.ui.editor.actions.ViewExportAction;
import net.sourceforge.actool.ui.editor.dnd.MappingDropTargetListener;
import net.sourceforge.actool.ui.editor.model.ArchitectureEditPartFactory;
import net.sourceforge.actool.ui.editor.model.Visibility;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteToolbar;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author jrosik
 * 
 */
public class ArchitectureEditor extends GraphicalEditorWithFlyoutPalette
								implements ISaveablePart2 {

	static public final String ID = "net.sourceforge.actool.ui.ArchitectureEditor";

	static private PaletteRoot palette;

	ArchitectureModel model = null;
	

	public ArchitectureModel getModel() {
		return model;
	}

	/**
	 */
	public ArchitectureEditor() {
		
		
		// TODO: Investigate what this actually does!
		setEditDomain(new DefaultEditDomain(this));
		
	}

	public void addViewerSynchronisation(EditPartViewer viewer) {
		getSelectionSynchronizer().addViewer(viewer);
	}

	public void removeViewerSynchronisation(EditPartViewer viewer) {
		getSelectionSynchronizer().removeViewer(viewer);
	}

	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		
		// Basic viewer configuration.
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setEditPartFactory(new ArchitectureEditPartFactory());
		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));
		
		ContextMenuProvider provider = new ArchitectureEditorContextMenuProvider(
				viewer, getActionRegistry());
		viewer.setContextMenu(provider);
		getSite().registerContextMenu(provider, viewer);

		// Drag and Drop support.
		viewer.addDropTargetListener(new MappingDropTargetListener(viewer));
	}


	@SuppressWarnings("unchecked")
	private void createAction(IAction action) {
		getActionRegistry().registerAction(action);
		getSelectionActions().add(action.getId());
	}
	
	protected void createActions() {
		super.createActions();
		
		//createAction(new ComponentVisibilityAction(this, Visibility.INVISIBLE));
		createAction(new ComponentVisibilityAction(this, Visibility.VISIBLE));
		createAction(new ComponentVisibilityAction(this, Visibility.FADED));
		
		createAction(new ViewExportAction(this));

		createAction(new ComponentMergeAction(this));
	}
	
	protected ActionRegistry getActionRegistry() {
		return super.getActionRegistry();
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);

		ActionRegistry registry = getActionRegistry();
		IActionBars bars = site.getActionBars();
		String id = ActionFactory.SAVE.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));

		id = ActionFactory.UNDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.REDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.DELETE.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
	}

	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();
			GraphicalViewer viewer = getGraphicalViewer();
				
				viewer.setContents(model);
			
	}

	/*
	 */
	protected PaletteRoot getPaletteRoot() {
		if (palette != null)
			return palette;
		palette = new PaletteRoot();

		// Create toolbar.
		PaletteToolbar toolbar = new PaletteToolbar("Tools");
		palette.acceptsType(toolbar);
		palette.add(toolbar);

		// Default Selection tool.
		ToolEntry tool = new PanningSelectionToolEntry();
		toolbar.add(tool);
		palette.setDefaultEntry(tool);

		toolbar.add(new MarqueeToolEntry());
		toolbar.add(new ConnectionCreationToolEntry("Connection Tool",
				"Tool to connect components", null, ImageDescriptor
						.createFromFile(this.getClass(),
								"icons/connector16.gif"), ImageDescriptor
						.createFromFile(this.getClass(),
								"icons/connector24.gif")));

		PaletteDrawer drawer = new PaletteDrawer("Components");
		palette.add(drawer);

		CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
				"Component", "Create a Component", Component.class,
				new SimpleFactory(Component.class), ImageDescriptor
						.createFromFile(this.getClass(),
								"icons/component16.gif"), ImageDescriptor
						.createFromFile(this.getClass(),
								"icons/component24.gif"));
		drawer.add(entry);

		return palette;
	}

	protected void setInput(IEditorInput input) {
		super.setInput(input);
		IFile file = ((IFileEditorInput) input).getFile();

		
		model = ModelManager.defaultModelManager().getArchitectureModel(file);
		setPartName(file.getName());
	}

	@Override
	public boolean isDirty() {
		return super.isDirty();
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return true;
	}
	
	@Override
	public int promptToSaveOnClose() {
		return YES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		IFile file = ((IFileEditorInput) getEditorInput()).getFile();

		// Write file contents into in-memory stream.
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ArchitectureModelWriter.write(stream, model);

		try {
			file.setContents(new ByteArrayInputStream(stream.toByteArray()),
					true, false, monitor);
			getCommandStack().markSaveLocation();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	
	public Object getAdapter(Class type) {
		if (type == IContentOutlinePage.class) {
			return new ArchitectureOutlinePage(this);
		}
		return super.getAdapter(type);
	}

	
}
