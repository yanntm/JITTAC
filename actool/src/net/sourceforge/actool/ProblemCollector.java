package net.sourceforge.actool;

import net.sourceforge.actool.model.da.ArchitectureModelVisitor;
import net.sourceforge.actool.model.da.Connector;
import net.sourceforge.actool.model.ia.IXReference;

import org.eclipse.core.resources.IProject;

public class ProblemCollector extends ArchitectureModelVisitor {
	private final ProblemManager manager;
	private final IProject project;
	
	public ProblemCollector(ProblemManager manager, IProject project) {
		this.manager = manager;
		this.project = project;
	}

	@Override
	public boolean visit(Connector connector) {
		for (IXReference xref: connector.getXReferences()) {
			if (xref.getSource().getProject().equals(project))
				manager.connectorXReferenceAdded(connector, xref);
		}

		return super.visit(connector);
	}
	
}
