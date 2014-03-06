package SAMGUI.sam_model.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pipe.dataLayer.*;
import SAMGUI.sam_model.Component;
import SAMGUI.sam_model.Connector;
import SAMGUI.sam_model.Port;
import SAMGUI.sam_model.SamModel;
import SAMGUI.sam_model.SamModelObject;

public class ElementaryNetComposition {

	private SamModel samModel;
	private DataLayer netCompositionModel; // output final net model for analysis
	private ArrayList samConnectorTransitions; //convert sam connectors to transitions
	private ArrayList interfaceArcs; //arcs that added to connect Petri nets from different component
	
	public ElementaryNetComposition(SamModel model){
		this.samModel = model;
		this.netCompositionModel = new DataLayer();
		buildElementaryNetComposition();
		this.netCompositionModel.pnmlName = model.getName();
	}
	
	public void buildElementaryNetComposition(){
		samModelToNetCompositionModel();
		samConnectorToCompositionModel();
		samArcToCompositionModel();
	}
	
	// stack all behavior model into netCompositionModel	
	private void samModelToNetCompositionModel(){
		
		HashMap<String, Component> samComponents = this.samModel.getComponents();
		for(Component component : samComponents.values()){
			addPetriNetObjectsToModelFromComponent(component);
		}
		
	}
	
	private void addPetriNetObjectsToModelFromComponent(Component component){
		
		if(component.isSetSubComposition){
			HashMap<String, Component> subComponents = component.subCompositionModel.getComponents();
			for(Component subComponent : subComponents.values()){
				addPetriNetObjectsToModelFromComponent(subComponent);
			}
		}else if(component.isSetElemNetSpec){
			DataLayer tempDataLayer = component.getElemNetModel();
			Iterator i = tempDataLayer.getPetriNetObjects();
			while(i.hasNext()){
				Object o = i.next();
				if(o instanceof pipe.dataLayer.PetriNetObject){
					if(o instanceof Place){
						netCompositionModel.addPetriNetObject((Place)o);
					}else if(o instanceof Transition){
						netCompositionModel.addPetriNetObject((Transition)o);
					}else if(o instanceof pipe.dataLayer.Arc){
						netCompositionModel.addPetriNetObject((pipe.dataLayer.Arc)o);
					}else{
						System.out.println("This PetriNetObject is not supposed to be added to netCompositionModel.");
					}
				}
			}
		}else{
			System.out.println("loop error of iterating model!");
		}
	}
	
	
	private void samConnectorToCompositionModel(){
		HashMap<String, Connector> samConnectors = this.samModel.getConnectors();
		for(Connector connector : samConnectors.values()){
			convertSamConnectorToTransition(connector);
		}
		
		//subCompositions also has connectors
		HashMap<String, Component> samComponents = this.samModel.getComponents();
		for(Component component : samComponents.values()){
			if(component.isSetSubComposition){
				subCompositionConnectorsToCompositionModel(component);
			}
		}
		
		
	}
	
	//recursive iterate hierarchical level of Sam to get all connectors from sub level.
	private void subCompositionConnectorsToCompositionModel(Component component){
		HashMap<String, Connector> subConnectors = component.subCompositionModel.getConnectors();
		for(Connector subConnector : subConnectors.values()){
			convertSamConnectorToTransition(subConnector);
		}
		
		HashMap<String, Component> subComponents = component.subCompositionModel.getComponents();
		for(Component subComponent : subComponents.values()){
			if(subComponent.isSetSubComposition){
				subCompositionConnectorsToCompositionModel(subComponent);				
//				HashMap<String, Component> subsubComponents = subComponent.subCompositionModel.getComponents();
//				for(Component subsubComponent : subsubComponents.values()){
//					subCompositionConnectorsToCompositionModel(subsubComponent); //resursive call it self
//				}
			}
//			else{
//				HashMap<String, Connector> subConnectors = subComponent.subCompositionModel.getConnectors();
//				for(Connector subConnector : subConnectors.values()){
//					convertSamConnectorToTransition(subConnector);
//				}
//			}
		}
	}
	
	/**
	 * convert connector to transition;
	 * add the transition to net composition model;
	 * @param connector
	 */
	private void convertSamConnectorToTransition(Connector connector){
		Transition tempTransition = null;
		
		String transName = connector.getName();
		String netCompositionFormla = connector.getNetCompositionFormla();
		
		tempTransition = new Transition(0,0);
		int id = this.netCompositionModel.getTransitions().length;
		tempTransition.setId(Integer.toString(id));
		tempTransition.setName(transName);
		tempTransition.setFormula(netCompositionFormla);
		//SUTODO:any other attributes to set while build a new transition???
		
		this.netCompositionModel.addPetriNetObject(tempTransition);
	}
	
