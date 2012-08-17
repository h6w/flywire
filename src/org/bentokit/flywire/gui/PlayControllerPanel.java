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
package org.bentokit.flywire.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.URI;
import java.io.IOException;
import java.io.File;
import javax.media.*;

import org.bentokit.flywire.Flywire;
import org.bentokit.flywire.Flywire.PlayMode;
import org.bentokit.flywire.config.Config;
import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.event.PlayListListener;
import org.bentokit.flywire.media.AutomaticDJ;
import org.bentokit.flywire.media.MediaItem;
import org.bentokit.flywire.media.PlayList;
import org.bentokit.flywire.media.Track;
import org.bentokit.flywire.util.TimeNumberFormat;

public class PlayControllerPanel extends JPanel implements PlayListListener, ActionListener {
    public static final long serialVersionUID = 1L; //Why do we do this?

    public static final String imagedir = "bin";
    // TODO: access control
    //Sound hashtable
    //SoundList soundList;
    //The currently chosen file
    URI chosenFile;
    //The URI of the program file
    URI codeBase;
    // timer Thread
    protected Thread timeThread = null;

    // Current Player
    Track currenttrack;

    // PlayList
    PlayList playlist;

    //Automatic DJ
    AutomaticDJ autodj;

    JButton playButton, stopButton, playallButton;
    JSlider progressBar;
    JLabel digitalTimer;
    int timex;

    // TODO: externalise?
    /**
     * These two integers control the prefetch retry logic in start(). If the
     * next MediaItem/Track is not prefetched on entry, we wait a total of
     * PREFETCH_TIMEOUT ms while checking the state every PREFETCH_STEP.
     *
     * I've tweaked these values after carefully observing the behaviour of the
     * system with uncompressed PCM media stored on the local disk. See the
     * comments in start() for more info.
     *
     * WARNING: there can be no audible output while the retry is in progress.
     */
    private static final int PREFETCH_TIMEOUT = 168; // total milliseconds
    private static final int PREFETCH_STEP = 12; // step milliseconds

    /**
     * Create the media player.
     */
    public PlayControllerPanel(PlayList playlist, KeyListener k) {
        try {
            codeBase = new URI("file:" + System.getProperty("user.dir") + "/");
        } catch (java.net.URISyntaxException urise) {
            urise.printStackTrace();
        }

        playButton = new JButton("Play One");
        playButton.addActionListener(this);

        playallButton = new JButton("", new ImageIcon(imagedir+File.separator+"play.png"));
        playallButton.setDisabledIcon(new ImageIcon(imagedir+File.separator+"playdisabled.png"));
        playallButton.setBorderPainted(false);
        playallButton.setContentAreaFilled(false);
        playallButton.addKeyListener(k);
        //playallButton.setMnemonic(KeyEvent.VK_P);
        playallButton.addActionListener(this);

        stopButton = new JButton("", new ImageIcon(imagedir+File.separator+"stop.png"));
        stopButton.setDisabledIcon(new ImageIcon(imagedir+File.separator+"stopdisabled.png"));
        stopButton.addKeyListener(k);
        stopButton.setBorderPainted(false);
        stopButton.setContentAreaFilled(false);
        stopButton.addActionListener(this);

        progressBar = new JSlider(JSlider.HORIZONTAL,0,1000,0);
        progressBar.setEnabled(false);

        digitalTimer = new JLabel("00:00:00");
        digitalTimer.setFont(new Font("Sans Serif",Font.BOLD,20));

        playable(false);
        stoppable(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playallButton);
        buttonPanel.add(stopButton);

        this.setLayout(new BorderLayout());

        //this.add(playButton);
        this.add(buttonPanel,"West");
        this.add(progressBar,"Center");
        this.setVisible(true);

        currenttrack = null;

        setPlayList(playlist);

        // Create the slider update thread
        /*
        timeThread = new Thread(this);
        timeThread.start();
        */
    }

    public void setPlayList(PlayList playlist) {
        this.playlist = playlist;
        playlist.addListener(this);
    }

    public void setAutomaticDJ(AutomaticDJ autodj) {
                this.autodj = autodj;
    }


    public JLabel getDigitalTimer() {
        return(digitalTimer);
    }


    //void addMedia(String filename) {
        //        soundList.startLoading(filename);
        //}

/*
    public Player getPlayer(String filename) {
        //ErrorHandler.info("Getting a player");
        MediaLocator mrl = null;
        Player player = null;

        chosenFile = new URI(codeBase, filename);
        //ErrorHandler.info("chosenFileURI:"+chosenFile);
        try {
            // Create a media locator from the file name
            mrl = new MediaLocator(chosenFile);

            // Create an instance of a player for this media
            try {
                player = Manager.createPlayer(mrl);
            } catch (NoPlayerException e) {
                ErrorHandler.error(e);
                ErrorHandler.fatal("Could not create player for " + mrl);
            }

        } catch (IOException e) {
            ErrorHandler.fatal("IO exception creating player for " + mrl);
        }
        
        if (player != null) player.prefetch();

        ErrorHandler.info("PlayControl.getPlayer - player for " + filename + " == " + player);
        return player;
    }
*/

