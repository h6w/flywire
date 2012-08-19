package org.bentokit.flywire.plugins.playlist;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import java.util.Map;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.event.PlayListListener;
import org.bentokit.flywire.media.MediaItem;
import org.bentokit.flywire.media.PlayList;
import org.bentokit.flywire.gui.MediaPanel;
import org.bentokit.flywire.gui.panels.MediaPanelFactory;
import org.bentokit.flywire.util.TimeNumberFormat;


/**
 * A UI that displays and allows the user to modify the playlist.
 * 
 * @author tudor
 *
 */
public class PlayListPanel extends JPanel implements PlayListListener, MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4563803799292262086L;
	
	PlayList playlist;  //The underlying playlist.
	
	public JPanel list; //Our display of the same playlist.
	JPanel viewPanel, bottomPanel;
	JScrollPane scrollPane;
	JButton clearAllButton;
	JLabel digitalTimer;
	final JCheckBox cueModeCheckBox;
	final JCheckBox repeatCheckBox;
	MediaPanelFactory factory;
	Map<MediaItem,MediaPanel> items = new HashMap<MediaItem,MediaPanel>();

	public PlayListPanel() {
		list = new JPanel();
		list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));

		viewPanel = new JPanel(new BorderLayout());
		viewPanel.add(list,"North");
		viewPanel.add(new JPanel(),"Center");

		//Build the buttonPanel
		bottomPanel = new JPanel(new BorderLayout());
		JPanel leftPanel = new JPanel(new FlowLayout());

		clearAllButton = new JButton("Clear All");
		clearAllButton.setFont(new Font("Sans Serif",Font.PLAIN,15));
		clearAllButton.addMouseListener(this);
		leftPanel.add(clearAllButton);

		cueModeCheckBox = new JCheckBox("Cue Mode");
		cueModeCheckBox.setFont(new Font("Sans Serif",Font.PLAIN,15));
		cueModeCheckBox.setSelected(false);
		leftPanel.add(cueModeCheckBox);

		repeatCheckBox = new JCheckBox("Repeat");
		repeatCheckBox.setFont(new Font("Sans Serif",Font.PLAIN,15));
		repeatCheckBox.setSelected(false);
		leftPanel.add(repeatCheckBox);

		digitalTimer = new JLabel("00:00:00");
		digitalTimer.setFont(new Font("Sans Serif",Font.BOLD,40));

		bottomPanel.add(leftPanel,"West");
		bottomPanel.add(digitalTimer,"East");

		scrollPane = new JScrollPane(viewPanel);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Playlist",
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
					new Font("Sans Serif",Font.BOLD,20)));
		add(scrollPane,"Center");
		add(bottomPanel,"North");
	}
		
	public PlayListPanel(PlayList playlist, MediaPanelFactory factory, KeyListener k) {
		this();
		setFactory(factory);
		setPlaylist(playlist);
		setKeyListener(k);		
	}

	public void setFactory(MediaPanelFactory factory) {
		this.factory = factory;
	}

	public void setPlaylist(PlayList playlist) {
		this.playlist = playlist;

		this.playlist.addListener(this);

		final PlayList finalPlaylist = playlist;

		cueModeCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				finalPlaylist.setCueMode((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		repeatCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				finalPlaylist.setRepeatMode((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
	}

	public void setKeyListener(KeyListener k) {
		addKeyListener(k);
		viewPanel.addKeyListener(k);
		list.addKeyListener(k);
		bottomPanel.addKeyListener(k);
		clearAllButton.addKeyListener(k);
		cueModeCheckBox.addKeyListener(k);
		repeatCheckBox.addKeyListener(k);

	}

	///////////////////////////////////////////////////////////////////////////
	// MouseListener interface ////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	
	public void mouseClicked(MouseEvent e) { if (e.getSource() == clearAllButton) playlist.clearAll(); }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }

	///////////////////////////////////////////////////////////////////////////
	// PlayListListener interface /////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////

	public void setCueMode(boolean cueMode) { this.cueModeCheckBox.setSelected(cueMode); }

	@Override
	public void playable(boolean isReady) {	; }

	@Override
	public void stoppable(boolean stoppable) { ; }

	public void mediaAdded(MediaItem item) {
		final MediaItem finalItem = item;
		final PlayList finalPlaylist = this.playlist;
		
		ErrorHandler.info("PlayListPanel received notification of added item "+item.getURI());
		MediaPanel panel = factory.createPanel(item);
		panel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.err.println("PlayListPanel MediaItem clicked.");
				finalPlaylist.remove(finalItem);
			}
		});
		this.items.put(item,panel);
		this.list.add(panel);
		this.scrollPane.getViewport().revalidate();
		ErrorHandler.info("Item added to PlayListPanel "+item.getURI());
	}

	public void mediaRemoved(MediaItem item) {
		ErrorHandler.info("PlayListPanel received notification of removed item "+item.getURI());
		this.list.remove(this.items.get(item));	
		this.scrollPane.getViewport().revalidate();
		this.factory.removeChild(this.items.get(item));
		item.removeListener(this.items.get(item));
		this.items.remove(item);
		ErrorHandler.info("Item removed from PlayListPanel "+item.getURI());
	}

	public void timesChanged(long total, long sofar) {
		digitalTimer.setText(TimeNumberFormat.formatTimeString((long)total-sofar));
	}

}
