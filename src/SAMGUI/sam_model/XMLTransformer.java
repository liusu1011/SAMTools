/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * 
 * Modified by Su Liu
 * Note: the load functions such as loadComponent's', load Connector's' ..... are not used;
 * we are currently using loadComponent, loadConnector,......
 */
package SAMGUI.sam_model;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLTransformer {
	SamModel model;
	
	public boolean saveFile(SamModel modelToSave, String path){
		try{
			try{
				model = modelToSave;
			    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			    Document document = documentBuilder.newDocument();
			    Element root = document.createElement("model");
			    document.appendChild(root);
			    
			    saveComponents(this.model, root, document);
			    saveConnectors(this.model, root, document);
			    savePorts(this.model, root, document);
			    saveArcs(this.model, root, document);    
			    
			    File outfile = new File(path);
			    
			    TransformerFactory transformerFactory = TransformerFactory.newInstance();
		        Transformer transformer = transformerFactory.newTransformer();
		        DOMSource source = new DOMSource(document);
			    StreamResult result =  new StreamResult(outfile);
			    try {
					transformer.transform(source, result);
				} catch (TransformerException e) {
					e.printStackTrace();
					return false;
				}
	
			    return true;
			}
			catch(ParserConfigurationException pce){
				return false;
			}
		
		}
		
		catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return false;
		}
		

	
	
	}
	
	public void saveModel(){
		
	}
	
	private void saveComponents(SamModel model, Element root, Document document){
	//	Example XML structure for component
	//		
	//		<component name="">
	//			<annotation>
	//		      <text></text>
	//		      <location>
	//		        <x></x>
	//		        <y></y>
	//		      </location>
	//		    </annotation>
	//		    <bounds>
	//		      <x></x>
	//		      <y></y>
	//		      <width></width>
	//		      <height></height>
	//		    </bounds>
	//		    <formula></formula>
	//		    <ports>
	//		      <portName index="0">name1</portName>
	//		      <portName index="1">name2</portName>
	//		    </ports>
	//	    </component>
		
		for(Component c: model.getComponents().values()){
			
			Element component = document.createElement("Component");
			Element bounds = document.createElement("bounds");
			Element x = document.createElement("x");
			Element y = document.createElement("y");
			Element width = document.createElement("width");
			Element height = document.createElement("height");
			Element formula = document.createElement("formula");
			Element ports = document.createElement("ports");
			Element annotation = document.createElement("annotation");
			Element subComposition = document.createElement("subComposition");
			Element elementaryNet = document.createElement("elementaryNet");
			
			component.setAttribute("name", c.getName());

			root.appendChild(component);
			
			component.appendChild(bounds);
			component.appendChild(formula);
			component.appendChild(ports);
			component.appendChild(annotation);
			component.appendChild(subComposition);
			component.appendChild(elementaryNet);
			
			//append subComposition under <subComposition> tag
			if(c.isSetSubComposition){
				saveComponents(c.subCompositionModel, subComposition, document);
				saveConnectors(c.subCompositionModel, subComposition, document);
			    savePorts(c.subCompositionModel, subComposition, document);
			    saveArcs(c.subCompositionModel, subComposition, document);
			}
			
			//append elementary net under <elementaryNet> tag
			if(c.isSetElemNetSpec){
				elementaryNet.appendChild(document.importNode(c.elemNetSpecModel.getDocumentElement(), true));
			}else System.out.println("Some components have empty elementary spec!");
			
			
			bounds.appendChild(x);
			bounds.appendChild(y);
			bounds.appendChild(width);
			bounds.appendChild(height);

			saveAnnotation(model, annotation, document, c);
			
			x.appendChild(document.createTextNode(String.valueOf(c.getX())));
			y.appendChild(document.createTextNode(String.valueOf(c.getY())));
			width.appendChild(document.createTextNode(String.valueOf(c.getWidth())));
			height.appendChild(document.createTextNode(String.valueOf(c.getHeight())));
			formula.appendChild(document.createTextNode(c.getFormula()));
			
			Integer i = 0;
			for(String port: c.getPorts()){
				
				if(port != null){
					Element portName = document.createElement("portName");
					portName.setAttribute("index", i.toString());
					portName.appendChild(document.createTextNode(port));
				
					ports.appendChild(portName);
				}
				
				i++;
			}
			
		}
	}
	
	private void saveConnectors(SamModel model, Element root, Document document){
	//	Example XML structure for connector
	//	
	//		<connector name="">
	//			<annotation>
	//		      <text></text>
	//		      <location>
	//		        <x></x>
	//		        <y></y>
	//		      </location>
	//		    </annotation>
	//		    <bounds>
	//		      <x></x>
	//		      <y></y>
	//		      <width></width>
	//		      <height></height>
	//		    </bounds>
	//		    <formula></formula>
	//		   <netCompositionFormula></netCompositionFormula>
	//		    <arcs>
	//		      <arcName index ="0">name1</arcName>
	//		      <arcName index ="1">name2</arcName>
	//		    </arcs>
	//	  </connector>
		for(Connector c: model.getConnectors().values()){
			
			Element connector = document.createElement("Connector");
			Element bounds = document.createElement("bounds");
			Element x = document.createElement("x");
			Element y = document.createElement("y");
			Element width = document.createElement("width");
			Element height = document.createElement("height");
			Element arcs = document.createElement("arcs");
			Element annotation = document.createElement("annotation");
			Element formula = document.createElement("formula");
			Element netCompoistionFormula = document.createElement("netCompositionFormula");
			
			connector.setAttribute("name", c.getName());
			
			root.appendChild(connector);
			connector.appendChild(bounds);
			connector.appendChild(arcs);
			connector.appendChild(annotation);
			connector.appendChild(formula);
			connector.appendChild(netCompoistionFormula);
			
			bounds.appendChild(x);
			bounds.appendChild(y);
			bounds.appendChild(width);
			bounds.appendChild(height);

			saveAnnotation(model, annotation, document, c);
			
			x.appendChild(document.createTextNode(String.valueOf(c.getX())));
			y.appendChild(document.createTextNode(String.valueOf(c.getY())));
			width.appendChild(document.createTextNode(String.valueOf(c.getWidth())));
			height.appendChild(document.createTextNode(String.valueOf(c.getHeight())));
			formula.appendChild(document.createTextNode(c.getFormula()));
			netCompoistionFormula.appendChild(document.createTextNode(c.getNetCompositionFormla()));
			
			Integer i = 0;
			for(String arc: c.getArcs()){
				
				Element arcName = document.createElement("arcName");
				arcName.setAttribute("index", i.toString());
				arcName.appendChild(document.createTextNode(arc));
				
				arcs.appendChild(arcName);
				
				i++;
			}
		}
	}
	
	private void savePorts(SamModel model, Element root, Document document){
	//	Example XML structure for port
	//	
	//		<port name="">
	//			<annotation>
	//		      <text></text>
	//		      <location>
	//		        <x></x>
	//		        <y></y>
	//		      </location>
	//		    </annotation>
	//		    <arcName></arcName>
	//		    <componentName></componentName>
	//		    <locationOnComponent></locationOnComponent>
	//		    <portType></portType>
	//			<placeInterface></placeInterface>
	//	    </port>
		
		for(Port p: model.getPorts().values()){
			
			Element port = document.createElement("Port");
			Element arcName = document.createElement("arcName");
			Element componentName = document.createElement("componentName");
			Element locationOnComponent = document.createElement("locationOnComponent");
			Element portType = document.createElement("portType");
			Element annotation = document.createElement("annotation");
			Element placeInterface = document.createElement("placeInterface");
			
			port.setAttribute("name", p.getName());
			
			root.appendChild(port);
			port.appendChild(arcName);
			port.appendChild(componentName);
			port.appendChild(locationOnComponent);
			port.appendChild(portType);
			port.appendChild(annotation);
			port.appendChild(placeInterface);
			
			saveAnnotation(model, annotation, document, p);
			
			arcName.appendChild(document.createTextNode(p.getArcName()));
			componentName.appendChild(document.createTextNode(p.getComponentName()));
			locationOnComponent.appendChild(document.createTextNode(String.valueOf(p.getLocationOnComponent())));
			portType.appendChild(document.createTextNode(String.valueOf(p.getPortType())));
			placeInterface.appendChild(document.createTextNode(p.getPlaceInterfaceName()));
		}
	}

	private void saveArcs(SamModel model, Element root, Document document){
	//	Example XML structure for arc
	//
	//		<arc name="">
	//			<annotation>
	//		      <text></text>
	//		      <location>
	//		        <x></x>
	//		        <y></y>
	//		      </location>
	//		    </annotation>
	//		    <locationOnConnector></locationOnConnector>
	//          <arcVar></arcVar>
	//		    <startName></startName>
	//		    <endName></endName>
	//		    <points>
	//		      <point index="">
	//		        <x></x>
	//		        <y></y>
	//		      </point>
	//		    </points>
	//	    </arc>
		
		for(Arc a: model.getArcs().values()){
			
			Element arc = document.createElement("Arc");
			Element locationOnConnector = document.createElement("locationOnConnector");
			Element arcVar = document.createElement("arcVar");
			Element startName = document.createElement("startName");
			Element endName = document.createElement("endName");
			Element points = document.createElement("points");
			Element annotation = document.createElement("annotation");
			
			arc.setAttribute("name", a.getName());
			
			root.appendChild(arc);
			arc.appendChild(locationOnConnector);
			arc.appendChild(arcVar);
			arc.appendChild(startName);
			arc.appendChild(endName);
			arc.appendChild(points);
			arc.appendChild(annotation);
			
			saveAnnotation(model, annotation, document, a);

			locationOnConnector.appendChild(document.createTextNode(String.valueOf(String.valueOf(a.getLocationOnConnector()))));
			arcVar.appendChild(document.createTextNode(a.getVar()));
			startName.appendChild(document.createTextNode(a.getStartName()));
			endName.appendChild(document.createTextNode(a.getEndName()));
			
			Integer i = 0;
			for(Point p: a.getPoints()){
				Element point = document.createElement("point");
				Element x = document.createElement("x");
				Element y = document.createElement("y");
				x.appendChild(document.createTextNode(String.valueOf(p.x)));
				y.appendChild(document.createTextNode(String.valueOf(p.y)));
				
				point.setAttribute("index", i.toString());
				point.appendChild(x);
				point.appendChild(y);
				points.appendChild(point);
				
				i++;
			}
			
		}
		
		
		
	}
	
	private void saveAnnotation(SamModel model, Element annotation, Document document, SamModelObject obj){
		Element text = document.createElement("text");
		Element location = document.createElement("location");
		
		annotation.appendChild(text);
		annotation.appendChild(location);
		
		Element x = document.createElement("x");
		Element y = document.createElement("y");
		
		location.appendChild(x);
		location.appendChild(y);
		
		text.appendChild(document.createTextNode(obj.getLabel().getText()));
		
		x.appendChild(document.createTextNode(String.valueOf(obj.getLabel().getX())));
		y.appendChild(document.createTextNode(String.valueOf(obj.getLabel().getY())));
		
		
		
		
		
	}
	
