package org.bentokit.flywire.event;

import org.bentokit.flywire.media.MediaItem;

public interface PlayListListener {
	public void setCueMode(boolean cueMode);
	public void playable(boolean isReady);
	public void stoppable(boolean stoppable);
    public void mediaAdded(MediaItem item);
    public void mediaRemoved(MediaItem item);
    public void timesChanged(long total, long sofar);
}
