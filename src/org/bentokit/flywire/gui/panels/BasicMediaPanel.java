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
package org.bentokit.flywire.gui.panels;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.BorderLayout;
import javax.swing.JLabel;

import org.bentokit.flywire.gui.MediaPanel;
import org.bentokit.flywire.media.MediaItem;

public class BasicMediaPanel extends MediaPanel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 4659592613009701859L;
	
	public BasicMediaPanel(MediaItem item)
    {
        super(item);
		item.getInfo().setNames(new String[]{ "filename", "duration" });
        layoutFields();
    }

    @Override
	public void layoutFields()
    {
        this.setLayout(new BorderLayout());
        if (this.item.getInfo() == null) {
            this.add(new JLabel("Unknown Media Item"), BorderLayout.WEST);
        } else {
            if (this.item.getInfo().getValue("duration") != null)
                this.add(this.item.getInfo().getValue("duration"), BorderLayout.EAST);
            else
                this.add(new JLabel("--:--"), BorderLayout.EAST);

            if (this.item.getInfo().getValue("filename") != null)
                this.add(this.item.getInfo().getValue("filename"), BorderLayout.WEST);
            else
                this.add(new JLabel("UNKNOWN"), BorderLayout.WEST);
        }
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
