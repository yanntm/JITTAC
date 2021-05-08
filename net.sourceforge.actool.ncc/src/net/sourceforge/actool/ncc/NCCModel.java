package net.sourceforge.actool.ncc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.ia.ImplementationChangeListener;
import net.sourceforge.actool.model.ia.ImplementationModel;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class NCCModel extends ImplementationModel {
	
	private ICProject project;
	
	private Map<String, NCCElement> elements = new HashMap<String, NCCElement>();
	private Vector<NCCXReference> xrefs = new Vector<NCCXReference>();

	protected NCCModel(ICProject project) {
		this.project = project;
	}
	
	public ICProject getProject() {
		return project;
	}
	
	public NCCElement createElement(int type, String name, String file, int startLine, int endLine) {
		NCCElement element = elements.get(name);
		if (element == null) {
			element= new NCCElement(this, type, name, file, 0, 0);
			elements.put(name, element);
		}
		
		return element;
	}
	
	public NCCElement getElement(String name) {
		return elements.get(name);
	}
	
	public boolean hasElement(String name) {
		return elements.containsKey(name);
	}
	
	public NCCXReference createXReference(int type, NCCElement source, NCCElement target) {
		NCCXReference xref = new NCCXReference(this, type, source, target);
		xrefs.add(xref);

		return xref;
	}
	
	public void _updateListener(ImplementationChangeListener listener) {
		if (!(listener instanceof ArchitectureModel))
			return;
		
		ArchitectureModel model = (ArchitectureModel) listener;
		Iterator<NCCXReference> iter = xrefs.iterator();
		while (iter.hasNext())
			model.addXReference(iter.next());
	}

	
	public void _restore(IPath path) {
		NCCModelReader reader = new NCCModelReader(this);
		try {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			reader.read(file);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void _store(IPath path) {
		throw new UnsupportedOperationException("NCC model files are created by external tool (nccgen)!");
	}
}
