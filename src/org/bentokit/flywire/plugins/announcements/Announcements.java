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
package org.bentokit.flywire.plugins.announcements;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.media.Time;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.event.TimedEventAdapter;
import org.bentokit.flywire.event.TimedEventManager;
import org.bentokit.flywire.gui.SelectionListPanel;
import org.bentokit.flywire.gui.panels.MediaPanelFactory;
import org.bentokit.flywire.event.MediaListener;
import org.bentokit.flywire.event.MediaItemEvent;
import org.bentokit.flywire.media.MediaItem;
import org.bentokit.flywire.media.PlayList;
import org.bentokit.flywire.media.SelectionList;
import org.bentokit.flywire.util.DirListener;
import org.bentokit.flywire.util.DirBabysitter;

public class Announcements implements MediaListener, DirListener {

    volatile Vector<Announcement> announcements = new Vector<Announcement>();
    volatile HashMap<MediaItem,Announcement> media = new HashMap<MediaItem,Announcement>();;

    public static final long serialVersionUID = 1L; //Why do we do this?

    static final String ScheduleMediaDir = "ScheduledMedia";
    static final String ScheduleDir = "Schedule";
    static final String LogDir = "Logs";
    
    PlayList playlist;
    SelectionList selectionList;
    
    Calendar nextAnnouncementTime;
    SimpleDateFormat printtimeformat;

//    ReloadAnnouncementsEvent reload_event;

    public Announcements(PlayList playlist) {
        this.selectionList = new SelectionList(playlist,"Announcements");
        this.playlist = playlist;

        DirBabysitter.add(new File(".",ScheduleDir),this);
    }

    //the announcements panel is waiting if there are mediaItems not in the playlist
    public boolean isWaiting() {
        for (MediaItem item : selectionList.getItems()) {
            if (!playlist.contains(item)) {
                ErrorHandler.info("Announcements:There are announcements waiting");
                return(true);
            }
        }
        ErrorHandler.info("Announcements:No announcements waiting");
        return(false);
    }

