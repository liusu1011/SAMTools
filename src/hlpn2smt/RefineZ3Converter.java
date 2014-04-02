package hlpn2smt;
import hlpn2smt.PlaceGraphNode.NodeType;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import formulaParser.ErrorMsg;
import formulaParser.Formula2SMTZ3;
import formulaParser.Parse;
import formulaParser.formulaAbsyntree.Sentence;
import pipe.dataLayer.Arc;
import pipe.dataLayer.BasicType;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.DataType;
import pipe.dataLayer.Place;
import pipe.dataLayer.Token;
import pipe.dataLayer.Transition;

public class RefineZ3Converter extends HLPNModelToZ3Converter{
	HashSet<String> initialMarkingPlaceSet; //stores place ID instead of name
	HashSet<String> propertyPlaceSet; //stores place ID instead of name
	HashSet<String> placeSet; //stores place ID of all the places
	ArrayList<Cluster> clusters; // partition the net into clusters, where transitions in one clusters can be combined

	public RefineZ3Converter(DataLayer _model, int _depth, ArrayList<Property> _prop) {
		super(_model, _depth, _prop);
		initialMarkingPlaceSet = findInitialMarkingPlaceSet();
		propertyPlaceSet = findPropertyPlaceSet();
		placeSet = findAllPlaceSet();
		clusters = new ArrayList<Cluster>();
		buildClusters();
		
	}
	
	public String convert(int depth){
		StringBuilder z3str = new StringBuilder();
		String functionHead = "void "+ model.pnmlName + "Checker(Z3_context ctx) {\n";
		z3str.append(functionHead);
		System.out.print("");
		z3str.append(declaration());
		z3str.append(buildStates());			
		z3str.append(iniStates());
//		buildPreTransitionFromPrPlace();
		z3str.append(buildTransitions());		
		z3str.append(pb.buildProperties());
		z3str.append("\n}\n");
		return z3str.toString();
	} 
	
	protected String buildTransitions(){
		StringBuilder z3transitions = new StringBuilder();
		z3transitions.append("//refined transitions"+nextline);
		//build depth number of transitions
		z3transitions.append("Z3_ast transitions_and["+depth+"];"+nextline);
		for(int i=0;i<depth;i++){
			z3transitions.append(nextline);
			z3transitions.append(oneBigTrans(i));
			String t = "transitions_and["+i+"] = BigTrans_S"+i+";"+nextline;
			z3transitions.append(t);
		}
		z3transitions.append(nextline);
		z3transitions.append("Z3_assert_cnstr(ctx, Z3_mk_and(ctx, "+depth+", transitions_and));"+nextline);
		
		return z3transitions.toString();
	}
	
	/**
	 * refined one using BFS start from the initial marking places
	 */
	protected String oneBigTrans(int currentStateID){
		StringBuilder trans = new StringBuilder();
		Transition[] transitions = model.getTransitions();
		
		
//		int tsize = transitions.length+1;
		int ref_tsize = this.clusters.size()-1+1;//the extra 1 is used for adding a dump transition
		trans.append("Z3_ast S"+currentStateID+"_trans_or["+ref_tsize+"];"+nextline);
		for(int i=0;i<ref_tsize-1;i++){
			trans.append(oneTrans(currentStateID, i));
			String t = "S"+currentStateID+"_trans_or["+i+"] = t"+i+"S"+currentStateID+";"+nextline;
			trans.append(t);
		}
		//add dump transition
		String tid = "tDumpS"+currentStateID;
		trans.append("Z3_ast "+tid+"_and["+places.length+"];"+nextline);
		for(int i=0;i<places.length;i++){
			String pname = places[i].getName();
			trans.append(tid+"_and["+i+"] = "+
					mk_eq(getPlaceSet(currentStateID+1, pname), getPlaceSet(currentStateID, pname))+
					";"+nextline);
		}
		trans.append("Z3_ast "+tid+" = " +
				mk_and(places.length, tid+"_and")+";"+nextline);
		trans.append("S"+currentStateID+"_trans_or["+(ref_tsize-1)+"] = Z3_mk_implies(ctx, Z3_mk_true(ctx), "+tid+");"+nextline);
		
		//disjunct small transitions to a big one
		trans.append("Z3_ast BigTrans_S"+currentStateID+" = Z3_mk_or(ctx, "+ref_tsize+", S"+currentStateID+"_trans_or);"+nextline);
		return trans.toString();
	}	
	
	/**
	 * Concatenate transition conditions of all the dependent paths
	 * @param PrPlaceID
	 * @return
	 */
	protected String buildPreTransitionFromPrPlace(String PrPlaceID) {
		String z3Conds = "";
		z3Conds = "Z3_mk_true(ctx)";
		ArrayList<ArrayList<String>> paths = findDependentPaths(PrPlaceID);
		for(ArrayList<String> path: paths) {
			for(int i=0;i<path.size()-1;i++) {
				Transition t = findTransBetweenPlaces(path.get(i), path.get(i+1));
				ArrayList<String> preConds = t.getZ3TransConds();
				for(String pcond: preConds){
					z3Conds = "mk_and(ctx, "+z3Conds+", "+pcond+")";
				}
			}
			Transition tend = findTransBetweenPlaces(path.get(path.size()-1), PrPlaceID);
			ArrayList<String> preConds = tend.getZ3TransConds();
			for(String pcond: preConds){
				z3Conds = "mk_and(ctx, "+z3Conds+", "+pcond+")";
			}
		}
		
		
		return z3Conds;
	}
	
