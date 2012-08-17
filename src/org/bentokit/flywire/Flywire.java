/*
    This file is part of Bentokit Flywire.

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
package org.bentokit.flywire;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

import org.bentokit.flywire.config.Config;
import org.bentokit.flywire.config.Constants;
import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.gui.dialog.AboutBox;
import org.bentokit.flywire.gui.dialog.setup.OptionsBox;
//import org.bentokit.flywire.gui.panels.InfoPanel;
import org.bentokit.flywire.gui.panels.MediaPanelFactory;
import org.bentokit.flywire.gui.panels.PanelEditor;
import org.bentokit.flywire.gui.panels.PanelModel;
import org.bentokit.flywire.gui.panels.PanelView;
import org.bentokit.flywire.gui.panels.MusicPickerPanel;
//import org.bentokit.flywire.gui.panels.PlayListPanel;
import org.bentokit.flywire.gui.panels.ShowListPanel;
//import org.bentokit.flywire.gui.panels.ShowMediaPanel;
//import org.bentokit.flywire.gui.panels.SimpleSelectionPanel;
//import org.bentokit.flywire.media.Announcements;
import org.bentokit.flywire.media.AutomaticDJ;
import org.bentokit.flywire.media.MusicPicker;
import org.bentokit.flywire.media.PlayList;
import org.bentokit.flywire.media.SelectionList;
import org.bentokit.krispi.*;
import org.bentokit.krispi.options.*;
import org.bentokit.krispi.options.types.*;
import org.bentokit.krispi.options.util.*;
import org.bentokit.krispi.options.ui.swing.*;
import org.bentokit.krispi.ui.swing.Dialog;
import info.clearthought.layout.TableLayout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.net.URL;
import java.net.URI;

public class Flywire extends JFrame implements KeyListener, Runnable, Optionable {
    public static final long serialVersionUID = 1L; //Why do we do this?

    String IDsDirectory = "StationMedia";

    PluginManager pluginManager;

    static ArrayList<OptionGroup> options = new ArrayList<OptionGroup>(Arrays.asList(new OptionGroup[] {
        new OptionGroup("general","General",
            new ArrayList<Option>(Arrays.asList(new Option[] {
              new BooleanOption("Always display hours", "Check to always show hours on the clock.", 
                    Constants.ALWAYS_SHOW_HOURS_KEY, Constants.ALWAYS_SHOW_HOURS_DEFAULT),
              new BooleanOption("Maximise main window", "Check to maximise the window on startup.",
                    Constants.WINDOW_MAXIMISED_KEY, Constants.WINDOW_MAXIMISED_DEFAULT),
              new BooleanOption("Expose List Mode", "Check to enable list mode.", 
                    Constants.EXPOSE_LIST_MODE_KEY, Constants.EXPOSE_LIST_MODE_DEFAULT),
              new BooleanOption("Ignore Control Files", "Check to ignore all control files.",
                    Constants.IGNORE_CONTROL_FILES_KEY, Constants.IGNORE_CONTROL_FILES_DEFAULT)
            }))
        ),
        new OptionGroup("panel.spare","Spare Panel",
            new ArrayList<Option>(Arrays.asList(new Option[] {
                new ChoiceOption("Startup play mode", "The default mode for the top left panel at startup.",
                    Constants.STARTUP_PLAY_MODE_KEY, Constants.STARTUP_PLAY_MODE_DEFAULT,
                    new ArrayList<StringOption>(Arrays.asList(new StringOption[] {
                        new StringOption("AutoDJ", "Automatic DJ", "", "autodj"),
                        new StringOption("Show", "Show Media", "", "show"),
                        new StringOption("List", "List Mode", "", "list"),
                        new StringOption("Last", "Whatever the last mode used was before shutdown.", "", "last")
                    }))  
                ),
            }))
        )
    }));

    public ArrayList<OptionGroup> getSettings() { return(options); }

    public enum PlayMode
    {
      SHOW,
      AUTODJ,
      LIST;
    };

    PlayList playlist;
    //PlayListPanel playlistPanel;
    //PlayControllerPanel playcontrol;

    //New PanelModel interface
    PanelModel model;

    // These three are interchanged, depending on the PlayMode.
    MusicPicker musicpicker;
    MusicPickerPanel musicpickerPanel;
    //ShowMediaPanel thisshow;
    ShowListPanel listpanel;

    //Announcements announcements;
    //AnnouncementsPanel announcementsPanel;
    //SelectionList stationIDs;
    //SimpleSelectionPanel stationIDsPanel;
    //InfoPanel infopanel;

    // The holder for the five main panels (see NOTES.txt)
    ChoicePanels choicepanels;

    OptionsBox optionsBox;

    Thread memoryThread;
    Runtime runtime;
    PlayMode playMode = PlayMode.SHOW;
    ModeChangeHelper modeHelper;

    JMenuBar menubar;
    JMenu switchMenu;
    JMenuItem showModeMenuItem;
    JMenuItem autodjModeMenuItem;
    JMenuItem listModeMenuItem;
    JMenuItem optionsMenuItem;
    JMenuItem newOptionsMenuItem;
    JMenuItem pluginManagerMenuItem;
    JMenuItem panelEditorMenuItem;
    JMenuItem exitMenuItem;
    JMenu helpMenu;
    JMenuItem aboutMenuItem;

    AutomaticDJ autodj;

    public MediaPanelFactory factory;

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P) {
            //playlist.setSequence(true);
            //playlist.start();
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void run() {
        long total = 0, max = 0;
        //long free = 0;
        long gctotal = 0, gcmax = 0;
        //long gcfree = 0;
        while(true) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,Locale.ENGLISH);
            df.setCalendar(Calendar.getInstance());
            if (total != runtime.totalMemory()) {
                total = runtime.totalMemory();
                ErrorHandler.mem("Total:"+runtime.totalMemory());
            }
            if (max != runtime.maxMemory()) {
                max = runtime.maxMemory();
                ErrorHandler.mem("Max:"+runtime.maxMemory());
            }
            runtime.gc();
            if (gctotal != runtime.totalMemory()) {
                gctotal = runtime.totalMemory();
                ErrorHandler.mem("GC Total:"+runtime.totalMemory());
            }
            if (gcmax != runtime.maxMemory()) {
                gcmax = runtime.maxMemory();
                ErrorHandler.mem("GC Max:"+runtime.maxMemory());
            }
            try {
                Thread.sleep(60000);
            }
            catch (InterruptedException ie) {
            }

        }
    }

//    public void run() {
//        while(true) {
//            ErrorHandler.info("Total:"+runtime.totalMemory());
//            ErrorHandler.info("Max:  "+runtime.maxMemory());
//            ErrorHandler.info("Free: "+runtime.freeMemory());
//            runtime.gc();
//            ErrorHandler.info("GC Total:"+runtime.totalMemory());
//            ErrorHandler.info("GC Max:  "+runtime.maxMemory());
//            ErrorHandler.info("GC Free: "+runtime.freeMemory());
//            try {
//                ErrorHandler.info("Sleeping for 10000 milliseconds");
//                Thread.sleep(10000);
//            }
//            catch (InterruptedException ie) {
//            }
//
//        }
//    }

    /**
     * @param iconName Resource name of icon
     */
    public void setIcon(String iconName) {
        try {
            URL pathShell;
            ClassLoader cl = Flywire.class.getClassLoader();
            pathShell = cl.getResource(iconName);

            Image icon = Toolkit.getDefaultToolkit().getImage(pathShell);
            setIconImage(icon);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Flywire() {
        super("Bentokit Flywire - playout client " + Config.getAppVersionString());
        new Pref("flywire"); //Initialise the prefs.
        setIcon("org/bentokit/flywire/resource/icon.png");

        // TODO: we need some persistent state (cbp 09/10/19)
        TimeZone.setDefault(TimeZone.getTimeZone("Australia/Melbourne"));
        ErrorHandler.initialise();
        //ParameterHandler p = new ParameterHandler();
        ErrorHandler.must("Program Started");

        URI[] pluginDirs = new URI[2];
        pluginDirs[0] = (new File("plugins")).toURI();
        pluginDirs[1] = (new File(System.getProperty("user.home")+File.separator+".flywire"+File.separator+"plugins")).toURI();

        this.pluginManager = new PluginManager(pluginDirs,"Flywire");

        //Load information from Panels
        model = new PanelModel(new File(System.getProperty("user.home")+File.separator+".flywire"+File.separator+"layout.xml"));
        
        //Create a PanelView
        PanelView view = new PanelView(this.pluginManager,model);
        //view.setBorder(new javax.swing.border.LineBorder(java.awt.Color.GREEN,1,true));

        //Add it to the screen
        this.getContentPane().setLayout(new TableLayout(new double[][] {{TableLayout.FILL},{TableLayout.FILL}}));
        this.getContentPane().add(view,"0,0");

/*
        //Create a playlist
        playlist = new PlayList();

        //Create a default factory for displaying media that directs media to a playlist.
        this.factory = new MediaPanelFactory(Font.decode("Arial-14"));
        this.factory.addField("filename");
        this.factory.addField("duration");

        playlistPanel = new PlayListPanel(playlist,this.factory,this);
        playcontrol = new PlayControllerPanel(playlist,this);
        playcontrol.setPlayList(playlist);

        announcements = new Announcements(playlist);
        announcementsPanel = new AnnouncementsPanel(announcements, this.factory, this);
        this.stationIDs = new DirSelectionList(playlist,new File(IDsDirectory),"IDs");        
        this.stationIDsPanel = new SimpleSelectionPanel("Station Media", this.stationIDs, playlist, this.factory, this);

        autodj = new AutomaticDJ(playlist,stationIDs,announcements);

        thisshow = new ShowMediaPanel(playlist,this.factory,this);
        listpanel = new ShowListPanel(playlist,this);
        musicpicker = new MusicPicker(playlist);
        musicpickerPanel = new MusicPickerPanel(musicpicker);
        autodj.setMusic(musicpicker);

        Config.getConfig().setLastPlayMode(playMode);

        infopanel = new InfoPanel(playlist,playcontrol, this);


        choicepanels = new ChoicePanels();
*/

        // Build the Switch Menu
        showModeMenuItem = new JMenuItem("Show mode");
        showModeMenuItem.setEnabled(playMode != PlayMode.SHOW);
        showModeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                modeHelper.fini();
                modeHelper = new ModeChangeHelper(showModeMenuItem);
                modeHelper.init();
                choicepanels.switchMode(PlayMode.SHOW);
            }
        });

        autodjModeMenuItem = new JMenuItem("Automatic DJ mode");
        autodjModeMenuItem.setEnabled(playMode != PlayMode.AUTODJ);
        autodjModeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ErrorHandler.info("Switching AutoDJ ON");
                //if (musicpicker == null) musicpicker = new MusicPicker(playlist);
                autodj.setMusic(musicpicker);
                // If we can't start the auto DJ we maintain the
                // current state.
                if (autodj.start()) {
                    modeHelper.fini();
                    modeHelper = new AutoDJChangeHelper(autodjModeMenuItem);
                    modeHelper.init();
                    choicepanels.switchMode(PlayMode.AUTODJ);
                } else {
                    ErrorHandler.info("Switching AutoDJ ON failed");
                    JOptionPane.showMessageDialog(choicepanels,"Automatic DJ could not be started.","Error!",JOptionPane.ERROR_MESSAGE);            

                }
            }
        });

        listModeMenuItem = new JMenuItem("List mode");
        listModeMenuItem.setEnabled(playMode != PlayMode.LIST);
        listModeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                modeHelper.fini();
                modeHelper = new ModeChangeHelper(listModeMenuItem);
                modeHelper.init();
                choicepanels.switchMode(PlayMode.LIST);
            }
        });

        modeHelper = new ModeChangeHelper(playMode == PlayMode.SHOW ? showModeMenuItem : playMode == PlayMode.LIST ? listModeMenuItem : autodjModeMenuItem);
        modeHelper.init();

        optionsMenuItem = new JMenuItem("Options...");
        optionsMenuItem.setEnabled(true);
        optionsMenuItem.addActionListener(new ActionListener() {
            public synchronized void actionPerformed(ActionEvent e) {
                if (optionsBox == null)
                {
                    optionsBox = new OptionsBox();
                    optionsBox.pack();
                }
                optionsBox.setVisible(true);
            }
        });

        newOptionsMenuItem = new JMenuItem("New Options...");
        newOptionsMenuItem.setEnabled(true);
        final Flywire finalFlywire = this;
        newOptionsMenuItem.addActionListener(new ActionListener() {
            public synchronized void actionPerformed(ActionEvent e) {
                try {
                    JDialog optionsDlg = new JDialog();
                    optionsDlg.getContentPane().add(new ConfigPanel(finalFlywire));
                    optionsDlg.pack();
                    optionsDlg.setVisible(true);
                }
                catch (UnknownOptionTypeException uote) {
                    uote.printStackTrace();
                }
            }
        });

        pluginManagerMenuItem = new JMenuItem("Plugin Manager");
        pluginManagerMenuItem.setEnabled(true);
        final PluginManager finalPluginManager = this.pluginManager;
        pluginManagerMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Dialog pluginDialog = new Dialog(finalPluginManager);
                pluginDialog.setVisible(true);                        
            }            
        });

        panelEditorMenuItem = new JMenuItem("Panel Editor");
        panelEditorMenuItem.setEnabled(true);
        final PanelModel finalPanelModel = this.model;
        panelEditorMenuItem.addActionListener(new ActionListener() {
            public synchronized void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("PanelEditor");
                frame.getContentPane().add(new PanelEditor(finalPluginManager,finalPanelModel));

                frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);    
            }
        });

        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setEnabled(true);
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quit();
            }
        });

        switchMenu = new JMenu("Switch mode to");
        switchMenu.add(showModeMenuItem);
        switchMenu.add(autodjModeMenuItem);
        switchMenu.add(listModeMenuItem);
        switchMenu.add(optionsMenuItem);
        switchMenu.add(newOptionsMenuItem);
        switchMenu.add(pluginManagerMenuItem);
        switchMenu.add(panelEditorMenuItem);
        switchMenu.add(exitMenuItem);

        menubar = new JMenuBar();
        menubar.add(switchMenu);

        //Build the Switch Menu
        aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AboutBox about = new AboutBox();
                about.pack();
                about.setVisible(true);
            }
        });

        helpMenu = new JMenu("Help");
        helpMenu.add(aboutMenuItem);

        menubar.add(helpMenu);

        setJMenuBar(menubar);


        runtime = Runtime.getRuntime();
        memoryThread = new Thread(this);
        memoryThread.start();

	    addWindowListener
	    (
	        new java.awt.event.WindowAdapter()
	        {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent e)
		    {
                        quit();
		    }
	        }
	    );

