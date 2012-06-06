package net.sourceforge.actool.model.ia;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;



class XReferenceCollection extends AbstractCollection<IXReference> {
	
	private class ArrayIterator implements Iterator<IXReference> {
		private int idx = 0;
		String[] array;
		private IXReferenceFactory factory;
		
		public ArrayIterator(IXReferenceFactory factory, String[] xrefs) {
			array = xrefs;
			this.factory = factory;
		}

		public boolean hasNext() {
			return idx < array.length;
		}

		public IXReference next() {
			return factory.createXReference((array[idx++]));
		}

		public void remove() {
			throw new UnsupportedOperationException(); 
		}
		
	}
	
	private String[] xrefs;
	private IXReferenceFactory factory;
	
	public XReferenceCollection(IXReferenceFactory factory, String[] xreferences) {
		xrefs = xreferences;
		this.factory = factory;
	}
	
	public Iterator<IXReference> iterator() {

		return new ArrayIterator(factory, xrefs);
	}

	public int size() {
		return xrefs.length;
	}
	
}


public class ImplementationChangeDelta {
	private IXReferenceFactory factory;
	
	private String[] common;
	private String[] added;
	private String[] removed;

	public ImplementationChangeDelta(IXReferenceFactory factory, String[] common, String[] added, String[] removed) {
		this.factory = factory;
		
		this.common = common;
		this.added = added;
		this.removed = removed;
	}
	
	public Collection<IXReference> getAddedXReferences() {
		return new XReferenceCollection(factory, added);
	}
	public Collection<IXReference> getCommonXReferences() {
		return new XReferenceCollection(factory, common);
	}
	
	public Collection<IXReference> getRemovedXReferences() {
		return new XReferenceCollection(factory, removed);
	}
}
