/****************************************************************************
 *
 * Copyright (C) 2005. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 * Consult the LICENSE file in the root of the distribution for terms and restrictions.
 *
 *       Release: 1.0
 *       Date: 15-August-2005
 *       Author: Paul Tepper
 *
 *****************************************************************************/
package org.mitre.dm.tools;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;
import java.util.regex.*;

import org.mitre.dm.qud.domain.PlanInstance;
import org.mitre.midiki.state.ImmutableCell;
import org.mitre.midiki.state.ImmutableInfoState;
import org.mitre.midiki.impl.mitre.AttributeImpl;
import org.mitre.midiki.impl.mitre.ContractImpl;
import org.mitre.midiki.impl.mitre.ImmutableInfoStateImpl;


/**
 * Generates a window that prints out the contents of the
 * Information State after every update.
 *
 * @author Paul Tepper
 * @version $Id: InfoStateGui.java,v 1.4 2005/08/24 22:29:38 cburke Exp $
 *
 */
public final class InfoStateGui extends JFrame {
    // Layout parameters
    public final int TEXTAREA_ROWS = 50;
    public final int TEXTAREA_COLUMNS = 40;
    
    // Interface elements
    protected JTextArea textArea;
    protected JPanel toolbar;
    protected JPanel contentPanel;
    protected JButton findButton;
    protected JTextField textField;
    protected DefaultHighlightPainter hiLitePtr;
	protected Color hiliteColor = Color.yellow;
    protected Set toplevelContracts;
	
	//For IS 
	int updateCount = 1;
	
