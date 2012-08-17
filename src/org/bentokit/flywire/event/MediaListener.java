package org.bentokit.flywire.event;

import org.bentokit.flywire.media.MediaItem;

/**
 * A listener class for listeners to a MediaItem.
 * @author tudor
 *
 */

public interface MediaListener {
	/**
	 * Used to notify listeners if the underlying URI or file disappeared.
	 */
	public void disappeared(MediaItem item);
	
	/**
	 * Used to notify listeners of a change in the player state.
	 * @param event - A MediaItemEvent containing the details of the state change.
	 */
	public void stateChanged(MediaItemEvent event);

	/**
	 * Used to notify listeners that the media is ready to play.
     * This means that the MediaItem's player is in a state ready to play.
	 *  
	 * @param mediaItem - The MediaItem corresponding to media that changed.
	 */
	public void mediaReady(MediaItem mediaItem);
	
	/**
	 * Used to notify listeners that the media is ready to play.
     * This means that the MediaItem's player is in a state ready to play.
	 *  
	 * @param mediaItem - The MediaItem corresponding to media that changed.
	 */
	public void mediaPlaying(MediaItem mediaItem);

	/**
	 * Used to notify listeners that the media was played.
	 *  
	 * @param mediaItem - The MediaItem corresponding to media that changed.
	 */
	public void mediaPlayed(MediaItem mediaItem);
	
	/**
	 * Used to notify listeners of a change in the underlying media.
	 * @param mediaItem - The MediaItem corresponding to media that changed.
	 */
	public void mediaChanged(MediaItem mediaItem);

	/**
	 * Used to notify listeners that the underlying media is no longer available and that the
	 * MediaItem is about to be destroyed.
	 * @param mediaItem - The MediaItem corresponding to media that was removed.
	 */
	public void mediaDeleted(MediaItem mediaItem);
	
	/**
	 * Used to notify listeners that the underlying media is available but that the
	 * MediaItem is not able to be played by the JMF.
	 * @param mediaItem - The MediaItem corresponding to media that was unplayable.
	 */
	public void mediaUnplayable(MediaItem mediaItem);
}
