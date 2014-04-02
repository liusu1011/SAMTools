package hlpn2smt;

import java.util.ArrayList;

public class PlaceGraphNode {
	public String ID;
	public NodeType type;
	public ArrayList<PlaceGraphNode> nextList;
	
	public enum NodeType {
		Regular, Initial, Property
	}
	
	public PlaceGraphNode(String _ID, NodeType _type, ArrayList<PlaceGraphNode> _nextList) {
		this.ID = _ID;
		this.type = _type;
		this.nextList = _nextList;
	}
}
