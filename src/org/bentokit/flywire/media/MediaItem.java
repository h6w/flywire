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
package org.bentokit.flywire.media;

import java.io.*;
import java.util.Vector;
import java.util.ArrayList;
import javax.media.*;
import javax.media.format.*;
import javax.swing.SwingWorker;

import java.net.URI;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.event.MediaItemEvent;
import org.bentokit.flywire.event.MediaListener;
import org.bentokit.flywire.util.FileBabysitter;
import org.bentokit.flywire.util.FileListener;

/**
 * A MediaItem is a wrapper connecting a URI to a Player and a MediaInfo instance.
 * 
 * At first, the player is null, and the 
 * 
 * @author tudor
 *
 */
public final class MediaItem implements ControllerListener, FileListener, Comparable<MediaItem>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -35016286517561100L;
	
	private static final boolean DEBUG_DEFAULT = false;
    private boolean DEBUG = DEBUG_DEFAULT;

    protected MediaInfo info;
    protected Player player;
    protected int lastKnownState;
    Time duration;
    URI uri;
    

    ///////////////////////////////////////////////////////////////////////////
    // Constructors ///////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public MediaItem(URI uri) {
        info = new MediaInfo(this);
        setURI(uri);
        duration  = null;
    	ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:New MediaItem created.");
    }
   
    ///////////////////////////////////////////////////////////////////////////
    // Player Methods /////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public void createPlayer()
    {
        // FIXME: we get called when a player has already been instantiated.
        // This is hideously wasteful (this change improved real load times
        // by a factor of 4). The problem should be fixed in the calling
        // logic. For expedience I've put it here. (cbp - Oct 2009)
        if (player == null)
        {
            try {
                try {
                    player = Manager.createPlayer(uri.toURL());
                } catch (NoPlayerException e) {
                    ErrorHandler.error(e);
                    //ErrorHandler.fatal("Could not create player for " + uri);
                    notifyListenersMediaUnplayable();
                    return;
                }
            } catch (IOException e) {
                ErrorHandler.fatal(this.getURI()+":MediaItem:IO exception creating player.");
                notifyListenersMediaUnplayable();
                return;
            }
            
            player.addControllerListener(this);
            player.prefetch();

            // To enable me to better find and fix the calling logic I added
            // this message. We can lose it once the "problem" is fixed.
            // (cbp - March 2010).
            ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:createPlayer - creating player for " + uri);
        } else {
            ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:createPlayer - player exists (" + uri + ")");
            return;
        }

    }

    public void lineup() {
        this.createPlayer();
    }

	public Player getPlayer() {
		return this.player;
	}
    
    public void start() {
        ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:Starting:creating player...");
        createPlayer();
        ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:Starting:player created.  Calling player.start()...");
        player.start();
        ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:Starting:player started.");        
    }

    /**
     *  Set the player to the start, instantiating a player if necessary. 
     */
    public void restart() { 
        if (player == null) { createPlayer(); }
        // BUGCHECK: javax.media.NotRealizedError: Cannot set media time on a unrealized controller
        player.setMediaTime(new Time(0)); 
    }

    /**
     *  Tell the player to stop, instantiating a player if necessary. 
     */
    public void stop() { 
        if (player == null) return;
        player.stop();
        player.setMediaTime(new Time(0)); 
    }


    /**
     * Destroys the player
     */
    public void destroyPlayer()
    {
        ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:Destroying player.");
        if (player != null)
        {
            player.removeControllerListener(this);
            player.close();
            player = null;
        }
        notifyListeners(new MediaItemEvent(this,MediaItemEvent.State.NONEXISTENT.getControllerState()));
    }

    ///////////////////////////////////////////////////////////////////////////
    // URI Methods ////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    /**
     * NOTE: if you're using this to replace a file you must cleanup first.
     * @param file
     */
    protected void setURI(URI uri) {
        if (player != null) this.destroyPlayer();
        this.uri = uri;
        this.duration = null; //To ensure that a proper duration is obtained if the mediaitem points to a different media.

        if (uri.getScheme().equals("file")) {
        	FileBabysitter.add(new File(uri.getPath()), this);
        }
        
        //Notify MediaInfoListeners that the URI has changed.
        this.info.notifyListeners();
    }

    public URI getURI() {
        return(this.uri);
    }

    ///////////////////////////////////////////////////////////////////////////
    // MediaTime Methods //////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    public Time getCurrentTime() {
        if (player == null) return(new Time(0));
        else return(player.getMediaTime());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Duration Methods ///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    public Time getDuration() { 
        //If we don't have the duration, get it.
        if (this.duration == null) {
            DurationWorker worker = new DurationWorker(this.uri);
            worker.execute(); //Call the SwingWorker thread.
            try {
                this.duration = worker.get();  //Get the result from the SwingWorker Thread (waits for completion).
                if (this.duration != null) this.info.notifyListeners();
                ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:Duration process finished and listeners notified.");
            } catch (java.util.concurrent.ExecutionException ee) {
                ErrorHandler.error(this.getURI()+":MediaItem:ExecutionException while getting Duration.");
            } catch (InterruptedException ie) {
                ErrorHandler.error(this.getURI()+":MediaItem:Interrupted while getting Duration.");
            }
        }
        return(this.duration);
    }

    public void setDuration(Time time) { this.duration = time; }

    public boolean durationUnknown() { return duration == null || duration == Duration.DURATION_UNKNOWN; }

    ///////////////////////////////////////////////////////////////////////////
    // MediaInfo Methods //////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    public void setInfo(MediaInfo info)
    {
        this.info = info;
    }
    
    public MediaInfo getInfo() {
    	return(this.info);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Collection Methods /////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
   

    /**
     * Imposes an arbitrary ordering on MediaItems.
     *
     * @param - m - The MediaItem to compareTo
     * @return - An arbitrary integer that returns a positive or negative value to imply if this
     * item is more or less important than m.
     */
    //TODO: Make this not arbitrary.
    public int compareTo(MediaItem m) {
        return(getURI().toString().compareTo(m.getURI().toString()));
    }

    public void destroy() { destroyPlayer(); }

    ///////////////////////////////////////////////////////////////////////////
    // ControllerListener Methods /////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
   
    /**
       Our controllerUpdate basically is listening for
       the signal to say the JMF has figured out the
       length of the media.   Once it has done that,
       and presuming it hasn't been added to the playlist
       in the meantime, the player is destroyed to preserve
       memory.

       TODO The problem with this is, that for media for which
       the JMF cannot determine the length, that media will be locked
       forever.  We need to set up a timeout thread that says after
       some time or signal we should give up.

       The JMF appears a little unstable at times trying to calculate
       media length.  We may even have to close and reopen the player
       a few times to be sure.
    */
    public void controllerUpdate(ControllerEvent evt) {
        if (evt instanceof AudioDeviceUnavailableEvent) ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:AudioDeviceUnavailableEvent");
        if (evt instanceof CachingControlEvent) ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:CachingControlEvent");
        if (evt instanceof ControllerClosedEvent) {
            String s = this.getURI()+":MediaItem:ControllerClosedEvent";
            if (evt instanceof ControllerErrorEvent) {
                s += ":ControllerErrorEvent";
                if (evt instanceof ConnectionErrorEvent)    
                    s += ":ConnectionErrorEvent";
                if (evt instanceof InternalErrorEvent)    
                    s += ":InternalErrorEvent";
                if (evt instanceof ResourceUnavailableEvent)    
                    s += ":ResourceUnavailableEvent:"+((ResourceUnavailableEvent)evt).getMessage();
            }
            if (evt instanceof DataLostErrorEvent) {
                s += ":DataLostErrorEvent";
            }
            ErrorHandler.info(s);
            notifyListenersMediaUnplayable();
        }
        if (evt instanceof DurationUpdateEvent) ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:DurationUpdateEvent");
        if (evt instanceof FormatChangeEvent) ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:FormatChangeEvent");
        if (evt instanceof MediaTimeSetEvent) ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:MediaTimeSetEvent");
        if (evt instanceof RateChangeEvent) ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:RateChangeEvent");
        if (evt instanceof StopTimeChangeEvent) ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:StopTimeChangeEvent");
        //if (evt instanceof TransitionEvent) ErrorHandler.info("MediaItem:TransitionEvent");
        //Notification of listeners.
        String tag = "MediaItem.controllerUpdate[" + this.uri + "]";
        if (DEBUG) ErrorHandler.info(tag + " - entry");
        if (DEBUG) ErrorHandler.info(tag + " - e == " + evt);
        if (evt instanceof TransitionEvent) {
        	TransitionEvent te = (TransitionEvent) evt;
            if (te.getCurrentState() != lastKnownState) {  //Filter out duplicate events
                MediaItemEvent mie = new MediaItemEvent(this,te.getCurrentState());
            	notifyListeners(mie);

                if (mie.getState() == MediaItemEvent.State.PREFETCHED) {
                    this.player.setMediaTime(new Time(0));
                    notifyListenersMediaReady();
                }
                if (mie.getState() == MediaItemEvent.State.STARTED) {
                    notifyListenersMediaPlaying();
                }
                lastKnownState = te.getCurrentState();
            }
        }
        if (evt instanceof EndOfMediaEvent) {
            ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:EndOfMediaEvent");
            notifyListenersMediaPlayed();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // FileListener Methods ///////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
   
	@Override
	public void fileChanged(File file) {
		//Restart the media
		//BUGCHECK: This may be an unwise thing to do if the player is currently playing.
		//
        /*
		try {
			findDuration();
		} catch (Exception e) {
			ErrorHandler.error("Unable to get duration for "+uri);
		}
        */
		
		if (player != null) {
	        destroyPlayer();                          
        	createPlayer();
		}
        synchronized(listeners) {
		    for (MediaListener listener : listeners) {
			    listener.mediaChanged(this);
		    }
        }
	}

	@Override
	public void fileDeleted(File file) {
		//Notify listeners of our destruction
        synchronized(listeners) {        
		    for (MediaListener listener : listeners) {
			    listener.mediaDeleted(this);
		    }
        }
		//Destroy
		destroy();
	}


    ///////////////////////////////////////////////////////////////////////////
    // Listeners and Listener Management //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
	
    protected volatile ArrayList<MediaListener> listeners = new ArrayList<MediaListener>();

	public void addListener(MediaListener listener) {
	    if (!listeners.contains(listener))
		    this.listeners.add(listener);
    }
    
    public void removeListener(MediaListener listener) {
    	this.listeners.remove(listener);
	    ErrorHandler.info(this.getInfo().get("filename")+":MediaItem:"+listener.getClass().getName()+" stopped listening.");
    }
    
    public void notifyListeners(MediaItemEvent event) {
    	for (MediaListener listener : new ArrayList<MediaListener>(listeners)) {
    		listener.stateChanged(event);
    	}
    }

    public void notifyListenersMediaReady() {
    	for (MediaListener listener : new ArrayList<MediaListener>(listeners)) {
    		listener.mediaReady(this);
    	}
    }

    public void notifyListenersMediaPlaying() {
    	for (MediaListener listener : new ArrayList<MediaListener>(listeners)) {
    		listener.mediaPlaying(this);
    	}
    }

    public void notifyListenersMediaPlayed() {
    	for (MediaListener listener : new ArrayList<MediaListener>(listeners)) {
    		listener.mediaPlayed(this);
    	}
    }

    public void notifyListenersMediaUnplayable() {
    	for (MediaListener listener : new ArrayList<MediaListener>(listeners)) {
    		listener.mediaUnplayable(this);
    	}
    }

    ///////////////////////////////////////////////////////////////////////////
    // DurationWorker /////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    class DurationWorker extends SwingWorker<Time,Void> implements ControllerListener {
        Object waitSync = new Object();
        boolean stateTransitionOK = true;

        Player player;
        URI uri;

        public DurationWorker(URI uri) {
            this.uri = uri;
        }

        /**
        * Block until the player has transitioned to the given state.
        * Return false if the transition failed.
        */
        boolean waitForState(int state) {
            synchronized (waitSync) {
                try {
                    while (this.player.getState() < state && stateTransitionOK)
                    waitSync.wait();
                } catch (Exception e) {}
            }
            return stateTransitionOK;
        }

        ///////////////////////////////////////////////////////////////////////////
        // SwingWorker Methods ///////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////
       
	    @Override
	    public Time doInBackground() {
            Time duration;
            try {
                //System.err.println("Creating player.");
                this.player = Manager.createPlayer(this.uri.toURL());
                //System.err.println("Adding listener.");
                this.player.addControllerListener(this);
                //System.err.println("Initiate realization.");
                this.player.realize();
                //System.err.println("Wait for realization.");
                if (!waitForState(this.player.Realized)) {
                    //System.err.println("Failed to realize the player.");
                    return(Duration.DURATION_UNKNOWN);
                }
                //System.err.println("Get time.");
                duration = this.player.getDuration();
                if (duration == Duration.DURATION_UNBOUNDED)
                    ErrorHandler.info(MediaItem.this.getInfo().get("filename")+":MediaItem:DurationWorker:JMF attempt at duration returned DURATION_UNBOUNDED");
                else if (duration == Duration.DURATION_UNKNOWN)
                    ErrorHandler.info(MediaItem.this.getInfo().get("filename")+":MediaItem:DurationWorker:JMF attempt at duration returned DURATION_UNKNOWN");
                else if (duration == Time.TIME_UNKNOWN)
                    ErrorHandler.info(MediaItem.this.getInfo().get("filename")+":MediaItem:DurationWorker:JMF attempt at duration returned TIME_UNKNOWN");
                else
                    ErrorHandler.info(MediaItem.this.getInfo().get("filename")+":MediaItem:DurationWorker:JMF attempt at duration returned "+(duration.getNanoseconds()/1000000000)+" seconds.");
                this.player.close();
                this.player = null;
                //System.err.println("DurationWorker process finished.");
                return duration; 
            } catch (NoPlayerException npe) {
                ErrorHandler.error(this.uri+":MediaItem:NoPlayerException.");
                ErrorHandler.error(npe.getMessage());
                for (StackTraceElement ste : npe.getStackTrace())
                    ErrorHandler.error(ste.toString());
            } catch (IOException ioe) {
                ErrorHandler.error(this.uri+":MediaItem:NoPlayerException.");
                ErrorHandler.error(ioe.getMessage());
                for (StackTraceElement ste : ioe.getStackTrace())
                    ErrorHandler.error(ste.toString());
            }
            return(null);
	    }

        ///////////////////////////////////////////////////////////////////////////
        // ControllerListener Methods /////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////
   
        /**
           This controllerUpdate is basically listening for
           the signal to say the JMF has figured out the
           length of the media.   Once it has done that,
           the player is destroyed to preserve memory.

           This means that this player is entirely separate
           from the player used for playback.


           BUGCHECK This is the recommended way to obtain duration from
           a url.  It does not appear to lock the way the previous
           approach did, but has not yet been extensively tested.

            NOTE: From the JMF 2.0 API Docs for javax.media.Player:
               "Because a Player cannot always know the duration of 
                the media it is playing, the Duration interface defines 
                that getDuration returns Duration.DURATION_UNKNOWN until 
                the duration can be determined. A DurationUpdateEvent is 
                generated when the Player can determine its duration or 
                the if its duration changes, which can happen at any time. 
                When the end of the media is reached, the duration should 
                be known."
        */
        public void controllerUpdate(ControllerEvent evt) {
            //Internal MediaItem checks.  e.g. Waiting for a particular state for duration or play.
            if (evt instanceof ConfigureCompleteEvent ||
                evt instanceof RealizeCompleteEvent ||
                evt instanceof PrefetchCompleteEvent) {
                synchronized (waitSync) {
                    stateTransitionOK = true;
                    waitSync.notifyAll();
                }
            } else if (evt instanceof ResourceUnavailableEvent) {
                synchronized (waitSync) {
                    stateTransitionOK = false;
                    waitSync.notifyAll();
                }
            } else if (evt instanceof EndOfMediaEvent) {
                this.player.setMediaTime(new Time(0));
            } else if (evt instanceof SizeChangeEvent) {
            }
        }
    }
}
