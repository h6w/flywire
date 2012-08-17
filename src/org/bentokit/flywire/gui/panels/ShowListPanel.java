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
package org.bentokit.flywire.gui.panels;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cbpaine
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import java.text.SimpleDateFormat;

import com.melloware.jspiff.jaxp.*;
import javax.xml.parsers.ParserConfigurationException;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.event.TimedEventAdapter;
import org.bentokit.flywire.event.TimedEventManager;
import org.bentokit.flywire.gui.PlayControllerPanel;
import org.bentokit.flywire.media.MediaItem;
import org.bentokit.flywire.media.PlayList;
import org.xml.sax.SAXException;
import java.net.URI;
import java.net.URISyntaxException;


public class ShowListPanel extends JPanel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -379702607786457982L;
	
	// Define serialVersionUID iff this class will be serialized.
    private static final String OtherPlaylistLabel = "Other playlists";
    private static final String ListFilenameRegex = "[0-9]{14}-.*";
    private File showListDir;

    private ListEntry currentEntry;

    protected static SimpleDateFormat stampFormat =
            new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);

    ListDropBox dropbox;
    PlayControllerPanel playcontrol;
    PlayList playlist;

    JPanel listPanel;
    JPanel viewPanel;

    public ShowListPanel(PlayList playlist, KeyListener k)
    {
        this.playlist = playlist;

        showListDir = new File("ShowMedia/PlayLists");
        if (!showListDir.exists()) showListDir.mkdirs();
        if (!showListDir.isDirectory()) {
             javax.swing.JOptionPane.showMessageDialog(null, "You have a PlayLists file, but I was looking\nfor a Directory.\nI cannot continue. Sorry.", "Alert", javax.swing.JOptionPane.ERROR_MESSAGE);
             System.exit(-1);
        }

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        dropbox = new ListDropBox();
        dropbox.setFont(new Font("Sans Serif",Font.PLAIN,15));
        dropbox.loadlist();

        viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());
        viewPanel.add(listPanel, "North");
        viewPanel.add(new JPanel(), "Center");
        JScrollPane scrollPane = new JScrollPane(viewPanel);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Show Playlists",
            TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
            new Font("Sans Serif",Font.BOLD,20)));

        add(dropbox, "North");
        add(scrollPane, "Center");

        addKeyListener(k);

        // Initiate regular update of the list of shows and the media in the
        // selected show
        //ReloadShowListEvent rsle = new ReloadShowListEvent(dropbox, Calendar.SECOND, 17);
    }

    // SelectionList interface ///////////////////////////////////////////////
    public void played(MediaItem m) { }
    public void remove(MediaItem m)
    {
        ErrorHandler.info("Removing media " + m.getURI());
    }

    class ListDropBox extends JComboBox implements ItemListener
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = -6411081126812217645L;
		String selectedListName;
        volatile boolean loading;

        ListDropBox()
        {
            setEditable(false);
            addItemListener(this);
        }

        public void loadlist()
        {
            int others = 0;
            String[] lists = showListDir.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    File target = new File(dir, name);
                    return !target.isDirectory() && !target.isHidden();
                }
            });
            if (lists != null && lists.length > 0)
            {
                /**
                 * There's a couple of rules that list file names must follow.
                 * Broadly, playlists fall into two categories. Those for
                 * specific programs and those that aren't. The first category's
                 * files are named YYYYMMDDhhmmss-literal program name.xml. The
                 * second category are those that don't start with a parseable
                 * timestamp.
                 *
                 * The drop box has a single entry for each specific program and
                 * a catch-all entry for all the non-specific lists.
                 */
                java.util.concurrent.ConcurrentSkipListSet<String> cumulus =
                        new java.util.concurrent.ConcurrentSkipListSet<String>();

                for (String s : lists)
                {
                    if (s.matches(ListFilenameRegex))
                        cumulus.add(s.substring(s.indexOf('-') + 1, s.lastIndexOf('.')));
                    else
                        ++others;
                }

                loading = true;
                setSelectedIndex(-1);
                removeAllItems();
                // Populate the drop box. Check whether the playlist that the
                // user last selected is still current.
                boolean stillCurrent = false;
                for (Iterator<String> i = cumulus.iterator(); i.hasNext(); )
                {
                    String item = (String) i.next();
                    addItem(item);
                    if (selectedListName != null && item.matches(selectedListName))
                        stillCurrent = true;
                }
                if (others > 0)
                {
                    addItem(OtherPlaylistLabel);
                    if (!stillCurrent && selectedListName != null)
                        stillCurrent = OtherPlaylistLabel.matches(selectedListName);
                }
                if (stillCurrent)
                    setSelectedItem(selectedListName);
                else
                    selectedListName = null;
                loading = false;

                updateSelection();
            }
        }

        protected void updateSelection()
        {
            selectedListName = (String) getSelectedItem();

            String[] matches = showListDir.list(new FilenameFilter() {
                boolean selectOthers = selectedListName.compareTo(OtherPlaylistLabel) == 0;
                public boolean accept(File dir, String name) {
                    File target = new File(dir, name);
                    return !target.isDirectory() && !target.isHidden() && selectOthers ? !name.matches(ListFilenameRegex) : name.matches(ListFilenameRegex + selectedListName + ".*");
                }
            });
            listPanel.removeAll();
            if (matches.length > 0)
            {
                Date currentDate = currentEntry == null ? null : currentEntry.getDate();
                currentEntry = null;

                for (String s : matches)
                {
                    ListEntry entry = new ListEntry(s);
                    listPanel.add(entry);
                    if (currentDate != null && currentDate.equals(entry.getDate()))
                    {
                        currentEntry = entry;
                        entry.select();
                    }
                }
            }
            listPanel.validate();
        }

        // ItemListener interface /////////////////////////////////////////////
        public void itemStateChanged(ItemEvent e)
        {
            // ignore these interrupts while we're loading the box.
            if (!loading) updateSelection();
        }
    }

    class ListEntry extends JPanel implements MouseListener
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = -1189478710469185018L;
		
		private JLabel label;
        private String filename;
        private String showDir;
        private String show;
        private Date date;

        public ListEntry(String filename)
        {
            label = new JLabel();
            label.setFont(new Font("Sans Serif", Font.PLAIN, 15));

            this.filename = filename;

            int dashPos = filename.indexOf('-');
            // In case you were wondering, -1 + 1 == 0
            show = filename.substring(dashPos + 1, filename.lastIndexOf('.'));
            showDir = filename.substring(0, filename.lastIndexOf('.'));

            if (dashPos > 0)
            {
                try
                {
                    date = stampFormat.parse(filename);
                    label.setText(new SimpleDateFormat().format(date));
                }
                catch (ParseException pe)
                {
                    ErrorHandler.error(pe);
                    label.setText(filename);
                }
            }
            else
                label.setText(show);

            setLayout(new BorderLayout());
            add(label, "West");
            addMouseListener(this);
        }

        public boolean isOther()
        {
            return date == null && filename != null;
        }

        public String getShow()
        {
            return show;
        }

        public Date getDate()
        {
            return date;
        }

        public void select()
        {
            // FIXME: hard-coded colours
            this.setBackground(Color.blue);
            label.setForeground(Color.white);
        }

        public void deselect()
        {
            Container c = this.getParent();
            if (c != null) this.setBackground(c.getBackground());
            // FIXME: hard-coded colours
            label.setForeground(Color.black);
        }

        protected void listLoader()
        {
            (new ListLoaderTask()).execute();
        }

        // MouseListener interface ////////////////////////////////////////////

        public void mouseClicked(MouseEvent e)
        {
            // Tell the luser that we noticed their click.
            if (currentEntry != null) currentEntry.deselect();
            currentEntry = this;
            select();
            listLoader();
        }

        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }

        // List loader background task ////////////////////////////////////////

        private class ListLoaderTask extends SwingWorker<Void, Void>
        {
            public Void doInBackground()
            {
                java.awt.Container contentPane = getRootPane().getContentPane();
                java.awt.Cursor cursor = contentPane.getCursor();
                contentPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                try
                {
                    XspfPlaylist xPlaylist = new XspfPlaylist("ShowMedia/PlayLists/" + filename);
                    XspfTrack tracks[] = xPlaylist.getPlaylistTrackList().getTrack();
                    File media;
                    for (XspfTrack track : tracks)
                    {
                        try
                        {
                            // WARNING: XSPF defines the URI field as an absolute
                            // path. The way I'm using it I'm just putting in the
                            // filename and I'm adding the path here. This is LAME
                            // but it gets me going. FIXME: there's a better way...
                            URI uri = new URI(track.getLocation(0));
                            media = new File("ShowMedia/" + showDir + "/" + uri.getPath());
                            ErrorHandler.info("Loading media from " + media.getCanonicalPath());

                            /*
                            String album;
                            if ((album = track.getAlbum()) == null)
                                album = "unknown album";

                            String title;
                            if ((title = track.getTitle()) == null)
                                title = "unknown title";

                            String trackNum;
                            if ((trackNum = track.getTrackNumAsString()) == null)
                                trackNum = "0";
                                */
/*
                            PlayListMediaItem item = new PlayListMediaItem(playcontrol, playlist, ShowListPanel.this, media, "placeholder descriptive text");
                            item.getInfo().setValues(new StringComponent[]{ 
                            		new StringComponent(media.getName()), 
                            		null, 
                            		new StringComponent(album), 
                       				new StringComponent(trackNum), 
                     				new StringComponent(title) 
                            });
                            item.addToPlaylist(true);
                            */
                        }
                        catch (URISyntaxException use)
                        {
                            ErrorHandler.error(use);
                        }
                    }
                }
                catch (IOException ioe)
                {
                    ErrorHandler.error(ioe);
                }
                catch (SAXException saxe)
                {
                    ErrorHandler.error(saxe);
                }
                catch (ParserConfigurationException pce)
                {
                    ErrorHandler.error(pce);
                }
                finally
                {
                    contentPane.setCursor(cursor);
                }
                return null;
            }

            @Override
            protected void done()
            {
                //
            }
        }
    }

    class ReloadShowListEvent extends TimedEventAdapter
    {
        private ListDropBox ldb;

        public ReloadShowListEvent(ListDropBox ldb, int dividefield, int divisor)
        {
            super(dividefield, divisor);
            this.ldb = ldb;
            TimedEventManager.getManager().addTimedEventListener(this);
        }

        public void doEvent()
        {
            ldb.loadlist();

            // FIXME: now this is lame! However, if it works satisfactorily
            // we could make it a little better by renaming this class.
            if (playlist.countItems() == 0)
            {
                if (currentEntry != null) currentEntry.deselect();
                currentEntry = null;
            }
        }
    }
}
