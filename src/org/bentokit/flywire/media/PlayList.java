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

import java.util.ArrayList;
import java.util.Vector;
import java.util.Iterator;

import javax.media.ControllerEvent;
import javax.media.TransitionEvent;
import javax.media.ControllerListener;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.event.MediaItemEvent;
import org.bentokit.flywire.event.MediaListener;
import org.bentokit.flywire.event.PlayListListener;

public class PlayList implements MediaListener, Runnable {
    public static final long serialVersionUID = 1L; //Why do we do this?

    volatile Vector<MediaItem> list;

    boolean sequence;
    boolean cueMode;
    boolean repeatMode;

	//boolean playable = false;
	//boolean stoppable = false;
	volatile MediaItem playing = null;
	
    // timer Thread
    protected Thread timeThread = null;


    public PlayList() {
        list = new Vector<MediaItem>();
        //playcontroller.addListener(this);
        sequence = false;
        cueMode = false;
        repeatMode = false;
    }

    /**
     *  Add a MediaItem to this Playlist. 
     *
     *  @param item - The MediaItem to add to the playlist.
     *  
     **/

    public int add(MediaItem item) {
        return add(item, true);
    }

    /**
     * Add a MediaItem to this Playlist.
     * @param item - The MediaItem to add to the playlist.
     * @param noDuplicates - True if adding a duplicate to this playlist is not allowed.
     * @return
     */
    public int add(MediaItem item, boolean noDuplicates) {
        ErrorHandler.info(item.getInfo().get("filename")+":PlayList:New Item being added to PlayList");
        //playable(true);
        if (noDuplicates && contains(item))
            return 0;

        // Must add the item to the list...
        list.add(item);
        item.addListener(this);
        ErrorHandler.info(item.getInfo().get("filename")+":PlayList:New Item added to PlayList, notifying listeners...");

        notifyListenersAdd(item);
        ErrorHandler.info(item.getInfo().get("filename")+":PlayList:Time so far:"+getTimeSoFar());
        notifyListenersTimeChange(getTotalTime(),getTimeSoFar());

        ErrorHandler.info(item.getInfo().get("filename")+":PlayList:New Item added to PlayList, listeners notified. Lining up...");

        item.lineup();

        ErrorHandler.info(item.getInfo().get("filename")+":PlayList:New Item added to PlayList, listeners notified. Lined up.");

        return list.size();
    }


    public boolean contains(MediaItem mediaItem)
    {
    	return(list.contains(mediaItem));
    }


    /** Removes the specified mediaitem from the playlist **/
    public void remove(MediaItem target)
    {
        ErrorHandler.info("Removing: " + target.getURI());
        if (!contains(target)) {
            ErrorHandler.error("Trying to remove an item not in the list!");
        	return;
        }

        
        if (isCurrentlyPlaying(target))
        {
            playing.stop();
            if (list.size() > 1) {
                playing = next();
                //start();
            }
        }
		target.removeListener(this);
        list.remove(target);
        target.destroyPlayer();

        if (list.size() <= 0) {
            notifyListenersPlayable(false);
            notifyListenersStoppable(false);
			playing = null;
        }
		
        notifyListenersRemove(target);
        notifyListenersTimeChange(getTotalTime(),getTimeSoFar());
    }

    /** Clears the playlist **/

    public void clearAll() {
        for (Iterator<MediaItem> iter = this.list.listIterator(); iter.hasNext();) {
            MediaItem item = iter.next();
            item.destroyPlayer();
            item.removeListener(this);
            notifyListenersRemove(item);
            iter.remove();
        }
        notifyListenersPlayable(false);
        notifyListenersStoppable(false);
        notifyListenersTimeChange(getTotalTime(),getTimeSoFar());
    }

    public int countItems() {
        return(list.size());
    }

    /*
    private void notifyListenersDisableCueMode() {
		for (PlayListListener listener : listeners) {
			listener.disableCueMode();
		}	
	}
	*/

	/** Set whether we want to continue playing after the media has finished. **/

    public void setSequence(boolean isSequence) {
            sequence = isSequence;
    }

    //public void notifyMediaAvailable() {
    //        playcontrol.mediaAvailable();
    //}

    /*
    public long getNextDuration() {
            if (list.size() <= 0) return 0;
            return(next().getDuration().getNanoseconds());
    }
    */

    protected long getTime()
    {
        long t = 0;
        int durationunknowncount = 0;

        for (MediaItem m : list)
        {
            while (m.durationUnknown() && durationunknowncount < 20)
            {
                try { Thread.sleep(50); }
                catch (InterruptedException ie) { }
                // BUGCHECK: viewed from in here, this could stall
                // the owning thread for up to getComponentCount()
                // seconds. (cbp)
                durationunknowncount++;
            }
            if (!m.durationUnknown())
                t += m.getDuration().getNanoseconds();
        }
        return t;
    }

