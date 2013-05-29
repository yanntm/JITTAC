package jittac.jdt.contentassist;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static net.sourceforge.actool.model.ModelManager.modelManager;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.actool.model.da.ArchitectureModel;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.TextStyle;

@SuppressWarnings("restriction")
public class ArchitecturallyConsistentJavaAllCompletionProposalComputer
        extends JavaAllCompletionProposalComputer implements IJavaCompletionProposalComputer {

    private boolean ignoreViolationProposals = true;
    private boolean styleProposals = false;


    private IJavaElement extractJavaElement(ICompletionProposal proposal) {
        if (proposal instanceof AbstractJavaCompletionProposal) {
            return ((AbstractJavaCompletionProposal) proposal).getJavaElement();
        }
        
        return null;
    }
    
    
    protected ICompletionProposal styleViolationProposal(ICompletionProposal proposal) {
        return new JavaCompletionProposalWrapper(proposal) {
            @Override
            public StyledString getStyledDisplayString() {
                StyledString string = new StyledString();
                string.append(super.getStyledDisplayString());
                string.setStyle(0, string.length(), new StyledString.Styler() {
                    @Override
                    public void applyStyles(TextStyle style) {
                        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
                        style.strikeoutColor = colorRegistry.get(JFacePreferences.ERROR_COLOR);;
                        style.strikeout = true;
                    }
                });
                return string;
            }
        };
    }
 
    @Override
    public List<ICompletionProposal> computeCompletionProposals(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        List<ICompletionProposal> proposals =  super.computeCompletionProposals(context, monitor);
        
        if (context instanceof JavaContentAssistInvocationContext) {
            JavaContentAssistInvocationContext jctx = (JavaContentAssistInvocationContext) context;
            IJavaElement sourceElement = jctx.getCoreContext().getEnclosingElement();
            if (sourceElement == null) {
                return proposals;
            }

            IProject project = jctx.getProject().getProject();
            Map<ArchitectureModel, Component> sourceComponents = newHashMap();
            for (ArchitectureModel model: modelManager().getControllingModels(project)) {
                Component component = model.resolveMapping(sourceElement.getResource());
                if (component != null) {
                    sourceComponents.put(model, component);
                }
            }

            List<ICompletionProposal> processed = newArrayList();
            for (ICompletionProposal proposal: proposals) {
                IJavaElement targetElement = extractJavaElement(proposal);
                if (targetElement == null) {
                    processed.add(proposal);
                    continue;
                }
                
                boolean violation = false;
                for (Entry<ArchitectureModel, Component> entry: sourceComponents.entrySet()) {
                    ArchitectureModel model = entry.getKey();
                    Component target = model.resolveMapping(targetElement.getResource());
                    if (target != null && !target.equals(entry.getValue())) {
                        Connector connector = target.getConnectorForSource(entry.getValue());
                        if (connector == null || !connector.isEnvisaged()) {
                            violation = true;
                            break;
                        }
                    }
                }
                
                if (violation && ignoreViolationProposals) {
                    continue;
                }
                if (violation && styleProposals) {
                    proposal = styleViolationProposal(proposal);
                }
                processed.add(proposal);
            }
            
            return processed;
        }
        
        return proposals;
    }
}
