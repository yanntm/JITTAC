package net.sourceforge.actool.ncc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sourceforge.actool.model.ia.IXReference;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class NCCModelReader {
	protected static final byte NCC_FILE	= 'P';
	protected static final byte NCC_FLINK	= 'L';
	protected static final byte NCC_SLINK	= 'Y';
	protected static final byte NCC_DEFINE	= 'D';
	protected static final byte NCC_FCALL	= 'F';
	protected static final byte NCC_GREAD	= 'g';
	protected static final byte NCC_GWRITE	= 'G';
	protected static final byte NCC_SREAD	= 's';
	protected static final byte NCC_SWRITE	= 'S';
	protected static final byte NCC_R		= 'R';
	
	protected NCCModel model = null;
	
	protected String filename = null;
	
	private int lineNo = 0;
	private String currentLine = null;
	private BufferedReader lineReader = null;
	
	private NCCElement currentFile = null;
	private NCCElement currentElement = null;

	
	public NCCModelReader(ICProject project) {
		this.model = new NCCModel(project);
	}
	
	public NCCModelReader(NCCModel model) {
		this.model = model;
	}
	
	protected void error(String message) {
		System.err.println(filename + ':' + getInputLineNo() + ": " + message);
	}
	
	protected void initInput(InputStream input) {
		lineNo = 0;
		lineReader = new BufferedReader(new InputStreamReader(input));
		currentLine = null;
	}
	
	protected String nextInputLine() throws IOException {
		++lineNo;
		currentLine = lineReader.readLine();
		return currentLine;
	}
	
	protected int getInputLineNo() {
		return lineNo;
	}
	
	protected String getInputLine() {
		return currentLine;
	}
	
	protected void skipInputBlock() throws IOException {
		while (getInputLine() != null && !getInputLine().trim().isEmpty())
			nextInputLine();
	}
	
	protected void preParse(InputStream input) throws IOException {
		
		initInput(input);
		while (nextInputLine() != null) {
			if (getInputLine().trim().isEmpty() || getInputLine().trim().startsWith("#"))
				continue;
			
			String[] split = getInputLine().split(": ", 2);
			if (split.length != 2 || split[0].length() != 1) {
				error("parse error: " + getInputLine());
			}
			
			boolean failed = false;
			String argument = split[1].trim();
			switch(split[0].charAt(0)) {
			case NCC_FILE:
				if (argument.startsWith("/include")
					|| argument.startsWith("/usr/include")
					|| argument.startsWith("/usr/local/include")) {
					skipInputBlock();
					continue;
				}
				
				currentFile = model.createElement(NCCElement.FILE, argument, argument, 0, -1);
				break;

			case NCC_FLINK:
				String[] flink = argument.split("\\s");
				failed = false;
				try {
					model.createElement(NCCElement.FUNC, flink[0].trim(), currentFile.getName(),
								  		Integer.parseInt(flink[1].trim()), Integer.parseInt(flink[2].trim()));
				} catch (NumberFormatException ex) {
					failed = true;
				} catch (ArrayIndexOutOfBoundsException ex) {
					failed = true;
				} finally {
					if (failed)
						error("error parsing FLINK: " + argument);
				}
				break;
				
			case NCC_SLINK:
				String[] slink = argument.split("\\s");
				failed = false;
				try {
					model.createElement(NCCElement.STRUCT, slink[0].trim(), currentFile.getName(),
					  					Integer.parseInt(slink[1].trim()), Integer.parseInt(slink[2].trim()));
				} catch (NumberFormatException ex) {
					failed = true;
				} catch (ArrayIndexOutOfBoundsException ex) {
					failed = true;
				} finally {
					if (failed)
						error("error parsing SLINK: " + argument);
				}
				break;
				
			case NCC_DEFINE:
			case NCC_FCALL:
			case NCC_GREAD:
			case NCC_GWRITE:
			case NCC_SREAD:
			case NCC_SWRITE:
			case NCC_R:
				// IGNORE
				break;

			default:
				error("unknown code: " + getInputLine());
			}
		}
	}
	
	protected void truParse(InputStream input) throws IOException {
		
		initInput(input);
		while (nextInputLine() != null) {
			if (getInputLine().trim().isEmpty() || getInputLine().trim().startsWith("#"))
				continue;
			
			String[] split = getInputLine().split(": ", 2);
			if (split.length != 2 || split[0].length() != 1) {
				error("parse error: " + getInputLine());
			}
			String argument = split[1].trim();
			
			int code = split[0].charAt(0);
			switch(code) {
			case NCC_DEFINE:
				currentElement = model.getElement(argument);
				if (currentElement == null) {
					// error("undefined function: " + argument);
					skipInputBlock();
					continue;
				}
				break;
				
			case NCC_FCALL:
			case NCC_GREAD:
			case NCC_GWRITE:
			case NCC_SREAD:
			case NCC_SWRITE:
				NCCElement element = model.getElement(argument);
				if (element == null) {
					// It may be a structure access,
					// find the first dot and it will give you the type.
					element = model.getElement(argument.split("\\.")[0]);
					if (element == null)
						continue;
				}
	
				model.createXReference(codeToXType(code), currentElement, element);				
				break;

			case NCC_R:
			default:
				// IGNORE: All errors should have been caught during pre-parse.
			}
		}
	}
	
	protected static int codeToXType(int code) {
		switch(code) {
		case NCC_FCALL:
			return IXReference.CALL;
			
		case NCC_GREAD:
		case NCC_SREAD:
			return IXReference.ACCESS;
			
		case NCC_SWRITE:			
		case NCC_GWRITE:
			return IXReference.ASSIGNMENT;
			
		default:
			return IXReference.UNKNOWN;
		}
	}
	
	public NCCModel read(IFile file) throws CoreException {
		if (!file.exists())
			return null;
		
		filename = file.getName();

		try {
			// Do the initial parse to assign entities to the containers.
			preParse(file.getContents());

			// Do the actual parse to get x-references.
			truParse(file.getContents());

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return model;
	}
}
