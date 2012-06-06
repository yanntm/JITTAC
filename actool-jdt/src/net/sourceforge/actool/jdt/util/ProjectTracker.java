package net.sourceforge.actool.jdt.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.actool.defaults;
import net.sourceforge.actool.model.ResourceMap;
import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class ProjectTracker implements PropertyChangeListener {
	
	private IProject project = null;
	private ArchitectureModel model = null;
	private Set<IResource> unmapped = new HashSet<IResource>();


	private class MappingCheckerVisitor implements IResourceVisitor {
		private ResourceMap map;
		private Set<IResource> unmapped;
		
		public MappingCheckerVisitor(ResourceMap map, Set<IResource> unmapped) {
			this.map = map;
			this.unmapped = unmapped;
		}
		
		public boolean visit(IResource resource) throws CoreException {
			// Only handle the leaf nodes, ie. IFile objects.
			if (resource.getType() != IResource.FILE)
				return true;
			
			// If this is not a compilation unit then it is not a java resource.
			IJavaElement element = JavaCore.create((IFile) resource);
			if (element == null || !(element instanceof ICompilationUnit)) 
				return false;
			
			
			// Check if the component is mapped or not!
			if (map.resolveMapping(resource) != null) {
				// Remove from unmapped, if it was unmapped remove also the problem marker.
				if (unmapped.remove(resource)) {
					resource.deleteMarkers(defaults.MARKER_UNMAPPED, false, IResource.DEPTH_ONE);
				}
			} else {
				// If it has not been unmapped before add a problem marker.
				if (unmapped.add(resource)) {
					IMarker marker = resource.createMarker(defaults.MARKER_UNMAPPED);
		            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		            marker.setAttribute(IMarker.MESSAGE, "File " + resource.getName()
		            									 + " is not mapped to any architectural element!");
				}
			}
			
			return false;
		}
	}

	public ProjectTracker(IProject project, ArchitectureModel model) {
		this.project = project;
		this.model = model;
		
		// Register with the model components.
		model.addPropertyChangeListener(this);
		Iterator<Component> iter = model.getComponents().iterator();
		while (iter.hasNext())
			iter.next().addPropertyChangeListener(this);
	}

	/**
	 * Scan project's resources to find any unmapped resources.
	 */
	public void scanForUnmappedResources() {
		MappingCheckerVisitor checker = new MappingCheckerVisitor(model.getResourceMap(), unmapped);
		IJavaProject jproject = JavaCore.create(project);
		if (project == null)
			return;
		
		// Process all package fragment roots of java project
		// which are not archives and are not external.
		try {
			IPackageFragmentRoot roots[] = jproject.getPackageFragmentRoots();
			for (int j = 0; j < roots.length; ++j) {
				IPackageFragmentRoot root = roots[j];
				if (root.isExternal() || root.isArchive())
					continue;

				root.getResource().accept(checker);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}


	public void propertyChange(PropertyChangeEvent event) {
		if (event.getPropertyName() == ArchitectureModel.COMPONENTS) {
			if (event.getNewValue() != null)
				((Component) event.getNewValue()).addPropertyChangeListener(this);
			if (event.getOldValue() != null)
				((Component) event.getOldValue()).removePropertyChangeListener(this);
			scanForUnmappedResources();
		} else if (event.getPropertyName() == Component.MAPPINGS)
			scanForUnmappedResources();
	}
}
