package pipe.gui.handler;

import hlpn2smt.HLPNModelToZ3Converter;
import hlpn2smt.Property;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import pipe.dataLayer.BasicType;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.DataType;
import pipe.dataLayer.Place;
import pipe.dataLayer.Token;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.ResultsTxtPane;
import analysis.myPromela;

public class BoundedModelCheckingModule extends AbstractAction {
	
	JButton btn;
	private static final String MODULE_NAME = "Bounded Model Checking";
	private EscapableDialog guiDialog;
	private JPanel leftPanel;
	private JPanel rightPanel;
	
	private ResultsTxtPane results;
	private JTextField steptext;
	private JLabel PreDefineSteps;
	//property definition
	private JLabel PropertySpec;
	private JLabel PropertyPlace;
	private JTextField PropertyPlaceText;
	private JLabel PropertyToken;
	private JTextField PropertyTokenText;
	private JLabel PropertyRelationType;
	private JComboBox relationTypeComboBox;
	private String relationTypeString = "CONJUNCTION";
	private JLabel PropertyOperator;
	private JComboBox operatorComboBox;
	private String operatorTypeString = "EQ";
	
//	private JButton addPropertyButton;//TODO:
	private JList propertyList;//TODO:
	private DefaultListModel<String> propertyListStrings;
	
