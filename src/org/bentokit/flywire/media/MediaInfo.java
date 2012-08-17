package org.bentokit.flywire.media;

import org.bentokit.flywire.gui.components.ValueComponent;
import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.util.TimeNumberFormat;
import java.net.URI;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;
import org.w3c.dom.Node;
import javax.xml.xpath.*;

/**
 * A container for a MediaItem's information so that anything that displays that information can obtain it.
 * This primarily consists of:
 *  - an XML Node which can be obtained and manipulated like any XML Node.
 *  - a static library of XPaths for maintaining and obtaining values from the XML document by shortcut strings.
 * 
 * Also, special values that will call the MediaItem equivalent function, including:
 * - "filename" - calls MediaItem.getURI().getPath().
 * - "uri" - calls MediaItem.getURI(); 
 * - "duration" - calls MediaItem.getDuration();
 *
 * @author tudor
 *
 */
public class MediaInfo {
    static Format formatter = new SimpleDateFormat("hh:mm:ss");

    Node xml;
    MediaItem item;

    public MediaInfo(MediaItem item) {
        this.xml = null;
        this.item = item;
    }

    public MediaInfo(MediaItem item, Node xml) {
        this(item);
        this.setNode(xml);
    }


    public void setMediaItem(MediaItem item) {
        this.item = item;
    }

    public MediaItem getMediaItem() {
        return(this.item);
    }

    public void setNode(Node xml) {
        this.xml = xml;
    }

    public Node getNode() {
        return(this.xml);
    }

    public String get(String shortcut) {
        if (shortcut.toLowerCase().equals("filename") 
            && !shortcuts.containsKey("filename")) {
            String fileWithPath = this.item.getURI().getPath();
            int slashIndex = fileWithPath.lastIndexOf('/');
            int dotIndex = fileWithPath.lastIndexOf('.');
            //System.err.println("Slash:"+slashIndex+",Dot:"+dotIndex);
            String filenameWithoutExtension;
            if (slashIndex < 0) return(fileWithPath);
            if (dotIndex < 0)
            {
              filenameWithoutExtension = fileWithPath.substring(slashIndex + 1);
            }
            else
            {
              filenameWithoutExtension = fileWithPath.substring(slashIndex + 1, dotIndex);
            }
            return(filenameWithoutExtension);
        } else if (shortcut.toLowerCase().equals("uri")) {
            return(this.item.getURI().toString());
        } else if (shortcut.toLowerCase().equals("duration")) {
            if (this.item.getDuration() != null) 
                return(TimeNumberFormat.formatTimeString(this.item.getDuration()));
            else
                return("");
        } else if (this.xml != null && shortcuts.containsKey(shortcut)) {
            try {
                return(shortcuts.get(shortcut).evaluate(this.xml));
            } catch (XPathExpressionException xpee) {
                ErrorHandler.error("XPath Expression failed to evaluate:"+xpee.getStackTrace());
                xpee.printStackTrace();
            }
        }
        ErrorHandler.error("Unknown MediaInfo shortcut:"+shortcut);
        return("["+shortcut+"]");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listeners and Listener Management //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public ArrayList<MediaInfoListener> listeners = new ArrayList<MediaInfoListener>();
	
	public void addListener(MediaInfoListener listener) {
    	this.listeners.add(listener);
    }
    
    public void removeListener(MediaInfoListener listener) {
    	this.listeners.remove(listener);
    }

    //NOTE: NotifyListeners is public in order for MediaItem to notify MediaInfo listeners when changing filename, uri, duration, etc..    
    public void notifyListeners() {
    	for (MediaInfoListener listener : new ArrayList<MediaInfoListener>(listeners)) {
    		listener.infoChanged();
    	}
    }

    ///////////////////////////////////////////////////////////////////////////
    // Static XPathExpression library /////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    static XPathFactory XPfactory = XPathFactory.newInstance();
    static XPath xpath = XPfactory.newXPath();
    static Map<String,XPathExpression> shortcuts = new HashMap<String,XPathExpression>();

    public static void addShortcut(String name, String expression) {
        try {
            shortcuts.put(name,xpath.compile(expression));        
        } catch (XPathExpressionException xpee) {
            ErrorHandler.error("XPath Expression failed to compile:"+xpee.getStackTrace());
            xpee.printStackTrace();
        }
    }

    public static void removeShortcut(String name) {
        shortcuts.remove(name);
    }

}
