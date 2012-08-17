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

import java.awt.*;
import java.awt.event.*;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.event.MediaItemEvent;
import org.bentokit.flywire.event.MediaListener;
import org.bentokit.flywire.gui.MediaPanel;
import org.bentokit.flywire.media.MediaItem;

/**
 * A Panel for the display of a MediaItem in a PlayListPanel.
 * @author tudor
 *
 */
public class PlayListMediaPanel extends MediaPanel implements MediaListener, MouseListener
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -8850832285688204651L;
	
	//private static final boolean DEBUG_DEFAULT = false;
    //private boolean DEBUG = DEBUG_DEFAULT;

    protected boolean destroyOnRemoval = false;
    PlayList playlist;
    
    public PlayListMediaPanel(PlayList playlist, MediaItem item)
    {
        super(item);
        this.playlist = playlist;

        
    }

    public void select() {
        setBackground(Color.blue);
    }

    public void deselect() {
        Container c = getParent();
        if (c != null) setBackground(c.getBackground());
    }

    ///////////////////////////////////////////////////////////////////////////
    // MouseListener interface ////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void mouseClicked(MouseEvent e)
    {
        ErrorHandler.info("mouseClicked: destroying panel for " + this.getMediaItem().getURI());
        playlist.remove(this.getMediaItem());
    }

    @Override
    public void mouseEntered(MouseEvent e) { }
    @Override
    public void mouseExited(MouseEvent e) { }
    @Override
    public void mousePressed(MouseEvent e) { }
    @Override
    public void mouseReleased(MouseEvent e) { }


    /*** Methods implemented for MediaPanel ***/
    
	@Override
	public void stateChanged(MediaItemEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layoutFields() {
		// TODO Auto-generated method stub
		
	}

	
    ///////////////////////////////////////////////////////////////////////////
    // MediaListener interface ////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	@Override
	public void disappeared(MediaItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaPlayed(MediaItem mediaItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaChanged(MediaItem mediaItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mediaDeleted(MediaItem mediaItem) {
		// TODO Auto-generated method stub
		
	}
	

}
