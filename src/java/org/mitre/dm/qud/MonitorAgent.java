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
package org.mitre.dm.qud;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;
import org.mitre.midiki.tools.*;

import org.mitre.dm.*;
import org.mitre.dm.tools.*;
import org.mitre.dm.qud.conditions.*;

import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.Dimension;

/**
 * Implements Midiki-centric viewing of logged events.
 * Has an InfoState, but does not require one.
 * Important processing of the events should be done in the
 * log event handler itself; this agent should provide
 * display and configuration options, but should not be
 * required for normal operations.
 * 
 */
 
public class MonitorAgent implements Agent, Observer
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.MonitorAgent");
    private LogEventHandler logEventHandler;
    private boolean traceInternalEvents = true;

    private Logger discourseLogger = null;
    DialogueSystem dialogueSystem = null;
    public void attachTo(DialogueSystem system)
    {
        dialogueSystem = system;
        discourseLogger = system.getLogger();
    }
    /*
     * Tabular view of logged events
     */
        // when the log changes, generally by publishing a new event,
        // we will want to fire an event on the table model.
        // e.g., fireTableRowsInserted(firstNew, lastNew);
        // set up and display the tabular view of logged events
    protected AbstractTableModel logEventTableModel =
        new AbstractTableModel() {
                String[] columnNames = {"Time",
                                        "Class",
                                        "Method",
                                        "Level",
                                        "Message",
                                        "Parameters"};
                public int getRowCount() {
                    return logEventHandler.el.events.size();
                }
                public int getColumnCount() {
                    return columnNames.length;
                }
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
                public Object getValueAt(int row, int col) {
                    LogRecord lr = (LogRecord)logEventHandler.el.events.get(row);
                    switch(col) {
                        case 0:
                            return new Long(lr.getMillis());
                            //break;
                        case 1:
                            return lr.getSourceClassName();
                            //break;
                        case 2:
                            return lr.getSourceMethodName();
                            //break;
                        case 3:
                            return lr.getLevel();
                            //break;
                        case 4:
                            return lr.getMessage();
                            //break;
                        case 5:
                            Object[] params = lr.getParameters();
                            if (params != null)
                                return Arrays.asList(params);
                            else
                                return params;
                            //break;
                        default:
                    }
                    return null;
                }
                public int findColumn(String columnName) {
                    for (int i=0; i<columnNames.length; i++) {
                        if (columnName.equalsIgnoreCase(columnNames[i])) {
                            return i;
                        }
                    }
                    return -1;
                }
                public String getColumnName(int column) {
                    if (column < 0) return "";
                    if (column >= columnNames.length) return "";
                    return columnNames[column];
                }
            };

    /**
     * Performs one-time <code>Agent</code> initialization.
     * This processing is likely to include initialization
     * of the <code>Contract</code>s and <code>Cell</code>s
     * for the <code>Agent</code>, as well as non-Midiki
     * initialization.
     *
     */
    public void init(Object config)
    {
        logger.info("MonitorAgent initialized.");
        logEventHandler = null;
    }
    /**
     * Release any resources which were created in <code>init</code>.
     * Following this routine, the agent will be terminated.
     *
     */
    public void destroy()
    {
    }
    /**
     * Connects the <code>Agent</code> to the provided information state.
     * In this routine, the <code>Agent</code> should register any
     * <code>Rule</code>s necessary for normal operation, and perform
     * any other <code>InfoState</code>-specific processing. 
     * This agent expects a LogEventHandler to be initialized for the
     * event tracing to work. Currently, this is ensured by the Executive
     * before the information state is constructed.
     *
     * @param infoState a compatible information state
     * @return <code>true</code> if connection succeeded
     */
    public boolean connect(InfoState infoState)
    {
        logger.info("MonitorAgent connected.");
        boolean haveParent = true;
        Logger myLogger = logger;
        while (haveParent) {
            Handler[] handlers = myLogger.getHandlers();
            if (handlers != null) {
                for (int i=0; i<handlers.length; i++) {
                    if (handlers[i] instanceof LogEventHandler) {
                        if (logEventHandler != null) {
                            logger.warning("Multiple LogEventHandlers!");
                        } else {
                            logEventHandler = (LogEventHandler)handlers[i];
                        }
                    }
                }
            } else {
                logger.warning("no registered handlers.");
            }
            Level myLevel = myLogger.getLevel();
            myLogger = myLogger.getParent();
            if (myLogger == null) haveParent = false;
        }
        // get a list of all of the existing loggers.
        PropertyTree loggerTree = new PropertyTree();
        Enumeration loggerEnum = LogManager.getLogManager().getLoggerNames();
        while (loggerEnum.hasMoreElements()) {
            String loggerName = (String)loggerEnum.nextElement();
            Logger loggerInstance =
                LogManager.getLogManager().getLogger(loggerName);
            loggerTree.addChild(new PropertyTree(loggerName, loggerInstance));
        }
        if (traceInternalEvents) {
            //Make sure we have predictable window decorations.
            JFrame.setDefaultLookAndFeelDecorated(true);

            // create the tabbed panel to contain all our log windows
            JTabbedPane tracePanel = new JTabbedPane();
            JFrame traceFrame = new JFrame("Midiki DME Trace");
            traceFrame.getContentPane().add(tracePanel);
            traceFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // display logger list as a tree table
            JTreeTable treeTable =
                new JTreeTable(new PropertyTreeModel(loggerTree));
            System.out.println("auto resize = "+treeTable.getAutoResizeMode());
            // turning off resizing makes table smaller than scrollpane,
            // and makes no difference to scrollpane resizing.
            treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            JScrollPane treeScrollPane = new JScrollPane(treeTable);
            tracePanel.addTab("Loggers", treeScrollPane);
            // register to be notified when log changes
            logEventHandler.el.addObserver(this);

            // create infoState monitor
            ClientMapModel clientMapModel = new ClientMapModel(logEventHandler);
            JTreeTable isTreeTable = new JTreeTable(clientMapModel);
            tracePanel.addTab("InfoState", new JScrollPane(isTreeTable));
            logEventHandler.el.addObserver(clientMapModel);

            // create data change event monitor
            DataChangeModel dataChangeModel = new DataChangeModel(logEventHandler);
            JTreeTable dcTreeTable = new JTreeTable(dataChangeModel);
            tracePanel.addTab("Data Change", new JScrollPane(dcTreeTable));
            logEventHandler.el.addObserver(dataChangeModel);

            // create the table of logged events
            JTable logEventTable = new JTable(logEventTableModel);
            JScrollPane scrollPane = new JScrollPane(logEventTable);
            tracePanel.addTab("Event Log", scrollPane);

            traceFrame.pack();
            traceFrame.show();
        }
        return true;
    }
    public void update(Observable o, Object arg)
    {
        try {
            logEventTableModel.fireTableRowsInserted(logEventHandler.el.events.size(),logEventHandler.el.events.size());
        } catch (NullPointerException npe) {
            // if the system has been shut down, sometimes this method will
            // get called after the handler is destroyed. Silently ignore
            // this exception.
        }
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
    /**
     * Get the value for the specified Midiki system property.
     *
     * @param key an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object getProperty(Object key)
    {
        return null;
    }
    /**
     * Set the value for the specified Midiki system property.
     *
     * @param key an <code>Object</code> value
     * @param value an <code>Object</code> value
     * @return previous value for the property
     */
    public Object putProperty(Object key, Object value)
    {
        return null;
    }
    /**
     * Get the system identifier for this <code>Agent</code>.
     * This is the name by which it is known to the system
     * as a whole, and should be unique.
     *
     * @return a <code>String</code> value
     */
    public String getId()
    {
        return getName();
    }
    /**
     * Get the name that this <code>Agent</code> calls itself.
     * A Midiki system might have several <code>Agent</code>s
     * with the same name, but each will have a unique id.
     *
     * @return a <code>String</code> value
     */
    public String getName()
    {
        return "monitor_agent";
    }
    /**
     * Get the set of <code>Contract</code>s this <code>Agent</code>
     * must find in its <code>InfoState</code>. There can be more,
     * but these must be there.
     *
     * @return a <code>Set</code> value
     */
    public Set getRequiredContracts()
    {
        return null;
    }
    /**
     * Get the <code>Set</code> of <code>Cell</code>s that this
     * <code>Agent</code> can provide to the <code>InfoState</code>.
     * The actual <code>InfoState</code> must include these.
     *
     * @return a <code>Set</code> value
     */
    public Set getProvidedCells()
    {
        return null;
    }
}
