/****************************************************************************
 *
 * Copyright (C) 2004. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 * Consult the LICENSE file in the root of the distribution for terms and restrictions.
 *
 *       Release: 1.0
 *       Date: 24-August-2004
 *       Author: Carl Burke
 *
 *****************************************************************************
*
* Modified from sample code provided by Sun Microsystems, Inc.
* demonstrating the implementation of a table whose first column
* is a tree. This code may be shifted to a separate library in the
* future, to clearly distinguish between MITRE code and adaptations
* of Sun Microsystems code.
*
*****************************************************************************/
/*
 * %W% %E%
 *
 * Copyright 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer. 
 *   
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution. 
 *   
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.  
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,   
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS 
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

package org.mitre.dm.tools;

import org.mitre.dm.Executive;
import org.mitre.midiki.state.*;
import org.mitre.midiki.tools.*;

import org.mitre.midiki.impl.mitre.*;

import java.util.*;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * ClientMapModel is a TreeTableModel representing a hierarchical file 
 * system. Nodes in the ClientMapModel are FileNodes which, when they 
 * are directory nodes, cache their children to avoid repeatedly querying 
 * the real file system. 
 * 
 * @version %I% %G%
 *
 * @author Philip Milne
 * @author Scott Violet
 */

import org.mitre.midiki.impl.mitre.*;
import java.util.logging.*;

