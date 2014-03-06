/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * 
 */
package SAMGUI.sam_model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SamModel {
	//stores all the model objexts
	private HashMap<String, Arc> arcs = new HashMap<String, Arc>();
	private HashMap<String, Component> components = new HashMap<String, Component>();
	private HashMap<String, Connector> connectors = new HashMap<String, Connector>();
	private HashMap<String, Port> ports = new HashMap<String, Port>();

	//isnt currently used for anything
	private String name;
	
	//whether the current model is a sub composition model
	public boolean isSubCompositionModel = false;

	public Component parentComposition;
	
	public SamModel() {

	}
	
	public SamModel(String name) {
		this.name = name;
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, Component> getComponents() {
		return components;
	}

	public HashMap<String, Connector> getConnectors() {
		return connectors;
	}

	public HashMap<String, Port> getPorts() {
		return ports;
	}

	public HashMap<String, Arc> getArcs() {
		return arcs;
	}

	public Component getComponent(String name) {
		return components.get(name);
	}

	public Connector getConnector(String name) {
		return connectors.get(name);
	}

	public Port getPort(String name) {
		return ports.get(name);
	}
	
	public Arc getArc(String name) {
		return arcs.get(name);
	}
	
	//takes name of something contained in one of the hashmaps above, and removes
	//it along with all its children (ex: component -> ports -> arcs)
	//returns number of objects removed
	public int remove(String name) {
		int result = 0;
		
		if(name == null){
			return 0;
		}
		
		Arc arc = arcs.remove(name);
		
		if (arc != null) {
			arc.delete();
			return 1;
		}

		Port port = ports.remove(name);

		if (port != null) {
			result = remove(port.getArcName());
			port.delete();
			result++;
			
			return result;
		}

		Connector connector = connectors.remove(name);

		if (connector != null) {
			for (String a : connector.getArcs()) {
				result += remove(a);
			}
			connector.delete();
			result++;
			
			return result;
		}

		Component component = components.remove(name);
		if (component != null) {
			for (String p : component.getPorts()) {
				result += remove(p);
			}
			result++;
			component.delete();
		}
		
		return result;
	}
	
	//adds value and with given key and sets key as name of value
	public boolean add(String key, Arc value) {
		if(!(containsKey(key))){
			value.setParentModel(this);
			value.setName(key);
			arcs.put(key, value);
			return true;
		}
		return false;	
	}

	//adds value and with given key and sets key as name of value
	public boolean add(String key, Component value) {
		if(!(containsKey(key))){
			value.setParentModel(this);
			value.setName(key);
			components.put(key, value);
			return true;
		}
		return false;
	}

	//adds value and with given key and sets key as name of value
	public boolean add(String key, Connector value) {
		if(!(containsKey(key))){
			value.setParentModel(this);
			value.setName(key);
			connectors.put(key, value);
			return true;
		}
		return false;
	}

	//adds value and with given key and sets key as name of value
	public boolean add(String key, Port value) {
		if(!(containsKey(key))){
			value.setParentModel(this);
			value.setName(key);
			ports.put(key, value);
			return true;
		}
		return false;
	}
	
	//name of value needs to be set before using this add
	public boolean add(Arc value) {
		if(!(containsKey(value.getName()))){
			value.setParentModel(this);
			arcs.put(value.getName(), value);
			return true;
		}
		return false;	
	}

	//name of value needs to be set before using this add
	public boolean add(Component value) {
		if(!(containsKey(value.getName()))){
			value.setParentModel(this);
			components.put(value.getName(), value);
			return true;
		}
		return false;
	}
	
	//name of value needs to be set before using this add
	public boolean add(Connector value) {
		if(!(containsKey(value.getName()))){
			value.setParentModel(this);
			connectors.put(value.getName(), value);
			return true;
		}
		return false;
	}

	//name of value needs to be set before using this add
	public boolean add(Port value) {
		if(!(containsKey(value.getName()))){
			value.setParentModel(this);
			ports.put(value.getName(), value);
			return true;
		}
		return false;
	}
	
	//checks all hashmaps to see if any contains key
	private boolean containsKey(String key){
		return (arcs.containsKey(key) || components.containsKey(key) || connectors.containsKey(key) || ports.containsKey(key));
	}
	
	//returns SamModelObject if it is contained in one of the hashmaps
	public SamModelObject getSamModelObject(String name){
		Arc arc = this.getArc(name);
		if(arc != null){
			return arc;
		}
		
		Port port = this.getPort(name);
		if(port != null){
			return port;
		}
		
		Component comp = this.getComponent(name);
		if(comp != null){
			return comp;
		}
		
		Connector conn = this.getConnector(name);
		if(conn != null){
			return conn;
		}
		
		return null;
	}
	
	public void setIsSubCompositionModelTure(){
		this.isSubCompositionModel = true;
	}
	
	public boolean getIsSubCompositionModel(){
		return this.isSubCompositionModel;
	}
	
	public void setParentComposition(Component c){
		this.parentComposition = c;
	}
	
	public Component getParentComposition(){
		return this.parentComposition;
	}
	
	public String getPropertyFormula(){
		String propertyFormula = "";
		boolean b = false;
		Iterator compItr = components.entrySet().iterator();
		while(compItr.hasNext()){
			Map.Entry<String, Component> entry = (Map.Entry<String, Component>)compItr.next();
			if(!b){
				propertyFormula += entry.getValue().getFormula();
				b = true;
			}else{
				propertyFormula += "\u2227" + entry.getValue().getFormula();
			}
			
		}
		
		return propertyFormula;
	}


}
