/*
* Copyright 2004-2009 by dronten@gmail.com
*
* This source is distributed under the terms of the GNU PUBLIC LICENSE version 3
* http://www.gnu.org/licenses/gpl.html
*/

package org.bentokit.flywire.gui.dialog.setup;

import org.bentokit.flywire.config.Constants;
import org.bentokit.flywire.gui.ComponentFactory;
import org.bentokit.flywire.util.Pref;

import javax.swing.*;
import java.awt.*;

/**
 * Set some misc. options (lookefeel, allowempty, sleep, basedir).
 */
public class General extends BaseSetupPanel {
    /**
	 * Generated serialVersionUID - TH 20101110
	 */
	private static final long serialVersionUID = -5353482791837978626L;
    private javax.swing.JCheckBox cbAlwaysShowHours;
    private javax.swing.JCheckBox cbExposeListMode;
    private javax.swing.JCheckBox cbIgnoreControlFiles;
    private javax.swing.JCheckBox cbWindowMaximised;


    /**
     *
     */
    public General() {
        cbAlwaysShowHours = ComponentFactory.createCheck(Pref.getPref(Constants.ALWAYS_SHOW_HOURS_KEY, Constants.ALWAYS_SHOW_HOURS_DEFAULT), "Always show hours", "Check to always show hours on the clock.", 0, 0);
        cbExposeListMode = ComponentFactory.createCheck(Pref.getPref(Constants.EXPOSE_LIST_MODE_KEY, Constants.EXPOSE_LIST_MODE_DEFAULT), "Enable list mode", "Check to enable list mode.", 0, 0);
        cbIgnoreControlFiles = ComponentFactory.createCheck(Pref.getPref(Constants.IGNORE_CONTROL_FILES_KEY, Constants.IGNORE_CONTROL_FILES_DEFAULT), "Maximise main window", "Check to maximise the window on startup.", 0, 0);
        cbWindowMaximised = ComponentFactory.createCheck(Pref.getPref(Constants.WINDOW_MAXIMISED_KEY, Constants.WINDOW_MAXIMISED_DEFAULT), "Ignore control files", "Check to ignore all control files.", 0, 0);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Box.createRigidArea(new Dimension(0, 5)));
        add(ComponentFactory.createOnePanel(cbAlwaysShowHours));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(ComponentFactory.createOnePanel(cbExposeListMode));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(ComponentFactory.createOnePanel(cbIgnoreControlFiles));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(ComponentFactory.createOnePanel(cbWindowMaximised));
    }

    /**
     * Save settings.
     */
    public void save() {
        Pref.setPref(Constants.ALWAYS_SHOW_HOURS_KEY, cbAlwaysShowHours.isSelected());
        Pref.setPref(Constants.EXPOSE_LIST_MODE_KEY, cbExposeListMode.isSelected());
        Pref.setPref(Constants.IGNORE_CONTROL_FILES_KEY, cbIgnoreControlFiles.isSelected());
        Pref.setPref(Constants.WINDOW_MAXIMISED_KEY, cbWindowMaximised.isSelected());
    }
}
