/**
 * 	Author:	Alexander Pataky
 * 	Date:	11/21/2010
 * 	Class:	SamSatusBar
 * 	Use:	Used to create simple status bar to display messages to user
 */

package SAMGUI.sam_view;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;

public class SamStatusBar extends JPanel {
	private static JLabel label;
	public static String ARC_START_SET = "Start/End must be connector or port type and both must be different types";
	public static String ARC_END_SET = "Arc successfully added";
	public static String ARC_INVALID = "Invalid selection for adding Arc";
	public static String EMPTY = " ";
	
	public SamStatusBar() {
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		label = new JLabel();
		this.add(label);
	}

	public void setLabel(String text) {
		label.setText(" " + text);
	}

}