        //void removeMedia(String filename) {
        //        soundList.remove(filename);
        //}

    /**
     * Start media file playback. This function is called the
     * first time that the Applet runs and every
     * time the user re-enters the page.
     */
/*
    public void start(Track track) throws javax.media.MediaError
    {
        if (currenttrack != null) {
            currenttrack.getPlayer().removeControllerListener(this);
            ErrorHandler.info("PlayControl.start - removed controllerListener from " + currenttrack.getName());
            // Once the listener is detached we no longer have a currenttrack.
            currenttrack = null;
        }
        assert currenttrack == null;
        // BUGCHECK: the original implementor's assumption was that we'd always
        // leave here with a valid current track. That's an invalid assertion.
        // I'm now invalidating currenttrack although I've not yet analysed the
        // consequences of doing this. cbp - Mar 2010.
        Player trackPlayer = track.getPlayer();
        assert trackPlayer != null;
        ErrorHandler.info("PlayControl.start - " + track.getName() + " state " + controllerStateToString(trackPlayer.getState(), true) + " -> " + controllerStateToString(trackPlayer.getTargetState(), true));
        assert trackPlayer.getTargetState() == Controller.Prefetched;
        if (trackPlayer.getState() != Controller.Prefetched)
        {
            // FIXME: this logic could get punted up the call chain from where
            // we can take better evasive action.
            ErrorHandler.error("PlayControl.start - " + track.getName() + " not prefetched, retrying...");
            int timeout = PREFETCH_TIMEOUT;

            do
            {
                try { Thread.sleep(PREFETCH_STEP); timeout -= PREFETCH_STEP; }
                catch(Exception e) { timeout = 0; }
            }
            while (timeout > 0 && trackPlayer.getState() != Controller.Prefetched);
            ErrorHandler.error("PlayControl.start - timeout == " + timeout);
            if (trackPlayer.getState() != Controller.Prefetched)
                throw new javax.media.NotPrefetchedError("can't deal with this here");
        }
        // Add ourselves as a listener for a player's events. Doing so makes
        // the track current.
        currenttrack = track;
        trackPlayer.addControllerListener(this);
        ErrorHandler.info("PlayControl.start - added controllerListener to " + track.getName());

        trackPlayer.setMediaTime(new Time(0));
        trackPlayer.start();

        assert currenttrack == track;
    }
*/

    /**
     * Returns the track currently playing or null if no track is playing.
     */

    public Track playingTrack() {
        return(currenttrack);
    }
    /**
     * Stop media file playback and release resource before
     * leaving the page.
     */
    public void stop() {
        if (currenttrack != null) {
            ErrorHandler.info("PlayControl.stop - stopping " + currenttrack.getName());
            playlist.stop();
//            currenttrack.getPlayer().stop();
//            currenttrack.getPlayer().removeControllerListener(this);
            ErrorHandler.info("PlayControl.stop - removed controllerListener from " + currenttrack.getName());
            currenttrack = null;
        }
    }

    
    /**
     * This controllerUpdate function must be defined in order to
     * implement a ControllerListener interface. This
     * function will be called whenever there is a media event
     *
     * FIXME: some of the actions we take trigger events which cause us to
     * be re-entered. This is presumably why the author made us synchronized.
     * Unfortunately, this can cause us to deadlock.
     */

/*
    int controllerUpdateCounter;

    //public synchronized void controllerUpdate(ControllerEvent event) // TODO: remove the synchronized
    public void controllerUpdate(ControllerEvent event) // TODO: remove the synchronized
    {
        ++controllerUpdateCounter;
        try
        {
            ErrorHandler.info("PlayControl.controllerUpdate - entry (" + controllerUpdateCounter + ")");
            if (event instanceof EndOfMediaEvent)
            {
                Flywire.PlayMode mode = Config.getConfig().getLastPlayMode();
                ErrorHandler.info("EOM event");

                synchronized(this)
                {
                    ErrorHandler.info("Sending EOM event to playlist");
                    playlist.EndOfMediaEvent();
                    if (mode == Flywire.PlayMode.AUTODJ)
                    {
                        ErrorHandler.info("Sending EOM event to autodj");
                        autodj.EndOfMediaEvent();
                    }
                }
            }
            else if (event instanceof MediaTimeSetEvent)
            {
                ErrorHandler.info("A media time event");
            }
            else if (event instanceof ControllerClosedEvent)
            {
                ErrorHandler.info("Controller closed - " + event.getSourceController());
            }
            else if (event instanceof ControllerErrorEvent)
            {
                // Tell TypicalPlayerApplet.start() to call it a day
                ErrorHandler.fatal(((ControllerErrorEvent)event).getMessage());
                // FIXME: if this does what I think it does we MUST fix it
            }
            else if (event instanceof TransitionEvent)
            {
                // Tell TypicalPlayerApplet.start() to call it a day
                //Player finishedPlayer = (Player) event.getSource();
                TransitionEvent e = (TransitionEvent) event;
                //if (e.getPreviousState() == e.getTargetState()) {
                    String previousStateStr = controllerStateToString(e.getPreviousState(), false);
                    String currentStateStr = controllerStateToString(e.getCurrentState(), false);
                    String targetStateStr = controllerStateToString(e.getTargetState(), false);
                    ErrorHandler.info(currenttrack == null ? "Unknown Track" : currenttrack.getName() + ":" + previousStateStr + "->" + currentStateStr + "=>" + targetStateStr);
                //}
            }
            else if (event instanceof StopEvent)
            {
                //StopEvent e = (StopEvent) event;
                ErrorHandler.info(currenttrack.getName()+":->Stopped");
            }
            else if (event instanceof DeallocateEvent)
            {
                //DeallocateEvent e = (DeallocateEvent) event;
                ErrorHandler.info(currenttrack.getName()+":->Deallocate");
            }
            else if (event instanceof DurationUpdateEvent)
            {
                // Tell TypicalPlayerApplet.start() to call it a day
                ErrorHandler.info("A duration update event");
            }
            else
            {
                if (currenttrack != null) ErrorHandler.error(currenttrack.getName()+":Unknown Event:"+event.toString());
                else ErrorHandler.error("Unknown Track:Unknown Event:"+event.toString());
            }
        }
        finally
        {
            ErrorHandler.info("PlayControl.controllerUpdate - exit (" + controllerUpdateCounter + ")");
            --controllerUpdateCounter;
        }
    }
*/