//	public boolean openFile1(SamModel model, String path) throws Throwable{
//		try {
//			File file = new File(path);
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			Document document;
//			try {
//				document = db.parse(file);
//			
//				document.getDocumentElement().normalize();
//				NodeList rootList = document.getElementsByTagName("model");
//				
//				Node rootNode = rootList.item(0);
//				
//				Element root = (Element) rootNode;
//				NodeList components = root.getElementsByTagName("Component");
//				loadComponents(components, model);
//				
//				NodeList connectors = root.getElementsByTagName("Connector");
//				loadConnectors(connectors, model);
//				
//				NodeList ports = root.getElementsByTagName("Port");
//				loadPorts(ports, model);
//				
//				NodeList arcs = root.getElementsByTagName("Arc");
//				loadArcs(arcs, model);
//				
//				return true;
//				
//			}
//			catch (FileNotFoundException e) {
//				JOptionPane.showMessageDialog(null, "Error reading from file");
//				return false;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//
//
//	}
	
	public boolean openFile(SamModel model, String path) throws Throwable{
		Element element;
		
		try {
			File file = new File(path);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document;
			try {
				document = db.parse(file);
			
				document.getDocumentElement().normalize();
				NodeList rootList = document.getElementsByTagName("model");
				
				Node rootNode = rootList.item(0);
				
				Element root = (Element) rootNode;
				
				NodeList nodeList = root.getChildNodes();
				Node node = null;
				for(int i = 0 ; i < nodeList.getLength() ; i++){
					node = nodeList.item(i);
					
					if(node instanceof Element){
						element = (Element)node;
						if("Component".equals(element.getNodeName())){
							loadComponent(model, element);
						}else if("Connector".equals(element.getNodeName())){
							loadConnector(model, element);
						}else if("Port".equals(element.getNodeName())){
							loadPort(model, element);
						}else if("Arc".equals(element.getNodeName())){
							loadArc(model, element);
						}
					}
				}
				
				return true;
				
			}
			catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(null, "Error reading from file");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}


	}	
	
	private void loadComponent(SamModel model, Element compElem) throws Throwable{
		Component compObj = new Component();
		compObj.setParentModel(model);
		compObj.setName(compElem.getAttribute("name"));
		NodeList nodeList = compElem.getChildNodes();
		Node node;
		Element element;
		
		for(int i = 0 ; i < nodeList.getLength() ; i++){
			node = nodeList.item(i);
			
			if(node instanceof Element){
				element = (Element)node;
				if("annotation".equals(element.getNodeName())){
					loadAnnotation(compObj, element);
				}else if("bounds".equals(element.getNodeName())){
					loadBounds(compObj, element);
				}else if("formula".equals(element.getNodeName())){
					String formula = element.getTextContent();
					if(formula != null){
						compObj.setFormula(formula);
					}
				}else if("ports".equals(element.getNodeName())){					
					NodeList portList = element.getChildNodes();
					String[] ports = new String[Component.MAX_PORTS];
					int k = portList.getLength();
					for(int j=0;j<portList.getLength();j++){
						Node n = portList.item(j);
						if(n instanceof Element){
							Element pElement = (Element)n;
							String portName = pElement.getTextContent();
							String indexS = pElement.getAttribute("index");
							if(indexS != null && portName != null){
								int index = Integer.parseInt(indexS);
								ports[index] = portName;						
							}
						}
					}
					compObj.setPorts(ports);
				}else if("subComposition".equals(element.getNodeName())){
					loadSubComposition(compObj, element);
				}else if("elementaryNet".equals(element.getNodeName())){
					loadElementaryNet(compObj, element);
				}
			}
		}
		model.add(compObj);	
	}

	private void loadConnector(SamModel model, Element connElem){
		Connector connObj = new Connector();
		connObj.setParentModel(model);
		connObj.setName(connElem.getAttribute("name"));
		NodeList nodeList = connElem.getChildNodes();
		Node node;
		Element element;
		
		for(int i = 0 ; i < nodeList.getLength() ; i++){
			node = nodeList.item(i);
			
			if(node instanceof Element){
				element = (Element)node;
				if("annotation".equals(element.getNodeName())){
					loadAnnotation(connObj, element);
				}else if("bounds".equals(element.getNodeName())){
					loadBounds(connObj, element);
				}else if("formula".equals(element.getNodeName())){
					String formula = element.getTextContent();
					if(formula != null){
						connObj.setFormula(formula);
					}
				}else if("netCompositionFormula".equals(element.getNodeName())){
					String netCompositionFormula = element.getTextContent();
					if(netCompositionFormula != null){
						connObj.setNetCompositionFormula(netCompositionFormula);
					}
				}else if("arcs".equals(element.getNodeName())){					
					NodeList arcList = element.getChildNodes();
					
				}
			}
		}
		model.add(connObj);	
	}

	
	private void loadPort(SamModel model, Element portElem){
		Node node;
		Element element;
		
		Port portObj = new Port();
		portObj.setParentModel(model);
		portObj.setName(portElem.getAttribute("name"));
		NodeList portList = portElem.getChildNodes();
		
		for(int j=0; j < portList.getLength(); j++){
			node = portList.item(j);
			if(node instanceof Element){
				element = (Element)node;
				if("annotation".equals(element.getNodeName())){
					loadAnnotation(portObj, element);
				}else if("arcName".equals(element.getNodeName())){
					String an = element.getTextContent();
					if(an != null){
						portObj.setArcName(an);
					}
				}else if("componentName".equals(element.getNodeName())){
					 String temp = element.getTextContent();
					 if(temp != null){
							portObj.setComponentName(temp);
						}
				}else if("locationOnComponent".equals(element.getNodeName())){
					 String temp1 = element.getTextContent();
					 if(temp1 != null){
							portObj.setLocationOnComponent(Integer.parseInt(temp1));
						}
				}else if("portType".equals(element.getNodeName())){
					 String temp2 = element.getTextContent();
					 if(temp2 != null){
							portObj.setPortType(Integer.parseInt(temp2));
						}
				}else if("placeInterface".equals(element.getNodeName())){
					String temp3 = element.getTextContent();
					if(temp3 != null){
						portObj.setPlaceInterface(temp3);
					}
				}
			}
		}
		
		model.add(portObj);
	}
	
