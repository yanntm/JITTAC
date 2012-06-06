package net.sourceforge.actool.model.da;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


public class ArchitectureModelReader {

	static class ArchitectureModelHandler extends DefaultHandler {
		private ArchitectureModel model = null;
		private IFile file;
		
		public ArchitectureModelHandler(IFile file) {
			this.file = file;
		}
		
		public ArchitectureModel getModel() {
			return model;
		}

		public void startElement(String uri, String name,
			      				 String qname, Attributes atts)
		throws SAXException {
			try {
				if (name.equals("model")) {
					model = new ArchitectureModel(file);
				} else if (name.equals("component")) {
					String id = atts.getValue("id");
					if (id == null)
						throw new IllegalArgumentException();
		
					model.createComponent(id, atts.getValue("name"));
				} else if (name.equals("connector")) {
					String source = atts.getValue("source");
					String target = atts.getValue("target");
					if (source == null || target == null)
						throw new IllegalArgumentException();
	
					model.connect(source, target);
				} else if (name.equals("map")) {
					String handle = atts.getValue("handle");
					String target = atts.getValue("target");
					if (handle == null || target == null)
						throw new IllegalArgumentException();
	
					IPath path =  Path.fromPortableString(handle);
					IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
					if (resource != null)
						model.getComponent(target).addMapping(resource);
				}
			} catch (IllegalArgumentException ex) {
				// TODO: Report errors in the file!
				ex.printStackTrace();
			}
		}
	}

	public static ArchitectureModel read(IFile file) throws CoreException {
		ArchitectureModelHandler handler = new ArchitectureModelHandler(file);
		
		
		// Parse the XML file containing the model.
		try {
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(handler);
			parser.parse(new InputSource(file.getContents()));
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} // TODO: Handle the errors properly!
		
		return handler.getModel();
	}
}


