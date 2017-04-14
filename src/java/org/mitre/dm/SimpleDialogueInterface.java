/****************************************************************************
 *
 * Copyright (C) 2004. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 * Consult the LICENSE file in the root of the distribution for terms and restrictions.
 *
 *       Release: 1.0
 *       Date: 24-August-2004
 *       Author: Carl Burke
 *
 *****************************************************************************/
package org.mitre.dm;

import org.mitre.dm.qud.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.border.TitledBorder;
import javax.swing.text.html.*;

import java.awt.*;              //for layout managers
import java.awt.event.*;        //for action and window events

import java.io.*;
import java.io.IOException;

import java.util.*;
import java.util.logging.*;
import java.beans.*;

/**
 * Provides a simple two-pane text interface for debugging
 * dialogues. The upper pane displays system output, and
 * echos user input in italics. The bottom pane is a text
 * input widget. Input is submitted to the IS when the
 * Enter key is pressed.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see JFrame
 * @see ActionListener
 */
public class SimpleDialogueInterface
    extends JFrame
    implements ActionListener
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.SimpleDialogueInterface");

    protected JScrollPane scrollPane;
    protected JTextPane textPane;
    public String theOutputValue;
    protected UserInput userInput;

    public void put(String text)
    {
        logger.logp(Level.INFO,"org.mitre.dm.SimpleDialogueInterface","put","OUTPUT EVENT",text);
        HTMLDocument doc = (HTMLDocument)textPane.getStyledDocument();
        HTMLEditorKit kit = (HTMLEditorKit)textPane.getEditorKit();
        //textPane.append(text+"\n");
        try {
            kit.insertHTML(doc,
                           doc.getEndPosition().getOffset()-1,
                           text /* +"<br>" */,
                           0, 0, null);
            /*
            System.out.println("endPosition == "+doc.getEndPosition().getOffset());
            doc.insertString(doc.getEndPosition().getOffset()-1, text+"<br>", null);
            try {
                ((HTMLDocument)doc).insertBeforeEnd(doc.getDefaultRootElement(), text+"<br>");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            */
        } catch (/* BadLocation */ Exception e) {
            e.printStackTrace();
        }
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    }

    public void putUserText(String text)
    {
        logger.logp(Level.INFO,"org.mitre.dm.SimpleDialogueInterface","put","OUTPUT EVENT",text);
        HTMLDocument doc = (HTMLDocument)textPane.getStyledDocument();
        HTMLEditorKit kit = (HTMLEditorKit)textPane.getEditorKit();
        //textPane.append(text+"\n");
        try {
            kit.insertHTML(doc,
                           doc.getEndPosition().getOffset()-1,
                           "<i>"+text+"</i>" /* +"<br>" */,
                           0, 0, null);
            /*
            System.out.println("endPosition == "+doc.getEndPosition().getOffset());
            doc.insertString(doc.getEndPosition().getOffset()-1, text+"<br>", null);
            try {
                ((HTMLDocument)doc).insertBeforeEnd(doc.getDefaultRootElement(), text+"<br>");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            */
        } catch (/* BadLocation */ Exception e) {
            e.printStackTrace();
        }
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    }

    public SimpleDialogueInterface(UserInput ui) {
        super("Simple Dialogue Interface");

        JPanel contentPanel = new JPanel();
        setContentPane(contentPanel);

        /*
         * Create the output window
         */
        textPane = createTextPane();
        scrollPane = new JScrollPane(textPane);

        userInput = ui;

        /*
         * Create the input window
         */
        //Create a text pane.
        inputPane = createInputPane();
        createActionTable(inputPane);

        // listen for changes
        Document doc = inputPane.getDocument();
        setDocumentListeners(doc);

        // check out the Keymap
        //enterAction = inputPane.getKeymap().getAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0,false));
        /*enterAction*/ Object ea = inputPane.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0,false));
        enterAction = inputPane.getActionMap().get(ea);
        //inputPane.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0,false), new Action() {
        inputPane.getActionMap().put(ea, new Action() {
                public Object getValue(String key) {
                    if (enterAction != null) {
                        return enterAction.getValue(key);
                    } else {
                        return null;
                    }
                }
                public void putValue(String key, Object value) {
                    if (enterAction != null) {
                        enterAction.putValue(key, value);
                    }
                }
                public boolean isEnabled() {
                    if (enterAction != null) {
                        return enterAction.isEnabled();
                    } else {
                        return true;
                    }
                }
                public void setEnabled(boolean b) {
                    if (enterAction != null) {
                        enterAction.setEnabled(b);
                    }
                }
                public void addPropertyChangeListener(PropertyChangeListener listener) {
                    if (enterAction != null) {
                        enterAction.addPropertyChangeListener(listener);
                    }
                }
                public void removePropertyChangeListener(PropertyChangeListener listener) {
                    if (enterAction != null) {
                        enterAction.removePropertyChangeListener(listener);
                    }
                }
                public void actionPerformed(ActionEvent e) {
                    //System.out.println("*** enter");
                    if (enterAction != null) {
                        enterAction.actionPerformed(e);
                    }
                    scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                }
            });

        /*
         * Put the components together
         */
        Box b = Box.createVerticalBox();
        b.add(scrollPane);
        b.add(inputPane);
        getContentPane().add(b);
    }

    private Element rootElement;
    private JTextPane createTextPane() {
        //JTextPane textPane = new JTextPane(20,40);
        JTextPane textPane = new JTextPane(new HTMLDocument());
        textPane.setEditorKit(new HTMLEditorKit());
        textPane.setEditable(false);
        textPane.setPreferredSize(new Dimension(600,400));
        //System.out.println(((HTMLDocument)textPane.getDocument()).getElement("body"));
        //rootElement = ((HTMLDocument)textPane.getDocument()).createDefaultRoot();
        //textPane.setLineWrap(true);  // JTextArea API
        //textPane.setWrapStyleWord(true);  // JTextArea API
        return textPane;
    }

    public void doExit() {
        System.exit(0);
    }
    protected JTextField inputPane;
    public String theInputValue;

    public static void main(String[] args) {
        final SimpleDialogueInterface frame = new SimpleDialogueInterface(null);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.doExit();
            }
            public void windowOpened(WindowEvent e) {
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    protected StringBuffer inputText;

    private boolean processText(String text)
    {
        text = text.trim();
        putUserText(text);
        theInputValue = text;
        logger.logp(Level.INFO,"org.mitre.dm.SimpleDialogueInterface","processText","INPUT EVENT",text);
        userInput.setInput(theInputValue);
        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
        return true;
    }

    private Action enterAction;

    private void setDocumentListeners(Document doc)
    {
        doc.addDocumentListener(new DocumentListener() {
              public void insertUpdate(DocumentEvent e) {
                  scriptChanged=true;
              }
              public void removeUpdate(DocumentEvent e) {
                  scriptChanged=true;
              }
              public void changedUpdate(DocumentEvent e) {
                  //Plain text components don't fire these events
                  scriptChanged=true;
              }
            });
    }

    public void actionPerformed(ActionEvent e) {
        //System.out.println("action performed "+e);
        JTextComponent source = (JTextComponent)e.getSource();
        processText(source.getText());
        source.setText("");
    }

    private boolean scriptChanged;
    private Hashtable actions;

    private void createActionTable(JTextComponent textComponent) {
        actions = new Hashtable();
        Action[] actionsArray = textComponent.getActions();
        for (int i = 0; i < actionsArray.length; i++) {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
    }    

    private JTextField createInputPane() {
        JTextField inputPane = new JTextField(30);
        inputPane.addActionListener(this);
        return inputPane;
    }

}