	ArrayList<Property> propertyBuilderList = new ArrayList<Property>();
	DataLayer sourceDataLayer = CreateGui.getModel();
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton){
			btn = (JButton) e.getSource();
			if(btn.getName().compareTo("btnModelToPromela") == 0){
				boundedModelCheckingWindow();
			}
		}
		
	}
	
	public void buildLeftPanel(){
		leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));	
	    //Add results textField
		leftPanel.add(new JLabel("Checking Result"));
		results = new ResultsTxtPane("Checking Results Shown Here: ");
		JScrollPane scrollPane = new JScrollPane(results);
		leftPanel.add(scrollPane); //SUTODO: the null parameter in ResultsTxtPane to be reconsider.
	}
	
	public void buildRightPanel(){
		rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));	
		// add property
		rightPanel.add(PropertySpec = new JLabel("---Property SPEC---\n"));
		rightPanel.add(PropertyPlace = new JLabel("Property Place: "));
		rightPanel.add(this.PropertyPlaceText = new JTextField());
		rightPanel.add(PropertyToken = new JLabel("Property Token: "));
		rightPanel.add(this.PropertyTokenText = new JTextField());
		rightPanel.add(PropertyRelationType = new JLabel("Property RelationType: "));
	    String[] relationTypeStrings = { "CONJUNCTION", "DISJUNCTION" };
	      relationTypeComboBox = new JComboBox(relationTypeStrings);
	      relationTypeComboBox.setSelectedIndex(0);
	      //listen to combobox relation type
	      relationTypeComboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				relationTypeString = (String)cb.getSelectedItem();
			}
	      });
	      rightPanel.add(relationTypeComboBox);

	      rightPanel.add(PropertyOperator = new JLabel("Property Operator: "));
	      String[] operatorStrings = { "EQ", "NEQ" };
	      operatorComboBox = new JComboBox(operatorStrings);
	      operatorComboBox.setSelectedIndex(0);
	      //listen to combobox operator type
	      operatorComboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox opCB = (JComboBox)e.getSource();
				operatorTypeString = (String)opCB.getSelectedItem();
			}
	      });
	      rightPanel.add(operatorComboBox);
	      
	      this.propertyListStrings = new DefaultListModel<String>();//init as null, but need to init with previous defined property
	      this.propertyList = new JList(propertyListStrings);
	      
	      propertyList.setBackground(Color.WHITE);
	      propertyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
	      JScrollPane scrollPropertyListPane = new JScrollPane();
	      scrollPropertyListPane.getViewport().add(propertyList);
	      rightPanel.add(new JLabel("Defined Properties"));
	      rightPanel.add(scrollPropertyListPane);
	      
	      rightPanel.add(new ButtonBar("Add To Property", new ActionListener(){
	    	  public void actionPerformed(ActionEvent e){
	    		  //add the property name to JList
	    		  propertyListStrings.addElement(PropertyPlaceText.getText());
	    		  //add the property to propertyList
	    		  buildPropertyFromGUI();
	    	  }
	      }, guiDialog.getRootPane()));
	      
	      
	      
	      //add  formula textbox
	      rightPanel.add(PreDefineSteps = new JLabel("Pre Define Checking Steps:"));
	      rightPanel.add(steptext = new JTextField(CreateGui.getModel().getPropertyFormula()));
	      
	      // 4 Add z3 check button
	      rightPanel.add(new ButtonBar("Z3 Run Check", checkButtonClick,
	              guiDialog.getRootPane()));
	}
	
	public void buildPropertyFromGUI(){
		String placeName = this.PropertyPlaceText.getText();
		Place p  = sourceDataLayer.getPlaceByName(placeName);
		DataType dt = p.getDataType();
		Token tok = new Token(dt);
		String tokenText = this.PropertyTokenText.getText();
		String tempTokenStr = tokenText.substring(1, tokenText.length()-1);
		String[] tokenStrs = tempTokenStr.split(",");
		for(int i=0;i<tokenStrs.length;i++){
			if(dt.getTypebyIndex(i)==0){
				tok.Tlist.add(new BasicType(0, Integer.parseInt(tokenStrs[i]),""));
			}else{
				tok.Tlist.add(new BasicType(1, 0, tokenStrs[i]));
			}
		}
		
		Property.RelationType reltype = Property.RelationType.CONJUNCTION;
		if(!relationTypeString.equals("CONJUNCTION"))reltype = Property.RelationType.DISJUNCTION;
		Property.Operator optype = Property.Operator.EQ;
		if(!operatorTypeString.equals("EQ"))optype = Property.Operator.NEQ;
		Property newProp = new Property(placeName, tok, reltype, optype);
		this.propertyBuilderList.add(newProp);
	}

	public void boundedModelCheckingWindow() {
		 // Build interface
		guiDialog = new EscapableDialog(CreateGui.appGui, MODULE_NAME, true);
		JPanel mainPanel = new JPanel();
		guiDialog.getContentPane().add(mainPanel);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		buildLeftPanel();
		buildRightPanel();
		
		   // 1 Set layout
//	      Container contentPane = guiDialog.getContentPane();
//		JScrollPane resultScrollPane = new JScrollPane();
//		JScrollPane controlScrollPane = new JScrollPane();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, rightPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(0.5);
		
		//Provide minimum sizes for the two components in the split pane
		Dimension minimumSize = new Dimension(100, 100);
		leftPanel.setMinimumSize(minimumSize);
		rightPanel.setMinimumSize(minimumSize);
	      
//		controlScrollPane.setLayout(new BoxLayout(controlScrollPane,BoxLayout.PAGE_AXIS));
		
		mainPanel.add(splitPane);	      	      
	      
	      // Make window fit contents' preferred size
	      guiDialog.pack();
	      
	      // Move window to the middle of the screen
	      guiDialog.setLocationRelativeTo(null);
	      mainPanel.setVisible(true);
	      guiDialog.setVisible(true);
	}
	
	  public String getName() {
	      return MODULE_NAME;
	   }
	  
	  /**
	    * Translate button click handler
	    */
	   ActionListener checkButtonClick = new ActionListener() {
	      
	      public void actionPerformed(ActionEvent e) {
	    	  BufferedWriter bufferedWriter = null;
			  BufferedReader bufferedReader = null;
			  String r = "";
	    	  
	    	  String steps = new String(steptext.getText());
	  	    if(steps.equals("")){
	  	    	steptext.setText("pre defined step must be specified before checking !");
	  	    	return;
	  	    }
	  	    results.setText("");
	    	HLPNModelToZ3Converter convert = new HLPNModelToZ3Converter(sourceDataLayer, Integer.parseInt(steps), propertyBuilderList);
	    	boolean z3CheckingResult = false; 
	    	try {
	    		String t;
	    		bufferedReader = new BufferedReader(new FileReader("z3Result.txt"));
	    		if((t=bufferedReader.readLine())!=null){
	    			r += t +"\n";
	    			if(t.equals("sat")){
	    				z3CheckingResult = true;
	    			}
	    		}
				while((t=bufferedReader.readLine())!=null){
					   r += t +"\n";
				}
				if(z3CheckingResult){
					r += "\n*******************Error Model**********************\n";
					bufferedReader = new BufferedReader(new FileReader("newOutput.txt"));
					while((t=bufferedReader.readLine())!=null){
					   r += t +"\n";
				   }  
				}
				   results.setText(r);
	    	}catch(IOException ioe){
				   ioe.printStackTrace();
			} 
	    	
	      }
	   };
	   
//	   /**
//	    * Verify button click handler
//	    */
//	   ActionListener cancelButtonClick = new ActionListener(){
//		   public void actionPerformed(ActionEvent e){
//			   this.setVisible(false);
//		   }
//	   };
	
}

