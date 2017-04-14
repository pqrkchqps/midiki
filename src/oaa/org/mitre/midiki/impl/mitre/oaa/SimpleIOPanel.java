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
package org.mitre.midiki.impl.mitre.oaa;

import org.mitre.midiki.logic.*;

import com.sri.oaa2.com.*;
import com.sri.oaa2.lib.*;
import com.sri.oaa2.icl.*;
import com.sri.oaa2.guiutils.*;

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
public class SimpleIOPanel
    extends JFrame
    implements ActionListener
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.oaa.SimpleIOPanel");

    protected JScrollPane scrollPane;
    protected JTextPane textPane;
    public String theOutputValue;

    public void put(String text)
    {
        logger.logp(Level.INFO,"org.mitre.midiki.impl.mitre.oaa.SimpleIOPanel","put","OUTPUT EVENT",text);
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
        logger.logp(Level.INFO,"org.mitre.midiki.impl.mitre.oaa.SimpleIOPanel","put","OUTPUT EVENT",text);
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

    public SimpleIOPanel() {
        super("Simple Dialogue Interface");

        JPanel contentPanel = new JPanel();
        setContentPane(contentPanel);

        /*
         * Create the output window
         */
        textPane = createTextPane();
        scrollPane = new JScrollPane(textPane);

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
        textPane.setPreferredSize(new Dimension(600,600));
        System.out.println(((HTMLDocument)textPane.getDocument()).getElement("body"));
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
        final SimpleIOPanel frame = new SimpleIOPanel();

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.doExit();
            }
            public void windowOpened(WindowEvent e) {
            }
        });

        frame.pack();
        frame.setVisible(true);

        frame.connect();
        frame.declareServices();
        frame.assertReadiness();
    }

    protected StringBuffer inputText;

    private boolean processText(String text)
    {
        text = text.trim();
        putUserText(text);
        theInputValue = text;
        logger.logp(Level.INFO,"org.mitre.midiki.impl.mitre.oaa.SimpleIOPanel","processText","INPUT EVENT",text);
        setInput(theInputValue);
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

    /*
     * OAA interface
     */

	LibOaa myOaa = new LibOaa(new LibCom(new LibComTcpProtocol(), null));

    protected IclList solvablesForIOPanel()
    {
        IclList serviceList = new IclList();
        IclStruct svc;
        ArrayList callbackParms;
        IclStruct cb;
        IclList solvableArgs;
        ArrayList solvableParms;
        IclStruct solv;

        // create new IclStruct for show_input_string
        svc = (IclStruct)IclTerm.fromString(true, 
                "show_input_string(InputString,InputId,ClearOutput)");
        // create and register a callback
        myOaa.oaaRegisterCallback("show_input_string",
            new OAAEventListener() {
                public boolean doOAAEvent(IclTerm goal, IclList params, IclList _answers) {
                    System.out.println("show_input_string callback: "+goal);
                    _answers.add(goal);
                    String text = goal.iterator().next().toString();
                    text = IclUtils.fixQuotes(text.trim());
                    putUserText(text);
                    return true;
                }
            });
        // build the solvable struct, append it to the serviceList
        callbackParms = new ArrayList();
        callbackParms.add(new IclStr("show_input_string"));
        cb = new IclStruct("callback", callbackParms);
        solvableArgs = new IclList();
        solvableArgs.add(cb);
        solvableParms = new ArrayList();
        solvableParms.add(svc);
        solvableParms.add(solvableArgs);
        solvableParms.add(new IclList());
        solv = new IclStruct("solvable", solvableParms);
        serviceList.add(solv);

        // create new IclStruct for show_output_string
        svc = (IclStruct)IclTerm.fromString(true, 
                 "show_output_string(OutputString,OutputId,ClearInput)");
        // create and register a callback
        myOaa.oaaRegisterCallback("show_output_string",
            new OAAEventListener() {
                public boolean doOAAEvent(IclTerm goal, IclList params, IclList _answers) {
                    System.out.println("show_output_string callback: "+goal);
                    _answers.add(goal);
                    String text = goal.iterator().next().toString();
                    text = IclUtils.fixQuotes(text.trim());
                    put(text);
                    return true;
                }
            });
        // build the solvable struct, append it to the serviceList
        callbackParms = new ArrayList();
        callbackParms.add(new IclStr("show_output_string"));
        cb = new IclStruct("callback", callbackParms);
        solvableArgs = new IclList();
        solvableArgs.add(cb);
        solvableParms = new ArrayList();
        solvableParms.add(svc);
        solvableParms.add(solvableArgs);
        solvableParms.add(new IclList());
        solv = new IclStruct("solvable", solvableParms);
        serviceList.add(solv);

        return serviceList;
    }
    protected IclTerm toIcl(Object obj)
    {
        if (obj instanceof Variable) {
            return new IclVar(((Variable)obj).name());
        } else if (obj instanceof Predicate) {
            ArrayList args = new ArrayList();
            Iterator acts = ((Predicate)obj).arguments();
            while (acts.hasNext()) {
                args.add(toIcl(acts.next()));
            }
            return new IclStruct(((Predicate)obj).functor(), args);            
        } else if (obj instanceof Integer) {
            return new IclInt(((Integer)obj).intValue());
        } else if (obj instanceof Long) {
            return new IclInt(((Long)obj).longValue());
        } else if (obj instanceof Float) {
            return new IclFloat(((Float)obj).floatValue());
        } else if (obj instanceof Double) {
            return new IclFloat(((Double)obj).doubleValue());
        } else if (obj instanceof String) {
            return new IclStr((String)obj);
        } else if (obj instanceof java.util.List) {
            ArrayList args = new ArrayList();
            Iterator acts = ((java.util.List)obj).iterator();
            while (acts.hasNext()) {
                args.add(toIcl(acts.next()));
            }
            return new IclList(args);
        } else {
            // serialize and punt
            return new IclStr(obj.toString());
        }
    }
    protected IclTerm composeRequest(String solvableName,
                                     java.util.List actuals)
    {
        ArrayList args = new ArrayList();
        Iterator acts = ((java.util.List)actuals).iterator();
        while (acts.hasNext()) {
            args.add(toIcl(acts.next()));
        }
        return new IclStruct((String)solvableName, args);
    }
    protected void fromIoPodium(Object inputStr, Object inputId,
                                Object outputStr, Object outputId)
    {
        ArrayList args = new ArrayList(4);
        args.add(inputStr);
        args.add(inputId);
        args.add(outputStr);
        args.add(outputId);
        IclTerm solvableRequest = composeRequest("fromIOPodium", args);
        System.out.println(solvableRequest);
        IclList solutions = new IclList();
        boolean retval = myOaa.oaaSolve(solvableRequest, (IclList)IclTerm.fromString(false,"[block(true)]"), solutions);
        if (!retval) {
            System.out.println("send failed");
        }
    }
    /**
     * Performs one-time <code>Agent</code> initialization.
     * This processing is likely to include initialization
     * of the <code>Contract</code>s and <code>Cell</code>s
     * for the <code>Agent</code>, as well as non-Midiki
     * initialization.
     *
     */
    public void init()
    {
    }
    /**
     * Release any resources which were created in <code>init</code>.
     * Following this routine, the agent will be terminated.
     *
     */
    public void destroy()
    {
    }

    public int inputId = 0;
    public int outputId = 0;
    /**
     * Set is.input to the specified string.
     *
     * @param input a <code>String</code> value
     */
    public boolean setInput(String input)
    {
        inputId++;
        fromIoPodium(input, new Integer(inputId), Variable.newVariable(), Variable.newVariable());
        return true;
    }
    /**
     * Connects the <code>Agent</code> to the provided information state.
     * In this routine, the <code>Agent</code> should register any
     * <code>Rule</code>s necessary for normal operation, and perform
     * any other <code>InfoState</code>-specific processing. 
     *
     * @param infoState a compatible information state
     * @return <code>true</code> if connection succeeded
     */
    public boolean connect()
    {
        logger.logp(Level.FINE,"org.mitre.dm.qud.IOAgent","connect","connecting to oaa");

        // First, connects to the facilitator
        if (myOaa.getComLib().comConnected("parent")) {
            logger.logp(Level.WARNING,
                        "org.mitre.midiki.impl.mitre.oaa.SimpleIOPanel",
                        "registerWithFramework",
                        "Already connected");
            return true;
        }
        
        if (!myOaa.getComLib().comConnect("parent", 
                                          IclTerm.fromString(true,"tcp(A,B)"), 
                                          (IclList)IclTerm.fromString(true,"[]"))) {
            logger.logp(Level.WARNING,
                        "org.mitre.midiki.impl.mitre.oaa.SimpleIOPanel",
                        "registerWithFramework",
                        "Couldn't connect to the facilitator");
            return false;
        }
                    
        // Once the connection is established,
        //performs handshaking with the facilitator.
        if (!myOaa.oaaRegister("parent",
                               "simpleIoPanel",
                               (IclList)IclTerm.fromString(true,"[]"), 
                               (IclList)IclTerm.fromString(true,"[]"))) 
        {
            logger.logp(Level.WARNING,
                        "org.mitre.midiki.impl.mitre.oaa.SimpleIOPanel",
                        "registerWithFramework",
                        "Could not register");
            return false;
        }

        /*
         * Set the rule to be fired when output is available.
         *
        Condition movesC = new Condition();
        movesC.extend(new OutputNotEmpty());
        ExistsRule moves =
            new ExistsRule(movesC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        sdi.put((String)infoState.cell("output").get("output"));
                        infoState.cell("output").put("output","");
                        return true;
                    }
                };
        boolean connected = infoState.cell("output").addInfoListener("output", new RuleBasedInfoListener(moves){});
        */
        logger.logp(Level.FINE,"org.mitre.dm.qud.IOAgent","connect","connected");

        return true;
    }
    /**
     * Describe <code>declareServices</code> method here.
     *
     * Store created signatures for addition to servers.
     */
    public boolean declareServices()
    {
        IclList services = solvablesForIOPanel();
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "declareServices",services);
        System.out.println("declareServices:"+services);
        Object solvRet = myOaa.oaaDeclare(services,
                                          IclTerm.fromString(false,"[]"),
                                          IclTerm.fromString(false,"[]"),
                                          IclTerm.fromString(false,"[]"));
        logger.logp(Level.FINER,
                    "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                    "declareServices","solvables returned",solvRet);
        return true;
    }
	/**
     * Defines the capabilities registered with Facilitator.
     *
     * @param goal an <code>IclTerm</code> value
     * @param params an <code>IclList</code> value
     * @param answers an <code>IclList</code> value
     * @return a <code>boolean</code> value
     */
    public boolean oaaDoEventCallback(IclTerm goal, IclList params, IclList answers) {
        System.out.println("MKAgentOaa.oaaDoEventCallback("+goal+")");
        return false;
    }
    /**
     * Declare that this federate is ready to participate.
     *
     * mainServer.start()
     */
    public void assertReadiness()
    {
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "assertReadiness");
        // Connection succeeded!
        myOaa.oaaReady(true);

        
        // Register OAA callback
        myOaa.oaaRegisterCallback("oaa_AppDoEvent",
                                  new OAAEventListener() {
                                          public boolean doOAAEvent(IclTerm goal, IclList params, IclList answers) 
                                          {
                                              return oaaDoEventCallback(goal, params, answers);
                                          }
                                      });
    }
    /**
     * Disconnects the <code>Agent</code> from the information state.
     * After this call, the <code>InfoState</code> is assumed to be
     * invalid, and no further processing should be performed
     * until another call to <code>connect</code>.
     * (The API does not require that all implementations
     * of <code>Agent</code> be able to <code>connect</code>
     * again following <code>disconnect</code>.)
     *
     */
    public void disconnect()
    {
    }
}

