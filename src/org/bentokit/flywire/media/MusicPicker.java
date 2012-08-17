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

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.media.MediaItem;
import org.bentokit.flywire.media.PlayList;

/**
 * The MusicPickerPanel randomly picks MediaItems from a list and fills the playlist to
 * a set capacity.  This is for AutoDJ.
 * 
 * @author tudor
 *
 */
public class MusicPicker implements FileFilter {
    public static final long serialVersionUID = 1L; //Why do we do this?

    public static String randomMediaDir = "AutoMedia";

    PlayList playlist;

    public JLabel panelTitle;

    Vector<String> musicHistory;

    public MusicPicker(PlayList playlist) {
        this.playlist = playlist;

        musicHistory = new Vector<String>();
    }

    public MediaItem nextMediaItem() {
        String nextFilename;
        //ErrorHandler.info("Running next()");

        if (musicHistory.size() <= 0) {
                nextFilename = selectRandomFile(new File(randomMediaDir+File.separator+"MUSIC"));
                if (nextFilename == "") return(null);
        }
        else {
                File[] filenames = (new File(randomMediaDir+File.separator+"MUSIC")).listFiles(this);  //this refers to FileFilter
                while (musicHistory.size() >= (filenames.length/2))
                    musicHistory.remove(musicHistory.firstElement());

                do {
                    nextFilename = selectRandomFile(new File(randomMediaDir+File.separator+"MUSIC"));
                } while (isInMusicHistory(nextFilename));
        }
        //ErrorHandler.info("Somewhere even later in next()");

        musicHistory.add(nextFilename);
        MediaItem item = new MediaItem(new File(nextFilename).toURI());
		return(item);
    }

    public boolean isInMusicHistory(String trackname) {
        for (int i = musicHistory.size()-1; i >= 0; i--) {
            String historytrackname = (String) musicHistory.get(i);
            if (historytrackname.compareTo(trackname) == 0) return(true);
        }
        return(false);
    }


    /** Called by playControl when the media has finished.

        Randomly selects a new track to be played.
    **/

    public void EndOfMediaEvent() {
        //ErrorHandler.info("EndOfMedia -> Updating Playlist");
        //playlist.add(nextTrack());
        //ErrorHandler.info("Finished updating playlist");
    }

    public void setSequence(boolean playInSequence) { ; }

    /** Start playing the media in the playlist in order **/

    public void start() {
        //playcontrol.start();
    }

    /** Stop playing the media in the playlist and move to the next item in the playlist **/

    public void stop() {
            //ErrorHandler.info("StoppedEvent");
         //   playcontrol.stop();
    }

    public long getTotalTime() { return(0); }
    public long getTimeSoFar() { return(0); }


    public File getDirectory() { return(new File(this.randomMediaDir)); }

    String selectRandomFile(File directory) {
        File[] filenames = directory.listFiles(this);  //this refers to FileFilter

        if (filenames == null) {
            notifyListenersInsufficientFiles(0);
            ErrorHandler.error("No files in directory "+directory.getPath());
            return("");
        }
        else if (filenames.length < 2) {
            notifyListenersInsufficientFiles(filenames.length);
            ErrorHandler.error("Less than two files in directory "+directory.getPath());
            return("");
        }

        int fileNumber = Math.round((float)((float)Math.random()*((float)filenames.length-1)));

        return(new String(filenames[fileNumber].getPath()));
    }


    public boolean accept(File file) {
        return(!file.isDirectory() && !file.isHidden());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listeners and Listener Management //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
	
    ArrayList<MusicPickerListener> listeners = new ArrayList<MusicPickerListener>();


	public void addListener(MusicPickerListener listener) {
		this.listeners.add(listener);		
	}
	
	public void removeListener(MusicPickerListener listener) {
		this.listeners.remove(listener);
	}
	    
    public void notifyListenersInsufficientFiles(int num) {
    	for (MusicPickerListener listener : new ArrayList<MusicPickerListener>(listeners)) {
    		listener.insufficientFiles(num);
    	}
    }
}
