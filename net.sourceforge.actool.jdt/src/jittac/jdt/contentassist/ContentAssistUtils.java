package jittac.jdt.contentassist;

import static com.google.common.collect.Maps.newHashMap;
import static jittac.jdt.contentassist.RelationStatus.ENVISAGED;
import static jittac.jdt.contentassist.RelationStatus.UNKNOWN;
import static jittac.jdt.contentassist.RelationStatus.VIOLATION;
import static net.sourceforge.actool.model.ModelManager.modelManager;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;

@SuppressWarnings("restriction")
abstract class ContentAssistUtils {
    private static final Color UNKNOWN_COLOR = new Color(null, 224, 128, 0);
    private static final Color ENVISAGED_COLOR = new Color(null, 0, 192, 0);
    private static final Color VIOLATION_COLOR = new Color(null, 192, 0, 0);
    
    private static StyledString.Styler unknownStyler =  new StyledString.Styler() {
        public void applyStyles(TextStyle textStyle) {
            textStyle.foreground = UNKNOWN_COLOR;
        }
    };
    
    private static StyledString.Styler envisagedStyler =  new StyledString.Styler() {
        public void applyStyles(TextStyle textStyle) {
            textStyle.foreground = ENVISAGED_COLOR;

        }
    };
    
    private static StyledString.Styler violationStyler =  new StyledString.Styler() {
        public void applyStyles(TextStyle textStyle) {
            textStyle.foreground = VIOLATION_COLOR;
            textStyle.strikeout = true;
        }
    };

    public static IJavaElement getTargetElement(ICompletionProposal proposal) {
        if (proposal instanceof AbstractJavaCompletionProposal) {
            return ((AbstractJavaCompletionProposal) proposal).getJavaElement();
        }
        
        return null;
    }
    
    public static IJavaElement getSourceElement(ContentAssistInvocationContext ctx) {
        if (!(ctx instanceof JavaContentAssistInvocationContext)) {
            return null;
        }
        return ((JavaContentAssistInvocationContext) ctx).getCoreContext().getEnclosingElement();
    }
    
    public static Map<ArchitectureModel, Component> 
            getControllingModelsComponents(IJavaElement element) {
        IResource resource = element.getResource();
        Collection<ArchitectureModel> models = modelManager().getControllingModels(resource.getProject());

        Map<ArchitectureModel, Component> modelComponents = newHashMap();
        for (ArchitectureModel model: models) {
            Component component = model.resolveMapping(resource);
            if (component != null) {
                modelComponents.put(model, component);
            }
        }

        return modelComponents;
    }
    
    public static RelationStatus getRelationStatus(
            Map<ArchitectureModel, Component> sourceComponents, IJavaElement target) {
        RelationStatus status = UNKNOWN;
        
        IResource targetResource = target.getResource();
        for (Entry<ArchitectureModel, Component> entry: sourceComponents.entrySet()) {
            ArchitectureModel model = entry.getKey();
            Component sourceComponent = entry.getValue();

            Component targetComponent = model.resolveMapping(targetResource);
            if (targetComponent != null && !targetComponent.equals(sourceComponent)) {
                Connector connector = targetComponent.getConnectorForSource(sourceComponent);
                if (connector != null && connector.isEnvisaged()) {
                    status = ENVISAGED;
                } else if (connector == null || !connector.isEnvisaged()) {
                    return VIOLATION;
                }
            }
        }

        return status;
    }
    
    public static StyledString.Styler unknownStyler() {
        return unknownStyler;
    }
    
    public static StyledString.Styler envisagedStyler() {
        return envisagedStyler;
    }
    
    public static StyledString.Styler violationStyler() {
        return violationStyler;
    }
}
