/*
    This file is part of Flywire.

    Flywire is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Flywire is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Flywire.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.bentokit.flywire.plugins.twolevelselectionlist;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Arrays;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.event.TimedEventAdapter;
import org.bentokit.flywire.event.TimedEventManager;
import org.bentokit.flywire.gui.SelectionListPanel;
import org.bentokit.flywire.gui.panels.MediaPanelFactory;
import org.bentokit.flywire.media.DirSelectionList;
import org.bentokit.flywire.media.PlayList;


public class ShowMediaPanel extends JPanel {
    public static final long serialVersionUID = 1L; //Why do we do this?

    File showMediaDir = new File("ShowMedia");
    
    ShowDropBox showdropbox;
    ReloadShowsEvent reloadEvent;
    
    JPanel dummyPanel;
    DirSelectionList selectionList;
    SelectionListPanel selectionListPanel;
    MediaPanelFactory factory;
    PlayList playlist;
    KeyListener keyListener;

    public ShowMediaPanel() {
        this.selectionList = null;
        this.selectionListPanel = null;

        this.dummyPanel = new JPanel();
        this.dummyPanel.setLayout(new BorderLayout());
        this.dummyPanel.setBorder(new EmptyBorder(50,50,50,50));            
        this.dummyPanel.add(new JLabel("Please select a show."),BorderLayout.CENTER);
        this.add(this.dummyPanel,BorderLayout.CENTER);             
    }

    public ShowMediaPanel(String title, File dir, PlayList playlist, MediaPanelFactory factory, KeyListener k) {
        setDir(dir);
        setFactory(factory);
        setPlayList(playlist);
        setKeyListener(k);
    }

    public void setTitle(String title) {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),title,
                   TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                   new Font("Sans Serif",Font.BOLD,20)));
    }

    public void setDir(File dir) {
        this.showMediaDir = dir;
        if (!showMediaDir.exists()) showMediaDir.mkdirs();
        if (!showMediaDir.isDirectory()) {
             JOptionPane.showMessageDialog(null, "You have a ShowMedia file, but I was looking\nfor a ShowMedia Directory.\nI cannot continue. Sorry.", "Alert", JOptionPane.ERROR_MESSAGE); 
             System.exit(-1);
        }
            
        showdropbox = new ShowDropBox(this,showMediaDir);
        showdropbox.setFont(new Font("Sans Serif",Font.PLAIN,15));

        this.add(showdropbox,"North");

        showdropbox.loadlist();

        // Update the list of shows and media in shows every minute
        reloadEvent = new ReloadShowsEvent(showdropbox);
    }

    public void setFactory(MediaPanelFactory factory) {
        this.factory = factory;
    }

    public void setPlayList(PlayList playlist) {
        this.playlist = playlist;
    }

    public void setKeyListener(KeyListener k) {
        this.keyListener = k;
        this.addKeyListener(k);
    }


    public void changeShow(String show) {
        ErrorHandler.info("ShowMediaPanel changing show to "+show);

        if (this.selectionList == null)
            this.remove(this.dummyPanel);
        else {
            this.selectionList.destroy();
            this.remove(this.selectionListPanel);
        }
        this.selectionList = new DirSelectionList(this.playlist,new File(showMediaDir,show),"ShowMedia");
        this.selectionListPanel = new SelectionListPanel(this.selectionList,this.factory,this.keyListener);
        this.add(this.selectionListPanel,BorderLayout.CENTER);
        this.validate();
    }
}


class ShowDropBox extends JComboBox implements ItemListener
{
    public static final long serialVersionUID = 1L; //Why do we do this?

    // This regular expression matches sub-directories of ShowMedia that
    // should be excluded from the show enumeration. TODO: this should
    // be externally configurable.
    private static final String ShowMediaDirExclusionExp =
        "[Pp]lay[Ll]ists|HorsesArses";

    String nowselected;
    File showMediaDir;
    ShowMediaPanel panel;

    public ShowDropBox(ShowMediaPanel panel, File showMediaDir) {
        this.panel = panel;
        nowselected = null;
        //this.setEditable(false);
        this.showMediaDir = showMediaDir;
        this.addItemListener(this);
    }

    public boolean isInList(String show) {
        for (int i = 0; i < this.getItemCount(); i++) {
            if (((String)this.getItemAt(i)).compareTo(show) == 0) return(true);
        }
        return(false);
    }

    public boolean isInArray(String needle, String[] haystack) {
        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i].compareTo(needle) == 0) return(true);
        }
        return(false);
    }

    public void loadlist() {
        String[] shows = showMediaDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                File target = new File(dir, name);
                boolean isDir = target.isDirectory();
                boolean isHidden = target.isHidden();
                boolean include = isDir && !isHidden;
                try
                {
                    include = include &&
                    (
                        ShowMediaDirExclusionExp == null ||
                        ShowMediaDirExclusionExp.isEmpty() ||
                        !name.matches(ShowMediaDirExclusionExp)
                    );
                }
                catch (java.util.regex.PatternSyntaxException pse)
                {
                    ErrorHandler.error("bad exclusion regex - " + pse);
                }
                return include;
            }
        });
        Arrays.sort(shows);
        //add new shows
        for (int i = 0; i < shows.length; i++) {
            //ErrorHandler.info("Checking against: "+shows[i]);
                if (!isInList(shows[i])) {
                    //ErrorHandler.info("Adding: "+shows[i]);
                    this.addItem(shows[i]);
                }
        }
        //removeFromPlaylist old shows
        for (int i = 0; i < this.getItemCount(); i++) {
            if (!isInArray((String)this.getItemAt(i),shows)) {
                //ErrorHandler.info("Removing: "+((String)this.getItemAt(i)));
                this.removeItemAt(i);
            }
        }
        if (shows.length <= 0) {
            this.addItem("No Shows Loaded");
        }
    }

    public void itemStateChanged(ItemEvent e) {
        String newselection = (String) this.getSelectedItem();
        ErrorHandler.info("ItemStateChange to "+newselection);

        if (newselection == null) ErrorHandler.error("newselection is null");
        if (nowselected == null) ErrorHandler.error("nowselected is null");

        if (newselection != null && (newselection.compareTo("No Shows Loaded") != 0)) {
            ErrorHandler.info("newselection is not null");
            if (nowselected == null || nowselected.compareTo(newselection) != 0)        {
                ErrorHandler.info("Combo box value changing to "+newselection);
                panel.changeShow(newselection);
                nowselected = newselection;
            }
        }
    }
}

class ReloadShowsEvent extends TimedEventAdapter
{
    ShowDropBox p;

    public ReloadShowsEvent(ShowDropBox p)
    {
        this.p = p;
        TimedEventManager.getManager().addTimedEventListener(this);
    }

    @Override
    public void doEvent() {
        p.loadlist();
    }
}
