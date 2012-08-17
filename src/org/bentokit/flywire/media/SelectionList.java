package org.bentokit.flywire.media;

import java.util.ArrayList;
import java.util.Vector;
import java.util.Collections;
import java.net.URI;

import org.bentokit.flywire.errorutils.ErrorHandler;

public class SelectionList {
	PlayList playlist;
	Vector<MediaItem> items;
    String name; //Name of the selection list for reporting purposes.

	public SelectionList(PlayList playlist, String name) {
		items = new Vector<MediaItem>();
		this.playlist = playlist;
        this.name = name;
	}
	
	public void add(MediaItem item) {
		ErrorHandler.info(item.getInfo().get("filename")+":SelectionList("+name+"):Adding...");
		items.add(item);
        Collections.sort(this.items);
		ErrorHandler.info(item.getInfo().get("filename")+":SelectionList("+name+"):Notifying Listeners...");
		//Notify all the listeners about the new MediaItem.
        notifyListenersMediaAdded(item);
		ErrorHandler.info(item.getInfo().get("filename")+":SelectionList("+name+"):Listeners notified.");
	}
	
    public boolean contains(URI uri) {
        for (MediaItem item : this.items) {
            if (item.getURI().equals(uri)) return(true);
        }
        return(false);
    }

    public PlayList getPlayList() { return(this.playlist); }

	public void remove(MediaItem item) {
		items.remove(item);
        notifyListenersMediaRemoved(item);
	}

    /**
     * @return The first available media item not in the playlist, or null is no items available that are not in the playlist
     **/
    public MediaItem nextMediaItem() throws NoMediaItemAvailableException {
        for (MediaItem item : this.items) {
            if (!playlist.contains(item)) {
                return(item);
            }
        }
        throw new NoMediaItemAvailableException();
    }

	
	public Vector<MediaItem> getItems() {
		return this.items;
	}

    public int size() { return(this.items.size()); }

    public String getName() { return(name); }

    ///////////////////////////////////////////////////////////////////////////
    // Listeners and Listener Management //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	ArrayList<SelectionListListener> listeners = new ArrayList<SelectionListListener>();

	public void addListener(SelectionListListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(SelectionListListener listener) {
		this.listeners.remove(listener);
	}
    
    public void notifyListenersMediaAdded(MediaItem item) {
	    for (SelectionListListener listener : new ArrayList<SelectionListListener>(listeners))
		    listener.mediaAdded(item);
    }

    public void notifyListenersMediaRemoved(MediaItem item) {
	    for (SelectionListListener listener : new ArrayList<SelectionListListener>(listeners))
		    listener.mediaRemoved(item);
    }
}
