package org.bentokit.flywire.gui.components;

import javax.swing.JPanel;

/**
 * A simple abstract class to link a value to its displayable representation.
 * The idea is that when the value changes, its display changes along with it.
 * Leaving the implementation of this to the inherited class allows us to do 
 * programmatic changes on a consistent interface while allowing the inherited
 * class to decide how that information is displayed graphically. 
 * 
 * However, NOT vice versa.
 * 
 * @author tudor
 *
 * @param <VT> The value type;
 */
public abstract class ValueComponent<VT> extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5721056025380128328L;

	/**
	 * Get the value as a String.
	 * @return This value as a String.
	 */
	public abstract VT getValue();
	
	/**
	 * This function should be overridden in each type to update the display as part of setting the value.
	 * @param str
	 */
	public abstract void setValue(VT value);
}
