package net.sourceforge.actool.ui.editor.commands;


import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;

import org.eclipse.gef.commands.Command;




public class ConnectorCreateCommand extends Command {
    private Component source, target;
    private Connector connector;

	public ConnectorCreateCommand(Component source) {
	    if (source == null)
	        throw new IllegalArgumentException();
	    
	    this.source = source;
	    this.setLabel("Create Connector");
	}
	
    public Component getSource() {
        return source;
    }
    
    public Component getTarget() {
        return target;
    }
    
    public void setTarget(Component target) {
        this.target = target;
    }
    
    public Connector getConnector() {
        return connector;
    }
 
    public boolean canExecute() {

        // No self-connections allowed.
        if (getSource() == null || getTarget() == null || getSource().equals(getTarget()))
            return false;
        
        // Only one envisaged connection is allowed.
        Connector connector = getSource().getConnectorForTarget(getTarget());
        if (connector != null && connector.isEnvisaged())
            return false;
        
        return true;
    }
    
	/**
	 * Execute command.
	 */
	public void execute() {
	    connector = getSource().getConnectorForTarget(getTarget());
	    if (connector == null)
	        connector = Connector.connect(getSource(), getTarget(), true);
	    if (!connector.isEnvisaged())
	        connector.setEnvisaged(true);
	}

	/**
	 * Re-execute command.
	 */
	public void redo() {
	    if (!connector.isEnvisaged())
	        connector.setEnvisaged(true);
	    if (!connector.isConnected())
	        connector.connect();
	}

	/**
	 * Undo the execution.
	 */
	public void undo() {
	    if (connector.isEnvisaged())
	        connector.setEnvisaged(false);
	    if (!connector.hasXReferences())
	        connector.disconnect();
	}
}
