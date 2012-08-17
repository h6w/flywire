package org.bentokit.flywire.plugins.playlist;

import javax.swing.JPanel;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

import org.bentokit.krispi.Tuple;
import org.bentokit.krispi.AppPath;
import org.bentokit.krispi.Plugin;

public class PlayListPlugin extends Plugin {
    public PlayListPlugin(Plugin p) {
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
        System.err.println("PLayListPlugin Panel being created.");
        return(new PlayListPanel());
    }

}
