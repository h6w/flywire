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

import java.util.Calendar;
import java.util.Vector;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.event.MediaItemEvent;
import org.bentokit.flywire.event.MediaListener;
//import org.bentokit.flywire.gui.panels.AnnouncementsPanel;
import org.bentokit.flywire.gui.panels.IDsPanel;
import org.bentokit.flywire.media.MusicPicker;
import org.bentokit.flywire.media.NoMediaItemAvailableException;

public class AutomaticDJ implements MediaListener {
    public enum TrackType { 
        MUSIC("MUSIC"), ID("ID"), ANNOUNCEMENT("ANNOUNCEMENT");
        String str;
        TrackType(String str) { this.str = str; }
    }
    

    class TracknameType {
        public String filename;
        public TrackType trackType;

        public TracknameType(String fn, TrackType tt)
        {
            filename = fn;
            trackType = tt;
        }
    }

    PlayList playlist;
    boolean DJisON;

    MusicPicker music;
    SelectionList ids;
    //Announcements announcements;

    Vector<TracknameType> history;
    int maxHistory = 5;
    int maxPlaylistSize = 5;

    public AutomaticDJ(PlayList playlist,
                       SelectionList ids) {
                        //, Announcements announcements) {
        this.playlist = playlist;
        this.ids = ids;
        //this.announcements = announcements;

        history = new Vector<TracknameType>();

        DJisON = false;
    }

    public MediaItem nextTrack() {
        MediaItem nextMediaItem;
        //ErrorHandler.info("Running next()");
        TrackType lastTrackType = history.size() > 0 ?  history.lastElement().trackType : TrackType.ID;
        TrackType nextTrackType = TrackType.MUSIC;
        switch (lastTrackType) {
            case MUSIC:
                //check to see if there are any annoucements outstanding
                //ErrorHandler.info("Next time:"+announcements.getNextTime().getTimeInMillis());
                //ErrorHandler.info("Now:      "+Calendar.getInstance().getTimeInMillis());
                //ErrorHandler.info("Playlist: "+playlist.getTotalTime());
                /**
                try {
                    if (announcements != null && announcements.getNextTime().getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()+playlist.getTotalTime() 
                        && announcements.isWaiting())
                        nextTrackType = TrackType.ANNOUNCEMENT;
                    else 
                    */
                    nextTrackType = TrackType.ID;
                /*
                } catch (NoMediaItemAvailableException nmae1) {
                    nextTrackType = TrackType.ID;
                }
                */
                break;
            case ID:
            case ANNOUNCEMENT:
                nextTrackType = TrackType.MUSIC;
                break;
            default:
                ErrorHandler.error("Unknown Last Media Type: defaulting to MUSIC");
                nextTrackType = TrackType.MUSIC;
                break;
        }
        //String nextFilename;
        //ErrorHandler.info("Somewhere in next()");
        //make sure we haven't picked the same file as the last track/promo
        assert music != null;
        if (nextTrackType == TrackType.ANNOUNCEMENT) {
/*
            try {
                //nextMediaItem = announcements.getSelectionList().nextMediaItem();
            } catch (NoMediaItemAvailableException nmae) {
                nextMediaItem = music.nextMediaItem();
            }
*/
        }
        else if (lastTracktypeName(nextTrackType) == null) {
            switch (nextTrackType) {
                case ID:
                    try {
                        if (ids != null) 
                            nextMediaItem = ids.nextMediaItem();
                        else
                            nextMediaItem = music.nextMediaItem();
                    } catch (NoMediaItemAvailableException nmae1) {
                        nextMediaItem = music.nextMediaItem();
                    }
                break;
                case MUSIC:
                default:
                    nextMediaItem = music.nextMediaItem();
                break;
            }
        }
        else {
/*
            do {
                switch (nextTrackType) {
                    case ID:
                        try {
                            if (ids != null)
                                nextMediaItem = ids.nextMediaItem();
                            else
                                nextMediaItem = music.nextMediaItem();
                        } catch (NoMediaItemAvailableException nmae1) {
                            nextMediaItem = music.nextMediaItem();
                        }
                    break;
                    case MUSIC:
                    default:
                        nextMediaItem = music.nextMediaItem();
                    break;
                }
            }
            while (nextMediaItem != null 
                   &&nextMediaItem.getURI().toString().compareTo(lastTracktypeName(nextTrackType)) == 0);
*/
        }
/*
        if (nextMediaItem == null) {

            this.stop();
            return(null);
        }
*/
        //ErrorHandler.info("Somewhere later in next()");

//        history.addElement(new TracknameType(nextMediaItem.getURI().toString(), nextTrackType));
        while (history.size() > maxHistory) history.remove(0);
//        nextMediaItem.addListener(this);        
        //ErrorHandler.info("Ending next()");
//        return(nextMediaItem);
        return(null);
    }

    /** Get the name of the last track of a particular type
    **/
    public String lastTracktypeName(TrackType trackType) {
        for (TracknameType tracknameType : history)
            if (tracknameType.trackType == trackType)
                return(tracknameType.filename);
        return null;
    }

    public boolean start() {
        MediaItem nextTrack;
        do {
            if ((nextTrack = nextTrack()) != null) {
                playlist.add(nextTrack);
                DJisON = true;
                ErrorHandler.info("Auto DJ started");
            } else {
                ErrorHandler.info("Automatic DJ cannot continue.");
                DJisON = false;
                return(false);
            }
        }
        while (nextTrack != null && playlist.countItems() <= maxPlaylistSize);
        return(true);
    }

    public void setMusic(MusicPicker music) {
        this.music = music;
    }

    public void stop() {
        DJisON = false;
        ErrorHandler.info("Auto DJ stopped");
    }

    ///////////////////////////////////////////////////////////////////////////
    // MediaListener interface ////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	public void disappeared(MediaItem item) { ; }
	public void stateChanged(MediaItemEvent event) { ; }
	public void mediaReady(MediaItem mediaItem) { ; }
	public void mediaPlaying(MediaItem mediaItem) { ; }
    public void mediaPlayed(MediaItem item) {
        ErrorHandler.info("AutoDJ:MediaPlayed:"+item.getURI());
        playlist.remove(item);
        if (DJisON) {
            ErrorHandler.info("and DJisON");
            while (playlist.countItems() <= maxPlaylistSize) {
                playlist.add(nextTrack());
            }
        } else ErrorHandler.info("but DJisOFF");
    }
	public void mediaChanged(MediaItem mediaItem) { ; }
	public void mediaDeleted(MediaItem mediaItem) { ; }
	public void mediaUnplayable(MediaItem mediaItem) { ; }


}
