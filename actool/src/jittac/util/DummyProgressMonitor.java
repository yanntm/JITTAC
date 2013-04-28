package jittac.util;
import org.eclipse.core.runtime.IProgressMonitor;


public class DummyProgressMonitor implements IProgressMonitor {
    private IProgressMonitor monitor;
    
    public DummyProgressMonitor() {
    }

    public DummyProgressMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }
 
    @Override
    public void beginTask(String name, int totalWork) {
 
    }

    @Override
    public void done() {
    }

    @Override
    public void internalWorked(double work) {
    }

    @Override
    public boolean isCanceled() {
        return monitor != null ? monitor.isCanceled() : false;
    }

    @Override
    public void setCanceled(boolean value) {
        if (monitor != null) {
            monitor.setCanceled(value);
        }
    }

    @Override
    public void setTaskName(String name) {
    }

    @Override
    public void subTask(String name) {
    }

    @Override
    public void worked(int work) {
    }
}
