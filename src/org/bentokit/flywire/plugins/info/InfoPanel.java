/*
    This file is part of Bentokit Flywire.

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
package org.bentokit.flywire.plugins.info;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import org.bentokit.krispi.options.Option;
import org.bentokit.krispi.options.OptionGroup;
import org.bentokit.krispi.options.types.StringOption;
import org.bentokit.krispi.options.types.FileOption;
import org.bentokit.flywire.gui.PlayControllerPanel;
import org.bentokit.flywire.media.PlayList;

public class InfoPanel extends JPanel implements Runnable {
    public static final long serialVersionUID = 1L; //Why do we do this?

	PlayList playlist;
	JButton clearAllButton;
	JPanel addressPanel, logoPanel, buttonPanel, timePanel;
	JLabel timeLabel;
	Thread timeThread;
	String[] address = {"3MBS 103.5 FM",
	                    "St Euphrasia",
	                    "1 St Heliers St",
	                    "Abbotsford",
	                    "VIC    3067",
	                    "phone: (03) 9416 1035",
	                    "email:  info@3mbs.org.au",
	                    "web:    http://www.3mbs.org.au" };
	SimpleDateFormat printtimeformat;

	public InfoPanel() {
//		this.playlist = playlist;

		//Build the timePanel
		timeLabel = new JLabel("00:00:00");
		timeLabel.setFont(new Font("Sans Serif",Font.BOLD,40));

		//timePanel = new JPanel();
		//timePanel.setLayout(new BoxLayout(timePanel,BoxLayout.Y_AXIS));
		//timePanel.add(new JLabel("Clock:"));
		//timePanel.add(timeLabel);

		//Build the Address
		addressPanel = new JPanel();
		addressPanel.setLayout(new BoxLayout(addressPanel,BoxLayout.Y_AXIS));
		for (int i = 0; i < address.length; i++) {
			JLabel addressLine = new JLabel(address[i]);
			addressLine.setFont(new Font("Sans Serif",Font.PLAIN,13));
			addressPanel.add(addressLine);
		}

		//Build the Logo
		ImageIcon logo = new ImageIcon("logo.gif");
		logoPanel = new JPanel(new BorderLayout());
		logoPanel.add(new JLabel(logo),"Center");
		logoPanel.add(timeLabel,"South");

		//Construct the whole panel
		this.setLayout(new GridLayout(1,2));
		//this.add(new JPanel());
		//this.add(timePanel);
		this.add(addressPanel);
		this.add(logoPanel);

		//this.addKeyListener(k);

		printtimeformat = new SimpleDateFormat("hh:mm:ss a",Locale.ENGLISH);

        // Create the clock update thread
        timeThread = new Thread(this);
        timeThread.start();

	}


    public void run() {
		while (true) {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+10"),Locale.ENGLISH);

  		    timeLabel.setText(printtimeformat.format(c.getTime()));


            try {
					Thread.sleep(50);
			}
			catch (InterruptedException ie) {
			}

		}
	}


    /**** Optionable Interface ****/

    static ArrayList<OptionGroup> options = new ArrayList<OptionGroup>(Arrays.asList(new OptionGroup[] {
        new OptionGroup("general","General",
            new ArrayList<Option>(Arrays.asList(new Option[] {
              new StringOption("Title", "A name for the panel.","title","SelectionList"),
              new FileOption("Directory", "The directory for the selection.","directory",new java.io.File(""))
            }))
        )
    }));

    public ArrayList<OptionGroup> getSettings() { return(options); }

}