//	private void loadPort(Component c, NodeList ports){
//		Node node;
//		Element element;
//		
//		
//		for (int i = 0; i < ports.getLength(); i++){
//			Node portNode = ports.item(i);
//			if(portNode != null && portNode.getNodeType() == Node.ELEMENT_NODE){
//				Port portObj = new Port();
//				portObj.setParentModel(c.getParentModel());
//				Element portElem = (Element) portNode;
//				portObj.setName(portElem.getAttribute("name"));
//				NodeList portList = portElem.getChildNodes();
//				for(int j=0; j < portList.getLength(); j++){
//					node = portList.item(j);
//					if(node instanceof Element){
//						element = (Element)node;
//						if("annotation".equals(element.getNodeName())){
//							loadAnnotation(portObj, element);
//						}else if("arcName".equals(element.getNodeName())){
//							
//						}else if("componentName".equals(element.getNodeName())){
//							 String temp = element.getTextContent();
//							 if(temp != null){
//									portObj.setComponentName(temp);
//								}
//						}else if("locationOnComponent".equals(element.getNodeName())){
//							 String temp1 = element.getTextContent();
//							 if(temp1 != null){
//									portObj.setLocationOnComponent(Integer.parseInt(temp1));
//								}
//						}else if("portType".equals(element.getNodeName())){
//							 String temp2 = element.getTextContent();
//							 if(temp2 != null){
//									portObj.setPortType(Integer.parseInt(temp2));
//								}
//						}
//					}
//				}
//				
//				model.add(portObj);
//			}
//		}
//	}
	
	private void loadArc(SamModel model, Element arcElem){
		Node node;
		Element element;
		String xS = null;
		String yS = null;
		
		Arc arcObj = new Arc(model, arcElem.getAttribute("name"));
		NodeList arcList = arcElem.getChildNodes();
		
		for(int j=0; j < arcList.getLength(); j++){
			node = arcList.item(j);
			if(node instanceof Element){
				element = (Element)node;
				if("annotation".equals(element.getNodeName())){
					loadAnnotation(arcObj, element);
				}else if("locationOnConnector".equals(element.getNodeName())){
					 String temp = element.getTextContent();
					 if(temp != null){
						 arcObj.setLocationOnConnector(Integer.parseInt(temp));
						}
				}else if("arcVar".equals(element.getNodeName())){
					 String temp = element.getTextContent();
					 if(temp != null){
						 arcObj.setVar(temp);
						}
				}else if("startName".equals(element.getNodeName())){
					 String temp1 = element.getTextContent();
					 if(temp1 != null){
						 arcObj.setStart(temp1);
						}
				}else if("endName".equals(element.getNodeName())){
					 String temp2 = element.getTextContent();
					 if(temp2 != null){
						 arcObj.setEnd(temp2);
						}
				}else if("points".equals(element.getNodeName())){
					NodeList points = element.getChildNodes();
					for(int k=0;k<points.getLength();k++){
						Node pointNode = points.item(k);
						if(pointNode != null && pointNode.getNodeType() == Node.ELEMENT_NODE){
							Element point = (Element) pointNode;
							NodeList pointList = point.getChildNodes();
							for(int g=0; g<pointList.getLength();g++){
								if("x".equals(element.getNodeName())){
									 xS = element.getTextContent();
								}else if("y".equals(element.getNodeName())){
									 yS = element.getTextContent();
								}
							}
							if(xS != null & yS != null){
								int x = Integer.parseInt(xS);
								int y = Integer.parseInt(yS);
								arcObj.addPoint(x, y);
							}
						}
					}
				}
			}
		}
		model.add(arcObj);
	}
	
