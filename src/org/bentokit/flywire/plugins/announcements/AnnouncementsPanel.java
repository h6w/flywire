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
package org.bentokit.flywire.plugins.announcements;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.media.Time;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.event.TimedEventAdapter;
import org.bentokit.flywire.event.TimedEventManager;
import org.bentokit.flywire.gui.SelectionListPanel;
import org.bentokit.flywire.gui.panels.MediaPanelFactory;
import org.bentokit.flywire.media.Announcements;
import org.bentokit.flywire.media.AnnouncementsListener;
import org.bentokit.flywire.media.MediaItem;
import org.bentokit.flywire.media.PlayList;
import org.bentokit.flywire.media.SelectionList;
import org.bentokit.flywire.util.TimeNumberFormat;


public class AnnouncementsPanel extends JPanel implements AnnouncementsListener, MouseListener, Runnable {
    public static final long serialVersionUID = 1L; //Why do we do this?

    Announcements announcements;
    SelectionListPanel selectionListPanel;
    JPanel noAnnouncementsPanel;

    JPanel timePanel;
    JLabel timeLabel;
    JButton addAllButton;
    Calendar nextAnnouncementTime;
    SimpleDateFormat parsedateformat;
    SimpleDateFormat printtimeformat;

    AnnouncementTimeEvent announcement_event;

    private volatile Thread flashThread;

    MediaPanelFactory factory;
    KeyListener keyListener;

    public AnnouncementsPanel(Announcements announcements, MediaPanelFactory factory, KeyListener k) {
        this.announcements = announcements;

        this.factory = factory;
        this.keyListener = k;

        this.noAnnouncementsPanel = new JPanel();
        this.noAnnouncementsPanel.setLayout(new BorderLayout());
        this.noAnnouncementsPanel.setBorder(new EmptyBorder(20,20,20,20));
        this.noAnnouncementsPanel.add(new JLabel("No Announcements"),BorderLayout.CENTER);
        

        timePanel = new JPanel(new BorderLayout());

        timeLabel = new JLabel("00:00");
        timeLabel.setFont(new Font("Sans Serif",Font.BOLD,25));

        addAllButton = new JButton("Add All");
        addAllButton.setFont(new Font("Sans Serif",Font.PLAIN,15));
        addAllButton.addMouseListener(this);
        JPanel addAllButtonPanel = new JPanel();
        addAllButtonPanel.add(addAllButton);

        timePanel.add(timeLabel,"Center");
        timePanel.add(addAllButtonPanel,"East");

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Sponsorship Announcements",
                       TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                       new Font("Sans Serif",Font.BOLD,20)));
        this.add(timePanel,"North");
        this.add(this.noAnnouncementsPanel,"Center");

        this.addKeyListener(k);
        addAllButton.addKeyListener(k);

        parsedateformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss",Locale.ENGLISH);
        printtimeformat = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);

        //Check that we haven't gone past an announcement every 100 seconds
        announcement_event = new AnnouncementTimeEvent(this, Calendar.SECOND, 100);

        flashThread = null;

        announcements.addListener(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // MouseListener interface ////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public void mouseClicked(MouseEvent e) {
        this.announcements.addAll();
    }
    public void mouseEntered(MouseEvent e) { ; }
    public void mouseExited(MouseEvent e) { ; }
    public void mousePressed(MouseEvent e) { ; }
    public void mouseReleased(MouseEvent e) { ; }
    
    ///////////////////////////////////////////////////////////////////////////
    // AnnouncementsListener interface ////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public void announcementsChanged(Calendar newTime, SelectionList newSelection) {
        this.timeLabel.setText(TimeNumberFormat.formatTimeString(newTime));
        if (this.selectionListPanel != null) this.remove(this.selectionListPanel);
        else this.remove(noAnnouncementsPanel);
        this.selectionListPanel = new SelectionListPanel(newSelection,this.factory,this.keyListener);
        this.add(this.selectionListPanel);
        this.validate();
    }

    public void announcementsNone() {
        flashThread = null;
        this.timeLabel.setForeground(Color.black);
        this.timeLabel.setText("No announcements");
        if (this.selectionListPanel != null) this.remove(this.selectionListPanel);
        this.add(noAnnouncementsPanel);
        this.validate();
    }

    public void announcementsError(String message) {
        JOptionPane.showMessageDialog(null, message, "Alert", JOptionPane.ERROR_MESSAGE); 
    }

    public void checkAnnouncementTime() {
        Calendar currentTime = Calendar.getInstance();
        //ErrorHandler.info("currentTime:"+currentTime.getTime());
        //ErrorHandler.info("NextTime:"+nextAnnouncementTime.getTime());

        if (currentTime.after(nextAnnouncementTime)) {
            if (flashThread == null) {
                flashThread = new Thread(this);
                flashThread.start();
            }
        } else {
            flashThread = null;
            //timePanel.setBackground(Color.lightGray);
            timeLabel.setForeground(Color.black);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Runnable interface /////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public void run() {
        while (flashThread != null) {
            if (timeLabel.getForeground() == Color.black) {
                timeLabel.setForeground(Color.red.brighter().brighter());
            } else {
                timeLabel.setForeground(Color.black);
            }

            try {
                Thread.sleep(750);
            }
            catch (InterruptedException ie) {
            }
        }
        timeLabel.setForeground(Color.black);
    }

}

class AnnouncementTimeEvent extends TimedEventAdapter
{
    AnnouncementsPanel p;
    public AnnouncementTimeEvent(AnnouncementsPanel p, int dividefield, int divisor)
    {
        super(dividefield, divisor);
        this.p = p;
        TimedEventManager.getManager().addTimedEventListener(this);
    }

    public void doEvent() {
        //ErrorHandler.info("Checking announcement time via event");
        p.checkAnnouncementTime();
    }
}
