package org.bentokit.flywire.gui.components;

import javax.swing.JLabel;

public class StringComponent extends ValueComponent<String> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5510491870786595601L;
	
	JLabel label;
	
	public StringComponent() {
		this.label = new JLabel();
	}
	
	public StringComponent(String value) {
		this();
		this.setValue(value);
	}
	
	@Override
	public String getValue() {
		return this.label.getText();
	}

	@Override
	public void setValue(String value) {
		this.label.setText(value);		
	}
	
}
