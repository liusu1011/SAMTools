/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * 
 */
package SAMGUI.sam_model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;

import SAMGUI.sam_controller.DeleteHandler;
import SAMGUI.sam_controller.MouseActionHandler;
import SAMGUI.sam_controller.SamPopupMenu;
import SAMGUI.sam_controller.SelectionHandler;

public abstract class SamModelObject extends JComponent {

	protected JLabel label = new JLabel("");
	protected boolean isLabelSet = false;
	protected boolean selected = false;
	protected static SelectionHandler selectHandler = new SelectionHandler();
	protected static DeleteHandler deleteHandler = new DeleteHandler();
	protected SamModel parentModel;

	SamModelObject() {
		MouseActionHandler labelHandler = new MouseActionHandler();
		label.addMouseListener(labelHandler);
		label.addMouseMotionListener(labelHandler);
		
		this.setFocusable(true);
		this.setRequestFocusEnabled(true);
		
		this.addFocusListener(selectHandler);
		this.addMouseListener(selectHandler);
		this.addKeyListener(deleteHandler);
		this.setComponentPopupMenu(new SamPopupMenu(this));
	}

	public void setParentModel(SamModel parentModel) {
		this.parentModel = parentModel;
	}

	public SamModel getParentModel() {
		return parentModel;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	// gets the text of the label belonging to this component
	public JLabel getLabel() {
		return label;

	}

	// sets the text of the label belonging to his component
	public void setLabelText(String s) {

		label.setText(s);

		label.setBounds(label.getLocation().x, label.getLocation().y,
				label.getPreferredSize().width,
				label.getPreferredSize().height);
		
		isLabelSet = true;
	}

	public void delete() {
		this.setVisible(false);
	
		if(this.getParent() != null){
			this.getParent().remove(this);
		}
	}

	public void addNotify() {
		super.addNotify();
		if(!isLabelSet){
			label.setText(this.getName());
			this.setLabelLocation();
		}
		this.getParent().add(label);
		//this.getParent().setComponentZOrder(label, 0);
		
	}
	
	public void setLabelLocation(){
		int y = this.getY() - 25;
		
		if(y < 0){
			y = 0;
		}
		
		label.setBounds(this.getX(), y,
				label.getPreferredSize().width,
				label.getPreferredSize().height);
	}
	
	public void removeNotify(){
		super.removeNotify();
		label.setVisible(false);
		label.getParent().remove(label);
		label = null;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (selected) {
			g.setColor(Color.BLUE);
		} else {
			g.setColor(Color.BLACK);
		}

	}
	
	//not abstract because not all SamModelObjects actually need
	//this method, but that way calling it wont give me a problem
	//and I dont have to implement it in every class
	public void updateChildren(){
	}
	
	//only port and connector need to implment this method
	public boolean removeArc(String name){
		return true;
	}
	
	public String toString(){
		return this.getBounds().toString();
	}


}
