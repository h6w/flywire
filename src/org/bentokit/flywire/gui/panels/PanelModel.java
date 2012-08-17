package org.bentokit.flywire.gui.panels;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Serializable;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.bentokit.krispi.PluginManager;

public class PanelModel implements Serializable {

    public enum PanelType
    {
        CONTAINER("Panel"), PLUGIN("Plugin"), NOVALUE("");

        private String name;

        PanelType(String name) { this.name = name; }
        public String toString() { return(this.name); }

        /**
        * Parse text into an element of this enumeration.
        *
        * @param aText takes one of the values 'Panel',
        * 'Plugin'
        */

        public static PanelType parseString(String aText){
            for (PanelType panelType : PanelType.values()) {
                if (aText.equals(panelType.toString())) {
                  return panelType;
                }
            }
            return(null);
        }
    }

    File file;
    //final PluginManager pluginManager;
    Document doc;

    public PanelModel(
        //PluginManager pluginManager, 
        File file) {
        this.file = file;
        //this.pluginManager = pluginManager;

        this.loadXML();
    }

    //public PluginManager getPluginManager() { return(this.pluginManager); }
    public Document getDocument() { return(this.doc); }

    public void loadDefaultXML(DocumentBuilder docBuilder) {
        System.err.println("Error in XML file.  Loading default XML.");
        this.doc = docBuilder.newDocument();

        ////////////////////////
        //Creating the XML tree

        //create the root element and add it to the document
        Element root = this.doc.createElement("ui");
        this.doc.appendChild(root);                
    }

    //Load the XML from a file, or construct a default Document.
    public void loadXML() {
        try {
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            try {
                if (this.file.exists()) {
                    this.doc = docBuilder.parse(this.file);
                } else {
                    this.loadDefaultXML(docBuilder);
                }
            } catch (org.xml.sax.SAXException se) {
                this.loadDefaultXML(docBuilder);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
        	pce.printStackTrace();
        }
    }

    public void saveXMLtoFile() {
        try {
            //Make the parent directories if they don't exist
            if (!this.file.getParentFile().exists())
                this.file.getParentFile().mkdirs();
            FileWriter fw = new FileWriter(this.file);
            System.err.println(this.outputXMLtoString());
            fw.write(this.outputXMLtoString());
            fw.flush();
            fw.close();
            System.err.println("Layout file "+this.file.toString()+" saved.");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public String outputXMLtoString() {
        StreamResult xmlOutput = new StreamResult(new StringWriter());

        // Configure transformer
        try {
            Transformer transformer = TransformerFactory.newInstance()
                            .newTransformer(); // An identity transformer
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(this.doc), xmlOutput);
        } catch (javax.xml.transform.TransformerConfigurationException tce) {
            tce.printStackTrace();
        } catch (javax.xml.transform.TransformerException te) {
            te.printStackTrace();
        }

        return(xmlOutput.getWriter().toString());    
    }
}
