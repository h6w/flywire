package org.bentokit.flywire.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Vector;

/**
 * A deliberately-static class to watch any number of files at once and notify 
 * any and all objects that are interested.  The idea being that if we place all
 * checks in one class, it will reduce the number of instances of watcher classes in memory.
 * @author tudor
 *
 */
public class FileBabysitter extends TimerTask {
	public static Map<File,Long> timeStamps = new HashMap<File,Long>();
	public static Map<File,ArrayList<FileListener>> listeners = new HashMap<File,ArrayList<FileListener>>(); 
	
    ///////////////////////////////////////////////////////////////////////////
    // Listeners and Listener Management //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	public synchronized static void add(File file, FileListener listener) {
		if (!timeStamps.containsKey(file))
			timeStamps.put(file,file.lastModified());
		if (!listeners.containsKey(file))
			listeners.put(file,new ArrayList<FileListener>());
		
		listeners.get(file).add(listener);
	}

	private synchronized static void remove(File file) {
		listeners.remove(file);
		timeStamps.remove(file);
	}
	
	public synchronized static void remove(File file, FileListener listener) {
		listeners.get(file).remove(listener);
		
		if (listeners.get(file).size() <= 0) {
			remove(file);
		}
	}

    ///////////////////////////////////////////////////////////////////////////
    // TimerTask interface ////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
	
    public synchronized final void run() {
    	for (File file : timeStamps.keySet()) {
    		//If the file no longer exists, notify the listeners and remove the file from babysitting.
    		if (!file.exists()) {
		    	for (FileListener listener : new ArrayList<FileListener>(listeners.get(file)))
		    		listener.fileDeleted(file);
		    	remove(file);
    		} else { //Else if the file timestamp has changed, notify the listeners of that file.
	    		long timeStamp = file.lastModified();
	    		
			    if( timeStamp != timeStamps.get(file) ) {
			    	for (FileListener listener : new ArrayList<FileListener>(listeners.get(file)))
			    		listener.fileChanged(file);
			    }
    		}
    	}
    }
}
