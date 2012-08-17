/*
* Copyright 2004-2009 by dronten@gmail.com
*
* This source is distributed under the terms of the GNU PUBLIC LICENSE version 3
* http://www.gnu.org/licenses/gpl.html
*/

package org.bentokit.flywire.gui.dialog.setup;

import javax.swing.*;
import java.awt.*;


/**
 * Set some misc. options (lookefeel, allowempty, sleep, basedir).
 */
public class Logging extends BaseSetupPanel {
    /**
	 * Generated serialVersionUID - TH 20101110
	 */
	private static final long serialVersionUID = -5353482791837978626L;


    /**
     *
     */
    public Logging() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Box.createRigidArea(new Dimension(0, 5)));
    }

    /**
     * Save settings.
     */
    public void save() {
    }
}
