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

import java.io.File;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.util.DirListener;
import org.bentokit.flywire.util.DirBabysitter;

public class DirSelectionList extends SelectionList implements DirListener {
	File dir;
	
	public DirSelectionList(PlayList playlist, String name) {
		super(playlist,name);
	}
	
	public DirSelectionList(PlayList playlist, File dir, String name) {
		super(playlist,name);
		this.setDir(dir);
	}
	
	public void setDir(File dir) {
		this.dir = dir;
        DirBabysitter.add(dir,this);
	}

    public void destroy() {
        DirBabysitter.remove(this.dir,this);
    }
	
    ///////////////////////////////////////////////////////////////////////////
    // DirListener interface //////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
	
	@Override
	public void fileAdded(File file) {
		// Create the MediaItem.
		ErrorHandler.info(file.toURI()+":DirSelectionList:Received notification of new item.");
		MediaItem item = new MediaItem(file.toURI());
		//Add the MediaItem to our list.
		this.add(item);
  		ErrorHandler.info(file.toURI()+":DirSelectionList:Item added.");					
	}
}
