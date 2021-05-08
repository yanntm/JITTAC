package net.sourceforge.actool.model.da;

/**
 * A visitor which calls {@link #visit(ArchitectureElement)}
 * on all elements of the model.
 */
public abstract class ArchitectureModelVisitor implements
		IArchitectureModelVisitor {

	@Override
	public boolean visit(ArchitectureElement element) {
		return true;
	}

	@Override
	public boolean visit(ArchitectureModel model) {
		return visit((ArchitectureElement) model);
	}

	@Override
	public boolean visit(Component component) {
		return visit((ArchitectureElement) component);
	}

	@Override
	public boolean visit(Connector connector) {
		return visit((ArchitectureElement) connector);
	}

}