    public static String controllerStateToString(int state, boolean showValue)
    {
        String stateStr;

        switch(state)
        {
            case Controller.Prefetched:
                stateStr = "Prefetched"; break;
            case Controller.Prefetching:
                stateStr = "Prefetching"; break;
            case Controller.Realizing:
                stateStr = "Realizing"; break;
            case Controller.Realized:
                stateStr = "Realized"; break;
            case Controller.Started:
                stateStr = "Started"; break;
            case Controller.Unrealized:
                stateStr = "Unrealized"; break;
            default:
                stateStr = "Unknown"; break;
        }
        if (showValue)
            stateStr += " (" + state + ")";

        return stateStr;
    }

    public void actionPerformed(ActionEvent event) {
                Object source = event.getSource();

                //PLAY BUTTON
                if (source == playButton) {
                        playlist.setSequence(false);
                        //Try to get the AudioClip.
                        playlist.start();
                        return;
                }

                //PLAY ALL BUTTON
                if (source == playallButton) {
                        playlist.setSequence(true);
                        //Try to get the AudioClip.
                        playlist.start();
                        return;
                }

                //STOP BUTTON
                if (source == stopButton) {
                        playlist.stop();
                        return;
                }

        }

        /**
         * The media time slider update thread
         */
/*
        public void run() {
                while (true) {
                        long sofar, total;
                        sofar = total = 0;
                        if (playlist != null) {
                                total = playlist.getTotalTime();
                                sofar = playlist.getTimeSoFar();
                        }
                        if (currenttrack != null) {
                                //if (currentplayer.getState() == Player.Started) {
                                        sofar += (currenttrack.getPlayer().getMediaTime().getNanoseconds()/1000000);
                        }

                        if (total >= 0 && total < (long) 3 * 3600 * 1000000000L) {
                                if (progressBar != null) {
                                        timex = (int) (((float) sofar / total) * progressBar.getMaximum());
                                        progressBar.setValue(timex);
                                }
                                digitalTimer.setText(TimeNumberFormat.formatTimeString(new Time((total - sofar) * 1000000)));
                        } else {
                                digitalTimer.setText("UNKNOWN");
                        }
                        try {
                                Thread.sleep(50);
                        }
                        catch (InterruptedException ie) {
                        }
                }
        }
*/

    ///////////////////////////////////////////////////////////////////////////
    // PlayListListener interface /////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	@Override
    public void playable(boolean playable) {
        playButton.setEnabled(playable);
        playallButton.setEnabled(playable);
        //if (!playable) digitalTimer.setText("00:00:00");
    }

    public void stoppable(boolean stoppable) {
        stopButton.setEnabled(stoppable);
        //if (!playable) digitalTimer.setText("00:00:00");
    }

	@Override
	public void setCueMode(boolean cueMode) {
		//DO Nothing.  We don't care if we're in cue mode or not.
	}

    public void mediaAdded(MediaItem item) {
		//DO Nothing.  We don't care if media has been added until it's playable.
    }

    public void mediaRemoved(MediaItem item) {
		//DO Nothing.  We don't care if media has been removed unless it affects playback.
    }

    public void timesChanged(long total, long sofar) {
        if (total >= 0 && total < (long) 3 * 3600 * 1000000000L) {
                if (progressBar != null) {
                        timex = (int) (((float) sofar / total) * progressBar.getMaximum());
                        progressBar.setValue(timex);
                }
        }
    }
}
