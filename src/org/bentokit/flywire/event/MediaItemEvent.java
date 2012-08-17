package org.bentokit.flywire.event;

import javax.media.Controller;

import org.bentokit.flywire.media.MediaItem;

/**
 * An event for communication when something changes in a MediaItem.
 * 
 * @author tudor
 *
 */
public class MediaItemEvent {
	public enum State {
		NONEXISTENT(-1), //When the player is null.
		UNREALIZED(Controller.Unrealized), //When the player is not null.
		REALIZING(Controller.Realizing),
		REALIZED(Controller.Realized),
		PREFETCHING(Controller.Prefetching),
		PREFETCHED(Controller.Prefetched),
		STARTED(Controller.Started);
		
		int controllerState;
		
		private State(int state) {
			this.controllerState = state; 
		}
		
		public int getControllerState() { return(this.controllerState); }
		
		public static State valueOf(int controllerState) {
			for (State state : State.values()) {
				if (state.getControllerState() == controllerState) return(state);
			}
			return(null);
		}
	}
	
	MediaItem source;
	State state;
	
	public MediaItemEvent(MediaItem source) {
		this.source = source;
		this.state = null;
	}
	
	public MediaItemEvent(MediaItem source, int controllerState) {
		this.source = source;
		this.state = State.valueOf(controllerState);
	}
	
	public State getState() { return(this.state); }
	public MediaItem getSource() { return(this.source); }
}
