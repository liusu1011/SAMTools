/**
 * 	Author:	Alexander Pataky
 * 	Date:	11/21/2010
 * 	Class:	SamCanvas
 * 	Use:	Extents JScrollPane and contains JPanel where model items will be drawn
 */

package SAMGUI.sam_view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import SAMGUI.sam_controller.CanvasAddMouseHandler;
import SAMGUI.sam_model.Arc;
import SAMGUI.sam_model.Component;
import SAMGUI.sam_model.Connector;
import SAMGUI.sam_model.Port;
import SAMGUI.sam_model.SamGlobals;
import SAMGUI.sam_model.SamModel;
import SAMGUI.sam_model.SamModelObject;
import SAMGUI.sam_view.SamFrame.Toggle;

public class SamCanvas extends JScrollPane {
	private final int BORDER_THICKNESS = 1;
	
	//number of times clicks have occurred while trying to add arc
	private int arcClicks = 0;
	
	//used for adding new arcs to model
	Arc tempArc;
	
	//used for inital naming of objects
	private final String compString = "Comp";
	private final String connString = "Conn";
	private final String portString = "P";
	private final String arcString = "A";
	
	
	private JPanel canvas = new JPanel();
	private Toggle toggle = Toggle.INVALID;
	private SamModel model = new SamModel();
	
	public SamModel getModel() {
		return model;
	}

	//counts for objects, used for naming purposes only
	private int compCount = 0;
	private int connCount = 0;
	private int portCount = 0;
	private int arcCount = 0;
	
	//used to keep track of number of labels in canvas for Z-Order
	private int labelCount = 0;
	
	public Toggle getToggle() {
		return toggle;
	}

	public void setToggle(Toggle toggle) {
		this.toggle = toggle;
	}
	
	public SamCanvas(String name) {
		this.setName(name);
		canvas = new JPanel();
		canvas.setBackground(Color.WHITE);
		canvas.setBorder(BorderFactory.createLineBorder(Color.BLACK, BORDER_THICKNESS));
		canvas.setPreferredSize(new Dimension(800, 600));
		this.setViewportView(canvas);
		canvas.setLayout(null);
		canvas.addMouseListener(new CanvasAddMouseHandler());
		this.setAutoscrolls(true);
		canvas.setAutoscrolls(true);
		

	}
	
	//loads canvas with all objects in model
	public SamCanvas(String name, SamModel model){
		this(name);
		this.model = model;
		this.model.isSubCompositionModel = model.getIsSubCompositionModel();
		this.model.parentComposition = model.getParentComposition();
		
		for(Component comp: model.getComponents().values()){
			canvas.add(comp);
		}
		
		for(Connector conn: model.getConnectors().values()){
			canvas.add(conn);
		}
		
		for(Port port: model.getPorts().values()){
			canvas.add(port);
		}
		
		for(Arc arc: model.getArcs().values()){
			canvas.add(arc);
			arc.updateBounds();
		}
	}
	
	public JPanel getCanvas() {
		return canvas;
	}
	
	//used by mouse event handler to add objects when toggle buttons are set
	public void addSamModelObject(Point p, Object obj) {
		boolean arcAdded = true;
		
		if (toggle == Toggle.COMPONENT) {
			if(obj instanceof JPanel){
				Component x = new Component();
				x.setLocation(p);
				model.add(compString + compCount, x);
				canvas.add(x);
				compCount++;
				labelCount++;
			}

		} else if (toggle == Toggle.CONNECTOR) {
			if(obj instanceof JPanel){
				Connector x = new Connector();
				x.setLocation(p);
				model.add(connString + connCount, x);
				canvas.add(x);
				connCount++;
				labelCount++;
			}

		} else if (toggle == Toggle.ARC) {
			if(obj instanceof SamModelObject){
				SamModelObject samObj = (SamModelObject) obj;
				if(arcClicks == 0){
					tempArc = new Arc(model, arcString + arcCount);
					if(!(tempArc.setStart(samObj))){
						arcClicks = 100;
					}
					else{
						SamFrame.getInstance().getStatusBar().setLabel(SamStatusBar.ARC_START_SET);
					}
				}
				
				else if(arcClicks == 1){
					if(tempArc.setEnd(samObj)){
						model.add(arcString + arcCount, tempArc);
						canvas.add(tempArc);
						arcCount++;
					}
					
				}
				arcClicks++;
			}
			else{
				arcClicks = 100;
				
			}
			
			
		} else if (toggle == Toggle.PORT) {
			if(obj instanceof Component){
				Component comp = (Component) obj;
				if(!(comp.isFull())){
					Port x = new Port();
					model.add(portString + portCount, x);
					portCount++;
					labelCount++;
					comp.addPort(x.getName());
					canvas.add(x);
				}
			}
			
		}
		
		if(toggle != Toggle.INVALID){
			canvas.repaint();
			canvas.validate();
		}
		
		if(toggle != Toggle.ARC){
			toggle = Toggle.INVALID;
			SamFrame.getInstance().setStatus(SamStatusBar.EMPTY);
		}
		
		if(arcClicks != 1){
			toggle = Toggle.INVALID;
			SamFrame.getInstance().setStatus(SamStatusBar.EMPTY);
			arcClicks = 0;
		}
		

		SamFrame.getInstance().setToggle(toggle);

	}
	
	//removes o from model
	public void removeSamModelObject(SamModelObject o){
		int x = model.remove(o.getName());
		labelCount = labelCount - x;

		canvas.repaint();
		canvas.validate();
	}
	

}
