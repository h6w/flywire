package org.bentokit.flywire.plugins.selectionlist;

import org.bentokit.krispi.Tuple;
import org.bentokit.krispi.options.*;
import org.bentokit.krispi.options.types.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

import org.bentokit.krispi.Tuple;
import org.bentokit.krispi.AppPath;
import org.bentokit.krispi.Plugin;

public class SelectionListPlugin extends Plugin implements Optionable {
    public SelectionListPlugin(Plugin p) {
        super(p);
    }

    public Map<AppPath,Tuple<Object,Method>> getAppPathLibrary() {
        Map<AppPath,Tuple<Object,Method>> library = new HashMap<AppPath,Tuple<Object,Method>>();
        try {
            library.put(
                new AppPath("pluginPanel"), new Tuple<Object,Method>(this,this.getClass().getMethod("pluginPanel"))
            );
        } catch (NoSuchMethodException nsme) {
            System.err.println("No such method \"pluginPanel\"");
        }

        return(library);
    }

    public JPanel pluginPanel() {
        System.err.println("SelectionListPlugin Panel being created.");
        return(new SimpleSelectionPanel());
    }

    /**** Optionable Interface ****/

    static ArrayList<OptionGroup> options = new ArrayList<OptionGroup>(Arrays.asList(new OptionGroup[] {
        new OptionGroup("general","General",
            new ArrayList<Option>(Arrays.asList(new Option[] {
              new StringOption("Title", "A name for the panel.",this.getPackageName()+".title","SelectionList"),
              new FileOption("Directory", "The directory for the selection.",this.getPackageName()+".directory",new java.io.File(""))
            }))
        )
    }));

    public ArrayList<OptionGroup> getSettings() { return(options); }


}
