package org.bentokit.flywire.plugins.twolevelselectionlist;

import javax.swing.JPanel;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;

import org.bentokit.krispi.options.Option;
import org.bentokit.krispi.options.OptionGroup;
import org.bentokit.krispi.options.Optionable;
import org.bentokit.krispi.options.types.StringOption;
import org.bentokit.krispi.options.types.FileOption;
import org.bentokit.krispi.Tuple;
import org.bentokit.krispi.AppPath;
import org.bentokit.krispi.Plugin;

public class TwoLevelSelectionListPlugin extends Plugin implements Optionable {
    public TwoLevelSelectionListPlugin(Plugin p) {
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
        return(new ShowMediaPanel());
    }

    /**** Optionable Interface ****/

    static ArrayList<OptionGroup> options = new ArrayList<OptionGroup>(Arrays.asList(new OptionGroup[] {
        new OptionGroup("general","General",
            new ArrayList<Option>(Arrays.asList(new Option[] {
              new StringOption("Title", "A name for the panel.",this.getPackageName()+".title","SelectionList"),
              new FileOption("Directory", "The top directory of the two-level selection list.",this.getPackageName()+".directory",new java.io.File(""))
            }))
        )
    }));

    public ArrayList<OptionGroup> getSettings() { return(options); }

}
