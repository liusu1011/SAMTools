package pipe.gui.widgets;

import formulaParser.FontUtil;
import hlpn2smt.HLPNModelToZ3Converter;
import hlpn2smt.Property;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Transition;

public class Z3Dialog extends JDialog{
	DataLayer appModel;
	private String steps = "";
	JTextField getStepsText;
	
	public Z3Dialog(DataLayer _appModel){
    	super();
    	this.appModel = _appModel;
    	initialize();
    }
	
	protected void initialize() {
		this.setSize(200, 150);
		setTitle("Z3Checker Analysis");
		getContentPane().setSize(500, 500);
		getContentPane().setLayout(new BorderLayout());
		getStepsText = new JTextField();
		getStepsText.setFont(FontUtil.loadDefaultTLFont());
		getStepsText.setSize(100, 25);
		getStepsText.setBorder(BorderFactory.createLineBorder(Color.black));
		getStepsText.setLocation(5, 5);
		getContentPane().add(getStepsText, BorderLayout.NORTH);
		
		JButton runBut = new JButton("Z3 Run Check");
		runBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                okPressed();
            }
        });
		runBut.setSize(100, 20);
		runBut.setLocation(getStepsText.getX(), getStepsText.getY()+25);
		runBut.setDefaultCapable(isFocused());
        getContentPane().add(runBut);
        
        JButton closeBut = new JButton("Cancel");
        closeBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
            	dispose();
            }
        });
        closeBut.setSize(100, 30);
        closeBut.setLocation(runBut.getX(), runBut.getY()+25);
        getContentPane().add(closeBut, BorderLayout.SOUTH);
        
        getContentPane().setVisible(true);
        this.rootPane.setDefaultButton(runBut);
	}
	
	private void okPressed() {
		steps = new String(getStepsText.getText());
	    if(steps == null){
	    	JOptionPane.showMessageDialog(this,"Errors found in formula.","Please type steps",JOptionPane.ERROR_MESSAGE);
	    	return;
	    }
		ArrayList<Property> propertyList = new ArrayList<Property>();
    	HLPNModelToZ3Converter convert = new HLPNModelToZ3Converter(appModel, Integer.parseInt(steps), propertyList);
	}
}
