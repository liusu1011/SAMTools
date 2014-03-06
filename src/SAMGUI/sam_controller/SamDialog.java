/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * dialog other dialogs extend
 */
package SAMGUI.sam_controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import SAMGUI.sam_model.SamModelObject;
import SAMGUI.sam_view.SamFrame;

public abstract class SamDialog extends JDialog {
	protected JTextField txtAnnotation;
	protected JPanel panel;
	
	private SamModelObject samObj;

	public SamDialog(SamModelObject samObj) {
		super(SamFrame.getInstance(), "Properties", true);
		this.samObj = samObj;
		this.setup();
		this.txtAnnotation.setText(samObj.getLabel().getText());
		
		
		
	}

	private void setup() {
		setMinimumSize(new Dimension(500, 400));
		setLocation(100,100);
		setResizable(false);
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SamDialog.this.dispose();
			}
		});
		btnCancel.setBounds(378, 338, 89, 23);
		panel.add(btnCancel);

		JButton btnSaveChanges = new JButton("Save");
		btnSaveChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				save();
			}
		});
		btnSaveChanges.setBounds(279, 338, 89, 23);
		panel.add(btnSaveChanges);

		JLabel lblRename = new JLabel("Annotation:");
		lblRename.setBounds(10, 14, 70, 14);
		panel.add(lblRename);

		txtAnnotation = new JTextField();
		txtAnnotation.setBounds(90, 11, 377, 20);
		panel.add(txtAnnotation);
		txtAnnotation.setColumns(10);
		
		this.getRootPane().setDefaultButton(btnSaveChanges);
		
		
	}
	
	protected void save(){
		samObj.setLabelText(txtAnnotation.getText());
	}

}
