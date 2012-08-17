package org.bentokit.flywire.gui.panels;

import info.clearthought.layout.TableLayout;

import org.bentokit.flywire.media.MediaItem;
import org.bentokit.flywire.gui.MediaPanel;
import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.util.Triple;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.Font;
import java.util.Vector;
import javax.swing.JLabel;

/**
 * Instantiations of this factory will produce MediaPanels that respond to MediaInfo changes.
 * Additionally, if any change is made to the factory, each panel previously created by it is updated also.
 **/
public class MediaPanelFactory {
    Vector<MediaPanel> children = new Vector<MediaPanel>();
    Vector<Triple<String,Double,Font>> fields = new Vector<Triple<String,Double,Font>>(); //Ordered list of field names, TableLayout cell width, and Font.
    Font defaultFont = null;


    public MediaPanelFactory(Font defaultFont) {
        this.defaultFont = defaultFont;
    }

    public void addField(String name) {
        this.addField(name,this.defaultFont);
    }

    public void addField(String name, Font font) {
        if (fields.size() <= 0)
            this.addField(name,TableLayout.FILL,font);
        else
            this.addField(name,TableLayout.PREFERRED,font);
    }

    public void addField(String name, Double width, Font font) {
        fields.add(new Triple(name,width,font));

        for (MediaPanel child : children) {
            ((TableLayout)child.getLayout()).setColumn(fields.size(),width);
            JLabel label = child.addField(name);
            label.setFont(font);
            child.add(label);
        }
    }

    public void removeChild(MediaPanel child) {
        children.remove(child);
    }

    public void removeField(String name) {
        fields.remove(name);
    }

    public MediaPanel createPanel(MediaItem item) {
        //ErrorHandler.info("MediaPanelFactory:createPanel()");
        MediaPanel newPanel = new MediaPanel(item);
        //ErrorHandler.info("MediaPanelFactory:createPanel() MediaPanel created.");

        double size[][] = new double[2][];
        size[0] = new double[fields.size()];  //Column widths
        size[1] = new double[1];  //Row heights
        size[1][0] = TableLayout.PREFERRED;

        int i = 0;
        for (Triple<String,Double,Font> field : fields) {
            size[0][i] = field.second;
            i++;
        }        
        TableLayout layout = new TableLayout(size);
        newPanel.setLayout(layout);

        i = 0;
        for (Triple<String,Double,Font> field : fields) {
            //System.err.print(field.first+"-"+field.second+":");
            JLabel label = newPanel.addField(field.first);
            //System.err.println("Field added.");
            label.setFont(field.third);
            newPanel.add(label,i+",0");
            i++;
        }
        children.add(newPanel);
        return(newPanel);
    }
}
