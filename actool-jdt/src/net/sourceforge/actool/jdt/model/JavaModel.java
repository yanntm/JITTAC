package net.sourceforge.actool.jdt.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.actool.defaults;
import net.sourceforge.actool.model.ia.ImplementationChangeDelta;
import net.sourceforge.actool.model.ia.ImplementationChangeListener;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;




/**
 * @since 0.2
 */
public class JavaModel extends AbstractJavaModel{

	Map<String, Collection<String>> store;	/// Data store for all the relation (organised by CU).
	
	ICompilationUnit currentUnit;			/// Current compilation unit
	
	Collection<String> common;				/// Unchanged references in current compilation unit.
	Collection<String> added;				/// References added to current compilation unit.
	Collection<String> removed;				/// References removed from current compilation unit.

	public JavaModel(IJavaProject project) {
		super(project);
		
		// Create the containers to store the references.
		store = new HashMap<String, Collection<String>>();
		common = new Vector<String>();
		added = new Vector<String>();
	}
	
	public void _restore(IPath path) {
		File file = path.toFile();
		if (!file.exists() || !file.canRead())
			return;
		store.clear();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
	
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line.trim().equals(""))
					continue;
				
				String[] split = line.split("\t", 2);
				if (split.length != 2)
					continue;
				
				String key = split[0];
				String xref = split[1];
				
				Collection<String> xrefs = store.get(key);
				if (xrefs == null) {
					xrefs = new Vector<String>();
					store.put(key, xrefs);
				}
				
				xrefs.add(xref);
			}
			
			reader.close();
		} catch (IOException ex) {}
	}

	public void _store(IPath path) {
		File file = path.toFile();
		if (!file.exists())
			file.delete();
	
		try {
			file.createNewFile();
			if (!file.canWrite())
				return;
			
			PrintStream stream = new PrintStream(file);
			
			Iterator<String> keys = store.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				Collection<String>  xrefs = store.get(key);
				
				Iterator<String> iter = xrefs.iterator();
				while (iter.hasNext()) {
					stream.println(key + "\t" + iter.next());
				}
			}
			
			stream.flush();
			stream.close();
		} catch (IOException ex) {}
	}

	public void _updateListener(ImplementationChangeListener listener) {
		Vector<String> added = new Vector<String>();
		
		Iterator<String> keys = store.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			try {
				JavaCore.create(key).getResource().deleteMarkers(defaults.MARKER_TYPE_OLD, true, IResource.DEPTH_INFINITE);
			} catch (CoreException ex) {}
			added.addAll(store.get(key));
		}
		
		listener.implementationChangeEvent(new ImplementationChangeDelta(getProject(), this, new String[0], added.toArray(new String[added.size()]), new String[0]));
	}

	public void addXReference(int type, IJavaElement source, IJavaElement target,
			int line, int offset, int length) {
				if (currentUnit == null)
					throw new IllegalStateException("No current compilation unit!");
				
				// Create a string representing the cross reference.
				String xref = (new JavaXReference(type, source, target, line, offset, length)).toString();
				
				// If the new reference was present in the old file it is a common reference,
				// otherwise it is new and should be added. The remaining ones are removed references.
				if (removed.remove(xref))
					common.add(xref);
				else
					added.add(xref);
			}

	/**
	 * Start processing a compilation unit (this unit becomes current).
	 * 
	 * @throws IllegalStateException if there is already a current unit
	 */
	public void beginUnit(ICompilationUnit unit) {
		if (currentUnit != null)
			throw new IllegalStateException("Already processing a compilation unit!");
		currentUnit = unit;
		removed = store.get(unit.getHandleIdentifier());
		if (removed == null) {
			removed = new Vector<String>();
			store.put(unit.getHandleIdentifier(), removed);
		}
	}

	/**
	 * End processing current compilation unit.
	 */
	public void endUnit() {
		// Fire the property change signal.
		fireModelChange(new ImplementationChangeDelta(getProject(), this,
													  common.toArray(new String[common.size()]),
													  added.toArray(new String[added.size()]),
													  removed.toArray(new String[removed.size()])));
		
		// Removed all references and put the new ones.// redundant remove?
		removed.clear();
		removed.addAll(common);
		removed.addAll(added);
		
		clearUnit();
	}

	/**
	 * Return current compilation unit.
	 * 
	 * @return current compilation unit
	 * @throws IllegalStateException if there is no current unit
	 */
	protected ICompilationUnit getCurrentUnit() {
		if (currentUnit == null)
			throw new IllegalStateException("No current compilation unit!");
		return currentUnit;
	}

	public void clearUnit() {
		added.clear();
		common.clear();
		removed = null;
		currentUnit = null;
	}

}