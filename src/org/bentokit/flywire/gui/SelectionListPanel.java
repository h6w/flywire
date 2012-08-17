package org.bentokit.flywire.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.gui.panels.MediaPanelFactory;
import org.bentokit.flywire.media.MediaItem;
import org.bentokit.flywire.media.PlayList;
import org.bentokit.flywire.media.SelectionList;
import org.bentokit.flywire.media.SelectionListListener;


public class SelectionListPanel extends JScrollPane implements SelectionListListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3814993408465344197L;
	Map<MediaItem,JPanel> items;
	private JPanel list;
    private MediaPanelFactory factory;
    private SelectionList selectionList;

	public SelectionListPanel(SelectionList selectionList, MediaPanelFactory factory, KeyListener k) {
		super();
        this.selectionList = selectionList;
        this.factory = factory;
        this.items = new HashMap<MediaItem,JPanel>();
		selectionList.addListener(this);
		
        this.list = new JPanel();
        this.list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        //this.list.add(new JLabel("VIEWPORT"));

        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());
        viewPanel.add(this.list,"North");
        viewPanel.add(new JPanel(),BorderLayout.CENTER);

        this.setViewportView(viewPanel);
        
        this.addKeyListener(k);
        viewPanel.addKeyListener(k);
        list.addKeyListener(k);

        for (MediaItem item : selectionList.getItems()) {
            this.mediaAdded(item);
        }
	}

    ///////////////////////////////////////////////////////////////////////////
    // SelectionListListener interface ////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	public void mediaAdded(MediaItem item) {
        final MediaItem finalItem = item;
        final PlayList finalPlaylist = this.selectionList.getPlayList();
        final String name = this.selectionList.getName();
        final String filename = item.getInfo().get("filename");

		ErrorHandler.info(filename+":SelectionListPanel("+name+"):Received notification of new item.");
        JPanel panel = factory.createPanel(item);
		//ErrorHandler.info("Panel Created");
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                ErrorHandler.info(filename+":SelectionListPanel("+name+"):MediaPanel item clicked.");
                finalPlaylist.add(finalItem);
            }
        });
		//ErrorHandler.info("Listener attached.");
        this.items.put(item,panel);
		//ErrorHandler.info("Item added to map.");
		this.list.add(panel);	
		//ErrorHandler.info("Item added to JPanel.");
        this.getViewport().validate();
		ErrorHandler.info(filename+":SelectionListPanel("+name+"):Item added.");		
	}

	public void mediaRemoved(MediaItem item) {
		ErrorHandler.info(item.getInfo().get("filename")+":SelectionListPanel("+this.selectionList.getName()+"):Received notification of removed item.");
		this.list.remove(this.items.get(item));	
        this.getViewport().validate();
        this.items.remove(item);
		ErrorHandler.info(item.getInfo().get("filename")+":SelectionListPanel("+this.selectionList.getName()+"):Item removed from SelectionListPanel.");		
	}
}