    /**
     * Right now you create the object in DMEAgent and call #showInfoState(ImmutableInfoState)
     * from somewhere in DMEAgent#update(ImmutableInfoState)
     *
     */
    public InfoStateGui() {
		super("Information State Viewer");
		contentPanel = new JPanel(new BorderLayout());
		toolbar = new JPanel(new FlowLayout());
        
		//Toolbar & buttons
		ActionListener buttonListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if ((ae.getSource() == findButton) && (textField.getText().length() > 0)){
					find(textField.getText());
				}
			}
		};	
	
		findButton = new JButton("Find");
		findButton.addActionListener(buttonListener);
		
        textField = new JTextField();
        textField.setColumns(10);
        
        toolbar.add(textField);
        toolbar.add(findButton);
 		
        //	Text panel
		textArea = new JTextArea(TEXTAREA_ROWS, TEXTAREA_COLUMNS);
		textArea.setEditable(false);
		
		hiLitePtr = new DefaultHighlightPainter(hiliteColor);
		
		JScrollPane scrollPane = 
			new JScrollPane(textArea,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		contentPanel.add(toolbar, BorderLayout.NORTH);
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		contentPanel.setOpaque(true); 
		contentPanel.setBackground(java.awt.Color.white);        
		
		//Window properties
		setLocation(200, 0);
		setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(contentPanel);
		
		//Display the window.
		pack();
		setVisible(true);		
	}
    
    /**
     * Set the information state display to be rooted at this contract.
     * Multiple roots may be specified through multiple calls.
     * Unspecified contracts will only appear in the display when they
     * appear as attributes.
     *
     * @param name Contract name
     */
    public void addToplevelContract(String name)
    {
        if (toplevelContracts == null) {
            toplevelContracts = new HashSet();
        }
        toplevelContracts.add(name);
    }

    /**
     * Print contents of information state
     * @param is
     */
    public void showInfoState(ImmutableInfoState is){
        
        ImmutableInfoStateImpl isi = (ImmutableInfoStateImpl) is;
        textArea.append("*** UPDATE: "+updateCount+" ***************\n");
        textArea.append("Contracts: \n");
        
        Set contracts;
        if (toplevelContracts == null)
            contracts = isi.getContracts();
        else {
            contracts = new HashSet();
            Iterator ctrIter = isi.getContracts().iterator();
            ContractImpl contract;
        
            while(ctrIter.hasNext()){
                contract = (ContractImpl) ctrIter.next();
                if (toplevelContracts.contains(contract.name())) {
                    contracts.add(contract);
                }
            }
        }
        Iterator setIter = contracts.iterator();
        ContractImpl contract;
        
        while(setIter.hasNext()){
            contract = (ContractImpl) setIter.next();
            textArea.append(contract.name() + ":[\n");
            printAttributes(is.cell(contract.name()),is, 1);
            textArea.append("]\n\n");
        }
        
        updateCount++;
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    
    /**
     * Helper function for #showInfoState, called recursively to print cells
     * and sub-cells of is
     *
     * @param contract	top level contract to print
     * @param is			information state of contract
     * @param depth		depth param, passed in recursively to generate whitespace
     * 					for indenting/pretty printing
     */
    public void printAttributes(ImmutableCell cell, ImmutableInfoState is, int depth) {
		
        ContractImpl contract = (ContractImpl) cell.getContract();
        Iterator attributes = contract.attributes();
        AttributeImpl attribute;
        String attrName;
        Object type;
        
        while(attributes.hasNext()){
            
            attribute = ((AttributeImpl) attributes.next());
            attrName = attribute.name();
            type = attribute.type();
				
			textArea.append(genWhitespace(depth) + attrName + ": ");										
			try {
				try {
					if(cell.get(attrName) != null){
						
					 	//if(cell.get(attrName).getClass().getName().equals("java.util.Stack")){
					 
						// Special block for printing PlanInstances on the plan stack
						if(attrName.equals("plan")){
					 		textArea.append("[ \n");
							Stack stack = (Stack) cell.get(attrName);
							Iterator it = stack.iterator();
			
							while(it.hasNext()){
					 			PlanInstance pi = (PlanInstance) it.next();
					 			depth++;
					 			textArea.append(genWhitespace(depth));
					 			//Reformat PlanInstance.toString() with regular expressions
					 			//There may be a more concise way to do this, but this works
					 			Pattern p = Pattern.compile("\\[\n");
					 			Matcher m = p.matcher(pi.toString());

					 			p = Pattern.compile("\\(");
					 			m = p.matcher(m.replaceFirst("[\n" + genWhitespace(depth))); //replace first [
					 			
					 			p = Pattern.compile("\\)(,\n|\n)");
					 			m = p.matcher(m.replaceAll(": ")); //replace ('s

					 			textArea.append(m.replaceAll("\n" + genWhitespace(depth))); //replace ),\n or )\n's
					 			
					 			depth --;
					 			textArea.append(genWhitespace(depth) + "]");
					 		}
					 	}
					 	else textArea.append(cell.get(attrName).toString());
						
						//textArea.append("("+ cell.get(attrName).getClass().toString()+")");
						
						if(type != null){
							//textArea.append(" ("+ type.getClass().getName() +")");
							if(type.getClass().getName().matches("org.mitre.midiki.impl.mitre.ContractImpl")){
							//if(attribute.hasCompatibleType(contract)){
                                ContractImpl ci = (ContractImpl)type;
								textArea.append(": [\n"); 
								depth++;
								printAttributes(cell.cell(attrName),is,depth);
								depth--;
								textArea.append(genWhitespace(depth) +"] ");
							}
						}
					}
					else textArea.append(" ---");
					
				}
				catch(RuntimeException rex){
					textArea.append(" (runtime exception)");
					System.out.println("\n");
					rex.printStackTrace();
				}
			}
			catch(NullPointerException npex){
				textArea.append(" (null pointer)");
				npex.printStackTrace();
			}
			textArea.append("\n");
		}
	}
	
	public void highlight(String pattern) {
		// First remove all old highlights
		removeHighlights();

		try {
			Highlighter hilite = textArea.getHighlighter();
			String text = textArea.getText();
			int pos = 0;
			
			// Search for pattern
			while ((pos = text.indexOf(pattern, pos)) >= 0) {
				// Create highlighter using private painter and apply around pattern
				hilite.addHighlight(pos, pos+pattern.length(), hiLitePtr);
				pos += pattern.length();
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
    
	/**
	 * Remove all highlights in textArea
	 */
	public void removeHighlights() {
		Highlighter hilite = textArea.getHighlighter();
		Highlighter.Highlight[] hilites = hilite.getHighlights();
		
		for (int i=0; i<hilites.length; i++) {
			hilite.removeHighlight(hilites[i]);
		}
	}
	
	/**
	 * Calls #highlight and moves the caret to the location of text if found
	 * @param s Text to find
	 */
	
	protected void find(String s){		
		String text = textArea.getText();
		highlight(s);
		Pattern p = Pattern.compile("s");
		Matcher m = p.matcher(text);
		if(m.find() && text.indexOf(s) >= 0) textArea.moveCaretPosition(text.indexOf(s));
	}
	
	/**
	 * Generates a string of whitespace of the specified length
	 * multiplied by 3. Used for generating indents in #printAttributes(ContractImpl,InfoState,int)
	 *  
	 * @param l	length of the desired string of whitespace
	 * @return	string of whitespace of length l*3
	 */
	public String genWhitespace(int l){
		int length = l*3;
		char chars[] = new char[length];
		for(int i=0; i<length; i++){
			chars[i] = ' ';
		}
		String string = new String(chars);
		return string;
	}
	
	/**
	 * Returns a string containing all of the contents of the window
	 * @return The string containg the window contents 
	 */
	public String getText() {
		try {
			return textArea.getText(0,textArea.getDocument().getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return "";
	}
}