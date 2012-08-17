package org.bentokit.flywire.gui;

import javax.swing.JLabel;

/**
 * Frequently used Utilities relating to the GUI.
 * @author tudor
 *
 */
public class GUIUtils {
	/**
	 * Set the foreground on container and all its children, recursively.
	 * @param container - The container to be changed.
	 * @param color - The color to set them to.
	 */
    public static void setForeground(java.awt.Container container, java.awt.Color color)
    {
        java.awt.Component[] components = container.getComponents();

        for (java.awt.Component component : components)
        {
            if (component instanceof JLabel)
                component.setForeground(color);
            else if (component instanceof java.awt.Container)
                GUIUtils.setForeground((java.awt.Container)component, color);
        }
        container.validate();
    }
    
	/**
	 * Set the foreground on container and all its children, recursively.
	 * @param container - The container to be changed.
	 * @param color - The color to set them to.
	 */
    public static void setBackground(java.awt.Container container, java.awt.Color color)
    {
        java.awt.Component[] components = container.getComponents();

        for (java.awt.Component component : components) {
            //component.setBackground(color);
            if (component instanceof JLabel)
                component.setBackground(color);
            if (component instanceof java.awt.Container)
                GUIUtils.setBackground((java.awt.Container)component, color);
        }
        container.validate();
   }


}
