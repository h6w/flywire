/*
* Copyright 2004-2009 by dronten@gmail.com
*
* This source is distributed under the terms of the GNU PUBLIC LICENSE version 3
* http://www.gnu.org/licenses/gpl.html
*/

package org.bentokit.flywire.config;


/**
 * All preference values are defined here.
 */
public class Constants {
    public static final String          APP_NAME                        = "Flywire";
    public static final String          APP_COPYRIGHT                   = "Copyright 2010 by Bentokit Project";
    public static final String          APP_HOMEPAGE                    = "http://www.bentokit.org";
    public static final String          APP_LICENSE                     = "This program is distributed under the terms of the GPL v3.";
    public static final String          APP_VERSION                     = "1.0";
    public static final String          CODEPAGE                        = "codepage";

    public static final int             LOG_LEVEL                       = 3;
    public static final boolean         DEBUG                           = false;

    public static final String          FLYWIRE_DIR_SHOWMEDIA_KEY          = "flywire.dir.showmedia";
    public static final String          FLYWIRE_DIR_SHOWMEDIA_DEFAULT      = "ShowMedia";
    public static final String          FLYWIRE_DIR_SCHEDULE_KEY           = "flywire.dir.schedule";
    public static final String          FLYWIRE_DIR_SCHEDULE_DEFAULT       = "Schedule";
    public static final String          FLYWIRE_DIR_SCHEDULEDMEDIA_KEY     = "flywire.dir.scheduledmedia";
    public static final String          FLYWIRE_DIR_SCHEDULEDMEDIA_DEFAULT = "ScheduledMedia";
    public static final String          FLYWIRE_DIR_LOGS_KEY               = "flywire.dir.logs";
    public static final String          FLYWIRE_DIR_LOGS_DEFAULT           = "Logs";

    public static final String[]        SETUP_DIALOG_TABS               = {"Directories         "};
	public static final String 			ALWAYS_SHOW_HOURS_KEY 			= "flywire.clock.show.hours";
	public static final boolean 		ALWAYS_SHOW_HOURS_DEFAULT 		= false;
	public static final String	 		EXPOSE_LIST_MODE_KEY 			= "flywire.listmode.expose";
	public static final boolean 		EXPOSE_LIST_MODE_DEFAULT 		= false;
	public static final String 			IGNORE_CONTROL_FILES_KEY 		= "flywire.controlfiles.ignore";
	public static final boolean 		IGNORE_CONTROL_FILES_DEFAULT 	= false;
	public static final String 			WINDOW_MAXIMISED_KEY 			= "flywire.startup.maximised";
	public static final boolean 		WINDOW_MAXIMISED_DEFAULT 		= false;
	public static final String 			STARTUP_PLAY_MODE_KEY 			= "flywire.startup.mode";
	public static final String 			STARTUP_PLAY_MODE_DEFAULT 		= "show";
	public static final String 			FLYWIRE_PLUGINS_PREFIX_KEY 		= "flywire.pluginprefix";
	public static final String 			FLYWIRE_PLUGINS_PREFIX_DEFAULT 	= "flywire.plugins";
}