//	private void loadArc(SamModel model, NodeList arcs){
//		Node node;
//		Element element;
//		String xS = null;
//		String yS = null;
//		
//		for (int i = 0; i < arcs.getLength(); i++){
//			Node arcNode = arcs.item(i);
//			if(arcNode != null && arcNode.getNodeType() == Node.ELEMENT_NODE){
//				Element arcElem = (Element) arcNode;
//				Arc arcObj = new Arc(model, arcElem.getAttribute("name"));
//				NodeList arcList = arcElem.getChildNodes();
//				for(int j=0; j < arcList.getLength(); j++){
//					node = arcList.item(j);
//					if(node instanceof Element){
//						element = (Element)node;
//						if("annotation".equals(element.getNodeName())){
//							loadAnnotation(arcObj, element);
//						}else if("locationOnConnector".equals(element.getNodeName())){
//							 String temp = element.getTextContent();
//							 if(temp != null){
//								 arcObj.setLocationOnConnector(Integer.parseInt(temp));
//								}
//						}else if("startName".equals(element.getNodeName())){
//							 String temp1 = element.getTextContent();
//							 if(temp1 != null){
//								 arcObj.setStart(temp1);
//								}
//						}else if("endName".equals(element.getNodeName())){
//							 String temp2 = element.getTextContent();
//							 if(temp2 != null){
//								 arcObj.setEnd(temp2);
//								}
//						}else if("points".equals(element.getNodeName())){
//							NodeList points = element.getChildNodes();
//							for(int k=0;k<points.getLength();k++){
//								Node pointNode = points.item(k);
//								if(pointNode != null && pointNode.getNodeType() == Node.ELEMENT_NODE){
//									Element point = (Element) pointNode;
//									NodeList pointList = point.getChildNodes();
//									for(int g=0; g<pointList.getLength();g++){
//										if("x".equals(element.getNodeName())){
//											 xS = element.getTextContent();
//										}else if("y".equals(element.getNodeName())){
//											 yS = element.getTextContent();
//										}
//									}
//									if(xS != null & yS != null){
//										int x = Integer.parseInt(xS);
//										int y = Integer.parseInt(yS);
//										arcObj.addPoint(x, y);
//									}
//								}
//							}
//						}
//					}
//				}
//				model.add(arcObj);
//			}
//		}
//	}
	
	private void loadAnnotation(SamModelObject obj, Element annotElem){
		Node node;
		Element element;
		NodeList annotList = annotElem.getChildNodes();
		for(int i = 0 ; i < annotList.getLength() ; i++){
			node = annotList.item(i);
			if(node instanceof Element){
				element = (Element)node;
				if("text".equals(element.getNodeName())){
					obj.setLabelText(element.getTextContent());
				}else if("location".equals(element.getNodeName())){
					Node xNode = element.getFirstChild();
					Node yNode = element.getLastChild();
					if(xNode != null && yNode != null){
						Element xE = (Element) xNode;
						Element yE = (Element) yNode;
						
						int x = Integer.parseInt(xE.getTextContent());
						int y = Integer.parseInt(yE.getTextContent());
						
						obj.getLabel().setLocation(x, y);
					}
				}
			}
		}
		
	}
	
	private void loadBounds(SamModelObject obj, Element boundsElem){
		Node node;
		Element element;
		String xS = null;
		String yS = null;
		String wS = null;
		String hS = null;
		int x = 0;
		int y = 0;
		int width;
		int height;
		
		NodeList boundsList = boundsElem.getChildNodes();
		for(int i=0; i<boundsList.getLength();i++){
			node = boundsList.item(i);
			if(node instanceof Element){
				element = (Element)node;
				if("x".equals(element.getNodeName())){
					xS = element.getTextContent();
				}else if("y".equals(element.getNodeName())){
					yS = element.getTextContent();
				}else if("width".equals(element.getNodeName())){
					wS = element.getTextContent();
				}else if("height".equals(element.getNodeName())){
					hS = element.getTextContent();
				}
			}
		}
		
		if(xS == null || yS == null || wS == null || hS == null){
			System.out.println("Load BOunds Not Complete");
		}else{
			x = Integer.parseInt(xS);
			y = Integer.parseInt(yS);
			width = Integer.parseInt(wS);
			height = Integer.parseInt(hS);
		}
		
		obj.setLocation(x, y);
		
		
	}
	
	private void loadSubComposition(Component c, Element e) throws Throwable{
		NodeList nodeList = e.getChildNodes();
		if(nodeList.getLength()==0){
			return;
		}
		c.subCompositionModel = new SamModel();
		c.isSetSubComposition = true;
		Node node = null;
		Element element;
		for(int i = 0; i<nodeList.getLength();i++){
			node = nodeList.item(i);
			if(node instanceof Element){
				element = (Element)node;
				if("Component".equals(element.getNodeName())){
					loadComponent(c.getSubComposiionModel(), element);
				}else if("Connector".equals(element.getNodeName())){
					loadConnector(c.getSubComposiionModel(), element);
				}else if("Port".equals(element.getNodeName())){
					loadPort(c.getSubComposiionModel(), element);
				}else if("Arc".equals(element.getNodeName())){
					loadArc(c.getSubComposiionModel(), element);
				}
			}
		}
	}
	
	private void loadElementaryNet(Component c, Element e) throws Throwable{
		NodeList nl;
		if(e.hasChildNodes()){
			nl = e.getChildNodes();
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		    Document elem_doc = documentBuilder.newDocument();
		    Node root = nl.item(0);
		    elem_doc.appendChild(elem_doc.importNode(root, true));
		    
			c.setElemSpec(elem_doc); //set component elementary net document
			c.setElemNetModel(c.elemPNDomToModel(elem_doc)); //set component

		}
	}
	
	
	