/*
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(choicepanels,"Center");
        getContentPane().add(playcontrol,"South");
*/

        if (Config.getConfig().isWindowMaximised())
        {
            // When displayed within a desktop window (think VNC or RDP) the
            // maximised JFrame's close button is adjacent to (and mistaken
            // for) the desktop window's close button. In such circumstances
            // we remove the frame's decorations.
            // TODO: (FIXME:) make this optional.
            //
            // NOTE: must be called before pack().
            setUndecorated(true);
        }
        else
        {
            java.awt.Point pos = Config.getConfig().getMainWindowPos();
            if (pos != null) setLocation(pos);
            java.awt.Dimension size = Config.getConfig().getMainWindowSize();
            if (size != null)
            {
                setSize(size);
                setPreferredSize(size);
            }
        }

        pack();

        if (Config.getConfig().isWindowMaximised())
        {
            // NOTE: must be called after pack().
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        addKeyListener(this);
/*
        playcontrol.addKeyListener(this);
        choicepanels.addKeyListener(this);
*/

        setVisible(true);
    }

    private void quit()
    {
	    Config cfg = Config.getConfig();

	    cfg.setMainWindowPos(getLocation());
	    cfg.setMainWindowSize(getSize());
	    setVisible(false);
            assert cfg.getLastPlayMode() == playMode || cfg.getStartPlayMode() == playMode;
	    cfg.save();
	    dispose();
	    System.exit(0);
    }

    public static void main(String[] argv) {
        try {
            //Windows Look and Feel
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            //Default Look and Feel
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) { }



        new Flywire();
    }


    protected class ModeChangeHelper
    {
        protected JMenuItem menuItem;

        ModeChangeHelper(JMenuItem menuItem)
        {
            this.menuItem = menuItem;
        }

        public void init()
        {
            if (menuItem != null) menuItem.setEnabled(false);
        }
        public void fini()
        {
            if (menuItem != null) menuItem.setEnabled(true);
        }
    }

    protected class AutoDJChangeHelper extends ModeChangeHelper
    {
        AutoDJChangeHelper(JMenuItem mi)
        {
            super(mi);
            ErrorHandler.info("Switching AutoDJ ON succeeded");
        }

        @Override
        public void fini()
        {
            super.fini();
            ErrorHandler.info("Switching AutoDJ OFF");
            autodj.stop();
        }
    }

    class ChoicePanels extends JPanel
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = -7181795707438377195L;
		
		JPanel westPanel, eastPanel;
        JPanel replaceablePanel;

        public ChoicePanels()
        {
            westPanel = new JPanel();
            westPanel.setLayout(new GridLayout(3,1));
            //westPanel.setLayout(new BoxLayout(westPanel,BoxLayout.Y_AXIS));
            switch (playMode)
            {
                case SHOW:
                    //replaceablePanel = thisshow; break;
                case LIST:
                    //replaceablePanel = listpanel; break;
                case AUTODJ:
                    //replaceablePanel = musicpickerPanel; break;
            }

            assert(replaceablePanel != null);

            westPanel.add(replaceablePanel);
            //westPanel.add(stationIDsPanel);
            //westPanel.add(announcementsPanel);

            eastPanel = new JPanel();
            eastPanel.setLayout(new BoxLayout(eastPanel,BoxLayout.Y_AXIS));
            //eastPanel.add(infopanel);
            //eastPanel.add(playlistPanel);

            setLayout(new GridLayout(1,2));
            // setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
            add(westPanel);
            add(eastPanel);
            addKeyListener(Flywire.this);
        }

        /**
         *
         * @param mode the mode we're switching <i>to</i>.
         */
        public void switchMode(Flywire.PlayMode mode) {
            switch(mode) {
                case AUTODJ:
                    westPanel.remove(replaceablePanel);
                    // MusicPickerPanel must be instantiated before we get
                    // to here.
                    assert musicpicker != null;
                    //westPanel.add(replaceablePanel = musicpickerPanel, 0);
                    westPanel.validate();
                    break;
                case SHOW:
                    westPanel.remove(replaceablePanel);
                    westPanel.validate();
                    //if (thisshow == null) thisshow = new ShowMediaPanel(playlist,Flywire.this.factory, Flywire.this);
                    //westPanel.add(replaceablePanel = thisshow, 0);
                    westPanel.validate();
                    break;
                case LIST:
                    westPanel.remove(replaceablePanel);
                    westPanel.validate();
                    //if (listpanel == null) listpanel = new ShowListPanel(playlist, Flywire.this);
                    //westPanel.add(replaceablePanel = listpanel, 0);
                    westPanel.validate();
                    break;
                default:
                    ErrorHandler.error("Aaaaaaargh!!!");
                    System.exit(-1);
            }
            Config.getConfig().setLastPlayMode(playMode = mode);
        }
    }
}