    /** Checks and removes any mediaItems in the list that have been removed.
        Returns true if any remain after this operation. **/
/*
    public boolean files_exist() {
        if (selectionList.getItems().size() <= 0) return(false);
        //Use an iterator instead of Java6-style iteration because we may be removing from the list we're iterating.
        Vector<MediaItem> mia = new Vector<MediaItem>();
        for (Iterator<MediaItem> iter = selectionList.getItems().iterator(); iter.hasNext();) {
            MediaItem m = iter.next();
            String filename = parsedateformat.format(nextAnnouncementTime.getTime())+";"+m.getURI();
                File file = new File(ScheduleDir+"/"+filename);
                if (!file.exists()) {
                    ErrorHandler.info(filename+":Announcements:File disappeared!");
                    playlist.remove(m);
                    mia.add(m);
                }
        }
        for (MediaItem m : mia) {
            selectionList.remove(m);
        }
        return(selectionList.getItems().size() > 0);
    }
*/

/*
    public void loadAnnouncements() {
        if (!files_exist()) {
            File dir = new File(".",ScheduleDir);
            if (!dir.exists()) dir.mkdirs();
            if (dir.exists() && !dir.isDirectory()) {
                 notifyListenersAnnouncementError("You have a Schedule file, but I was looking\nfor a Schedule Directory.\nPlease accept my apologies.  I don't know what I should do here.  If you can think of what I should do, please contact the Flywire team."); 
                 System.exit(-1);
            }

            if (!dir.exists()) {
                notifyListenersAnnouncementError("I could not create the Schedule Directory at "+dir.getAbsolutePath()+".  Please check the permissions of the folder."); 
                System.exit(-1);            	
            }

            File mediaDir = new File(".",ScheduleMediaDir);
            if (!mediaDir.exists()) dir.mkdirs();
            if (mediaDir.exists() && !mediaDir.isDirectory()) {
                 notifyListenersAnnouncementError("You have a ScheduledMedia file, but I was looking\nfor a Scheduled Media Directory.\nPlease accept my apologies.  I don't know what I should do here.  If you can think of what I should do, please contact the Flywire team."); 
                 System.exit(-1);
            }
            
            if (!mediaDir.exists()) {
                notifyListenersAnnouncementError("I could not create the Scheduled Media Directory at "+mediaDir.getAbsolutePath()+".  Please check the permissions of the folder."); 
                System.exit(-1);            	
            }

            File[] media = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    boolean isDir = (new File(dir,name)).isDirectory();
                    boolean isHidden = (new File(dir,name)).isHidden();
                    return((!isDir) && (!isHidden));
                } 
            });

            //ErrorHandler.info("Loading Schedule from Dir:"+dir);

            //Calendar currentTime = (Calendar) nextAnnouncementTime.clone();
            //if (currentTime.getTimeInMillis() == Long.MAX_VALUE) {
            //    currentTime = Calendar.getInstance();
            //}

            Calendar proposedMediaTime = (Calendar) nextAnnouncementTime.clone();
            proposedMediaTime.setTimeInMillis(Long.MAX_VALUE);
            //ErrorHandler.info("Current:"+currentTime.getTime());
            //ErrorHandler.info("Proposed:"+proposedMediaTime.getTime());

            //find the next time
            for (int i = 0; i < media.length; i++) {
                String m = media[i].getName();

                StringTokenizer st = new StringTokenizer(m,";");
                Date d = new Date();
                try {
                    d = parsedateformat.parse(st.nextToken().trim());
                } catch (ParseException e) { ErrorHandler.error(e); }
                String mediaFilename = st.nextToken().trim();
               
                File mediaFile = new File(ScheduleMediaDir,mediaFilename);
                if (mediaFile.exists()) {                
                    Calendar mediaTime = Calendar.getInstance();
                    mediaTime.setTime(d);
                //  ErrorHandler.info("Current:"+currentTime.getTime());
                    ErrorHandler.info("Read:"+mediaTime.getTime());
                    if (mediaTime.before(proposedMediaTime)) {
                            ErrorHandler.info("Read and Setting:"+mediaTime.getTime());
                            proposedMediaTime = mediaTime;
                    }
                    ErrorHandler.info("Possible:"+mediaTime.getTime());
                    ErrorHandler.info("Proposed:"+proposedMediaTime.getTime());
                } else {
                    ErrorHandler.info("Could not find media for item:"+m+". Ignoring...");
                }

                try {
                    Thread.sleep(5);
                }
                catch (InterruptedException ie) {
                }
            }

            //if (nextAnnouncementTime.getTimeInMillis() == Long.MAX_VALUE)
            //    nextAnnouncementTime.setTimeInMillis(0);

            nextAnnouncementTime = proposedMediaTime;

            if (proposedMediaTime.getTimeInMillis() == Long.MAX_VALUE) {
                notifyListenersAnnouncementsNone();
                return;
            }

            //load media with that time
            for (int i = 0; i < media.length; i++) {
                String m = (String) media[i].getName();
                StringTokenizer st = new StringTokenizer(m,";");
                String timeStr = st.nextToken().trim();
                String url = st.nextToken().trim();
                if (
                Date d = new Date();
                try {
                    d = parsedateformat.parse(st.nextToken().trim());
                } catch (ParseException e) { ErrorHandler.error(e); }
                Calendar mediaTime = Calendar.getInstance();
                mediaTime.setTime(d);
                if (mediaTime.equals(proposedMediaTime)) {
                    loadMedia(st.nextToken().trim());
                }
                ErrorHandler.info("Announcements:Loading:"+m);
            }

            notifyListenersAnnouncementsChanged(nextAnnouncementTime,this.selectionList);
        }
    }
*/


/*
    boolean loadMedia(String filename) {
        File dir = new File(".",ScheduleMediaDir);

        if (!dir.exists()) dir.mkdirs();
        if (!dir.isDirectory()) {
            notifyListenersAnnouncementError("You have a ScheduleMedia file, but I was looking\nfor a ScheduleMedia Directory.\nI cannot continue.\nPlease accept my apologies.\nI don't know what I should do here.\nIf you can think of what I should do,\nplease contact the Flywire team."); 
            System.exit(-1);
        }

        File file = new File(ScheduleMediaDir,filename);
        if (!this.selectionList.contains(file.toURI())) {
            ErrorHandler.info("Loading media:"+filename);
            MediaItem m;
		    m = new MediaItem(file.toURI());
            if (!m.exists()) return(false);
            m.addListener(this);
            this.selectionList.add(m);
            //if (m == null) ErrorHandler.error("MediaItem is null!");
            //playcontrol.addMedia(filename);
        }
        return(true);
    }
*/

