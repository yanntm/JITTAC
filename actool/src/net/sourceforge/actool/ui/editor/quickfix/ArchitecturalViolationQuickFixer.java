package net.sourceforge.actool.ui.editor.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class ArchitecturalViolationQuickFixer implements
		IMarkerResolutionGenerator {

	@Override
	public IMarkerResolution[] getResolutions(IMarker mk) {
		return new IMarkerResolution[] {
		    new OpenArchitecturalModelQuickFix("Show violation in architectural model")
		   ,new EmailArcitectQuickFix("Propose Architectural Change")
		 
		};
	}

}
