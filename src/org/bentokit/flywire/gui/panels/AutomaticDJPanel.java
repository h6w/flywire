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
import org.bentokit.flywire.gui.panels.AnnouncementsPanel;
import org.bentokit.flywire.gui.panels.IDsPanel;
import org.bentokit.flywire.gui.panels.MusicPickerPanel;

public class AutomaticDJ {
    public static final int MUSIC = 0;
    public static final int ID = 1;
    public static final int ANNOUNCEMENT = 2;

    PlayList playlist;
    boolean DJisON;

    MusicPickerPanel music;
    IDsPanel ids;
    AnnouncementsPanel announcements;

    Vector<TracknameType> history;
    int maxHistory = 5;
    int maxPlaylistSize = 5;

    public AutomaticDJ(PlayList playlist, MusicPickerPanel music,
                       IDsPanel ids, AnnouncementsPanel announcements) {
        this.playlist = playlist;
        this.music = music;
        this.ids = ids;
        this.announcements = announcements;

        history = new Vector<TracknameType>();

        DJisON = false;
    }

    public void EndOfMediaEvent() {
        if (DJisON) {
            ErrorHandler.info("and DJisON");
            while (playlist.countItems() <= maxPlaylistSize) {
                playlist.add(nextTrack());
            }
            playlist.removePlayedItems();
            while (playlist.countItems() <= maxPlaylistSize) {
                playlist.add(nextTrack());
            }
        } else ErrorHandler.info("but DJisOFF");
    }

    public MediaItem nextTrack() {
        MediaItem nextMediaItem;
        //ErrorHandler.info("Running next()");
        int lastTrackType = history.size() > 0 ?  history.lastElement().trackType : ID;
        int nextTrackType = MUSIC;
        switch (lastTrackType) {
            case MUSIC:
                //check to see if there are any annoucements outstanding
                //ErrorHandler.info("Next time:"+announcements.getNextTime().getTimeInMillis());
                //ErrorHandler.info("Now:      "+Calendar.getInstance().getTimeInMillis());
                //ErrorHandler.info("Playlist: "+playlist.getTotalTime());
                if ((announcements.getNextTime().getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()+playlist.getTotalTime()) && announcements.isWaiting())
                    nextTrackType = ANNOUNCEMENT;
                //if not, play an id
                else nextTrackType = ID;
                break;
            case ID:
                //nextTrackType = MUSIC;
                nextTrackType = MUSIC;
                break;
            case ANNOUNCEMENT:
                nextTrackType = MUSIC;
                break;
            default:
                ErrorHandler.error("Unknown Last Media Type: defaulting to MUSIC");
                nextTrackType = MUSIC;
                break;
        }
        //String nextFilename;
        //ErrorHandler.info("Somewhere in next()");
        //make sure we haven't picked the same file as the last track/promo
        assert music != null;
        if (nextTrackType == ANNOUNCEMENT) nextMediaItem = announcements.nextMediaItem();
        else if (lastTracktypeName(nextTrackType) == null) {
            switch (nextTrackType) {
                case MUSIC:
                nextMediaItem = music.nextMediaItem();
                break;
                case ID:
                nextMediaItem = ids.nextMediaItem();
                break;
                default:
                nextMediaItem = music.nextMediaItem();
                break;
            }
        }
        else {
            do {
                switch (nextTrackType) {
                    case MUSIC:
                    nextMediaItem = music.nextMediaItem();
                    break;
                    case ID:
                    nextMediaItem = ids.nextMediaItem();
                    break;
                    default:
                    nextMediaItem = music.nextMediaItem();
                    break;
                }
            }
            while (nextMediaItem != null 
                   &&nextMediaItem.getURI().toString().compareTo(lastTracktypeName(nextTrackType)) == 0);
        }

        if (nextMediaItem == null) {

            this.stop();
            return(null);
        }
        //ErrorHandler.info("Somewhere later in next()");

        // BUGCHECK: all our callers call addToPlaylist() on what we return.
        // Ultimately only one item gets added to the list due to a policy
        // encoded into PlayList. However the double call is unnecessary.
        // I'm not convinced that this is the place to fix it. (cbp - Mar 2010)
        // ErrorHandler.info("Adding "+nextMediaItem.getFilename());
        // nextMediaItem.addToPlaylist();
        history.addElement(new TracknameType(nextMediaItem.getURI().toString(), nextTrackType));
        while (history.size() > maxHistory) history.remove(0);
        //ErrorHandler.info("Ending next()");
        return(nextMediaItem);
    }

    /** Get the name of the last track of a particular type
    **/
    public String lastTracktypeName(int trackType) {
        for (TracknameType tracknameType : history)
            if (tracknameType.trackType == trackType)
                return(tracknameType.filename);
        return null;
    }

    public boolean start(MusicPickerPanel panel) {
        if (music == null) music = panel;
        assert music != null;
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

    public void stop() {
        DJisON = false;
        ErrorHandler.info("Auto DJ stopped");
    }

    public static String getTypeString(int type) {
        switch(type) {
            case MUSIC: return("MUSIC");
            case ID: return("ID");
            case ANNOUNCEMENT: return("ANNOUNCEMENT");
            default: return("UNKNOWN");
        }
    }

    class TracknameType {
        public String filename;
        public int trackType;

        public TracknameType(String fn, int tt)
        {
            filename = fn;
            trackType = tt;
        }
    }

}