//	private void loadComponents(NodeList components, SamModel model) throws Throwable{
//	//	Example XML structure for component
//	//		
//	//		<component name="">
//	//			<annotation>
//	//		      <text></text>
//	//		      <location>
//	//		        <x></x>
//	//		        <y></y>
//	//		      </location>
//	//		    </annotation>
//	//		    <bounds>
//	//		      <x></x>
//	//		      <y></y>
//	//		      <width></width>
//	//		      <height></height>
//	//		    </bounds>
//	//		    <formula></formula>
//	//		    <ports>
//	//		      <portName index="0">name1</portName>
//	//		      <portName index="1">name2</portName>
//	//		    </ports>
//	//	    	<subComposition></subComposition>
//	//	    	<elementaryNet></elementaryNet>
//	//	    </component>
//		for (int i = 0; i < components.getLength(); i++) {
//			Node compNode = components.item(i);
//			
//			if(compNode != null && compNode.getNodeType() == Node.ELEMENT_NODE){
//				Component compObj = new Component();
//				compObj.setParentModel(model);
//				Element compElem = (Element) compNode;
//				
//				compObj.setName(compElem.getAttribute("name"));
//				loadAnnotation(compObj, compElem);
//				loadBounds(compObj, compElem);
//				
//				String formula = getElementText(compElem, "formula");
//				
//				if(formula != null){
//					compObj.setFormula(formula);
//				}
//				
//				NodeList nl = compElem.getElementsByTagName("elementaryNet");
//				
//				if(nl.item(0).hasChildNodes()){
//					DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//				    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//				    Document elem_doc = documentBuilder.newDocument();
//				    Node root = nl.item(0).getChildNodes().item(0).getChildNodes().item(0);
//				    
//				    elem_doc.appendChild(elem_doc.importNode(root, true));
//				    System.out.println(elem_doc.getChildNodes().item(0).getChildNodes().getLength());
//				    System.out.println(elem_doc.getDocumentElement().getNodeName());
//				    
//					compObj.setElemSpec(elem_doc);
//				}
//				
//				
//				
//				NodeList portList = compElem.getElementsByTagName("ports");
//				String[] ports = new String[Component.MAX_PORTS];
//				
//				for(int j = 0; j < portList.getLength(); j++){
//					String portName = getElementText(compElem, "portName", j);
//					String indexS = getAttribute(compElem, "portName", "index", j);
//					
//					if(indexS != null && portName != null){
//						int index = Integer.parseInt(indexS);
//						ports[index] = portName;						
//					}
//				}
//				
//				
//				compObj.setPorts(ports);
//				model.add(compObj);
//						
//			}
//		}
//	}
//	
//	private void loadConnectors(NodeList connectors, SamModel model){
//	//	Example XML structure for connector
//	//	
//	//		<connector name="">
//	//			<annotation>
//	//		      <text></text>
//	//		      <location>
//	//		        <x></x>
//	//		        <y></y>
//	//		      </location>
//	//		    </annotation>
//	//		    <bounds>
//	//		      <x></x>
//	//		      <y></y>
//	//		      <width></width>
//	//		      <height></height>
//	//		    </bounds>
//	//		    <arcs>
//	//		      <arcName index ="0">name1</arcName>
//	//		      <arcName index ="1">name2</arcName>
//	//		    </arcs>
//	//	  </connector>
//		for (int i = 0; i < connectors.getLength(); i++) {
//			Node connNode = connectors.item(i);
//			
//			if(connNode != null && connNode.getNodeType() == Node.ELEMENT_NODE){
//				Connector connObj = new Connector();
//				connObj.setParentModel(model);
//				Element connElem = (Element) connNode;
//				
//				connObj.setName(connElem.getAttribute("name"));
//				
//				String formula = getElementText(connElem, "formula");
//				
//				if(formula != null){
//					connObj.setFormula(formula);
//				}
//				loadAnnotation(connObj, connElem);
//				loadBounds(connObj, connElem);
//				
//				model.add(connObj);
//			}
//		}
//		
//	}
//	
//	private void loadPorts(NodeList ports, SamModel model){
//	//	Example XML structure for port
//	//	
//	//		<port name="">
//	//			<annotation>
//	//		      <text></text>
//	//		      <location>
//	//		        <x></x>
//	//		        <y></y>
//	//		      </location>
//	//		    </annotation>
//	//		    <arcName></arcName>
//	//		    <componentName></componentName>
//	//		    <locationOnComponent></locationOnComponent>
//	//		    <portType></portType>
//	//			<placeInterface></placeInterface>
//	//	    </port>
//		
//		for (int i = 0; i < ports.getLength(); i++) {
//			Node portNode = ports.item(i);
//			
//			if(portNode != null && portNode.getNodeType() == Node.ELEMENT_NODE){
//				Port portObj = new Port();
//				portObj.setParentModel(model);
//				Element portElem = (Element) portNode;
//				
//				portObj.setName(portElem.getAttribute("name"));
//				loadAnnotation(portObj, portElem);
//				
//				String temp = getElementText(portElem, "componentName");
//				
//				if(temp != null){
//					portObj.setComponentName(temp);
//				}
//				
//				temp = getElementText(portElem, "locationOnComponent");
//				
//				if(temp != null){
//					int locationOnComponent = Integer.parseInt(temp);
//					portObj.setLocationOnComponent(locationOnComponent);
//					
//				}
//				
//				temp = getElementText(portElem, "portType");
//				
//				if(temp != null){
//					int portType = Integer.parseInt(temp);
//					portObj.setPortType(portType);
//				}
//				
//				temp = getElementText(portElem, "placeInterface");
//				
//				if(temp != null){
//					portObj.setPlaceInterface(temp);
//				}
//
//				model.add(portObj);
//	
//			}
//		}
//		
//	}
//	
//	private void loadArcs(NodeList arcs, SamModel model){
//	//	Example XML structure for arc
//	//
//	//		<arc name="">
//	//			<annotation>
//	//		      <text></text>
//	//		      <location>
//	//		        <x></x>
//	//		        <y></y>
//	//		      </location>
//	//		    </annotation>
//	//		    <locationOnConnector></locationOnConnector>
//	//		    <startName></startName>
//	//		    <endName></endName>
//	//		    <points>
//	//		      <point index="">
//	//		        <x></x>
//	//		        <y></y>
//	//		      </point>
//	//		    </points>
//	//	    </arc>
//		
//		for (int i = 0; i < arcs.getLength(); i++) {
//			Node arcNode = arcs.item(i);
//			
//			if(arcNode != null && arcNode.getNodeType() == Node.ELEMENT_NODE){
//				Element arcElem = (Element) arcNode;
//				Arc arcObj = new Arc(model, arcElem.getAttribute("name"));
//				loadAnnotation(arcObj, arcElem);
//				
//				String temp = getElementText(arcElem, "locationOnConnector");
//				
//				if(temp != null){
//					int locationOnConnector = Integer.parseInt(temp);
//					arcObj.setLocationOnConnector(locationOnConnector);
//					
//				}
//				
//				temp = getElementText(arcElem, "startName");
//				
//				if(temp != null){
//					arcObj.setStart(temp);
//				}
//				
//				temp = getElementText(arcElem, "endName");
//				
//				if(temp != null){
//
//					arcObj.setEnd(temp);
//				}
//				
//				NodeList pointList = arcElem.getElementsByTagName("points");
//				
//				for(int j = 0; j < pointList.getLength(); j++){
//					Node pointNode = pointList.item(j);
//					
//					if(pointNode != null && pointNode.getNodeType() == Node.ELEMENT_NODE)
//					{
//						Element point = (Element) pointNode;
//						
//						String xS = getElementText(point, "x");
//						String yS = getElementText(point, "y");
//						
//						if(xS != null & yS != null){
//							int x = Integer.parseInt(xS);
//							int y = Integer.parseInt(yS);
//							arcObj.addPoint(x, y);
//						}
//					}
//				}
//
//				model.add(arcObj);
//			}
//		}
//		
//	}
//	
//	private void loadAnnotation(SamModelObject obj, Element component){
//		NodeList annotList = component.getElementsByTagName("annotation");
//		Node annotNode = annotList.item(0);
//		
//		if(annotNode != null && annotNode.getNodeType() == Node.ELEMENT_NODE){
//			Element annotation = (Element) annotNode;
//			Node textNode = annotation.getFirstChild();
//			
//			if(textNode != null && textNode.getNodeType() == Node.ELEMENT_NODE){
//				Element text = (Element) textNode;
//				obj.setLabelText(text.getTextContent());
//			}
//			
//			Node locationNode = annotation.getLastChild();
//			
//			if(locationNode != null && locationNode.getNodeType() == Node.ELEMENT_NODE){
//				Element location = (Element) locationNode;
//				
//				Node xNode = location.getFirstChild();
//				Node yNode = location.getLastChild();
//				
//				if(xNode != null && xNode.getNodeType() == Node.ELEMENT_NODE){
//					
//					if(yNode != null && yNode.getNodeType() == Node.ELEMENT_NODE){
//						Element xE = (Element) xNode;
//						Element yE = (Element) yNode;
//						
//						int x = Integer.parseInt(xE.getTextContent());
//						int y = Integer.parseInt(yE.getTextContent());
//						
//						obj.getLabel().setLocation(x, y);
//						
//					}
//				}
//			}
//			
//		}
//	}
		
