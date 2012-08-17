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
package org.bentokit.flywire.gui.panels;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileFilter;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;

import org.bentokit.flywire.errorutils.ErrorHandler;
import org.bentokit.flywire.media.MediaItem;
import org.bentokit.flywire.media.MusicPicker;
import org.bentokit.flywire.media.MusicPickerListener;
import org.bentokit.flywire.media.PlayList;

/**
 * The MusicPickerPanel randomly picks MediaItems from a list and fills the playlist to
 * a set capacity.  This is for AutoDJ.
 * 
 * @author tudor
 *
 */
public class MusicPickerPanel extends JPanel implements MusicPickerListener {
    public static final long serialVersionUID = 1L; //Why do we do this?

    public JLabel panelTitle;

    MusicPicker picker;

    public MusicPickerPanel(MusicPicker picker) {
        this.picker = picker;
        picker.addListener(this);

        panelTitle = new JLabel("Automatic DJ Mode");
        panelTitle.setFont(new Font("Sans Serif",Font.BOLD,20));
        panelTitle.setHorizontalAlignment(JLabel.CENTER);

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Music Picker",
                       TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                       new Font("Sans Serif",Font.BOLD,20)));
        this.add(panelTitle,"Center");
        this.add(new JLabel("Click \"Switch mode to\" (above) \nto change to a different mode"),"South");

    }

    public void insufficientFiles(int num) {
        if (num == 0) {
            JOptionPane.showMessageDialog(this,"There are no files in the directory "+picker.getDirectory().getPath()+".  AutoDJ requires at least 2.","Error!",JOptionPane.ERROR_MESSAGE);            
            ErrorHandler.error("No files in directory "+picker.getDirectory().getPath());
        }
        else if (num < 2) {
            JOptionPane.showMessageDialog(this,"There are "+num+" files in the directory "+picker.getDirectory().getPath()+".  AutoDJ requires at least 2.","Error!",JOptionPane.ERROR_MESSAGE);            
            ErrorHandler.error("Less than two files in directory "+picker.getDirectory().getPath());
        }

    }
}
