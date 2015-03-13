/**
 * 
 */
package net.sourceforge.actool.ui.editor.quickfix;

import net.sourceforge.actool.defaults;
import net.sourceforge.actool.model.da.ArchitectureModel;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.PlatformUI;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sean
 *
 */
public class EmailArcitectQuickFix implements IMarkerResolution {

	String label;
	EmailArcitectQuickFix(String label) {
       this.label = label;
    }
    
    public String getLabel() {
       return label;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public void run(IMarker arg0) {
		// TODO Auto-generated method stub
		if(Desktop.isDesktopSupported()){
		      Desktop desktop = Desktop.getDesktop();
		      composeEmail(desktop,arg0);
		    }
	}
	
	/** 
	   Opens the default email client with prepopulated content, but doesn't send the email.
	   You should test your own email clients and character sets.
	 * @param desktop 
	  */
	  void composeEmail(Desktop desktop, IMarker marker){
	    try {
	      String url = 
	        "mailTo:" + ArchitectureModel.getEmail((IPath)marker.getAttribute(defaults.MODEL))+ 
	        ";?subject=" + getEmailSubject(marker) + 
	        "&body=" + getEmailBody(marker)
	      ;
	      URI mailTo = new URI(url);
	      //log(mailTo);
	      desktop.mail(mailTo);
	      
	    }
	    catch (IOException ex) {
	    	Logger.getAnonymousLogger().warning("Cannot launch mail client");
	    	MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Email Client Error", "Cannot launch mail client");
	    	
	    }
	    catch (URISyntaxException ex) {
	    	Logger.getAnonymousLogger().warning("Bad mailTo URI: " + ex.getInput());
	    } catch (CoreException e) {
			// TODO Auto-generated catch block
	    	Logger.getAnonymousLogger().warning(e.getMessage());
		}
	  }
	  
	  private String getEmailSubject(IMarker marker){
		    return encodeUnusualChars("[Arcitectual Change Proposal:\t"+marker.getResource().getProject().getName()+"]");
	  }
		  
	  private String getEmailBody(IMarker marker){
		StringBuilder result = new StringBuilder();
		try {
			String NL = System.getProperty("line.separator"); 
		    result.append("Hello,");
		    result.append(NL);
		    result.append(NL);
		    //exercises a range of common characters :
		    result.append("In implementing "+marker.getResource().getProject().getName()+" a proposal for changing the Architecture has been created by "+System.getProperty("user.name")+".");
		    result.append(NL);
		    result.append("Project:\t"+marker.getResource().getProject().getName()+".");
		    result.append(NL);
		    result.append("File:\t"+marker.getResource().getName()+".");
		    result.append(NL);
		    result.append("Line:\t"+marker.getAttribute(IMarker.LINE_NUMBER)+".");
		    result.append(NL);
		    result.append("Current Violation:\t"+marker.getAttribute(IMarker.MESSAGE)+".");
		    result.append(NL);
			result.append("Architectural Model:\t."+marker.getAttribute(defaults.MODEL));
			result.append(NL);
			result.append("Reason:");
			result.append(NL);
			result.append(NL);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			Logger.getAnonymousLogger().warning(e.getMessage());
		}
	    
	    
	    return encodeUnusualChars(result.toString());
	  }
	  
	  /** 
	   This is needed to handle special characters.
	   This method hasn't been tested with non-Latin character sets.
	   
	   Encodes all text except characters matching [a-zA-Z0-9]. 
	   All other characters are hex-encoded. The encoding is '%' plus the hex 
	   representation of the character in UTF-8. 
	   
	   <P>See also :
	   http://tools.ietf.org/html/rfc2368 - mailto
	   http://tools.ietf.org/html/rfc1738 - URLs
	  */
	  private String encodeUnusualChars(String aText){
	    StringBuilder result = new StringBuilder();
	    CharacterIterator iter = new StringCharacterIterator(aText);
	    for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
	      char[] chars = {c};
	      String character = new String(chars);
	      if(isSimpleCharacter(character)){
	        result.append(c);
	      }
	      else {
	        hexEncode(character, "UTF-8", result);
	      }
	    }
	    return result.toString();
	  }
	  private static final Pattern SIMPLE_CHARS = Pattern.compile("[a-zA-Z0-9]");
	  private boolean isSimpleCharacter(String aCharacter){
	    Matcher matcher = SIMPLE_CHARS.matcher(aCharacter);
	    return matcher.matches();
	  }
	  
	  /**
	   For the given character and encoding, appends one or more hex-encoded characters.
	   For double-byte characters, two hex-encoded items will be appended.
	  */
	  private static void hexEncode(String aCharacter, String aEncoding, StringBuilder aOut) {
	    try  {
	      String HEX_DIGITS = "0123456789ABCDEF"; 
	      byte[] bytes = aCharacter.getBytes(aEncoding);
	      for (int idx = 0; idx < bytes.length; idx++) {
	        aOut.append('%');
	        aOut.append(HEX_DIGITS.charAt((bytes[idx] & 0xf0) >> 4));
	        aOut.append(HEX_DIGITS.charAt(bytes[idx] & 0xf));
	      }
	    }
	    catch (UnsupportedEncodingException ex) {
	    	Logger.getAnonymousLogger().warning(ex.getMessage());
	    }
	  }

}
