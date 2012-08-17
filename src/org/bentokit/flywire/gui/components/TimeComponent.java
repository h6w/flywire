package org.bentokit.flywire.gui.components;

import javax.media.Time;
import javax.swing.JLabel;

import org.bentokit.flywire.util.TimeNumberFormat;

public class TimeComponent extends ValueComponent<Time> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1541249157945873867L;
	Time time;
	JLabel label;
	
	public TimeComponent() {
		this.time = null;
		this.label = new JLabel("");
		this.add(this.label);
    	this.label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
	}
	
	
	public Time getValue() {
		return this.time;
	}
	
	@Override
	public void setValue(Time value) {
		String timeString = value == null ||
		value == javax.media.Player.DURATION_UNKNOWN ?
				"UNKNOWN" : TimeNumberFormat.formatTimeString(value);
		this.label.setText(timeString);
	}
}
