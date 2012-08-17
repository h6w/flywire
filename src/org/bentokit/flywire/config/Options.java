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

import org.bentokit.flywire.Flywire;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * NB: the default starting state of the options recreates the
 * look and behaviour of Flywire when I started working with
 * it in August 2009.
 *
 * @author cbpaine
 */
public class Options implements java.io.Serializable, Cloneable
{
    private static final long serialVersionUID = 1L;

    enum LogType
    {
	NONE("No logging"),
	CONSOLE("Console"),
	FILE("File"),
	JIT("JIT");

	private final String face;

	LogType(String face)
	{
	    this.face = face;
	}

        @Override
	public String toString()
	{
	    return face;
	}
    }

    java.util.logging.Level logLevel;
    LogType logType = LogType.CONSOLE;
    String logPath;

    boolean alwaysShowHours = false;
    boolean windowMaximised = true;
    boolean exposeListMode = false;
    boolean ignoreControlFiles = false;
    Flywire.PlayMode lastPlayMode = Flywire.PlayMode.SHOW;
    Flywire.PlayMode forcedPlayMode = Flywire.PlayMode.SHOW;
    boolean forceStartPlayMode = true;

    /** Creates a new instance of Options */
    public Options()
    {
	logLevel = java.util.logging.Level.FINE;
	logType = LogType.JIT;
	logPath = null;
    }

    @Override
    public Options clone()
    {
	try
	{
	    return (Options) super.clone();
	}

	catch (CloneNotSupportedException ex)
	{
	    java.util.logging.Logger.getLogger(Options.class.getName())
		.severe("unexpected internal failure - " + ex.getLocalizedMessage());
	    return new Options();
	}
    }

    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException
    {
	ois.defaultReadObject();

	// The code that follows attempts to reconcile differences in evolving
	// variants of this class. Until I see the need we won't have any kind
	// of explicit version identifier.
	if (logType == null)
	{
	    // Initialise with default logging options
	    Options dflt = new Options();
	    logType = dflt.logType;
	    logPath = dflt.logPath;
	    logLevel = dflt.logLevel;
	}
    }
}