	private void samArcToCompositionModel(){
		HashMap<String, SAMGUI.sam_model.Arc> samArcs = this.samModel.getArcs();
		for(SAMGUI.sam_model.Arc arc : samArcs.values()){
			convertSamArcToPNArc(arc);
		}
		
		//subCompositions also has arcs
		HashMap<String, Component> samComponents = this.samModel.getComponents();
		for(Component component : samComponents.values()){
			if(component.isSetSubComposition){
				subCompositionArcsToCompositionModel(component);
			}
		}		
	}
	
	//recursive iterate hierarchical level of Sam to get all arcs from sub level.
	private void subCompositionArcsToCompositionModel(Component component){
		HashMap<String, SAMGUI.sam_model.Arc> subArcs = component.subCompositionModel.getArcs();
		for(SAMGUI.sam_model.Arc subArc : subArcs.values()){
			convertSamArcToPNArc(subArc);
		}
		
		HashMap<String, Component> subComponents = component.subCompositionModel.getComponents();
		for(Component subComponent : subComponents.values()){
			if(subComponent.isSetSubComposition){
				subCompositionArcsToCompositionModel(subComponent); //resursive call it self
			}
		}
	}
	
	
	/**
	 * two task:
	 * 1.convert sam_model arc to Petri net arc;
	 * 2.add to net composition model;
	 * @param arc
	 */
	private void convertSamArcToPNArc(SAMGUI.sam_model.Arc arc){
		SamModelObject start = arc.getStart();
		SamModelObject end = arc.getEnd();
		Place tempStartPlace = null;
		Connector tempStartConnector = null;
		Place tempEndPlace = null;
		Connector tempEndConnector = null;
		Integer i = 0;
		double d = i.doubleValue();
		Transition tempTrans = null;
		pipe.dataLayer.Arc tempArc = null;
		
		if(start != null){
			if(start instanceof Port){
				Port port = (Port)start;
				tempStartPlace = port.getPlaceInterface();
			}else if (start instanceof Connector){
				tempStartConnector = (Connector)start;
				String n = tempStartConnector.getName();
				PetriNetObject o = findPNObjectFromModelByName(n, "Transition");
				if(o instanceof Transition){
					tempTrans = (Transition)o;
				}
			}
		}
		
		if(end != null){
			if(end instanceof Port){
				Port port = (Port)end;
				tempEndPlace = port.getPlaceInterface();
			}else if(end instanceof Connector){
				tempEndConnector = (Connector)end;
				String m = tempEndConnector.getName();
				PetriNetObject o = findPNObjectFromModelByName(m, "Transition");
				if(o instanceof Transition){
					tempTrans = (Transition)o;
				}
			}
		}
		
		//new arc
		if(start != null && end != null){
			if(start instanceof Port){
				tempArc = new pipe.dataLayer.NormalArc(d, d, d, d, tempStartPlace, tempTrans, 0, "", false);
				tempArc.setVar(arc.getVar());
				tempArc.setVar();
				int arcId = this.netCompositionModel.getArcs().length;
				tempArc.setId(Integer.toString(arcId));
				tempStartPlace.addConnectFrom(tempArc);
				tempTrans.addConnectTo(tempArc);
			}else if(start instanceof Connector){
				tempArc = new pipe.dataLayer.NormalArc(d, d, d, d,tempTrans, tempEndPlace, 0, "", false);
				tempArc.setVar(arc.getVar());
				tempArc.setVar();
				int arcId = this.netCompositionModel.getArcs().length;
				tempArc.setId(Integer.toString(arcId));
				tempTrans.addConnectFrom(tempArc);
				tempEndPlace.addConnectTo(tempArc);
			}
		}
		
		//add to net model
		if(tempArc != null)
			this.netCompositionModel.addPetriNetObject(tempArc);
		
	}
	
	private PetriNetObject findPNObjectFromModelByName(String name, String type){
		PetriNetObject result = null;
		
		if(type.equals("Place")){
			Place [] places = this.netCompositionModel.getPlaces();
			for(int i=0; i<places.length;i++){
				if(places[i].getName().equals(name)){
					result = places[i];
					break;
				}
			}
		}else if(type.equals("Transition")){
			Transition [] transitions = this.netCompositionModel.getTransitions();
			for(int i=0; i<transitions.length;i++){
				if(transitions[i].getName().equals(name)){
					result = transitions[i];
					break;
				}
			}
		}else if(type.equals("Arc")){
			Arc [] arcs = this.netCompositionModel.getArcs();
			for(int i=0; i<arcs.length;i++){
				if(arcs[i].getName().equals(name)){
					result = arcs[i];
					break;
				}
			}
		}
		
		return result;
	}
	
	public DataLayer getNetCompositionModel(){
		return this.netCompositionModel;
	}
	
}
