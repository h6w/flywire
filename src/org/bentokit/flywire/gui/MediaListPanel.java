package org.bentokit.flywire.gui;

import java.util.Vector;

import javax.swing.JPanel;

import org.bentokit.flywire.gui.panels.BasicMediaInfoPanel;
import org.bentokit.flywire.media.MediaItem;

public class MediaListPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5186559204887953431L;
	
	volatile Vector<MediaPanel> items;
	
	public MediaListPanel() {
		this.items = new Vector<MediaPanel>();
	}
	
	public void add(MediaItem item) {
		this.add(new BasicMediaInfoPanel(item));
	}
	
	public void remove(MediaItem itemToRemove) {
		for (MediaPanel item : items) {
			if (item.getMediaItem().equals(itemToRemove)) {
				this.remove(item);
			}
		}
	}
}