	protected String oneTrans(int currentStateID, int currTransID){
		StringBuilder oneTrans = new StringBuilder();
		Transition t = model.getTransition(currTransID);
		String transitionName = t.getName();
		String formula = t.getFormula();
		ArrayList<String> arcVarList = new ArrayList<String>();
//		ArrayList<String> transPreConds = new ArrayList<String>();
		
		//build token const for every arc variables
		//add token_arcvar const belongs to place set
		Iterator<Arc> itr_in = t.getArcInList().iterator();
		while(itr_in.hasNext()){
			Arc thisArc = itr_in.next();
			String arcVar = thisArc.getVar();
			String inPlaceName = thisArc.getSource().getName();
			arcVarList.add(arcVar);
			String z3var = "S"+currentStateID+"_"+transitionName+"_"+inPlaceName+"_"+arcVar;
			if(!(thisArc.getSource() instanceof Place)){
				continue;
			}
			
			//if it is powerset, new a const set and mk_eq to place set;
			//if it is a regular place, new a const token, and mk_set_member to place set;
			String setArcVarToPlaceClause = "";
			if(((Place)(thisArc.getSource())).getDataType().getPow()){
				oneTrans.append("Z3_ast "+z3var+" = " +
						"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"S"+currentStateID+arcVar+"\"), "+ 
						"Z3_mk_set_sort(ctx, "+getSortByPlaceName(inPlaceName)+"));"
						+nextline);	
				setArcVarToPlaceClause =
					"Z3_mk_eq(ctx, "+z3var+", "+getPlaceSet(currentStateID, inPlaceName)+")";
			}else{
				oneTrans.append("Z3_ast "+z3var+" = " +
						"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"S"+currentStateID+arcVar+"\"), "+ 
						getSortByPlaceName(inPlaceName)+");"
						+nextline);
				setArcVarToPlaceClause = 
					"Z3_mk_set_member(ctx, "+z3var+", "+getPlaceSet(currentStateID, inPlaceName)+")";
			}
			if(!setArcVarToPlaceClause.equals(""))
				t.addZ3TransConds(setArcVarToPlaceClause);
//				transPreConds.add(setArcVarToPlaceClause);
		}
		Iterator<Arc> itr_out = t.getArcOutList().iterator();
		while(itr_out.hasNext()){
			Arc thisArc = itr_out.next();
			String arcVar = thisArc.getVar();
			String outPlaceName = thisArc.getTarget().getName();
//			TODO: solve the arcVar ' pie problem
//			if(arcVar.charAt(arcVar.length()-1) == 47){
//				arcVar = arcVar.substring(0, arcVar.length()-1) + "_pie";
//			}
			arcVarList.add(arcVar);
			String z3var = "S"+currentStateID+"_"+transitionName+"_"+outPlaceName+"_"+arcVar;
			if(!(thisArc.getTarget() instanceof Place)){
				continue;
			}
			//if it is powerset, new a const set and mk_eq to place set;
			//if it is a regular place, new a const token, and mk_set_member to place set;
			if(((Place)(thisArc.getTarget())).getDataType().getPow()){
				oneTrans.append("Z3_ast "+z3var+" = " +
						"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"S"+currentStateID+arcVar+"\"), "+ 
						"Z3_mk_set_sort(ctx, "+getSortByPlaceName(outPlaceName)+"));"
						+nextline);	
			}else{
				oneTrans.append("Z3_ast "+z3var+" = " +
						"Z3_mk_const(ctx, Z3_mk_string_symbol(ctx, \"S"+currentStateID+arcVar+"\"), "+
						getSortByPlaceName(outPlaceName)+");"
						+nextline);
				String setArcVarToPlaceClause = 
						"Z3_mk_set_member(ctx, "+z3var+", "+getPlaceSet(currentStateID, outPlaceName)+")";
				if(!setArcVarToPlaceClause.equals(""))
					t.addZ3TransConds(setArcVarToPlaceClause);
			}
			
		}
		
		ErrorMsg errorMsg = new ErrorMsg(formula);
		Parse p = new Parse(formula, errorMsg);
		Sentence s = p.absyn;
		Formula2SMTZ3 f2z = new Formula2SMTZ3(errorMsg, t, currentStateID, 
				this.placeNameIdMap, this.placeNameSortMap, this.stringConstantMap);
		s.accept(f2z);
		
		t.addZ3TransConds(s.z3str);
//		transPreConds.add(s.z3str);
		
		//extra variables are generated from uservariables when quantifier presents.
		for(String extraVars:f2z.z3GetExtraVars()){
			oneTrans.append(extraVars+nextline);
			t.addZ3TransConds(extraVars);
		}
//		transPreConds.addAll(f2z.z3GetPreConds());
		for(String ps:f2z.z3GetPreConds()){
			t.addZ3TransConds(ps);
		}
//		ArrayList<String> printarray = t.getZ3TransConds();
//		for(String pt:printarray) {
//			System.out.println(pt);
//		}
		return oneTrans.toString();
	}
	
