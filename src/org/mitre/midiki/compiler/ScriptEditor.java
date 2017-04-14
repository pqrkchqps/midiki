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
/*
 * Script editor for the MITRE Dialogue Toolkit
 */

package org.mitre.midiki.compiler;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.border.TitledBorder;

import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import java.awt.*;              //for layout managers
import java.awt.event.*;        //for action and window events

import java.io.*;
import java.io.IOException;

import java.util.*;

import org.mitre.midiki.compiler.parser.*;

public class ScriptEditor extends JFrame
                             implements ActionListener {
    protected JLabel actionLabel;
    protected JMenu templateMenu;

    // actions, hung off the menu at first
    // file actions
    protected Action fileOpenAction;
    protected Action fileNewAction;
    protected Action fileSaveAction;
    protected Action fileSaveAsAction;
    protected Action fileExitAction;
    // edit actions
    protected UndoAction editUndoAction;
    protected RedoAction editRedoAction;
    protected Action editCopyAction;
    protected Action editCutAction;
    protected Action editPasteAction;
    // parse actions
    protected Action parseCompileAction;
    protected Action parseTemplateAction;
    protected Action parseInstallAction;
    protected Action parseFirstErrAction;
    protected Action parseNextErrAction;
    protected Action parsePrevErrAction;

    protected JPanel contentPane;
    protected JTextPane textPane;
    protected JFileChooser fc;
    protected UndoManager undo;

    protected MKParser parser = null;
    protected int errorIndex = 0;

    static public boolean DEBUG_XML_GENERATION = false;

    // note: this only shows ParseException errors, not TokenMgrError errors.
    // Both of those, and perhaps others, will need to be caught.
    protected void showParseError(int err)
    {
        if (parser==null) {
            actionLabel.setText("No parse, no errors");
            return;
        }
        if (parser.parse_errors.size()==0) {
            actionLabel.setText("No errors");
            return;
        }
        Exception pe =
            (Exception)(parser.parse_errors.elementAt(err));
        int doc_loc = 0;
        if (pe instanceof ParseException) {
            Token t = ((ParseException)pe).currentToken;
            doc_loc = t.offsetInFile;
        }
        textPane.setCaretPosition(doc_loc);
        actionLabel.setText(pe.getMessage());
    }

    protected void showStatusMessage(String msg)
    {
        actionLabel.setText(msg);
    }

    class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }
          
        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            updateUndoState();
            editRedoAction.updateRedoState();
        }
          
        protected void updateUndoState() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }      
    }    

    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
                System.out.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            updateRedoState();
            editUndoAction.updateUndoState();
        }

        protected void updateRedoState() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }    

    public ScriptEditor() {
        super("ScriptEditor");

        //Create a label to put messages during an action event.
        actionLabel = new JLabel("Type text and then Return in a field.");
        actionLabel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        //Create a text pane.
        textPane = createTextPane();
        JScrollPane paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paneScrollPane.setPreferredSize(new Dimension(700, 400));
        paneScrollPane.setMinimumSize(new Dimension(10, 10));

        //Put the action pane and the text pane in a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              actionLabel,
                                              paneScrollPane);
        splitPane.setOneTouchExpandable(true);

        //Put everything in the frame.
        contentPane = new JPanel();
        BoxLayout box = new BoxLayout(contentPane, BoxLayout.X_AXIS);
        contentPane.setLayout(box);
        contentPane.add(splitPane);
        scriptTitle = BorderFactory.createTitledBorder("Loading");
        contentPane.setBorder(BorderFactory.createCompoundBorder(
                        scriptTitle,
                        BorderFactory.createEmptyBorder(5,5,5,5)));

        setContentPane(contentPane);
        createActionTable(textPane);
        setJMenuBar(buildMenuBar());

        // create the file chooser
        userDir = System.getProperty("user.dir");
        userHome = System.getProperty("user.home");
        mkitHome = System.getProperty("mkit.home", userHome);
        fileSep = System.getProperty("file.separator");
        try {
            File homeDir = new File(userDir);
            fc = new JFileChooser(userDir);
            MKFileFilter mkfilt = new MKFileFilter();
            mkfilt.addExtension("mks");
            mkfilt.addExtension("txt");
            mkfilt.setDescription("Script files");
            fc.setFileFilter(mkfilt);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        // load and install the templates
        installTemplates();

        // listen for changes
        Document doc = textPane.getDocument();
        setDocumentListeners(doc);

        // start out with an empty script
        newScript();
    }

    private void setDocumentListeners(Document doc)
    {
        doc.addDocumentListener(new DocumentListener() {
              public void insertUpdate(DocumentEvent e) {
                  scriptChanged=true;
                  editUndoAction.updateUndoState();
                  editRedoAction.updateRedoState();
              }
              public void removeUpdate(DocumentEvent e) {
                  scriptChanged=true;
                  editUndoAction.updateUndoState();
                  editRedoAction.updateRedoState();
              }
              public void changedUpdate(DocumentEvent e) {
                  //Plain text components don't fire these events
                  scriptChanged=true;
                  editUndoAction.updateUndoState();
                  editRedoAction.updateRedoState();
              }
            });
        undo = new UndoManager();
        doc.addUndoableEditListener(new UndoableEditListener() {
                public void undoableEditHappened(UndoableEditEvent e) {
                    //Remember the edit and update the menus
                    undo.addEdit(e.getEdit());
                    editUndoAction.updateUndoState();
                    editRedoAction.updateRedoState();
                }
            });
    }

    private MKLangspecMachine mkLanguage;

    private boolean loadLanguageMachine(String source)
    {
        try {
            FileReader fis = new FileReader(source);
            showStatusMessage("Compiling language spec...");
            parser = new MKParser(fis);
            ASTCompilationUnit n = parser.CompilationUnit();
            MKLangspecVisitor lang = new MKLangspecVisitor();
            Object result = lang.visit(n, parser.parse_errors);
            if (parser.parse_errors.size() == 0) {
                LinkedList code = generateCode(n);
                dumpSymbolTable(lang.symbolTable);
                dumpCodeArray(code);
                updateCodeAddresses(code);  // to cleanse lexical hooks
                mkLanguage = new MKLangspecMachine(lang.symbolTable, code);
                //MKStack mkStack = new MKStack();
                //mkLanguage.filter((MKInstruction[])null, "language", mkStack);
                templateList.add(new MKTemplate("language", 
                                                true,
                                                true,
                                                source,
                                                mkLanguage));
            }
            showStatusMessage("Language spec loaded");
            //fis.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadTemplateDatabase(String source)
    {
        try {
            FileInputStream fis = new FileInputStream(source);
            ObjectInputStream ois = new ObjectInputStream(fis);
            templateList = (Vector)ois.readObject();
            fis.close();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (InvalidClassException e) {
            System.out.println("Template library obsolete; bootstrapping.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void installTemplates()
    {
        //System.out.println("userDir"+userDir+", mkitHome="+mkitHome);
        if (!loadTemplateDatabase(mkitHome+fileSep+"templates.mk")) {
            if (!loadTemplateDatabase(userDir+fileSep+"templates.mk")) {
                System.out.println("Can't load template database");
                templateList = new Vector();
                if (!loadLanguageMachine(mkitHome+fileSep+"language.txt")) {
                    if (!loadLanguageMachine(userDir+fileSep+"language.txt")) {
                        System.out.println("Can't load language specification file");
                        System.exit(0);
                    }
                }
            }
        }
        //
        mkLanguage = defaultTemplate().getMachine();
        listTemplates();
    }

    private void replaceNamedTemplates()
    {
        if (mkLanguage==null) {
            return;
        }
        if (scriptOutputCode==null) {
            return;
        }
        // extract context refs from symbol table.
        // if the name is already in the list, remove it.
        // then add a new template and langspec machine.
        Iterator it = scriptExecutionContext.getSymbolTable().symbols();
        while (it.hasNext())
        {
            MKSymbol sym = (MKSymbol)(it.next());
            if (sym.getType() == MKContext.ctxtType) {
                String name = sym.getName();
                if (name.equals("$global")) continue;
                System.out.println("*** langspec name = "+name);

                // find existing template (if any)

                Enumeration ten = templateList.elements();
                MKTemplate tp = null;
                while (ten.hasMoreElements()) {
                    MKTemplate temp = (MKTemplate)ten.nextElement();
                    if (temp==null) continue;
                    if (temp.getName().equals(name)) {
                        tp = temp;
                        break;
                    }
                }
                if (tp != null) {
                    templateList.removeElement(tp);
                }

                // append a new template

                templateList.add(new MKTemplate(name, 
                                                false,
                                                false,
                                                scriptName,
                    new MKLangspecMachine(scriptExecutionContext.getSymbolTable(),
                                          scriptOutputCode)));
            }
        }
        listTemplates();
        updateTemplates();
    }

    private void listTemplates()
    {
        LinkedList tempList = new LinkedList();
        Enumeration enum = templateList.elements();
        while (enum.hasMoreElements()) {
            MKTemplate t = (MKTemplate)(enum.nextElement());
            if (t==null) continue;
            System.out.println(t.getName()+"\t"+t);
            tempList.add(t.getName());
        }
        Object[] templateArray = tempList.toArray();
        Arrays.sort(templateArray);
        templateMenu.removeAll();
        for (int i=0; i<templateArray.length; i++) {
            if (((String)templateArray[i]).startsWith("_")) continue;
            JMenuItem templateItem = new JMenuItem(parseTemplateAction);
            templateItem.setText((String)templateArray[i]);
            templateMenu.add(templateItem);
        }
    }

    private void updateTemplates()
    {
        try {
            FileOutputStream fos =
                new FileOutputStream(userDir+fileSep+"templates.mk");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(templateList);
            fos.close();
        } catch (Exception e) {
            System.out.println("*** Template storage failed ***");
            e.printStackTrace();
        }
    }

    private MKTemplate defaultTemplate()
    {
        Enumeration enum = templateList.elements();
        while (enum.hasMoreElements()) {
            MKTemplate t = (MKTemplate)(enum.nextElement());
            if (t.getName().equals("language")) return t;
        }
        return null;
    }

    private Vector templateList;
    private boolean scriptChanged;
    private String scriptName;
    private File scriptFile;
    private MKTemplate scriptTemplate;
    private TitledBorder scriptTitle;
    private LinkedList scriptOutputCode;
    private MKStack scriptExecutionContext;
    private String userDir;
    private String userHome;
    private String mkitHome;
    private String fileSep;

    private void setScriptTitle()
    {
        String templateName = "{null}";
        if (scriptTemplate != null) {
            templateName = scriptTemplate.getName();
        }
        scriptTitle.setTitle(scriptName+":"+templateName);
        contentPane.repaint(contentPane.getVisibleRect());
    }

    private MKStack cleanParseStack()
    {
        if (scriptTemplate == defaultTemplate())
            return new MKLangspecStack();
        else
            return new MKStack();
    }

    // opening a file or creating a new file must reset all parser variables.
    // also check to make sure the undo/redo histories are cleared.
    private void newScript()
    {
        scriptName = "*new script*";
        scriptFile = null;
        scriptChanged = false;
        scriptTemplate = defaultTemplate();
        setScriptTitle();
        scriptExecutionContext = null;
        StyledEditorKit dsek = new StyledEditorKit();
        Document doc = dsek.createDefaultDocument();
        setDocumentListeners(doc);
        textPane.setEditorKit(dsek);
        textPane.setDocument(doc);
        parseInstallAction.setEnabled(false);
    }

    private void openScript()
    {
        int returnVal = fc.showOpenDialog(this);
                        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            scriptFile = fc.getSelectedFile();
            scriptName = scriptFile.getName();
            StyledEditorKit dsek = new StyledEditorKit();
            Document doc = dsek.createDefaultDocument();
            try {
                FileReader fr = new FileReader(scriptFile);
                dsek.read(fr, doc, 0);
                fr.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setDocumentListeners(doc);
            textPane.setEditorKit(dsek);
            textPane.setDocument(doc);
        } else {
            return;
        }
        scriptChanged = false;
        setScriptTitle();
        scriptExecutionContext = null;
        contentPane.repaint(contentPane.getVisibleRect());
        parseInstallAction.setEnabled(false);
    }

    private boolean askSaveScript()
    {
        if (!scriptChanged) return false;
        // ask the user if they want to save the current file.
        int decision = JOptionPane.showConfirmDialog(this,
                                      "The current script has been changed. "+
                                      "Would you like to save your changes?",
                                      "File has changed",
                                      JOptionPane.YES_NO_CANCEL_OPTION,
                                      JOptionPane.QUESTION_MESSAGE);
        if (decision == JOptionPane.YES_OPTION) {
            if (scriptName.equals("*new script*")) {
                saveScriptAs();
            } else {
                saveScript();
            }
        } else if (decision == JOptionPane.NO_OPTION) {
            return false;
        } else if (decision == JOptionPane.CANCEL_OPTION) {
            return true;
        }        // returns true if the user cancels this function
        return false;
    }

    private void saveScript()
    {
        scriptChanged = false;
        try {
            FileWriter fw = new FileWriter(scriptFile);
            Document doc = textPane.getDocument();
            textPane.getEditorKit().write(fw, doc, 0, doc.getLength());
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean askOverwrite()
    {
        // ask the user if they want to overwrite an existing file.
        int decision = JOptionPane.showConfirmDialog(this,
                                      "The specified file already exists. "+
                                      "Would you like to replace that file "+
                                      "with this script?",
                                      "File exists",
                                      JOptionPane.YES_NO_OPTION,
                                      JOptionPane.QUESTION_MESSAGE);
        if (decision == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }

    private void saveScriptAs()
    {
        boolean checking = true;

        while (checking) {
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                scriptFile = fc.getSelectedFile();
                if (scriptFile.exists()) {
                    if (!askOverwrite()) continue;
                }
                scriptName = scriptFile.getName();
                setScriptTitle();
                contentPane.repaint(contentPane.getVisibleRect());
                saveScript();
                break;
            } else {
                return; //save aborted
            }
        }
    }

    private PrintWriter getXmlTempPrintWriter()
    {
        String path = scriptFile.getPath();
        int loc = path.lastIndexOf(".");
        if (loc>=0) {
            path = path.substring(0, loc)+"_temp.xml";
        } else {
            path = path+"_temp.xml";
        }
        System.out.println("Temp file output to "+path);
        try {
            return new PrintWriter(new FileWriter(path));
        } catch (IOException ioe) {
            return null;
        }
    }

    private PrintWriter getXmlPrintWriter()
    {
        String path = scriptFile.getPath();
        int loc = path.lastIndexOf(".");
        if (loc>=0) {
            path = path.substring(0, loc)+".xml";
        } else {
            path = path+".xml";
        }
        System.out.println("Script file output to "+path);
        try {
            return new PrintWriter(new FileWriter(path));
        } catch (IOException ioe) {
            return null;
        }
    }

    private Hashtable actions;

    private void createActionTable(JTextComponent textComponent) {
        actions = new Hashtable();
        Action[] actionsArray = textComponent.getActions();
        for (int i = 0; i < actionsArray.length; i++) {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
    }    

    private Action getActionByName(String name) {
        return (Action)(actions.get(name));
    }

    private void buildActions()
    {
        // file actions
        fileOpenAction = new AbstractAction("Open...", null) {
                public void actionPerformed(ActionEvent e) {
                    if (askSaveScript()) return;
                    openScript();
                }
            };
        fileNewAction = new AbstractAction("New", null) {
                public void actionPerformed(ActionEvent e) {
                    if (askSaveScript()) return;
                    newScript();
                }
            };
        fileSaveAction = new AbstractAction("Save", null) {
                public void actionPerformed(ActionEvent e) {
                    if (scriptName.equals("*new script*")) {
                        saveScriptAs();
                    } else {
                        saveScript();
                    }
                }
            };
        fileSaveAsAction = new AbstractAction("Save As...", null) {
                public void actionPerformed(ActionEvent e) {
                    saveScriptAs();
                }
            };
        fileExitAction = new AbstractAction("Exit", null) {
                public void actionPerformed(ActionEvent e) {
                    if (askSaveScript()) return;
                    System.exit(0);
                }
            };
        // edit actions
        editUndoAction = new UndoAction();
        editRedoAction = new RedoAction();
        editCopyAction = getActionByName(DefaultEditorKit.copyAction);
        editCutAction = getActionByName(DefaultEditorKit.cutAction);
        editPasteAction = getActionByName(DefaultEditorKit.pasteAction);
        // parse actions
        parseCompileAction = new AbstractAction("Compile", null) {
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (scriptName.equals("*new script*")) {
                            showStatusMessage("Saving...");
                            saveScriptAs();
                        } else if (scriptChanged) {
                            showStatusMessage("Saving...");
                            saveScript();
                        }
                        showStatusMessage("Compiling...");
                        Document doc = textPane.getDocument();
                        parser =
                            new MKParser(new StringReader(doc.getText(0, doc.getLength())));
                        ASTCompilationUnit n = parser.CompilationUnit();
                        // first see what it looks like with mkLanguage
                        mkLanguage = scriptTemplate.getMachine();
                        System.out.println("*** compiling as '"+
                                           scriptTemplate.getName()+
                                           "'");
                        if (parser.parse_errors.size() == 0) {
                            LinkedList icode = generateCode(n);
                            dumpCodeArray(icode);
                            PrintWriter fw = getXmlTempPrintWriter();
                            dumpXmlForCode(fw, icode);
                            fw.close();
                            // must add errors and symbol table to filter()
                            // must store symbol table and code array locally
                            scriptExecutionContext = cleanParseStack();
                            scriptOutputCode =
                                mkLanguage.filter(icode,
                                                  scriptTemplate.getName(),
                                                  scriptExecutionContext,
                                                  parser.parse_errors);
                        }
                        // the filter routine needs to take the parse_errors
                        // vector and add template-based ParseException
                        // objects to it, at which point this second check
                        // will make sense.
                        updateCodeAddresses(scriptOutputCode);
                        if (parser.parse_errors.size() == 0) {
                            dumpSymbolTable(scriptExecutionContext.getSymbolTable());
                            dumpCodeArray(scriptOutputCode);
                            PrintWriter fw = getXmlPrintWriter();
                            dumpXmlForCode(fw, scriptOutputCode);
                            fw.close();
                            parseInstallAction.setEnabled(true);
                        } else {
                            System.out.println("parser.parse_errors.size() == "+parser.parse_errors.size());
                        }
                        parseFirstErrAction.actionPerformed(e);
                        // then use the visitor
                        /*
                        System.out.println("************* visitor");
                        MKLangspecVisitor lang = new MKLangspecVisitor();
                        Object result = lang.visit(n, parser.parse_errors);
                        parseFirstErrAction.actionPerformed(e);
                        if (parser.parse_errors.size() == 0) {
                            LinkedList code = generateCode(n);
                            dumpSymbolTable(lang.symbolTable);
                            dumpCodeArray(code);
                            MKLangspecMachine mlm =
                                new MKLangspecMachine(lang.symbolTable, code);
                            MKStack mkStack = new MKStack() {
                                    public void recognize(String statementType)
                                    {
                                        System.out.println(statementType+"\t"+
                                                           getDepth());
                                    }
                                };
                            mlm.filter((MKInstruction[])null, "language", mkStack);
                        }
                        */
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
        parseTemplateAction = new AbstractAction("Select template", null) {
                public void actionPerformed(ActionEvent e) {
                    Iterator it = templateList.iterator();
                    while (it.hasNext()) {
                        MKTemplate t = (MKTemplate)(it.next());
                        if (t.getName().equals(e.getActionCommand())) {
                            scriptTemplate = t;
                            setScriptTitle();
                            return;
                        }
                    }
                }
            };
        parseInstallAction = new AbstractAction("Install template", null) {
                public void actionPerformed(ActionEvent e) {
                    replaceNamedTemplates();
                }
            };
        parseInstallAction.setEnabled(false);
        parseFirstErrAction = new AbstractAction("First error", null) {
                public void actionPerformed(ActionEvent e) {
                    parseFirstErrAction.setEnabled((parser != null) &&
                                                   (parser.parse_errors.size() > 0));
                    parseNextErrAction.setEnabled((parser != null) &&
                                                   (parser.parse_errors.size() > 1));
                    parsePrevErrAction.setEnabled(false);
                    errorIndex = 0;
                    showParseError(errorIndex);
                }
            };
        parseFirstErrAction.setEnabled(false);
        parseNextErrAction = new AbstractAction("Next error", null) {
                public void actionPerformed(ActionEvent e) {
                    errorIndex++;
                    showParseError(errorIndex);
                    parseNextErrAction.setEnabled((parser != null) &&
                                                   (errorIndex < (parser.parse_errors.size()-1)));
                    parsePrevErrAction.setEnabled((parser != null) &&
                                                   (errorIndex > 0));
                }
            };
        parseNextErrAction.setEnabled(false);
        parsePrevErrAction = new AbstractAction("Prev error", null) {
                public void actionPerformed(ActionEvent e) {
                    errorIndex--;
                    showParseError(errorIndex);
                    parseNextErrAction.setEnabled((parser != null) &&
                                                   (errorIndex < (parser.parse_errors.size()-1)));
                    parsePrevErrAction.setEnabled((parser != null) &&
                                                   (errorIndex > 0));
                }
            };
        parsePrevErrAction.setEnabled(false);
    }

    public JMenuBar buildMenuBar()
    {
        JMenuItem mi;
        buildActions();
        JMenuBar mb = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        mi = new JMenuItem(fileNewAction);
        mi.setMnemonic(KeyEvent.VK_N);
        fileMenu.add(mi);
        mi = new JMenuItem(fileOpenAction);
        mi.setMnemonic(KeyEvent.VK_O);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                 ActionEvent.CTRL_MASK));
        fileMenu.add(mi);
        mi = new JMenuItem(fileSaveAction);
        mi.setMnemonic(KeyEvent.VK_S);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                 ActionEvent.CTRL_MASK));
        fileMenu.add(mi);
        mi = new JMenuItem(fileSaveAsAction);
        mi.setMnemonic(KeyEvent.VK_A);
        fileMenu.add(mi);
        mi = new JMenuItem(fileExitAction);
        mi.setMnemonic(KeyEvent.VK_X);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                                                 ActionEvent.CTRL_MASK));
        fileMenu.add(mi);
        mb.add(fileMenu);
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.add(editUndoAction);
        editMenu.add(editRedoAction);
        mi = new JMenuItem("Copy");
        mi.setMnemonic(KeyEvent.VK_C);
        mi.setAccelerator((KeyStroke)editCopyAction.getValue(Action.ACCELERATOR_KEY));
        editMenu.add(mi);
        mi = new JMenuItem("Cut");
        mi.setMnemonic(KeyEvent.VK_T);
        mi.setAccelerator((KeyStroke)editCutAction.getValue(Action.ACCELERATOR_KEY));
        editMenu.add(mi);
        mi = new JMenuItem("Paste");
        mi.setMnemonic(KeyEvent.VK_P);
        mi.setAccelerator((KeyStroke)editPasteAction.getValue(Action.ACCELERATOR_KEY));
        editMenu.add(mi);
        mb.add(editMenu);
        JMenu parseMenu = new JMenu("Parse");
        parseMenu.setMnemonic(KeyEvent.VK_P);
        mi = new JMenuItem(parseCompileAction);
        mi.setMnemonic(KeyEvent.VK_C);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
                                                 ActionEvent.CTRL_MASK));
        parseMenu.add(mi);
        templateMenu = new JMenu("Template...");
        parseMenu.add(templateMenu);
        mi = new JMenuItem(parseInstallAction);
        mi.setMnemonic(KeyEvent.VK_I);
        parseMenu.add(mi);
        mi = new JMenuItem(parseFirstErrAction);
        mi.setMnemonic(KeyEvent.VK_F);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
                                                 ActionEvent.CTRL_MASK));
        parseMenu.add(mi);
        mi = new JMenuItem(parseNextErrAction);
        mi.setMnemonic(KeyEvent.VK_N);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                                                 ActionEvent.CTRL_MASK));
        parseMenu.add(mi);
        mi = new JMenuItem(parsePrevErrAction);
        mi.setMnemonic(KeyEvent.VK_P);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
                                                 ActionEvent.CTRL_MASK));
        parseMenu.add(mi);
        mb.add(parseMenu);
        return mb;
    }

    public void actionPerformed(ActionEvent e) {
        TextComponent source = (TextComponent)e.getSource();
        String prefix = "You typed \"";
        actionLabel.setText(prefix + source.getText() + "\"");
    }

    private JTextPane createTextPane() {
        JTextPane textPane = new JTextPane();
        initStylesForTextPane(textPane);
        return textPane;
    }

    protected void initStylesForTextPane(JTextPane textPane) {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                                        getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = textPane.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = textPane.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);

        s = textPane.addStyle("bold", regular);
        StyleConstants.setBold(s, true);

        s = textPane.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);

        s = textPane.addStyle("large", regular);
        StyleConstants.setFontSize(s, 16);
    }

    public LinkedList generateCode(SimpleNode n)
    {
        if (n==null) return null;
        LinkedList code = new LinkedList();
        n.generateCode(code);
        Iterator it = code.iterator();
        for(int addr = 0; it.hasNext(); addr++)
        {
            MKInstruction inst = (MKInstruction)(it.next());
            inst.setAddress(addr);
        }
        return code;
    }

    public void dumpSymbolTable(MKSymbolTable st)
    {
        if (DEBUG_XML_GENERATION) {
            System.out.println("*** SYMBOL TABLE ***");
            Iterator it = st.symbols();
            while (it.hasNext())
            {
                dumpSymbol(it.next(), "");
            }
            System.out.println("--- END ---");
        }
    }

    public void dumpContext(MKContext st, String indent)
    {
        Iterator it = st.symbols();
        while (it.hasNext())
        {
            dumpSymbol(it.next(), indent);
        }
    }

    public void dumpSymbol(Object s, String indent)
    {
        MKSymbol sym = (MKSymbol)s;
        if (sym==null) return;
        System.out.print(indent+
                         sym.getQualifiedName()+"\t"+
                         sym.getType()+"\t"+
                         sym.getValue());
        if (sym.getType() == MKContext.contextType) {
            MKContext symCtx = (MKContext)(sym.getValue());
            if (symCtx.getRef() != null) {
                System.out.println("\t#"+symCtx.getRef().getAddress());
            } else {
                System.out.println("\t*");
            }
            dumpContext(symCtx, indent+"  ");
        } else {
            System.out.println();
        }
    }

    public void dumpCodeArray(LinkedList code)
    {
        if (code==null) return;
        if (DEBUG_XML_GENERATION) {
            System.out.println("*** GENERATED CODE ***");
            Iterator it = code.iterator();
            while (it.hasNext())
            {
                MKInstruction inst = (MKInstruction)(it.next());
                System.out.println(inst);
            }
            System.out.println("--- END ---");
        }
    }

    public void updateCodeAddresses(LinkedList code)
    {
        int addr=0;
        if (code==null) return;
        Iterator it = code.iterator();
        while (it.hasNext())
        {
            MKInstruction inst = (MKInstruction)(it.next());
            if (inst != null) {
                inst.setAddress(addr++);
                inst.setLexicalHook(null);
            } else {
                System.out.println("Null instruction in code list");
            }
        }
    }

    public void dumpXmlForList(PrintWriter out,
                               MKInstruction cur, 
                               Iterator cit)
    {
        out.println("<list inst=\""+cur.getAddress()+"\">");
        if (DEBUG_XML_GENERATION) {
            System.out.println("<list inst=\""+cur.getAddress()+"\">");
        }
        // check the instruction for number of elements.
        // if the list is empty, don't fetch the next element!
        Integer lci = (Integer)cur.getArgument();
        if (lci.intValue() != 0) {
            // parse all of the elements here
            cur = (MKInstruction)(cit.next());
            if (cur.getOpcode() == MKInstruction.MKIN_LISTELM) {
                int elmIdx=0;
                do {
                    out.println("<listelement index=\""+elmIdx+"\" inst=\""+cur.getAddress()+"\">");
                    if (DEBUG_XML_GENERATION) {
                        System.out.println("<listelement index=\""+elmIdx+"\" inst=\""+cur.getAddress()+"\">");
                    }
                    cur = dumpXmlForCode(out, cit);
                    out.println("</listelement>");
                    if (DEBUG_XML_GENERATION) {
                        System.out.println("</listelement>");
                    }
                    elmIdx++;
                } while ((cur.getOpcode() != MKInstruction.MKIN_LISTCDR) &&
                         (cur.getOpcode() != MKInstruction.MKIN_ENDLIST));
                if (cur.getOpcode() == MKInstruction.MKIN_LISTCDR) {
                    out.println("<listtail inst=\""+cur.getAddress()+"\">");
                    if (DEBUG_XML_GENERATION) {
                        System.out.println("<listtail inst=\""+cur.getAddress()+"\">");
                    }
                    cur = dumpXmlForCode(out, cit);
                    out.println("</listtail>");
                    if (DEBUG_XML_GENERATION) {
                        System.out.println("</listtail>");
                    }
                }
            }
        }
        out.println("</list>");
        if (DEBUG_XML_GENERATION) {
            System.out.println("</list>");
        }
    }

    private int dumpXmlForCallInstance = 0;
    public void dumpXmlForCall(PrintWriter out, 
                               MKInstruction cur, 
                               Iterator cit)
    {
        int thisInstance = dumpXmlForCallInstance;
        dumpXmlForCallInstance++;
        if (DEBUG_XML_GENERATION) {
            System.out.println("dumpXmlForCall("+thisInstance+")");
        }
        out.println("<call inst=\""+cur.getAddress()+"\">");
        if (DEBUG_XML_GENERATION) {
            System.out.println("<call inst=\""+cur.getAddress()+"\">");
        }
        cur = (MKInstruction)(cit.next());
        // parse all of the arguments here
        if (cur.getOpcode() == MKInstruction.MKIN_ARG) {
            int argIdx=0;
            do {
                out.println("<argument index=\""+argIdx+"\" inst=\""+cur.getAddress()+"\">");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<argument index=\""+argIdx+"\" inst=\""+cur.getAddress()+"\">");
                }
                cur = dumpXmlForCode(out, cit);
                if (cur==null) {
                    if (DEBUG_XML_GENERATION) {
                        System.out.println("dumpXmlForCode() returned null to dumpXmlForCall("+thisInstance+")");
                    } else {
                        throw new NullPointerException("dumpXmlForCode returned null: endargs expected");
                    }
                    break;
                }
                out.println("</argument>");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("</argument>");
                }
                argIdx++;
            } while (cur.getOpcode() != MKInstruction.MKIN_ENDARGS);
        } else {
            // if no ARG, must process this instruction before it gets
            // dropped on the floor.
            cur = dumpXmlForInstruction(out, cit, cur);
        }
        // generate XML for the operator/method name
        cur = dumpXmlForCode(out, cit);
        // now terminate the call
        if (cur==null) {
            if (DEBUG_XML_GENERATION) {
                System.out.println("dumpXmlForCall("+thisInstance+"): expected op/method on empty iterator");
            } else {
                throw new NullPointerException("dumpXmlForCode returned null: op/method expected");
            }
        } else switch (cur.getOpcode()) {
            case MKInstruction.MKIN_OP:
                if (DEBUG_XML_GENERATION) {
                    System.out.println("call OP:"+cur);
                }
                out.println("<operator inst=\""+cur.getAddress()+"\"/>");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<operator inst=\""+cur.getAddress()+"\"/>");
                }
                break;
            case MKInstruction.MKIN_METHOD:
                if (DEBUG_XML_GENERATION) {
                    System.out.println("call METHOD:"+cur);
                }
                out.println("<method inst=\""+cur.getAddress()+"\"/>");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<method inst=\""+cur.getAddress()+"\"/>");
                }
                break;
            default:
                System.out.println("ERROR:"+cur);
        }
        out.println("</call>");
        if (DEBUG_XML_GENERATION) {
            System.out.println("</call>");
        }
        if (DEBUG_XML_GENERATION) {
            System.out.println("-- leaving dumpXmlForCall("+thisInstance+")");
        }
    }

    private MKInstruction dumpXmlForInstruction(PrintWriter out,
                                                Iterator cit,
                                                MKInstruction cin)
    {
        if (cin==null) return cin;
        if (startedName) {
            switch (cin.getOpcode()) {
                case MKInstruction.MKIN_FMNAME:
                case MKInstruction.MKIN_FMINST:
                    break;
                default:
                    while (attrCount > 0) {
                        out.println("</attr>");
                        if (DEBUG_XML_GENERATION) {
                            System.out.println("</attr>");
                        }
                        attrCount--;
                    }
                    out.println("</name>");
                    if (DEBUG_XML_GENERATION) {
                        System.out.println("</name>");
                    }
                    startedName = false;
            }
        }
        switch (cin.getOpcode()) {
            case MKInstruction.MKIN_BKTYPE:
                out.println("<block type=\""+
                            normalize((String)cin.getArgument())+
                            "\" inst=\""+cin.getAddress()+"\">");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<block type=\""+
                                       normalize((String)cin.getArgument())+
                                       "\" inst=\""+cin.getAddress()+"\">");
                }
            case MKInstruction.MKIN_BKSTART:
                break;
            case MKInstruction.MKIN_BKEND:
                out.println("</block>");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("</block>");
                }
                break;
            case MKInstruction.MKIN_STMT:
                out.println("<statement type=\""+
                            normalize((String)cin.getArgument())+
                            "\" embedded=\"false\" inst=\""+cin.getAddress()+"\">");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<statement type=\""+
                                       normalize((String)cin.getArgument())+
                                       "\" embedded=\"false\" inst=\""+cin.getAddress()+"\">");
                }
                break;
            case MKInstruction.MKIN_STBOT:
                out.println("</statement>");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("</statement>");
                }
                break;
            case MKInstruction.MKIN_STTOP:
                break;
            case MKInstruction.MKIN_PUSH:
                String ptype = null;
                if (cin.getArgument() instanceof String) ptype="string";
                if (cin.getArgument() instanceof Integer) ptype="integer";
                if (cin.getArgument() instanceof Double) ptype="double";
                if (cin.getArgument() instanceof Character) ptype="char";
                String pvalue = normalize(cin.getArgument().toString());
                out.println("<constant type=\""+ptype+
                            "\" value = \""+pvalue+"\" inst=\""+cin.getAddress()+"\" />");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<constant type=\""+ptype+
                                       "\" value = \""+pvalue+"\" inst=\""+cin.getAddress()+"\" />");
                }
                break;
            case MKInstruction.MKIN_ARITY:
                dumpXmlForCall(out, cin, cit);
                break;
            case MKInstruction.MKIN_ARG:
                return cin;
            case MKInstruction.MKIN_ENDARGS:
                return cin;
            case MKInstruction.MKIN_METHOD:
                return cin;
            case MKInstruction.MKIN_FCNAME:
                startedName = true;
                out.println("<name type=\"classByName\" value=\""+
                            normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<name type=\"classByName\" value=\""+
                                       normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                }
                break;
            case MKInstruction.MKIN_FCINST:
                startedName = true;
                out.println("<name type=\"classByInstance\" value=\""+
                            normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<name type=\"classByInstance\" value=\""+
                                       normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                }
                break;
            case MKInstruction.MKIN_FVNAME:
                startedName = true;
                out.println("<name type=\"valueByName\" value=\""+
                            normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<name type=\"valueByName\" value=\""+
                                       normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                }
                break;
            case MKInstruction.MKIN_FVINST:
                startedName = true;
                out.println("<name type=\"valueByInstance\" value=\""+
                            normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<name type=\"valueByInstance\" value=\""+
                                       normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                }
                break;
            case MKInstruction.MKIN_FMNAME:
                out.println("<attr type=\"byName\" value=\""+
                            normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<attr type=\"byName\" value=\""+
                                       normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                }
                attrCount++;
                break;
            case MKInstruction.MKIN_FMINST:
                out.println("<attr type=\"byInstance\" value=\""+
                            normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<attr type=\"byInstance\" value=\""+
                                       normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\">");
                }
                attrCount++;
                break;
            case MKInstruction.MKIN_FONAME:
                out.println("<name type=\"operator\" value=\""+
                            normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\" />");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<name type=\"operator\" value=\""+
                                       normalize((String)cin.getArgument())+"\" inst=\""+cin.getAddress()+"\" />");
                }
                break;
            case MKInstruction.MKIN_OP:
                return cin;
            case MKInstruction.MKIN_LIST:
                dumpXmlForList(out, cin, cit);
                break;
            case MKInstruction.MKIN_LISTELM:
                return cin;
            case MKInstruction.MKIN_LISTCDR:
                return cin;
            case MKInstruction.MKIN_ENDLIST:
                return cin;
            case MKInstruction.MKIN_HALT:
                out.println("</halt>");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("</halt>");
                }
                break;
            case MKInstruction.MKIN_ESTMT:
                out.println("<statement type=\""+
                            cin.getArgument()+
                            "\" embedded=\"true\" inst=\""+cin.getAddress()+"\">");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("<statement type=\""+
                                       cin.getArgument()+
                                       "\" embedded=\"true\" inst=\""+cin.getAddress()+"\">");
                }
                break;
            case MKInstruction.MKIN_ESTBOT:
                out.println("</statement>");
                if (DEBUG_XML_GENERATION) {
                    System.out.println("</statement>");
                }
                break;
            case MKInstruction.MKIN_ESTTOP:
                break;
            default: // output nothing for this code
                System.out.println("Unrecognized instruction "+cin);
                break;
        }
        return cin;
    }

    private boolean startedName = false;
    private int attrCount = 0;
    private int dumpXmlForCodeInstance = 0;
    public MKInstruction dumpXmlForCode(PrintWriter out, Iterator cit)
    {
        int thisInstance = dumpXmlForCodeInstance;
        dumpXmlForCodeInstance++;
        if (DEBUG_XML_GENERATION) {
            System.out.println("dumpXmlForCode("+thisInstance+"): "+cit.hasNext());
        }
        MKInstruction cin = null;
        //startedName = false;
        while (cit.hasNext()) {
            cin = (MKInstruction)(cit.next());
            if (DEBUG_XML_GENERATION) {
                System.out.println("** "+cin);
            }
            cin = dumpXmlForInstruction(out, cit, cin);
            // some instructions cause us to pop out of this routine.
            // check for them here.
            if (cin!=null) {
                switch (cin.getOpcode()) {
                    case MKInstruction.MKIN_ARG:
                        if (DEBUG_XML_GENERATION) {
                            System.out.println("popping "+thisInstance+" at ARG");
                        }
                        return cin;
                    case MKInstruction.MKIN_ENDARGS:
                        if (DEBUG_XML_GENERATION) {
                            System.out.println("popping "+thisInstance+" at ENDARGS");
                        }
                        return cin;
                    case MKInstruction.MKIN_METHOD:
                        if (DEBUG_XML_GENERATION) {
                            System.out.println("popping "+thisInstance+" at METHOD");
                        }
                        return cin;
                    case MKInstruction.MKIN_OP:
                        if (DEBUG_XML_GENERATION) {
                            System.out.println("popping "+thisInstance+" at OP");
                        }
                        return cin;
                    case MKInstruction.MKIN_LISTELM:
                        if (DEBUG_XML_GENERATION) {
                            System.out.println("popping "+thisInstance+" at LISTELM");
                        }
                        return cin;
                    case MKInstruction.MKIN_LISTCDR:
                        if (DEBUG_XML_GENERATION) {
                            System.out.println("popping "+thisInstance+" at LISTCDR");
                        }
                        return cin;
                    case MKInstruction.MKIN_ENDLIST:
                        if (DEBUG_XML_GENERATION) {
                            System.out.println("popping "+thisInstance+" at ENDLIST");
                        }
                        return cin;
                    default: // output nothing for this code
                        break;
                }
            }
        }
        if (DEBUG_XML_GENERATION) {
            System.out.println("Exiting dumpXmlForCode("+thisInstance+")");
        }
        return cin;
    }
        
    public void dumpXmlForCode(PrintWriter out, LinkedList ocode)
    {
        if (ocode==null) return;
        out.println("<?xml version=\"1.0\" ?>");
        out.println("<!DOCTYPE code SYSTEM \"mk_langspec_030602.dtd\">");
        out.println("<code");
        out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        out.println("  xsi:noNamespaceSchemaLocation=\"mk_langspec_030602.xsd\">");
        if (scriptTemplate != null) {
            out.println("<template>");
            out.println("<templateName>"+scriptTemplate.getName()+
                        "</templateName>");
            out.println("<templateSource>"+scriptTemplate.getSource()+
                        "</templateSource>");
            out.println("</template>");
        }
        if (DEBUG_XML_GENERATION) {
            System.out.println("<?xml version=\"1.0\" ?>");
            System.out.println("<!DOCTYPE code SYSTEM \"mk_langspec_030602.dtd\">");
            System.out.println("<code");
            System.out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
            System.out.println("  xsi:noNamespaceSchemaLocation=\"mk_langspec_030602.xsd\">");
            if (scriptTemplate != null) {
                System.out.println("<template>");
                System.out.println("<templateName>"+scriptTemplate.getName()+
                                   "</templateName>");
                System.out.println("<templateSource>"+scriptTemplate.getSource()+
                                   "</templateSource>");
                System.out.println("</template>");
            }
        }
        startedName = false;
        dumpXmlForCode(out, ocode.iterator());
        out.println("</code>");
        if (DEBUG_XML_GENERATION) {
            System.out.println("</code>");
        }
    }

    /**
     * Normalizes the given string.
     * @param s The string to normalize.
     * @return The normalized string.
     */
    protected String normalize(String s) {
        StringBuffer str = new StringBuffer();
        int len = (s != null) ? s.length() : 0;
        for ( int i = 0; i < len; i++ ) {
            char ch = s.charAt(i);
            switch ( ch ) {
                case '<': {
                    str.append("&lt;");
                    break;
                }
                case '>': {
                    str.append("&gt;");
                    break;
                }
                case '&': {
                    str.append("&amp;");
                    break;
                }
                case '"': {
                    str.append("&quot;");
                    break;
                }
                default: {
                    str.append(ch);
                }
            }
        }
        return (str.toString());
    }

    public static void main(String[] args) {
        JFrame frame = new ScriptEditor();

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.pack();
        frame.setVisible(true);
    }
}

