package jittac.jdt.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.text.MessageFormat;

import net.sourceforge.actool.jdt.model.AbstractJavaModel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class JavaASTHandler extends ASTRequestor {
    private static final MessageFormat progressMessageFormat
            = new MessageFormat("[JITTAC] Processing Java AST of file {0} out of {1} ({2}): {3}");

    AbstractJavaModel model;
    IProgressMonitor monitor;
    int total, processed = 0;
    
    public JavaASTHandler(AbstractJavaModel model, int total, IProgressMonitor monitor) {
        this.model = checkNotNull(model);
        this.monitor = checkNotNull(monitor);
        this.total = total;
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

        JavaASTProcessor processor = new JavaASTProcessor(model);
        try {
            ast.accept(processor);
            monitor.worked(1);
        } finally {
            model.clearUnit();
            processed++;
        }
    }
}