    /** Returns the total amount of time on the playlist in nanoseconds **/
    public long getTotalTime()
    {
        return getTime();
    }

    /** Returns the total amount of time on the playlist up to the current track in nanoseconds **/
    public long getTimeSoFar()
    {
        long t = 0;
        int durationunknowncount = 0;
        boolean currentSeen = false;

        if (this.playing == null) return(0);

        for (MediaItem m : list)
        {
            if (m.durationUnknown()) return(-1);  //Now return unknown if any item has duration unknown.

       		if (m.equals(this.playing)) {
                return (t+m.getCurrentTime().getNanoseconds());
            }
        	
            t += m.getDuration().getNanoseconds();
        }
        return t;
    }


    /** Updates the highlight to the currently playing mediaitem */
    /*
    public void updateSelect() {
        if (currentlyPlaying != null) {
            Object[] mediaitems = list.toArray();
            list.scrollRectToVisible(currentlyPlaying.getBounds());
            for (int i = 0; i < mediaitems.length; i++) {
                    PlayListMediaPanel m = (PlayListMediaPanel)mediaitems[i];
                if (currentlyPlaying == m)
                    m.select();
                else m.deselect();
            }
        }
    }
    */

    public MediaItem next() {
        if (playing == null && list.size() > 0) return(list.get(0));
        int currentIndex = list.indexOf(playing);
        if (currentIndex+1 >= list.size()) return(null);
        else return(list.get(currentIndex+1));
    }

    public void setCueMode(boolean cueMode) {
    	if (this.cueMode != cueMode) { //To avoid a race condition.
    		for (PlayListListener listener : listeners) {
    			listener.setCueMode(cueMode);
    		}
    	}
    	this.cueMode = cueMode;
    }

    public boolean isCueMode() { return(this.cueMode); }
    
    public void setRepeatMode(boolean repeatMode) {
    	this.repeatMode = repeatMode;
    }
    
	//public Track playingTrack() { return(this.currentlyPlaying); }
	
	//public void playable(boolean playable) { this.playable = playable; }
	
	//public boolean isPlayable() { return(this.playable); }

	//public void stoppable(boolean stoppable) { this.stoppable = stoppable; }
	
	//public boolean isStoppable() { return(this.stoppable); }

    public boolean isCurrentlyPlaying(MediaItem item)
    {
        return item == playing;
    }
	
    public void lineup(MediaItem m) {
    	this.playing = m;
    }
    
    /** Start playing the media in the playlist in order **/

    public void start() {
        if (playing == null) this.playing = next();
        if (playing == null) {
            ErrorHandler.error("No item resulted from next()");
            return;
        }

        //Create the time manager thread
        timeThread = new Thread(this);
        timeThread.start();

        //Start playing.
        playing.start();
        //ErrorHandler.info("StartedEvent");
        //if (list.size() > 0) {
        //    ErrorHandler.info("Starting "+playing.getURI());
        //    lineup(list.get(0));
        //} else {
        //    ErrorHandler.error("Nothing in playlist!");
        //}
    }

    /** Stop playing the media in the playlist and move to the next item in the playlist **/

    public void stop() {
		//ErrorHandler.info("StoppedEvent");
		if (list.size() > 0) {
                notifyListenersPlayable(true);
        } else notifyListenersPlayable(false);
		notifyListenersStoppable(false);
        playing.stop();
        playing = null;
		timeThread = null;
    }


	/*** Methods implemented for PlayListListener ***/

    ///////////////////////////////////////////////////////////////////////////
    // ControllerListener interface ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
/*
    @Override
	public void controllerUpdate(ControllerEvent event) {
    	if (event instanceof javax.media.TransitionEvent) {
            System.err.print("PlayList received TransitionEvent:");
            int state = ((TransitionEvent)event).getCurrentState();
            if (state == MediaItemEvent.State.NONEXISTENT.getControllerState()) System.err.println("Nonexistent");
            else if (state == MediaItemEvent.State.PREFETCHING.getControllerState()) System.err.println("Prefetching");
            else if (state == MediaItemEvent.State.PREFETCHED.getControllerState()) System.err.println("Prefetched");
            else if (state == MediaItemEvent.State.REALIZING.getControllerState()) System.err.println("Realizing");
            else if (state == MediaItemEvent.State.REALIZED.getControllerState()) System.err.println("Realized");
            else if (state == MediaItemEvent.State.STARTED.getControllerState()) System.err.println("Started");
			else System.err.println("UNKNOWN!");
        }

    	if (event instanceof javax.media.TransitionEvent) {
            if (((TransitionEvent)event).getCurrentState() == MediaItemEvent.State.PREFETCHED.getControllerState()) {
                notifyListenersPlayable(true);
            }
        }
    	if (event instanceof javax.media.TransitionEvent) {
            if (((TransitionEvent)event).getCurrentState() == MediaItemEvent.State.STARTED.getControllerState()) {
                notifyListenersStoppable(true);
            }
        }
    	if (event instanceof javax.media.EndOfMediaEvent) {
    		javax.media.EndOfMediaEvent endEvent = (javax.media.EndOfMediaEvent) event;
    		if (playing != null && endEvent.getSource().equals(playing.getPlayer())) {
    			EndOfMediaEvent();
    		} else {
    			ErrorHandler.error("Received EndOfMediaEvent for a non-playing track.");
    		}
    	}
	}    
*/
    