    public Calendar getNextTime() throws NoMediaItemAvailableException {
        if (nextAnnouncementTime == null) throw new NoMediaItemAvailableException();
        return(nextAnnouncementTime);
    }

    public SelectionList getSelectionList() { return(this.selectionList); }

    public void addAll() {
        for (MediaItem item : this.selectionList.getItems()) {
            playlist.add(item);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // MediaListener interface ////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	public void disappeared(MediaItem item) { 
        mediaDeleted(item);
    }
	public void stateChanged(MediaItemEvent event) { ; }
	public void mediaReady(MediaItem mediaItem) { ; }
	public void mediaPlaying(MediaItem mediaItem) { ; }
    public void mediaPlayed(MediaItem mediaItem) {
        if (this.playlist.isCueMode()) return;
        this.playlist.remove(mediaItem);
        Announcement a = this.media.get(mediaItem);
        a.log();
        this.selectionList.remove(mediaItem);
        this.announcements.remove(a);
        mediaItem.removeListener(this);
        if (this.selectionList.size() <= 0) {
            if (this.announcements.size() > 0) {
                this.selectionList = new SelectionList(playlist,"Announcements");
                nextAnnouncementTime = this.announcements.get(0).getTime();
                for (int i = 0; i < this.announcements.size() && this.announcements.get(i).getTime().equals(nextAnnouncementTime); i++) {
                    MediaItem m = new MediaItem(this.announcements.get(i).getURI());
                    this.media.put(m,this.announcements.get(i));
                    this.selectionList.add(m);
                    m.addListener(this);
                }
                notifyListenersAnnouncementsChanged(nextAnnouncementTime,this.selectionList);
            } else {
                nextAnnouncementTime = null;
                notifyListenersAnnouncementsNone();
            }
        }
    } //For selectionList
    
	public void mediaChanged(MediaItem mediaItem) { ; }
	public void mediaDeleted(MediaItem mediaItem) { 
        Announcement a = this.media.get(mediaItem);
        this.media.remove(mediaItem);
        this.selectionList.remove(mediaItem);
        this.playlist.remove(mediaItem);
        if (this.selectionList.size() <= 0) {
            if (this.announcements.size() > 0) {
                this.selectionList = new SelectionList(playlist,"Announcements");
                nextAnnouncementTime = this.announcements.get(0).getTime();
                for (int i = 0; i < this.announcements.size() && this.announcements.get(i).getTime().equals(nextAnnouncementTime); i++) {
                    MediaItem m = new MediaItem(this.announcements.get(i).getURI());
                    this.media.put(m,this.announcements.get(i));
                    this.selectionList.add(m);
                    m.addListener(this);
                }
                notifyListenersAnnouncementsChanged(nextAnnouncementTime,this.selectionList);
            } else {
                nextAnnouncementTime = null;
                notifyListenersAnnouncementsNone();
            }
        }
        
    }
	public void mediaUnplayable(MediaItem mediaItem) { ; }

    ///////////////////////////////////////////////////////////////////////////
    // DirListener interface //////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public void fileAdded(File file) {
		ErrorHandler.info(file.getName()+":Announcements:Received notification of new item.");
        Announcement newAnnouncement = new Announcement(file.getName());
        if (!newAnnouncement.exists()) {
            ErrorHandler.info("Could not find media for item:"+file.getName()+". Ignoring...");
            return;
        }
        this.announcements.add(newAnnouncement);
        Collections.sort(this.announcements);
        if (this.nextAnnouncementTime == null || this.announcements.get(0).getTime().before(nextAnnouncementTime)) {
            this.selectionList = new SelectionList(playlist,"Announcements");
            nextAnnouncementTime = this.announcements.get(0).getTime();
            ErrorHandler.info(file.getName()+":Announcements:New or more recent time.  Created new SelectionList.");
        }
        if (this.nextAnnouncementTime.equals(newAnnouncement.getTime())) {
            MediaItem m = new MediaItem(newAnnouncement.getURI());
            m.addListener(this);
            this.media.put(m,newAnnouncement);
            this.selectionList.add(m);
            ErrorHandler.info(file.getName()+":Announcements:Time is current.  Added to current SelectionList.");
            notifyListenersAnnouncementsChanged(nextAnnouncementTime,this.selectionList);
        } else
            ErrorHandler.info(file.getName()+":Announcements:Time is not current.  Stored for later.");
  		ErrorHandler.info(file.getName()+":Announcements:Item added.");					
    }

