package net.sourceforge.actool.ncc;

import net.sourceforge.actool.model.ia.AbstractXReference;
import net.sourceforge.actool.model.ia.IElement;

public class NCCXReference extends AbstractXReference {
	
	private NCCElement source, target;

	protected NCCXReference(NCCModel model, int type, NCCElement source, NCCElement target) {
		super(type, source.getOffset(), source.getLength(), -1);
		
		this.source = source;
		this.target = target;
	}

	public  IElement getSource() {
		return source;
	}
	
	public  IElement getTarget() {
		return target;
	}
	
	public  int getOffset() {
		return getSource().getOffset();
	}
	
	public int getLength() {
		return getSource().getLength();
	}

	public int getLine() {
		return 0;
	}
}
