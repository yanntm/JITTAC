package net.sourceforge.actool.ui.editor.commands;


import net.sourceforge.actool.model.da.Connector;

import org.eclipse.gef.commands.Command;




public class ConnectorDeleteCommand extends Command {
	Connector connector;

	public ConnectorDeleteCommand(Connector connector) {
	    this.connector = connector;
	    this.setLabel("Delete Connector");
	}
	
	protected Connector getConnector() {
	    return connector;
	}
	
	public boolean canExecute() {
	    return !getConnector().inState(Connector.DIVERGENT);
	}
	
	/**
	 * Execute command.
	 */
	public void execute() {
	    if (getConnector().inState(Connector.DIVERGENT))
	        throw new IllegalArgumentException("Cannot delete a `divergent' connector!");

	    if (getConnector().inState(Connector.ABSENT)) {
	        // TODO: Handle the XReferences!
	        getConnector().disconnect();
	    } else if (getConnector().inState(Connector.CONVERGENT))
	        getConnector().setEnvisaged(false);
	    else
	        throw new IllegalStateException();
	}

	/**
	 * Re-execute command.
	 */
	public void redo() {
	    execute();
	}

	/**
	 * Undo the execution.
	 */
	public void undo() {
	    if (!getConnector().isEnvisaged())
	        getConnector().setEnvisaged(true);
	    if (!getConnector().isConnected())
	        getConnector().connect();
	    
	    // TODO: Handle the XReferences!
	}
}