    /** If not at the end of the playlist, and sequence is true,
    moves to the next item in the playlist and starts playing.
	Otherwise, stop playing.
	Called by playControl when the media has finished.
	**/
	
/*
	public void EndOfMediaEvent() {
	    //ErrorHandler.info("EndOfMediaEvent");
	    lineup(next());
	    if (playing != null) {
	        //ErrorHandler.error("currentlyPlaying != null, starting currentPlaying");
	        start();
	    } else {
	        if (cueMode || repeatMode) {
	            ErrorHandler.info("Running in " + (cueMode ? "cue" : "repeat") + " mode, returning to beginning");
	            playing = list.get(0);
	            //updateSelect();
	            if (cueMode)
	                setCueMode(false);
	            else
	                start();
	        } else {
	            ErrorHandler.info("Returning "+list.size()+" items");
	            while (list.size() > 0) {
                    ErrorHandler.info("Getting the component.");
                    //MediaItem item = list.get(0);
                    //m.clearNumber();
                    ErrorHandler.info("Deselecting it.");
                    //item.deselect();
                    ErrorHandler.info("Returning it to the sender.");
                    //item.returnToSender(true);
                    ErrorHandler.info(list.size()+" items remaining.");
	            }
	            notifyListenersPlayable(true);
	        }
	    }
	    if (list.size() > 0) {
	            notifyListenersPlayable(true);
	    } else {
	        notifyListenersPlayable(false);
	    }
	}
*/

    ///////////////////////////////////////////////////////////////////////////
    // MediaListener interface ////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	@Override
	public void disappeared(MediaItem item) { ; }

	@Override
	public void stateChanged(MediaItemEvent event) { ; }

	@Override
	public void mediaReady(MediaItem mediaItem) {
        ErrorHandler.info(mediaItem.getInfo().get("filename")+":PlayList:Playlist received MediaReady event");
        notifyListenersPlayable(true);
    }

	@Override
	public void mediaPlaying(MediaItem mediaItem) {
        ErrorHandler.info(mediaItem.getInfo().get("filename")+":PlayList:Playlist received MediaPlaying event");
        notifyListenersStoppable(true);
    }

	@Override
	public void mediaPlayed(MediaItem mediaItem) { 
        this.playing = next();
        if (this.playing != null) {
            start();
        } else {
            if (!(this.cueMode || this.repeatMode)) clearAll();
            if (this.repeatMode) start();
        }
    }

	@Override
	public void mediaChanged(MediaItem mediaItem) { ; }

	@Override
	public void mediaDeleted(MediaItem mediaItem) { ; }

	@Override
	public void mediaUnplayable(MediaItem mediaItem) { ; }

    ///////////////////////////////////////////////////////////////////////////
    // Listeners and Listener Management //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
	
    volatile ArrayList<PlayListListener> listeners = new ArrayList<PlayListListener>();

	public void addListener(PlayListListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(PlayListListener listener) {
		this.listeners.remove(listener);
	}
	    
    public void notifyListenersPlayable(boolean playable) {
    	for (PlayListListener listener : new ArrayList<PlayListListener>(listeners)) {
    		listener.playable(playable);
    	}
    }

    public void notifyListenersStoppable(boolean stoppable) {
    	for (PlayListListener listener : new ArrayList<PlayListListener>(listeners)) {
    		listener.stoppable(stoppable);
    	}
    }

    public void notifyListenersAdd(MediaItem item) {
    	for (PlayListListener listener : new ArrayList<PlayListListener>(listeners)) {
    		listener.mediaAdded(item);
    	}
    }

    public void notifyListenersRemove(MediaItem item) {
    	for (PlayListListener listener : new ArrayList<PlayListListener>(listeners)) {
    		listener.mediaRemoved(item);
    	}
    }

    public void notifyListenersTimeChange(long total, long sofar) {
    	for (PlayListListener listener : new ArrayList<PlayListListener>(listeners)) {
    		listener.timesChanged(total,sofar);
    	}
    }

    ///////////////////////////////////////////////////////////////////////////
    // Running Player Update Thread ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
	
    /**
     * The media time update thread
     */
    public void run() {
        long total = -1;
        long sofar = -1;
        while (this.playing != null) {
            if (total != getTotalTime() || sofar != getTimeSoFar()) {
                total = getTotalTime();
                sofar = getTimeSoFar(); 
                this.notifyListenersTimeChange(total,sofar);
            }
            try {
                    Thread.sleep(100);
            }
            catch (InterruptedException ie) {
            }
        }
    }
}
