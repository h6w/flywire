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
package org.bentokit.flywire.plugins.selectionlist;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;

import org.bentokit.flywire.gui.SelectionListPanel;
import org.bentokit.flywire.gui.panels.MediaPanelFactory;
import org.bentokit.flywire.media.DirSelectionList;
import org.bentokit.flywire.media.MediaItem;
import org.bentokit.flywire.media.PlayList;
import org.bentokit.flywire.media.SelectionList;

public class SimpleSelectionPanel extends JPanel {
    public static final long serialVersionUID = 1L; //Why do we do this?

    SelectionList selectionList;
    SelectionListPanel selectionListPanel;
    PlayList playlist;
    KeyListener keyListener;
    MediaPanelFactory factory;

    public SimpleSelectionPanel() {
    }

    public SimpleSelectionPanel(String title, SelectionList selectionList, PlayList playlist, MediaPanelFactory factory, KeyListener k) {
        setTitle(title);
        setPlayList(playlist);
        setFactory(factory);
        setKeyListener(k);
        setSelectionList(selectionList);
    }

    public void setTitle(String title) {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),title,
                       TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                       new Font("Sans Serif",Font.BOLD,20)));
    }

    public void setSelectionList(SelectionList selectionList) {
        this.selectionList = selectionList;
        this.selectionListPanel = new SelectionListPanel(this.selectionList,this.factory,this.keyListener);
        this.add(this.selectionListPanel,"Center");
    }

    public void setFactory(MediaPanelFactory factory) {
        this.factory = factory;
    }

    public void setPlayList(PlayList playlist) {
        this.playlist = playlist;
    }

    public void setKeyListener(KeyListener k) {
        this.keyListener = k;
        this.addKeyListener(k);
    }
    
    public MediaItem nextMediaItem() {
    	//TODO: This is WRONG.  It only returns the first media item.
    	return(this.selectionList.getItems().get(0));
    }
}


