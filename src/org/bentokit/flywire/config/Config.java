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
package org.bentokit.flywire.config;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cbpaine
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bentokit.flywire.Flywire;

public class Config
{
    private static final String VERSION_NUM = "0.36";
    private static final String VERSION_TAG = "3MBS";

    private static final String CONFIG_STORE_PATH =
	System.getProperty("user.home") +
	System.getProperty("file.separator") +
	".flywire-config";

    // Configure debug and logging. This is not part of the class interface.
    // If you set DEBUG false in released code logging will be turned off or
    // it will be delegated to a higher authority.
    protected static final boolean DEBUG = false;
    protected static final Level LEVEL = DEBUG ? Level.FINE : null;
    protected static final Logger logger = Logger.getLogger(Config.class.getName());
    static { logger.setLevel(LEVEL); }

    protected static Config TheConfig;

    // The (base)names of files we use to carry control data and that may be
    // in containers that hold media files. If Config.ignoreControlFiles() is
    // true, files with these names will be filtered from media file lists.
    protected static final String[] controlFileNames =
    {
        "complete",
    };

    private PersistentValues values;

    public static synchronized Config getConfig()
    {
	if (TheConfig == null)
	    TheConfig = new Config();

	return TheConfig;
    }

    public void save()
    {
	// save the configuration
	try
	{
	    FileOutputStream fos = new FileOutputStream(CONFIG_STORE_PATH);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(values);
	    oos.flush();
	    oos.close();
	    fos.close(); // Unnecessary?
	}
	catch (FileNotFoundException fnfe)
	{
	    logger.log(Level.SEVERE, "unexpected FileNotFoundException", fnfe);
	}
	catch (IOException ioe)
	{
	    logger.warning("error saving configuration - " + ioe.getLocalizedMessage());
	}
    }

    public void load()
    {
	// load the configuration
	boolean loaded = false;
	try
	{
	    FileInputStream fis = new FileInputStream(CONFIG_STORE_PATH);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    values = (PersistentValues) ois.readObject();
	    loaded = true;
	    ois.close();
	    fis.close();
	}
	catch (FileNotFoundException fnfe) { }
	catch (ClassNotFoundException cnfe)
	{
	    logger.severe("invalid saved configuration - " + cnfe.getLocalizedMessage());
	}
	catch (IOException ioe)
	{
	    logger.warning("error loading configuration - " + ioe.getLocalizedMessage());
	}

	if (!loaded) values = new PersistentValues();
    }

    /** Creates a new instance of Config */
    protected Config()
    {
	load();
    }

    //////////////////////////////////////////////////////////////////////////
    // Accessors
    //////////////////////////////////////////////////////////////////////////

    public static String getAppVersionString()
    {
	return "(v" + VERSION_NUM + (VERSION_TAG.isEmpty() ? "" : " - " + VERSION_TAG) + ")";
    }

    public boolean isWindowMaximised()
    {
        return values.options.windowMaximised;
    }

    public void setWindowMaximised(boolean state)
    {
	values.options.windowMaximised = state;
    }

    public boolean ignoreControlFiles()
    {
        return values.options.ignoreControlFiles;
    }

    public void ignoreControlFiles(boolean state)
    {
	values.options.ignoreControlFiles = state;
    }

    public static boolean isControlFile(java.io.File f)
    {
        String fn = f.getName();
        // Linear search because there's less than a handful of them.
        // FIXME: use a sorted collection if we end up with too many more.
        for (String name : controlFileNames)
            if (name.equals(fn))
                return true;

        return false;
    }

    public boolean alwaysShowHours()
    {
        return values.options.alwaysShowHours;
    }

    public void alwaysShowHours(boolean state)
    {
        values.options.alwaysShowHours = state;
    }

    public void setMainWindowPos(java.awt.Point pos)
    {
	values.mainWindowPos = pos;
    }

    public java.awt.Point getMainWindowPos()
    {
	return values.mainWindowPos;
    }

    public void setMainWindowSize(java.awt.Dimension size)
    {
	values.mainWindowSize = size;
    }

    public java.awt.Dimension getMainWindowSize()
    {
	return values.mainWindowSize;
    }

    public void setOptionsBoxPos(java.awt.Point pos)
    {
	values.optionsBoxPos = pos;
    }

    public java.awt.Point getOptionsBoxPos()
    {
	return values.optionsBoxPos == null ? new java.awt.Point(0,0) : values.optionsBoxPos;
    }

    public void forceStartPlayMode(boolean state)
    {
        values.options.forceStartPlayMode = state;
    }

    public boolean isStartPlayModeForced()
    {
        return values.options.forceStartPlayMode;
    }

    public Flywire.PlayMode getStartPlayMode()
    {
        return values.options.forceStartPlayMode ? values.options.forcedPlayMode : values.options.lastPlayMode;
    }

    public Flywire.PlayMode getLastPlayMode()
    {
        return values.options.lastPlayMode;
    }

    public void setForcedPlayMode(Flywire.PlayMode mode)
    {
       values.options.forcedPlayMode = mode;
    }

    public void setLastPlayMode(Flywire.PlayMode mode)
    {
       values.options.lastPlayMode = mode;
    }
}

/**
 * This is a wrapper around the persistent elements of the application's
 * configuration.
 */

class PersistentValues implements java.io.Serializable
{
    //////////////////////////////////////////////////////////////////////////
    // Version control
    private static final long serialVersionUID = 1L;

    private static final int internalVersion = 2;
    private int externalVersion = internalVersion;

    //////////////////////////////////////////////////////////////////////////
    // The Payload
    //////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    // App state
    String appVersion = Config.getAppVersionString();
    java.awt.Point mainWindowPos;
    java.awt.Dimension mainWindowSize;
    java.awt.Point optionsBoxPos;

    //////////////////////////////////////////////////////////////////////////
    // User-settable stuff
    Options options = new Options();

    //////////////////////////////////////////////////////////////////////////
    // Implementation
    //////////////////////////////////////////////////////////////////////////

    /**
     * We use this override to correct for different (but compatible) variants
     * of this class.
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
	ois.defaultReadObject();

	if (externalVersion != internalVersion)
	{
	    Logger logger = Logger.getLogger(Config.class.getName());

	    if (externalVersion < internalVersion) // written by earlier version
	    {
		logger.info("updating persistent data from v" + externalVersion + " to v" + internalVersion);
                Options defaultOptions = new Options();

                switch (externalVersion)
                {
                    case 0: // prehistoric development version
                    case 1: // some basic UI stuff
                        options.forceStartPlayMode = defaultOptions.forceStartPlayMode;
                        options.forcedPlayMode = defaultOptions.forcedPlayMode;
                        options.lastPlayMode = defaultOptions.lastPlayMode;
                    case 2: // added play mode persistence
                }
	    }
	    else // written by more recent version
	    {
		logger.warning("persistent data v" + internalVersion + " more recent than v" + externalVersion);
	    }
	    externalVersion = internalVersion;
	}
    }
}
