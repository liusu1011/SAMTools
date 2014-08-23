package pipe.gui.handler;

import hlpn2smt.HLPNModelToZ3Converter;
import hlpn2smt.Property;

import java.awt.Color;
import java.awt.Container;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.ResultsTxtPane;
import analysis.myPromela;

public class BoundedModelCheckingModule extends AbstractAction {
	
	JButton btn;
	private static final String MODULE_NAME = "Bounded Model Checking";
	private ResultsTxtPane results;
	private JTextField steptext;
	private JLabel PreDefineSteps;
	//property definition
	private JLabel PropertySpec;
	private JLabel PropertyName;
	private JTextField PropertyNameText;
	private JLabel PropertyToken;
	private JTextField PropertyTokenText;
	private JLabel PropertyRelationType;
	private JComboBox relationTypeComboBox;
	private String relationTypeString;
	private JLabel PropertyOperator;
	private JComboBox operatorComboBox;
	private String operatorTypeString;
	
//	private JButton addPropertyButton;//TODO:
	private JList propertyList;//TODO:
	private DefaultListModel<String> propertyListStrings;
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton){
			btn = (JButton) e.getSource();
			if(btn.getName().compareTo("btnModelToPromela") == 0){
				boundedModelCheckingWindow();
			}
		}
		
	}
	public void boundedModelCheckingWindow() {
		 // Build interface
		EscapableDialog guiDialog = 
	              new EscapableDialog(CreateGui.appGui, MODULE_NAME, true);
		   // 1 Set layout
	      Container contentPane = guiDialog.getContentPane();
	      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));

	      
	      // 3 Add results pane
	      contentPane.add(results = new ResultsTxtPane(null)); //SUTODO: the null parameter in ResultsTxtPane to be reconsider.
	      
	      // add property
	      contentPane.add(PropertySpec = new JLabel("Property Spec: "));
	      contentPane.add(PropertyName = new JLabel("Property Name: "));
	      contentPane.add(this.PropertyNameText = new JTextField());
	      contentPane.add(PropertyToken = new JLabel("Property Token: "));
	      contentPane.add(this.PropertyTokenText = new JTextField());
	      contentPane.add(PropertyRelationType = new JLabel("Property RelationType: "));
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
	      contentPane.add(relationTypeComboBox);

	      contentPane.add(PropertyOperator = new JLabel("Property Operator: "));
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
	      contentPane.add(operatorComboBox);
	      
	      contentPane.add(new ButtonBar("Add Property", new ActionListener(){
	    	  public void actionPerformed(ActionEvent e){
	    		  //add the property name to JList
	    	  }
	      },
	              guiDialog.getRootPane()));
	      
	      this.propertyListStrings = new DefaultListModel<String>();//init as null, but need to init with previous defined property
	      this.propertyList = new JList(propertyListStrings);
	      
	      propertyList.setBackground(Color.WHITE);
	      propertyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
	      JScrollPane scrollPropertyListPane = new JScrollPane();
	      scrollPropertyListPane.getViewport().add(propertyList);
	      contentPane.add(new JLabel("Defined Properties"));
	      contentPane.add(scrollPropertyListPane);
	      
	      //add  formula textbox
	      contentPane.add(PreDefineSteps = new JLabel("Pre Define Checking Steps:"));
	      contentPane.add(steptext = new JTextField(CreateGui.getModel().getPropertyFormula()));
	      
	      // 4 Add z3 check button
	      contentPane.add(new ButtonBar("Z3 Run Check", checkButtonClick,
	              guiDialog.getRootPane()));
	      
//	      contentPane.add(new ButtonBar("Cancel", cancelButtonClick, 
//	    		  guiDialog.getRootPane()));
	      	      
	      
	      // 5 Make window fit contents' preferred size
	      guiDialog.pack();
	      
	      // 6 Move window to the middle of the screen
	      guiDialog.setLocationRelativeTo(null);
	      
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
	    	  DataLayer sourceDataLayer = CreateGui.getModel();
	    	  String steps = new String(steptext.getText());
	  	    if(steps.equals("")){
	  	    	steptext.setText("pre defined step must be specified before checking !");
	  	    	return;
	  	    }
	  	    ArrayList<Property> propertyList = new ArrayList<Property>();
	    	HLPNModelToZ3Converter convert = new HLPNModelToZ3Converter(sourceDataLayer, Integer.parseInt(steps), propertyList);
	    	try {
	    		bufferedReader = new BufferedReader(new FileReader("z3output.txt"));
				   String t;
				   while((t=bufferedReader.readLine())!=null){
					   r += t +"\n";
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

