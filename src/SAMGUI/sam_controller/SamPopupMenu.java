/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * popupmenu for sammodel objects
 */
package SAMGUI.sam_controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import pipe.gui.CreateGui;

import SAMGUI.sam_model.*;
import SAMGUI.sam_view.SamFrame;

public class SamPopupMenu extends JPopupMenu {
	private SamModelObject obj;
	
	public SamPopupMenu(SamModelObject samObj) {
		super();
		
		this.obj = samObj;
		
		JMenuItem menuItem = new JMenuItem(new DeleteFromMenuHandler(obj)); //pass action as parameter, pass object as param to action?
		menuItem.setText("Delete");
		this.add(menuItem);
		
		menuItem = new JMenuItem(new PropertyMenuHandler(obj)); //pass action as parameter, pass object as param to action?
		menuItem.setText("Properties");
		this.add(menuItem);
		
		if(obj instanceof Component){
			menuItem = new JMenuItem();
			menuItem.setText("Lower Level Composition");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					SamFrame.getInstance().addSubCompositionTab((Component)obj);
				}
			});
			
			this.add(menuItem);
		}		
		
		if(obj instanceof Component){
			menuItem = new JMenuItem();
			menuItem.setText("Element Level Specification");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						init_PIPEplus((Component)obj);
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerFactoryConfigurationError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			this.add(menuItem);
		}
		
		if(obj instanceof Connector){
			menuItem = new JMenuItem();
			menuItem.setText("Add/Edit Formula");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					initFormulaPanel();
				}
			});
			
			this.add(menuItem);
		}
				
		if(obj instanceof Arc){
			menuItem = new JMenuItem(); //pass action as parameter, pass object as param to action?
			menuItem.setText("Add Point");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					addPoint();
				}
			});
			this.add(menuItem);
			
			menuItem = new JMenuItem(); //pass action as parameter, pass object as param to action?
			menuItem.setText("Remove Point");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					removePoint();
				}
			});
			this.add(menuItem);
		}
	}

	public SamPopupMenu(SamModelObject obj, String label) {
		this(obj);
		super.setLabel(label);
		
		// TODO Auto-generated constructor stub
	}
	
	private void addPoint(){
		Arc arc = (Arc) obj;
		arc.addPoint();
	}
	
	private void removePoint(){
		Arc arc = (Arc) obj;
		arc.removePoint();
	}
	
	private void init_PIPEplus(Component curComponent) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException{
		CreateGui cg = new CreateGui();
		cg.init(curComponent);
	}
	
	private void initFormulaPanel(){
		ConnectorFormulaDialog fd = new ConnectorFormulaDialog((Connector)obj);
		fd.setVisible(true);
	}

}
