package jittac.jdt.builder;

import static com.google.common.base.Preconditions.checkNotNull;
import static jittac.Preferences.IGNORE_INTRAPROJECT_REFERENCES;
import static jittac.Preferences.IGNORE_LIBRARY_REFERENCES;
import static jittac.Preferences.preferenceStore;

import java.text.MessageFormat;

import net.sourceforge.actool.jdt.model.AbstractJavaModel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class JavaASTHandler extends ASTRequestor {
    private static final MessageFormat progressMessageFormat
            = new MessageFormat("[JITTAC] Extracting Java IA of ''{2}'' project (file {0} of {1}): {3}");

    AbstractJavaModel model;
    IProgressMonitor monitor;
    int total, processed = 0;
    
    public JavaASTHandler(AbstractJavaModel model, int total, IProgressMonitor monitor) {
        this.model = checkNotNull(model);
        this.monitor = checkNotNull(monitor);
        this.total = total;
    }
    
    protected JavaASTProcessor createJavaASTProcessor() {
        JavaASTProcessor processor = new JavaASTProcessor(model);
        processor.setIgnoreLibraryReferences(
                preferenceStore().getBoolean(IGNORE_LIBRARY_REFERENCES));
        processor.setIgnoreIntraProjectReferences(
                preferenceStore().getBoolean(IGNORE_INTRAPROJECT_REFERENCES));

        return processor;
    }

    private void updateSourcePath(ICompilationUnit source) {
        Object[] arguments = new Object[] {
            processed + 1, total, source.getJavaProject().getElementName(),
            source.getPath().removeLastSegments(1).toPortableString()
        };

        StringBuffer buffer = new StringBuffer(128);
        progressMessageFormat.format(arguments, buffer, null);
        monitor.subTask(buffer.toString());
    }

    @Override
    public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
        updateSourcePath(source);

        try {
            ast.accept(createJavaASTProcessor());
            monitor.worked(1);
        } finally {
            model.clearUnit();
            processed++;
        }
    }
}

