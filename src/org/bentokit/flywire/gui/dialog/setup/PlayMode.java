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
public class PlayMode extends BaseSetupPanel {
    /**
	 * Generated serialVersionUID - TH 20101110
	 */
	private static final long serialVersionUID = -5353482791837978626L;
    private javax.swing.JComboBox comboStartupPlayMode;


    /**
     *
     */
    public PlayMode() {
        comboStartupPlayMode = ComponentFactory.createCombo(new String[] { 
        	"Auto DJ",
        	"Show",
        	"Last",
        	"List"
        }, Pref.getPref(Constants.STARTUP_PLAY_MODE_KEY, Constants.STARTUP_PLAY_MODE_DEFAULT), "The default mode for the top left panel at startup.", 0, 0);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Box.createRigidArea(new Dimension(0, 5)));
        add(ComponentFactory.createOnePanel(comboStartupPlayMode));
    }

    /**
     * Save settings.
     */
    public void save() {
        Pref.setPref(Constants.STARTUP_PLAY_MODE_KEY, comboStartupPlayMode.getSelectedIndex());
    }
}
