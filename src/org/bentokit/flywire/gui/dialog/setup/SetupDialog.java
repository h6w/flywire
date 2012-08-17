/*
* Copyright 2011 Bentokit Project
*
* This source is distributed under the terms of the GNU PUBLIC LICENSE version 3
* http://www.gnu.org/licenses/gpl.html
*/

package org.bentokit.flywire.gui.dialog.setup;

import org.bentokit.flywire.gui.ComponentFactory;
import org.bentokit.flywire.gui.dialog.BaseDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * A preference dialog.
 */
public class SetupDialog extends BaseDialog implements ActionListener, ListSelectionListener {
    /**
	 * Generated serialVersionUID - TH 20101110
	 */
	private static final long serialVersionUID = 2907101170772243752L;
	private JPanel              aSetupPanel = null;
    private JList               aList = null;
    private BaseSetupPanel[]    aPanels = null;
    private BaseSetupPanel      aCurrentPanel = null;


    /**
     * Create setup dialog.
     *
     * @param owner
     * @param titleText
     * @param closeText
     * @param choices
     * @param panels
     * @throws HeadlessException
     */
    public SetupDialog(JFrame owner, String titleText, String closeText, String[] choices, BaseSetupPanel[] panels) throws HeadlessException {
        super(owner, titleText, true);
        aPanels = panels;
        JButton closeButton = ComponentFactory.createButton(closeText, "", this, 0, 0);
        aList = new JList(choices);
        JScrollPane scrollPaneList = new JScrollPane();
        JPanel mainPanel = new JPanel();
        aSetupPanel = new JPanel();

        for (BaseSetupPanel panel : aPanels) {
            panel.setVisible(false);
        }

        aList.addListSelectionListener(this);
        scrollPaneList.getViewport().setView(aList);
        aSetupPanel.setLayout(ComponentFactory.createBorderLayout(5, 5));
        mainPanel.setLayout(ComponentFactory.createBorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainPanel.add(scrollPaneList, BorderLayout.WEST);
        mainPanel.add(aSetupPanel, BorderLayout.CENTER);
        mainPanel.add(closeButton, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(600, 600));
        setMaximumSize(new Dimension(2000, 2000));
        setContentPane(mainPanel);
        pack();
        aList.setSelectedIndex(0);
    }


    /**
     * Let all child panels save them self before quitting.
     *
     * @param actionEvent
     */
    public void actionPerformed(ActionEvent actionEvent) {
        for (BaseSetupPanel panel : aPanels) {
            panel.save();
        }
        setVisible(false);
    }


    /**
     * @param listSelectionEvent
     */
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        int index = aList.getSelectedIndex();

        if (aCurrentPanel != null) {
            aCurrentPanel.setVisible(false);
            aCurrentPanel = null;
        }

        if (index < aPanels.length) {
            aCurrentPanel = aPanels[index];
            aCurrentPanel.setVisible(true);
            aSetupPanel.add(aCurrentPanel, BorderLayout.NORTH);
            pack();
        }
    }
}