    ///////////////////////////////////////////////////////////////////////////
    // FileListener interface /////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public void fileChanged(File file) { ; }

    public void fileDeleted(File file) {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listeners and Listener Management //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
	
    volatile ArrayList<AnnouncementsListener> listeners = new ArrayList<AnnouncementsListener>();

	public void addListener(AnnouncementsListener listener) {
        this.listeners.add(listener);
        if (this.selectionList.size() <= 0) 
            listener.announcementsNone();
        else
            listener.announcementsChanged(this.nextAnnouncementTime,this.selectionList);
	}
	
	public void removeListener(AnnouncementsListener listener) {
   		this.listeners.remove(listener);
	}
	    
    public void notifyListenersAnnouncementsChanged(Calendar newTime, SelectionList newSelectionList) {
    	for (AnnouncementsListener listener : new ArrayList<AnnouncementsListener>(listeners)) {
    		listener.announcementsChanged(newTime,newSelectionList);
    	}
    }

    public void notifyListenersAnnouncementsNone() {
    	for (AnnouncementsListener listener : new ArrayList<AnnouncementsListener>(listeners)) {
    		listener.announcementsNone();
    	}
    }

    public void notifyListenersAnnouncementError(String message) {
    	for (AnnouncementsListener listener : new ArrayList<AnnouncementsListener>(listeners)) {
    		listener.announcementsError(message);
    	}
    }

}

/*
class ReloadAnnouncementsEvent extends TimedEventAdapter
{
    Announcements p;
    public ReloadAnnouncementsEvent(Announcements p, int dividefield, int divisor)
    {
        super(dividefield, divisor);
        this.p = p;
        TimedEventManager.getManager().addTimedEventListener(this);
    }

    public void doEvent() {
        //ErrorHandler.info("Loading new announcements via event");
        p.loadAnnouncements();
    }
}
*/

class Announcement implements Comparable<Announcement> {
    static SimpleDateFormat parsedateformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss",Locale.ENGLISH);
    static SimpleDateFormat printtimeformat = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);

    Calendar time;
    URI uri;
    String filename; //Filename as listed by the schedule file.
    
    public Announcement(String filestring) {
        time = Calendar.getInstance();
        StringTokenizer st = new StringTokenizer(filestring,";");
        try {
            time.setTime(parsedateformat.parse(st.nextToken().trim()));
        } catch (ParseException e) { ErrorHandler.error(e); }
        //try {
            filename = st.nextToken().trim();
            uri = (new File(Announcements.ScheduleMediaDir+File.separator+filename)).toURI();
        //} catch (URISyntaxException e) { ErrorHandler.error(e); }
    }

    public Calendar getTime() { return(this.time); }
    public URI getURI() { return(this.uri); }

    public boolean exists() {
        return((new File(Announcements.ScheduleMediaDir+File.separator+this.filename)).exists());
    }

    public int compareTo(Announcement that) {
        return(this.time.compareTo(that.getTime()));
    }

    public void log() {
        String logfilename = parsedateformat.format(time.getTime())+";"+this.filename;
        try {
            FileOutputStream logfile = new FileOutputStream(Announcements.LogDir+File.separator+logfilename+".played");
            PrintWriter pw = new PrintWriter(logfile,true);
            Calendar c = Calendar.getInstance();
            pw.println(parsedateformat.format(c.getTime()));
            logfile.close();
        }
        catch (FileNotFoundException fnfe) {
            ErrorHandler.error("Could not save log. Error: "+fnfe+"\nFile concerned:"+logfilename);
        }
        catch (IOException e) {
            ErrorHandler.error("Could not save log. Error: "+e+"\nFile concerned:"+logfilename);
        }
        File schedulefile = new File(Announcements.ScheduleDir+"/"+logfilename);
        schedulefile.delete();
    }
}
