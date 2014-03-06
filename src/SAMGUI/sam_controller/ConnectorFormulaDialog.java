package SAMGUI.sam_controller;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import formulaParser.ErrorMsg;
import formulaParser.Yylex;
import formulaParser.parser;

import pipe.gui.CreateGui;
import pipe.gui.widgets.FormulaPanel;

import SAMGUI.sam_model.Connector;

public class ConnectorFormulaDialog extends JDialog{

	private Connector connector;
	private ConnectorFormulaPanel m_Panel;
	private String fomula = "";
	
	
	public ConnectorFormulaDialog(Connector _connector){
		this.connector = _connector;
		initialize();
	}


	private void initialize() {
		setTitle("Conenctor Formula Editor");
        m_Panel = new ConnectorFormulaPanel(connector);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(m_Panel);
        getContentPane().setLayout(null);
        getContentPane().add(scrollPane, java.awt.BorderLayout.NORTH);
        scrollPane.setSize(m_Panel.getSize().width + 5,
                           m_Panel.getSize().height + 5);
        scrollPane.setLocation(5, 5);
        Border lineborder = BorderFactory.createEtchedBorder(EtchedBorder.
                RAISED);
        scrollPane.setBorder(lineborder);

        JButton closeBut = new JButton("Ok");
        closeBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
            	//checkVarConsistencyPressed();
                okPressed();
            }
        });
        closeBut.setSize(100, 30);
        closeBut.setLocation((scrollPane.getWidth() + scrollPane.getX()) / 3,
                             scrollPane.getY() + scrollPane.getHeight() + 5);

        getContentPane().add(closeBut);
        this.setSize(scrollPane.getWidth() + scrollPane.getX() + 20,
                     closeBut.getY() + closeBut.getHeight() + 45);
	}
	
    private void okPressed(){
    	ErrorMsg errorMsg = null;
    	try{
    	String strtoParse = new String(m_Panel.m_textField.getText());
    	if(strtoParse == null){
    		JOptionPane.showMessageDialog(this,"Errors found in formula.","Please type formula",JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
    	/**
    	 * Check formula grammar;
    	 */
    	java.io.Reader inp = (java.io.Reader) (new java.io.StringReader(strtoParse));    
    	errorMsg = new ErrorMsg(strtoParse);
    	parser p = new parser(new Yylex(inp,errorMsg));
        p.parse();
        
        //store new formula to transition object
        String currentFormula = connector.getNetCompositionFormla();
        if(strtoParse == null)return;
        String newFormula = strtoParse;
        if(currentFormula != newFormula){
        	connector.setNetCompositionFormula(newFormula);
        	
        setVisible(false);
        	
            //store new formula as XML
//            myTransition.setXMLFormula();
            //update transition variable list
//            myTransition.setTranVarList();
        }
    	/**
    	 * Check variable consistency;
    	 */
    	
//    	CheckVar ckvar = new CheckVar(myTransition);
//    	boolean b = ckvar.check();
//    	if(!b){
//    		JOptionPane.showMessageDialog(this,"Variable inconsistency found!","Variable Error",JOptionPane.ERROR_MESSAGE);
//    		return;
//    	}
    	}catch(Exception e){
    		System.err.println("Exception:Syntax Error Found!");
    		JOptionPane.showMessageDialog(this,"Errors found in formula.","Formula Error",JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
    	//change the transition rectangle color
    	
    	
//        setVisible(false);
//        if (m_dlgInterface!=null)
//            m_dlgInterface.parseExecuted();
        
    	//Test transition enabled HERE
    	/***********************************/
//    	myTransition.TestCheckTransitionIsEnabled();
//      CheckTransitionIsEnabled(myTransition);
    	/*********************************/
    }
	
	
}