public class DataChangeModel extends AbstractTreeTableModel 
                             implements TreeTableModel, Observer {

    // Names of the columns.
    static protected String[]  cNames = {"Name", "Type", "Data"};

    // Types of the columns.
    static protected Class[]  cTypes = {TreeTableModel.class, String.class, Object.class};

    protected TreePath currentPath;
    protected SequenceTree localNode;
    protected LogEventHandler logEventHandler;

    public DataChangeModel() {
        super(new SequenceTree("Data Change Events",null));
        currentPath = new TreePath(root);
    }

    public DataChangeModel(LogEventHandler leh) { 
        this();
        logEventHandler = leh;
        // when we are initialized, we should scan the event list for
        // configuration information that was logged before we got here.
        // We will put that information into the SequenceTree.
        Iterator it = leh.el.events.iterator();
        while (it.hasNext()) {
            LogRecord lr = (LogRecord)it.next();
            update(leh.el, lr);
        }
    }

    public void update(Observable o, Object arg)
    {
        // the Observable is our log event handler.
        // the Object is the LogRecord being added.
        // This model only cares about some of these events.
        LogRecord lr = (LogRecord)arg;
        if (lr.getLoggerName().equals("org.mitre.midiki.agent.RuleBasedInfoListener")) {
            // add or seal top-level entry
            if (lr.getMessage().startsWith("ENTRY")) {
                localNode = new SequenceTree(lr.getSourceClassName(), "running");
                SequenceTree seqRoot = (SequenceTree)root;
                seqRoot.addChild(localNode);
                currentPath = new TreePath(root);
                currentPath = currentPath.pathByAddingChild(localNode);
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
            } else if (lr.getMessage().startsWith("RETURN")) {
                if (currentPath == null) {
                    currentPath = new TreePath(root);
                }
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.setValue("completed");
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
                return;
            }
        } else if (lr.getLoggerName().equals("org.mitre.midiki.agent.RuleSet")) {
            if (lr.getMessage().startsWith("ENTRY")) {
                SequenceTree newNode = new SequenceTree(lr.getSourceClassName(), "running");
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.addChild(newNode);
                currentPath = currentPath.pathByAddingChild(newNode);
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
            } else if (lr.getMessage().startsWith("succeeded")) {
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.setValue("succeeded");
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
                return;
            } else if (lr.getMessage().startsWith("failed")) {
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.setValue("failed");
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
                return;
            } else /* executing */ {
                return;
            }
        } else if (lr.getLoggerName().equals("org.mitre.midiki.agent.ExistsRule")) {
            if (lr.getMessage().startsWith("ENTRY")) {
                SequenceTree newNode = new SequenceTree(lr.getSourceClassName(), "running");
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.addChild(newNode);
                currentPath = currentPath.pathByAddingChild(newNode);
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
            } else if (lr.getMessage().startsWith("succeeded")) {
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.setValue("succeeded");
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
                return;
            } else if (lr.getMessage().startsWith("failed")) {
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.setValue("failed");
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
                return;
            } else /* executing */ {
                return;
            }
        } else if (lr.getLoggerName().equals("org.mitre.midiki.agent.ForAllRule")) {
            if (lr.getMessage().startsWith("ENTRY")) {
                SequenceTree newNode = new SequenceTree(lr.getSourceClassName(), "running");
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.addChild(newNode);
                currentPath = currentPath.pathByAddingChild(newNode);
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
            } else if (lr.getMessage().startsWith("succeeded")) {
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.setValue("succeeded");
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
                return;
            } else if (lr.getMessage().startsWith("failed")) {
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.setValue("failed");
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
                return;
            } else if (lr.getMessage().startsWith("backtracking")) {
                SequenceTree newNode =
                    new SequenceTree(lr.getSourceClassName(), lr.getMessage());
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.addChild(newNode);
                currentPath = currentPath.pathByAddingChild(newNode);
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
            } else /* executing */ {
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.setValue("running");
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
                return;
            }
            return;
        } else if (lr.getLoggerName().equals("org.mitre.midiki.agent.Condition")) {
            if (lr.getSourceClassName().equals("org.mitre.midiki.agent.Condition")) return;
            SequenceTree newNode = new SequenceTree(lr.getSourceClassName(), lr.getMessage());
            if (lr.getMessage().startsWith("passed")) {
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.addChild(newNode);
                currentPath = currentPath.pathByAddingChild(newNode);
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
            } else if (lr.getMessage().startsWith("failed to")) {
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.addChild(newNode);
                currentPath = currentPath.pathByAddingChild(newNode);
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
            } else if (lr.getMessage().startsWith("completed")) {
                currentPath = currentPath.getParentPath();
                return;
            } else if (lr.getMessage().startsWith("accepted")) {
                //currentPath = currentPath.getParentPath();
                return;
            } else if (lr.getMessage().startsWith("failed")) {
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.addChild(newNode);
                currentPath = currentPath.pathByAddingChild(newNode);
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
            } else if (lr.getMessage().startsWith("backtracking")) {
                localNode = (SequenceTree)currentPath.getLastPathComponent();
                localNode.addChild(newNode);
                currentPath = currentPath.pathByAddingChild(newNode);
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
                currentPath = currentPath.getParentPath();
            }
        }

    }

    //
    // Some convenience methods. 
    //

    protected Logger getLogger(Object node) {
        SequenceTree loggerNode = ((SequenceTree)node); 
        return (Logger)loggerNode.value();       
    }

    protected Object[] childStorage = null;
    protected Object[] getChildren(Object node) {
        if (node == null) return null;
        if (node instanceof SequenceTree) {
            SequenceTree loggerNode = ((SequenceTree)node); 
            if ((childStorage == null) ||
                (childStorage.length < loggerNode.childCount())) {
                childStorage = new Object[loggerNode.childCount()];
            }
            Iterator ci = loggerNode.children();
            for (int i=0; ci.hasNext(); i++) {
                childStorage[i] = ci.next();
            }
        }
        return childStorage;
    }

    //
    // The TreeModel interface
    //

    public int getChildCount(Object node) {
        if (node instanceof SequenceTree) {
            SequenceTree loggerNode = ((SequenceTree)node);
            return loggerNode.childCount();
        } else {
            return 0;
        }
    }

    public Object getChild(Object node, int i) { 
        return getChildren(node)[i]; 
    }

    public boolean isLeaf(Object node) {
        if (node instanceof SequenceTree) {
            SequenceTree propNode = (SequenceTree)node;
            return propNode.isLeaf();
        } else {
            return true;
        }
    }

    //
    //  The TreeTableNode interface. 
    //

    public int getColumnCount() {
        return cNames.length;
    }

    public String getColumnName(int column) {
        return cNames[column];
    }

    public Class getColumnClass(int column) {
        return cTypes[column];
    }
 
    protected Level getApplicableLevel(Logger l)
    {
        if (l.getLevel() != null) {
            return l.getLevel();
        } else {
            return l.getParent().getLevel();
        }
    }

    /**
     * Returns a value obtained by exploiting resources along the
     * path to the node. Not used in this model.
     */
    public Object getPathValueAt(TreePath node, int column) {
        if (node == null) return "node is null";
        return getValueAt(node.getLastPathComponent(), column);
    }

    protected SequenceTree mediatorRoot;
    public Object getValueAt(Object node, int column) {
        if (node instanceof SequenceTree) {
            SequenceTree propNode = (SequenceTree)node;
            switch(column) {
                case 0:
                    return propNode.name();
                case 1:
                    return propNode.value();
                case 2:
                    return null;
                default:
                    return null;
            }
        } else {
            return node.getClass().getName();
        }
    }

    public TreeCellRenderer getTreeCellRenderer()
    {
        return new DefaultTreeCellRenderer() {
                    public Component getTreeCellRendererComponent(JTree tree,
                                              Object value,
                                              boolean selected,
                                              boolean expanded,
                                              boolean leaf,
                                              int row,
                                              boolean hasFocus)
                    {
                        if (value instanceof SequenceTree)
                            return super.getTreeCellRendererComponent(tree,
                                                                  ((SequenceTree)value).name(),selected,expanded,leaf,row,hasFocus);
                        else
                            return null;
                    }
                };
    }

    public String toString()
    {
        return ""+localNode;
    }
}
