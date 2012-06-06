package net.sourceforge.actool.ncc;

import net.sourceforge.actool.model.ia.IElement;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class NCCElement implements IElement {
	public static final int UNKNOWN		= 0;
	public static final int FILE		= 1;
	public static final int FUNC		= 2;
	public static final int STRUCT		= 3;
	
	private NCCModel model;
	
	private String name , file;
	private int offset, length;
	
	protected NCCElement(NCCModel model, int type, String name, String file, int start, int end) {
		this.model = model;
		this.name = name;
		this.file = file;
		
		IPath path = getResource().getLocation();
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		try {
			manager.connect(path, LocationKind.IFILE, null);
			IDocument doc = manager.getTextFileBuffer(path, LocationKind.IFILE).getDocument();

			this.offset = doc.getLineOffset(start);
			this.length = doc.getLineOffset(end) - this.offset;			
			manager.disconnect(path, LocationKind.IFILE, null);
			
			return;
		} catch (BadLocationException ex) {
		} catch (CoreException ex) {
		}
		
		this.offset = 0;
		this.length = 0;
	}
	
	protected NCCModel getModel() {
		return model;
	}
	
	protected ICProject getCProject() {
		return getModel().getProject();
	}
	
	public IProject getProject() {
		return getCProject().getProject();
	}
	
	public String getName() {
		return name;
	}
	
	public IResource getResource() {
		return getProject().findMember(file);
	}
	
	public int getOffset() {
		return offset;
	}
	
	public int getLength() {
		return length;
	}
	
	public String toString() {
		return "[" + getResource().getName() + "] "  + getName();
	}
}
