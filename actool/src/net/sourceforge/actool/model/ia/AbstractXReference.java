package net.sourceforge.actool.model.ia;

import org.eclipse.core.resources.IResource;

public abstract class AbstractXReference implements IXReference {
	private int type;
	private int offset, length, line;

	protected AbstractXReference(int type, int offset, int length, int line) {
		this.type = type;
		
		this.offset = offset;
		this.length = length;
		this.line = line;
	}

	public int getType() {
		return type;
	}

	public IResource getResource() {
		return getSource().getResource();
	}
	
	public int getOffset() {
		return offset;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getLine() {
		return line;
	}
}
