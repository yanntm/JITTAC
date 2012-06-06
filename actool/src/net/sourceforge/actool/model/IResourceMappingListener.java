package net.sourceforge.actool.model;

import net.sourceforge.actool.model.da.Component;

import org.eclipse.core.resources.IResource;

public interface IResourceMappingListener {

	/**
	 * Called when resource is mapped to a component.
	 * 
	 * @param resource - resource being mapped
	 * @param component - component to which the resource is being mapped
	 */
	public void resourceMapped(IResource resource, Component component);

	/**
	 * Called when resource is un-mapped from a component and remains unmapped.
	 * 
	 * @param resource - resource being un-mapped
	 * @param component - component to which the resource was mapped
	 */
	public void resourceUnMapped(IResource resource, Component component);

	/**
	 * Called when resource is re-mapped from one component to another.
	 * 
	 * @param resource - resource which is being re-mapped
	 * @param from - component to which the resource was mapped
	 * @param to - component to which the resource is being mapped
	 */
	public void resourceReMapped(IResource resource, Component from, Component to);
}
