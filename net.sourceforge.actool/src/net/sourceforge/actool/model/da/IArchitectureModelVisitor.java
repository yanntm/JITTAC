package net.sourceforge.actool.model.da;

public interface IArchitectureModelVisitor {

	/**
	 * Visit an element
	 * 
	 * @param element to be visited
	 * @return <code>true</code> if the members should be visited;
	 * 		   <code>false</code> if they should be skipped
	 */
	public boolean visit(ArchitectureElement element);
	
	/**
	 * Visit architecture model.
	 * 
	 * @param model
	 * @return <code>true</code> if the members should be visited;
	 * 		   <code>false</code> if they should be skipped
	 */
	public boolean visit(ArchitectureModel model);
	
	/**
	 * Visit a component.
	 * @param component
	 * @return <code>true</code> if the members should be visited;
	 * 		   <code>false</code> if they should be skipped
	 */
	public boolean visit(Component component);
	
	/**
	 * Visit a connector (source).
	 * 
	 * NOTE: Only source connectors are traversed on components
	 * 		 to avoid duplicate visits.
	 * 
	 * @param connector being visited
	 * @return <code>true</code> if the members should be visited;
	 * 		   <code>false</code> if they should be skipped
	 */
	public boolean visit(Connector connector);
}