	protected HashSet<String> findInitialMarkingPlaceSet() {
		HashSet<String> set = new HashSet<String>();
		for(Place p: model.getPlaces()) {
			if(p.getToken().getTokenCount() != 0) {
				set.add(p.getId());
			}
		}
		
		return set;
	}
	
	protected HashSet<String> findPropertyPlaceSet() {
		HashSet<String> set = new HashSet<String>();
		for(Property p: properties) {
			String pname = p.placeName;
			set.add(pname);
		}
		
		return set;
	}
	
	protected HashSet<String> findAllPlaceSet() {
		HashSet<String> set = new HashSet<String>();
		for(Place p: model.getPlaces()) {
			set.add(p.getId());
		}
		return set;
	}
	
	//build clusters from initial marking, property places, and after property places
	protected void buildClusters() {
		this.clusters.add(buildInitialClusters());
		this.clusters.addAll(buildPropertyClusters());
//		this.clusters.add(buildAfterClusters());
	}

	private Cluster buildInitialClusters() {
//		ArrayList<String> searchList = new ArrayList<String>();
		Cluster newCluster = new Cluster();
		//add initial marking to new cluster
		Iterator<String> iniIterator = this.initialMarkingPlaceSet.iterator();
		while(iniIterator.hasNext()) {
			String placeID = iniIterator.next();
			newCluster.add(placeID);
			this.placeSet.remove(placeID);
		}
		//search and expand new cluster use BFS
		Iterator<String> clitr = newCluster.placeIDs.iterator();
		ArrayList<String> bfsTemp = new ArrayList<String>();
		while(clitr.hasNext()) {
			String pid = clitr.next();
			bfsTemp.add(pid);
		}
		
		while(!bfsTemp.isEmpty()) {
			String curPid = bfsTemp.remove(0);
			Place curPlace = this.model.getPlaceById(curPid);
			ArrayList<Transition> outputTransitions = curPlace.getTransOutList();
			for(Transition t: outputTransitions) {
				ArrayList<Place> nextPlaces = t.getPlaceOutList();
				for(Place np: nextPlaces) {
					//not a property place and have not searched yet
					if(!propertyPlaceSet.contains(np.getId()) && this.placeSet.contains(np.getId())) {
						newCluster.add(np.getId());
						bfsTemp.add(np.getId());
						placeSet.remove(np.getId());
					}
				}
			}
		}

		return newCluster;
	}
	
	/**
	 * each property place is a singleton cluster
	 * @return
	 */
	private ArrayList<Cluster> buildPropertyClusters() {
		ArrayList<Cluster> newClusters = new ArrayList<Cluster>();
		Iterator<String> propItr = this.propertyPlaceSet.iterator();
		while(propItr.hasNext()) {
			String pid = propItr.next();
			Cluster c = new Cluster(pid);
			if(newClusters.add(c))
				this.placeSet.remove(pid);
		}
		return newClusters;
	}
	
//	private Cluster buildAfterClusters() {
//		
//	}

	/**
	 * DFS find a path in initial cluster to property place;
	 * reverse direction from property place, because in a path to PrPlace, it may have two paths merge into one path through one transition
	 * thus, if start from one initial place, it missed another branch that will merge into this path. 
	 */
	private ArrayList<ArrayList<String>> findDependentPaths(String PrPlaceID) {
		ArrayList<ArrayList<String>> paths = new ArrayList<ArrayList<String>>();
		findDependentPathsDFS(paths, new ArrayList<String>(), PrPlaceID);
		return paths;
	}
	
	private void findDependentPathsDFS(ArrayList<ArrayList<String>> paths, ArrayList<String> path, String curPid) {
		Place curPlace = this.model.getPlaceById(curPid);
		if(initialMarkingPlaceSet.contains(curPid)) {
			paths.add(new ArrayList<String>(path));
			return;
		}else if(curPlace.getTransInList().isEmpty()) {
			return;
		}
		
		ArrayList<Transition> inputTransitions = curPlace.getTransInList();
		for(Transition t: inputTransitions) {
			ArrayList<Place> inputPlaces = t.getPlaceInList();
			for(Place p: inputPlaces) {
				path.add(p.getId());
				findDependentPathsDFS(paths, path, p.getId());
				path.remove(path.size()-1);
			}
		}
	}
	
	private Transition findTransBetweenPlaces(String pid1, String pid2) {
		ArrayList<Transition> p1OutTrans = this.model.getPlaceById(pid1).getTransOutList();
		ArrayList<Transition> p2InTrans = this.model.getPlaceById(pid2).getTransInList();
		for(int i=0;i<p1OutTrans.size();i++) {
			for(int j=0;j<p2InTrans.size();j++) {
				Transition t1 = p1OutTrans.get(i);
				Transition t2 = p2InTrans.get(j);
				if(t1.getId().equals(t2.getId())) {
					return t1;
				}
			}
		}
		return null;
	}
}
