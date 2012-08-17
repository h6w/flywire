package org.bentokit.flywire.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Vector;


public class DirBabysitter extends TimerTask {
    public static DirBabysitter SELF;
    public volatile Timer timer = null;
	private Map<File,ArrayList<File>> files = new HashMap<File,ArrayList<File>>();
	
    public int size() {
        return(files.keySet().size());
    }

    public void stop() {
        timer.cancel();
    }
	
    synchronized void readDir(File dir) {
		List<File> existing = Arrays.asList(dir.listFiles());
		for (File file : existing) {
			if (!files.get(dir).contains(file))
				for (DirListener listener : new ArrayList<DirListener>(listeners.get(dir))) {
					listener.fileAdded(file);
                    files.get(dir).add(file);
				}
		}
        for (File file : new ArrayList<File>(files.get(dir))) {
            if (!existing.contains(file)) files.get(dir).remove(file);
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Listeners and Listener Management //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	private Map<File,ArrayList<DirListener>> listeners = new HashMap<File,ArrayList<DirListener>>();

	public static void add(File dir, DirListener listener) {
        if (SELF == null) SELF = new DirBabysitter();
        SELF.addDir(dir,listener);
    }

	public static void remove(File dir, DirListener listener) {
        SELF.removeListener(dir,listener);
        if (SELF.size() <= 0) {
            SELF.stop();
            SELF = null;
        }
    }

	public void addDir(File dir, DirListener listener) {
		if (!files.containsKey(dir))
			files.put(dir,new ArrayList<File>());
		if (!listeners.containsKey(dir))
			listeners.put(dir,new ArrayList<DirListener>());
		listeners.get(dir).add(listener);

        readDir(dir);

        if (timer == null) {
            timer = new Timer();
            timer.schedule(this,0,5000);
        }
	}
	
	synchronized private void removeDir(File dir) {
		listeners.remove(dir);
		files.remove(dir);
        if (listeners.size() <= 0) {
            this.timer.cancel();
            this.timer = null;
        }
	}
	
	synchronized public void removeListener(File file, DirListener listener) {
		listeners.get(file).remove(listener);
		
		if (listeners.get(file).size() <= 0) {
			removeDir(file);
		}
	}

    ///////////////////////////////////////////////////////////////////////////
    // TimerTask interface ////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

	synchronized public void run() {
		for (File dir : files.keySet()) {
            readDir(dir);
		}
	}

}
