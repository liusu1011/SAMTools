package hlpn2smt;

import java.util.ArrayList;
import java.util.HashSet;

import pipe.dataLayer.Place;
import pipe.dataLayer.Transition;

public class Cluster {
	boolean expand;
//	ArrayList<Transition> transitions;
//	ArrayList<String> placeIDs;
	HashSet<String> placeIDs;
	
	public Cluster() {
		expand = true;
//		transitions = new ArrayList<Transition>();
		placeIDs = new HashSet<String>();
	}
	
	public Cluster(String pid) {
		expand = true;
		placeIDs = new HashSet<String>();
		placeIDs.add(pid);
	}
	
	public HashSet<String> getClusterPlaceIDs() {
		return placeIDs;
	}
	
	public void add(String pid) {
		placeIDs.add(pid);
	}
	
	public void remove(String pid) {
		placeIDs.remove(pid);
	}
	
	public int size() {
		return placeIDs.size();
	}
	
//	public Cluster(Transition t) {
//		expand = false;
//		transitions = new ArrayList<Transition>();
//		transitions.add(t);
//	}
//	
//	public ArrayList<String> getClusterPreConditions() {
//		ArrayList<String> preConditions = new ArrayList<String>();
//		
//		for(Transition trans : transitions) {
//			if(!trans.getZ3TransConds().isEmpty()) {
//				preConditions.addAll(trans.getZ3TransConds());
//			}
//		}
//		
//		return preConditions;
//	}
}
