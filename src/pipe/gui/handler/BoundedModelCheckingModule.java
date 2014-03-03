package pipe.gui.handler;

import hlpn2smt.HLPNModelToZ3Converter;
import hlpn2smt.Property;

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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

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
	      
	      //add  formula textbox
	      contentPane.add(PreDefineSteps = new JLabel("Pre Define Checking Steps:"));
	      contentPane.add(steptext = new JTextField(CreateGui.getModel().getPropertyFormula()));
	      
	      // 4 Add button
	      contentPane.add(new ButtonBar("Z3 Run Check", checkButtonClick,
	              guiDialog.getRootPane()));
	      
	      contentPane.add(new ButtonBar("Cancel", cancelButtonClick, 
	    		  guiDialog.getRootPane()));
	      
	      
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
	   
	   /**
	    * Verify button click handler
	    */
	   ActionListener cancelButtonClick = new ActionListener(){
		   public void actionPerformed(ActionEvent e){
			   
		   }
	   };
	
}

