/* Author: Alexander Pataky
 * Date: 12/05/2010
 * 
 * dialog to set properties for component
 */
package SAMGUI.sam_controller;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;

import SAMGUI.sam_model.Component;
import SAMGUI.sam_model.SamModelObject;
import SAMGUI.sam_widgets.FontUtil;

import ltlparser.*;
import ltlparser.errormsg.ErrorMsg;
import ltlparser.ltlabsyntree.LogicSentence;


public class ComponentDialog extends SamDialog {
	private JTextField txtForumla;
	private Component comp;
	public Font m_font;
	
	ComponentDialog(Component comp) {
		super(comp);
		this.comp = comp;
		this.setup();
	}
	
	@Override
	protected void save(){
		super.save();
		comp.setFormula(txtForumla.getText());
		
		//check formula error by LTL parser;
		ErrorMsg errorMsg = null;
		try{
			String strtoParse = new String(txtForumla.getText());
			if(strtoParse == null){
	    		JOptionPane.showMessageDialog(this,"Errors found in formula.","Please type formula",JOptionPane.ERROR_MESSAGE);
	    		return;
			}
			
	    	java.io.Reader inp = (java.io.Reader) (new java.io.StringReader(strtoParse));    
	    	errorMsg = new ErrorMsg(strtoParse);
	    	LTLParser p = new LTLParser(new Yylex(inp,errorMsg));
	        p.parse();			
		}catch(Exception e){
			System.err.println("Exception:Syntax Error Found!");
    		JOptionPane.showMessageDialog(this,"Errors found in formula.","Formula Error",JOptionPane.ERROR_MESSAGE);
    		return;
		}
		this.dispose();
	}
	
	private void setup(){
		txtForumla = new JTextField(this.comp.getFormula());
		m_font = FontUtil.loadDefaultTLFont();
		txtForumla.setFont(m_font);
		txtForumla.setSize(655, 25);		
		txtForumla.setBorder(new LineBorder(new Color(0, 0, 0)));
		txtForumla.setBounds(49, 90, 396, 27);
		txtForumla.setText(comp.getFormula());
		panel.add(txtForumla);
		
		JLabel lblFormula = new JLabel("Formula:");
		lblFormula.setBounds(10, 65, 70, 14);
		panel.add(lblFormula);
		
        //Now the labels and the buttons
        int curY = 135;
        int curX = 10;
        
        // Connective Symbols
        JLabel connectiveSymbols = new JLabel("Logical Connectives: ");
        connectiveSymbols.setSize(120,16);
        panel.add(connectiveSymbols);
        connectiveSymbols.setLocation(curX,curY+5);
        curX += connectiveSymbols.getWidth()+5;
        JButton btConAnd = createButton('\u2227',curX,curY);
        curX += btConAnd.getWidth();
        JButton btConOr = createButton('\u2228',curX,curY);
        curX += btConOr.getWidth();
        JButton btConNot = createButton('\u00AC',curX,curY);
        curX += btConNot.getWidth();
        JButton btConImplies = createButton('\u21D2',curX,curY);
        curX += btConImplies.getWidth();
        JButton btConDoubleImplies = createButton('\u21D4',curX,curY);
        
        curX = 10;
        curY = curY + btConDoubleImplies.getHeight()+5;
        JLabel quantifierSymbols = new JLabel("Quantifiers: ");
        quantifierSymbols.setSize(120,16);
        panel.add(quantifierSymbols);
        quantifierSymbols.setLocation(curX,curY+5);
        curX += quantifierSymbols.getWidth()+5;
        JButton btFOForAll = createButton('\u2200',curX,curY);
        curX += btFOForAll.getWidth();
        JButton btFOExists = createButton('\u2203',curX,curY);
        curX += btFOExists.getWidth();
        
        curX = 10;
        curY = curY + btFOExists.getHeight()+5;
        System.out.println(curY);
        JLabel temporalSymbols = new JLabel("Temporal Ops: ");
        temporalSymbols.setSize(120,16);
        panel.add(temporalSymbols);
        temporalSymbols.setLocation(curX,curY+5);
        curX += temporalSymbols.getWidth()+5;
        JButton btAlways = createButton('\u25A1',curX,curY);
        curX += btAlways.getWidth();
        JButton btSometimes = createButton('\u25CA',curX,curY);
        curX += btSometimes.getWidth();
        JButton btNext = createButton('\u25CB',curX,curY);
        curX += btNext.getWidth();
        		
	}
	
    private JButton createButton(char code, int x, int y){
        final char codeFinal = code;
        JButton but = new JButton(""+code);
        but.setFont(m_font);
        but.setSize(56,30);
        but.setPreferredSize(but.getSize());
        but.setFocusable(false);
        but.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e){
                buttonPressed(codeFinal);
            }
        });
        panel.add(but);
        but.setLocation(x,y);
        return but;
    }
    
    private void buttonPressed(char code){
        int unicodeVal = code;
        StringBuffer curText = new StringBuffer( txtForumla.getText());
        int caretPos = txtForumla.getCaretPosition();
        if (txtForumla.getSelectedText()!=null){
            int start = txtForumla.getSelectionStart();
            int end = txtForumla.getSelectionEnd();
            curText.replace(start,end,""+code);
            caretPos = start;
        }else{
            curText.insert(caretPos,code);
        }
        txtForumla.setText(curText.toString());
        txtForumla.setCaretPosition(caretPos+1);
    }
}
