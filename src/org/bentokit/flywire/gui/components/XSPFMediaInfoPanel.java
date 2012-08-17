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
package org.bentokit.flywire.gui.components;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.swing.JPanel;
import javax.swing.border.*;

import org.bentokit.flywire.gui.MediaPanel;
import org.bentokit.flywire.media.MediaInfo;
import org.bentokit.flywire.media.MediaItem;

import java.awt.BorderLayout;
import java.awt.Dimension;

public class XSPFMediaInfoPanel extends MediaPanel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3383014228384486476L;

	public XSPFMediaInfoPanel(MediaItem item)
    {
		super(item);
        MediaInfo info = new MediaInfo();
        info.setNames(new String[]{ "filename", "duration", "libnum", "item", "description" });
        this.setInfo(info);
    }


    @Override
    public void layoutFields()
    {
        this.setLayout(new BorderLayout());
        JPanel west = new JPanel();
        Border padding = new EmptyBorder(0, 4, 0, 4);
        ValueComponent<?> field;
        Dimension d;

        west.setLayout(new BorderLayout());
        west.setOpaque(false);

        MediaInfo info = this.getInfo();
        field = info.getValue("libnum");
        field.setBorder(padding);
        west.add(field, BorderLayout.CENTER);

        field = info.getValue("item");
        field.setBorder(padding);
        d = field.getPreferredSize();
        d.width = 25;
        field.setPreferredSize(d);
        west.add(field, BorderLayout.EAST);

        d = west.getPreferredSize();
        d.width = 80;
        west.setPreferredSize(d);

        this.add(west, BorderLayout.WEST);

        field = info.getValue("duration");
        field.setBorder(padding);
        this.add(field, BorderLayout.EAST);

        field = info.getValue("description");
        field.setBorder(padding);
        d = field.getPreferredSize();
        d.width = 120;
        field.setPreferredSize(d);
        this.add(field, BorderLayout.CENTER);
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
