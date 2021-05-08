package net.sourceforge.actool.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.actool.ACTool;
import net.sourceforge.actool.model.da.Component;
import net.sourceforge.actool.model.da.Connector;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ui.IWorkbenchPart;

public class EventLogger {
	protected static final String STARTUP			= "STRT";
	protected static final String SHUTDOWN			= "STOP";
	protected static final String MODEL_INIT_BEGIN	= "MBGN";
	protected static final String MODEL_INIT_END	= "MEND";
	protected static final String BUILD_BEGIN		= "BBGN";
	protected static final String BUILD_END			= "BEND";
	protected static final String XREFERENCE_ADDED	= "VINC";
	protected static final String XREFERENCE_REMOVED= "VDEC";
	protected static final String MAPPING_ADDED		= "MADD";
	protected static final String MAPPING_REMOVED	= "MDEL";
	protected static final String PART_ACTIVATED	= "PACT";
	protected static final String PART_DEACTIVATED	= "PDCT";
	
	private OutputStreamWriter writer;
	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private String uid;
	
	public EventLogger(String uid, OutputStream out) {
		assert out != null;
		this.uid = uid;
		writer = new OutputStreamWriter(out);
	}
	
	public static EventLogger getInstance() {
		return ACTool.getEventLogger();
	}
	
	public synchronized void logStartup() {
		logEvent(STARTUP, uid);
	}
	
	public synchronized void logShutdown() {
		logEvent(SHUTDOWN, uid);		
	}
	
	public synchronized void logModelInitBegin(IResource model) {
		logEvent(MODEL_INIT_BEGIN, model.getProjectRelativePath().toPortableString());
	}
	
	public synchronized void logModelInitEnd(IResource model) {
		logEvent(MODEL_INIT_END, model.getProjectRelativePath().toPortableString());
	}
	
	public synchronized void logMappingAdded(IResource resource, Component component) {
		logEvent(MAPPING_ADDED, resource.getProjectRelativePath().toPortableString() + '>' + component.getID());
	}
	
	public synchronized void logMappingRemoved(IResource resource, Component component) {
		logEvent(MAPPING_REMOVED, resource.getProjectRelativePath().toPortableString() + '>' + component.getID());
	}
	
	public synchronized void logBuildBegin(ICompilationUnit cunit) {
		logEvent(BUILD_BEGIN, cunit.getResource().getProjectRelativePath().toPortableString());
	}

	public synchronized void logBuildEnd(ICompilationUnit cunit) {
		logEvent(BUILD_END, cunit.getResource().getProjectRelativePath().toPortableString());
	}

	public synchronized void logPartActivated(IWorkbenchPart part) {
		logEvent(PART_ACTIVATED, part.getClass().getCanonicalName()
				 				 + "(" + part.getTitle() + ")");
	}

	public synchronized void logPartDeactivated(IWorkbenchPart part) {
		logEvent(PART_DEACTIVATED, part.getClass().getCanonicalName()
				 				   + "(" + part.getTitle() + ")");
	}
	
	protected void logXReference(String event, Connector connector) {
		if (connector.isEnvisaged())
			return;
		
		StringBuilder builder = new StringBuilder();

 
		builder.append(connector.getSource().getID());
		builder.append('>');
		builder.append(connector.getTarget().getID());
		builder.append(':');
		builder.append(Integer.toString(connector.getNumXReferences()));
		
		logEvent(event, builder.toString());
	}
	
	protected void logEvent(String event) {
		logEvent(currentTimestamp(), event, null);
	}
	
	protected void logEvent(Date timestamp, String event) {
		logEvent(timestamp, event, null);
	}
	
	protected void logEvent(String event, String message) {
		logEvent(currentTimestamp(), event, message);
	}

	protected void logEvent(Date timestamp, String event, String message) {
		try {	
			// Write the timestamp.
			writer.write(formatter.format(timestamp));
			writer.write('|');

			// Write the event type,
			writer.write(event);
			writer.write('|');
			
			// Write message.
			if (message != null)
				writer.write(message);
						
			writer.write('\n');
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected Date currentTimestamp() {
		return new Date();
	}
	
	public synchronized void close()  {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