//	private void loadBounds(SamModelObject obj, Element SamModelObject){
//		NodeList boundsList = SamModelObject.getElementsByTagName("bounds");
//		Node boundNode = boundsList.item(0);
//		
//		if(boundNode != null && boundNode.getNodeType() == Node.ELEMENT_NODE){
//			Element bounds = (Element) boundNode;
//			
//			String xS = getElementText(bounds, "x");
//			String yS = getElementText(bounds, "y");
//			
//			if(xS != null & yS != null){
//				int x = Integer.parseInt(xS);
//				int y = Integer.parseInt(yS);
//				
//				obj.setLocation(x, y);
//			}
//			
//		}
//		
//	}
//	
//	private String getElementText(Element parent, String elementToGet){
//		return getElementText(parent, elementToGet, 0);
//	}
//	
//	private String getElementText(Element parent, String elementToGet, int index){
//		String result = null;
//		
//		NodeList nodeList = parent.getElementsByTagName(elementToGet);
//		
//		if(nodeList.getLength() > 0){
//			Node item = nodeList.item(index);
//			
//			if(item != null && item.getNodeType() == Node.ELEMENT_NODE){
//				Element elem = (Element) item;
//				result = elem.getTextContent();
//			}
//		}
//		
//		return result;
//	}
//	
//	private String getAttribute(Element parent, String elementToGetAttFrom, String attributeName){
//		return getAttribute(parent, elementToGetAttFrom, attributeName, 0);
//	}
//	
//	private String getAttribute(Element parent, String elementToGetAttFrom, String attributeName, int index){
//		String result = null;
//		
//		NodeList nodeList = parent.getElementsByTagName(elementToGetAttFrom);
//		
//		if(nodeList.getLength() > 0){
//			Node item = nodeList.item(index);
//			
//			if(item != null && item.getNodeType() == Node.ELEMENT_NODE){
//				Element elem = (Element) item;
//				result = elem.getAttribute(attributeName);
//			}
//		}
//		
//		return result;
//	}
//	
	
	
	
}
