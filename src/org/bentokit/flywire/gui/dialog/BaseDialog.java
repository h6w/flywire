/*
* Copyright 2011 Bentokit Project
*
* This source is distributed under the terms of the GNU PUBLIC LICENSE version 3
* http://www.gnu.org/licenses/gpl.html
*/

package org.bentokit.flywire.gui.dialog;

import javax.swing.*;
import java.awt.*;


/**
 * Base dialog object.
 * Implements common routines.
 */
public abstract class BaseDialog extends JDialog {
    /**
	 * Generated serialVersionUID - TH 20101110
	 */
	private static final long serialVersionUID = -341792794504667703L;


	/**
     * Create dialog.
     *
     * @param owner - Set to null to create a window that can hide below application.
     * @param title
     * @throws java.awt.HeadlessException
     */
    public BaseDialog(JFrame owner, String title, boolean modal) throws HeadlessException {
        super(owner, title, modal);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                close();
                setVisible(false);
                dispose();
            }
        });

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }


    /**
     * Center dialog on application window.
     */
    public void centerOnApplication() {
/*    	
        Point winPos = BRipper.get().getLocation();
        Dimension winSize = BRipper.get().getSize();
        Dimension dlgSize = getSize();

        double x = winPos.getX() + (winSize.width / 2);
        double y = winPos.getY() + (winSize.height / 2);

        x -= (dlgSize.width / 2);
        y -= (dlgSize.height / 2);
        setLocation(new Point((int) x, (int) y));
*/        
    }


    /**
     * Center dialog on screen.
     */
    public void centerOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dlgSize = getSize();
        Point newPos = new Point((screenSize.width / 2) - (dlgSize.width / 2), (screenSize.height / 2) - (dlgSize.height / 2));
        setLocation(newPos);
    }


    /**
     *
     */
    public void close() {
    }

    
    /**
     * Center dialog on parent window.
     */
    public void leftOnParent() {
        Point windowPos = this.getOwner().getLocation();
        setLocation(windowPos);
    }
}
