package hlpn2smt;
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
		
	public RefineZ3Converter(DataLayer _model, int _depth, ArrayList<Property> _prop) {
		super(_model, _depth, _prop);
		initialMarkingPlaceSet = findInitialMarkingPlaceSet();
		propertyPlaceSet = findPropertyPlaceSet();
	}
	
	public String convert(int depth){
		StringBuilder z3str = new StringBuilder();
		String functionHead = "void "+ model.pnmlName + "Checker(Z3_context ctx) {\n";
		z3str.append(functionHead);
		System.out.print("");
		z3str.append(declaration());
		z3str.append(buildStates());			
		z3str.append(iniStates());
		z3str.append(buildTransitions());		
		z3str.append(pb.buildProperties());
		z3str.append("\n}\n");
		return z3str.toString();
	} 
	
	/**
	 * find initial places 
	 */
	
	/**
	 * start from initial places and BFS the net
	 */
	
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
		int tsize = transitions.length+1;//the extra 1 is used for adding a dump transition
		trans.append("Z3_ast S"+currentStateID+"_trans_or["+tsize+"];"+nextline);
		for(int i=0;i<tsize-1;i++){
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
		trans.append("S"+currentStateID+"_trans_or["+(tsize-1)+"] = Z3_mk_implies(ctx, Z3_mk_true(ctx), "+tid+");"+nextline);
		
		//disjunct small transitions to a big one
		trans.append("Z3_ast BigTrans_S"+currentStateID+" = Z3_mk_or(ctx, "+tsize+", S"+currentStateID+"_trans_or);"+nextline);
		return trans.toString();
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
}
