package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import pipe.dataLayer.BasicType;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.dataLayer.Token;
import pipe.dataLayer.abToken;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.PetriNetChooserPanel;
import pipe.gui.widgets.ResultsTxtPane;
import pipe.gui.widgets.EscapableDialog;
//import sam_model.SamModel;
//import sam_model.analysis.ElementaryNetComposition;
import analysis.myPromela;
import pipe.gui.GuiFrame;
import pipe.gui.CreateGui;

public class SimulationModuleHandler extends AbstractAction{
	JButton btn;
	private static final String MODULE_NAME = "Simulate up to a specified number of transition fire!";
	private ResultsTxtPane results;
	private JTextField fireBoundText;
	private JLabel fireStepNumberSpecLabel;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton){
			btn = (JButton) e.getSource();
			if(btn.getName().compareTo("btnModelToPromela") == 0){
				simulationReportWindow();
			}
		}
		
	}
	public void simulationReportWindow() {
		 // Build interface
		EscapableDialog guiDialog = 
	              new EscapableDialog(CreateGui.appGui, MODULE_NAME, true);
		   // 1 Set layout
	      Container contentPane = guiDialog.getContentPane();
	      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));

	      //adfadf
	      System.out.print("");
	      // 3 Add results pane
	      contentPane.add(results = new ResultsTxtPane(null)); //SUTODO: the null parameter in ResultsTxtPane to be reconsider.
	      
	      //add  formula textbox
	      contentPane.add(fireStepNumberSpecLabel = new JLabel("Specify an Upper bound of transition fires:"));
	      contentPane.add(fireBoundText = new JTextField());
	      
	      // 4 Add button
	      contentPane.add(new ButtonBar("SimulateAll", simulateAllClick,
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
	   ActionListener simulateAllClick = new ActionListener() {
	      
	      public void actionPerformed(ActionEvent e) {
	    	  int upperBound = Integer.parseInt(fireBoundText.getText());
	    	  String simResults = "";
	    	  int stateCount = 0;
	    	  long startTime = System.currentTimeMillis();
	    	  simResults += "state"+ stateCount+ ":" + getStateSnapshot() + "\n\n";
	    	  while(CreateGui.getAnimator().doHighLevelRandomFiring() && stateCount < upperBound) {
	    		  simResults += "Trans: " + CreateGui.getAnimationHistory().getLastFiredTransitionName() + "\n\n";
	    		  stateCount++;
	    		  simResults += "state"+ stateCount+ ":" + getStateSnapshot() + "\n\n";
	    	  }
	    	  long endTime   = System.currentTimeMillis();
	    	  long totalTime = endTime - startTime;
			  simResults += "Total Time Consumed: "+ totalTime/1000.0+" seconds.";
			  results.setEnabled(true);
	    	  results.setText(simResults);
	      };
	   
	   public String getStateSnapshot() {
		   String state = "";
		   DataLayer data = CreateGui.currentPNMLData();
		   Place[] places = data.getPlaces();
		   for(int i=0;i<places.length;i++) {
			   Place p = places[i];
			   state = state+p.getName()+"{";
			   abToken abTok = p.getToken();
			   for(Token tok:abTok.listToken) {
				   state += "[";
				   for(int j=0;j<tok.Tlist.size();j++) {
					   BasicType bt = tok.Tlist.elementAt(j);
					   String val = "";
					   if(bt.kind==0)val = Integer.toString(bt.Tint);
					   else if(bt.kind==1) val = bt.Tstring;
					   state += val;
					   if(j<tok.Tlist.size()-1)
						   state += ",";
				   }
				   state += "] ";
			   }
			   state += "}";
		   }
		   
		   return state;
	   }
	   };
}

