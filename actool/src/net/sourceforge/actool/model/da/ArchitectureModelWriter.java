package net.sourceforge.actool.model.da;

import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sourceforge.actool.model.ResourceMapping;

import org.eclipse.core.resources.IResource;

public class ArchitectureModelWriter {
	private static final String EOL = System.getProperty("line.separator");
    
	
	protected static void writeIndentation(XMLStreamWriter writer, int level)
		throws XMLStreamException {
		String indent = "";
		for (int i=0; i < level; ++i)
			indent += "\t";
		writer.writeCharacters(indent);
	}
	
	
    protected static void writeComponents(XMLStreamWriter writer, ArchitectureModel model, int level)
        throws XMLStreamException {

    	// Write all the components.
        writer.writeCharacters(EOL);
        writeIndentation(writer, level++);
        writer.writeStartElement("components");
        writer.writeCharacters(EOL);

        Iterator<Component> iter = model.getComponents().iterator();
        while (iter.hasNext()) {
            Component component = iter.next();
            
            // Write the single component, ignore name if the same as id.
            writeIndentation(writer, level);
            writer.writeEmptyElement("component");
            writer.writeAttribute("id", component.getID());
            if (!component.getID().equals(component.getName()))
                writer.writeAttribute("name", component.getName());
            writer.writeCharacters(EOL);
        }
        writeIndentation(writer, --level);
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }
    
    protected static void writeConnectors(XMLStreamWriter writer, ArchitectureModel model, int level)
        throws XMLStreamException {
        // Write all the connectors.
        writer.writeCharacters(EOL);
        writeIndentation(writer, level++);
        writer.writeStartElement("connectors");
        writer.writeCharacters(EOL);

        Iterator<Connector> iter = model.getConnectors().iterator();
        while (iter.hasNext()) {
            Connector connector = iter.next();
            if (!connector.isEnvisaged())
                continue;
            
            // Write the connector.
            writeIndentation(writer, level);
            writer.writeEmptyElement("connector");
            writer.writeAttribute("source", connector.getSource().getID());
            writer.writeAttribute("target", connector.getTarget().getID());
            writer.writeCharacters(EOL);
        }
        writeIndentation(writer, --level);
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }
    
    protected static void writeMappings(XMLStreamWriter writer, ArchitectureModel model, int level)
        throws XMLStreamException {
        // Write all the connectors.
        writer.writeCharacters(EOL);
        writeIndentation(writer, level++);
        writer.writeStartElement("maping");
        writer.writeCharacters(EOL);

        Iterator<ResourceMapping> iter = model.getResourceMap().iterator();
        while (iter.hasNext()) {
           ResourceMapping mapping = iter.next();
           if (mapping == null)
        	   continue; // When you remove a mapping, a null value sometimes persists.
           
           writeIndentation(writer, level);
           writer.writeEmptyElement("map");
           writer.writeAttribute("target", mapping.getComponent().getID());
           
           IResource resource = mapping.getResource();
           writer.writeAttribute("handle", resource.getFullPath().toPortableString());
           writer.writeCharacters(EOL);
        }
        
        writeIndentation(writer, --level);
        writer.writeEndElement();
        writer.writeCharacters(EOL);
    }

    public static void write(OutputStream stream, ArchitectureModel model){
        try {
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
            int level = 0;

            // Write header and open the root component.
            writer.writeStartDocument();
            
            writer.writeCharacters(EOL);
            writer.writeStartElement("model");
            writer.writeCharacters(EOL);
            
            // Write all file contents.
            level++;
            writeComponents(writer, model, level);
            writeConnectors(writer, model, level);
            writeMappings(writer, model, level);
            level--;
            
            // Finish the document.
            writer.writeEndElement();
            writer.writeCharacters(EOL);
            writer.writeEndDocument();
            
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
}
