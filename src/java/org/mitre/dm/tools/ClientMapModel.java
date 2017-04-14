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

public class ClientMapModel extends AbstractTreeTableModel 
                             implements TreeTableModel, Observer {

    // Names of the columns.
    static protected String[]  cNames = {"Name", "Type", "Data"};

    // Types of the columns.
    static protected Class[]  cTypes = {TreeTableModel.class, Object.class, Object.class};

    protected PropertyTree localNode;
    protected LogEventHandler logEventHandler;

    public ClientMapModel() {
        super(new PropertyTree("Information State",null)); 
    }

    public ClientMapModel(LogEventHandler leh) { 
        this();
        logEventHandler = leh;
        // when we are initialized, we should scan the event list for
        // configuration information that was logged before we got here.
        // We will put that information into the PropertyTree.
        Iterator it = leh.el.events.iterator();
        while (it.hasNext()) {
            LogRecord lr = (LogRecord)it.next();
            update(leh.el, lr);
        }
    }

    protected void appendMapMembers(PropertyTree prop, Map map)
    {
        prop.setValue(map); // store map so we can access named contracts...
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry)it.next();
            Contract ctr = (Contract)me.getKey();
            prop.addChild(new PropertyTree(ctr.name(), new CellHistory((CellStore)me.getValue())));
        }
    }

    protected boolean haveMediators = false;
    protected String mediatorTag;

    public void update(Observable o, Object arg)
    {
        // the Observable is our log event handler.
        // the Object is the LogRecord being added.
        // This model only cares about some of these events.

        LogRecord lr = (LogRecord)arg;

        if (lr.getLoggerName().equals("org.mitre.dm.Executive")) {
            // we are processing configuration and/or IS stepping events
            if (lr.getMessage().equals("ClientMap")) {
                // load clients within the default Mediator branch
                if (haveMediators) {
                    Object[] path = new Object[2];
                    path[0] = root;
                    path[1] = ((PropertyTree)root).child(mediatorTag);
                    appendMapMembers((PropertyTree)path[1],
                                     (Map)lr.getParameters()[0]);
                    fireTreeStructureChanged(this, path, null, null);
                } else {
                    Object[] path = new Object[2];
                    path[0] = root;
                    path[1] = new PropertyTree("default", lr.getParameters()[0]);
                    appendMapMembers((PropertyTree)path[1],
                                     (Map)lr.getParameters()[0]);
                    ((PropertyTree)root).addChild((PropertyTree)path[1]);
                    fireTreeStructureChanged(this, path, null, null);
                }
            } else if (lr.getMessage().equals("MediatorInstance")) {
                // start a new Mediator tree branch
                haveMediators = true;
                Executive.MediatorInstance mi =
                    (Executive.MediatorInstance)lr.getParameters()[0];
                mediatorTag = mi.tag;
                ((PropertyTree)root).addChild(new PropertyTree(mi.tag, mi.clientMap));
                fireTreeStructureChanged(this, new Object[]{root}, null, null);
            }
        } else if (lr.getLoggerName().equals("org.mitre.midiki.impl.mitre.CellClient")) {
            // NOTE: CellInstance doesn't carry along the Mediator data,
            // so I don't have enough information to address it properly!
            // Solution: pass it as message text. That needs the mediator tag,
            // which is different than the agent name currently stored,
            // so more mods need to be made.
            //
            // This code assumes that changes are being applied in forward
            // direction.
            if (lr.getSourceMethodName().equals("rootInstance")) {
                CellInstance ci = (CellInstance)lr.getParameters()[0];
                String address = lr.getMessage()+"."+ci.instanceType.name();
                PropertyTree node = ((PropertyTree)root).find(address);
                if (node != null) {
                    CellHistory ch = (CellHistory)node.value();
                    ch.setRoot(ci.instanceId);
                    //ch.cellInstances.remove(ci.instanceId);
                    ch.registerInstance(ci);
                }
            } else if (lr.getSourceMethodName().equals("newInstance")) {
                CellInstance ci = (CellInstance)lr.getParameters()[0];
                String address = lr.getMessage()+"."+ci.instanceType.name();
                PropertyTree node = ((PropertyTree)root).find(address);
                if (node != null) {
                    CellHistory ch = (CellHistory)node.value();
                    //ch.cellInstances.remove(ci.instanceId);
                    ch.registerInstance(ci);
                }
            } else if (lr.getSourceMethodName().equals("changedCellData")) {
                CellInstance ci = (CellInstance)lr.getParameters()[0];
                String address = lr.getMessage()+"."+ci.instanceType.name();
                PropertyTree node = ((PropertyTree)root).find(address);
                if (node != null) {
                    CellHistory ch = (CellHistory)node.value();
                    ch.registerInstance(ci);
                }
            } else if (lr.getSourceMethodName().equals("recordChange")) {
                Object ciRef = lr.getParameters()[0];
                if (ciRef instanceof CellInstance) {
                CellInstance ci = (CellInstance)ciRef;
                String address = lr.getMessage()+"."+ci.instanceType.name();
                PropertyTree node = ((PropertyTree)root).find(address);
                if (node != null) {
                    CellHistory ch = (CellHistory)node.value();
                    ch.registerInstance(ci);
                } else {
                    System.out.println("Node not found: "+address);
                }
                } else {
                    System.out.println("ciRef not CellInstance: "+ciRef);
                }
            }
            fireTreeStructureChanged(this, new Object[]{root}, null, null);
        }

    }

    //
    // Some convenience methods. 
    //

    protected Logger getLogger(Object node) {
        PropertyTree loggerNode = ((PropertyTree)node); 
        return (Logger)loggerNode.value();       
    }

    protected Object[] childStorage = null;
    protected Object[] getChildren(Object node) {
        if (node == null) return null;
        if (node instanceof PropertyTree) {
            PropertyTree loggerNode = ((PropertyTree)node); 
            if (loggerNode.value() instanceof CellHistory)
                return getChildren(loggerNode.value());
            if ((childStorage == null) ||
                (childStorage.length < loggerNode.childCount())) {
                childStorage = new Object[loggerNode.childCount()];
            }
            Iterator ci = loggerNode.children();
            for (int i=0; ci.hasNext(); i++) {
                childStorage[i] = ci.next();
            }
        } else if (node instanceof CellHistory) {
            CellHistory clientNode = ((CellHistory)node);
            int childCount = 0;
            Iterator it = clientNode.contract().attributes();
            for (; it.hasNext(); childCount++) it.next();
            it = clientNode.contract().queries();
            for (; it.hasNext(); childCount++) it.next();
            it =clientNode.contract().methods();
            for (; it.hasNext(); childCount++) it.next();
            if ((childStorage == null) ||
                (childStorage.length < childCount)) {
                childStorage = new Object[childCount];
            }
            childCount = 0;
            it = clientNode.contract().attributes();
            for (; it.hasNext(); childCount++) childStorage[childCount] = it.next();
            it = clientNode.contract().queries();
            for (; it.hasNext(); childCount++) childStorage[childCount] = it.next();
            it =clientNode.contract().methods();
            for (; it.hasNext(); childCount++) childStorage[childCount] = it.next();
        } else if (node instanceof AttributeImpl) {
            AttributeImpl attrNode = (AttributeImpl)node;
            if (attrNode.type() instanceof Contract) {
                String contractName = ((Contract)attrNode.type()).name();
                PropertyTree cellNode = ((PropertyTree)root).find("default."+contractName);
                return getChildren(cellNode.value());
            } else {
                if (attrNode.type() instanceof Collection) {
                    return null;
                } else {
                    return null;
                }
            }
        } else if (node instanceof QueryImpl) {
            QueryImpl clientNode = ((QueryImpl)node);
            int childCount = 0;
            Iterator it = clientNode.parameters();
            for (; it.hasNext(); childCount++) it.next();
            childCount = 0;
            it = clientNode.parameters();
            for (; it.hasNext(); childCount++)
                childStorage[childCount] = it.next();
        } else if (node instanceof MethodImpl) {
            MethodImpl clientNode = ((MethodImpl)node);
            int childCount = 0;
            Iterator it = clientNode.parameters();
            for (; it.hasNext(); childCount++) it.next();
            childCount = 0;
            it = clientNode.parameters();
            for (; it.hasNext(); childCount++)
                childStorage[childCount] = it.next();
        }
        return childStorage;
    }

    //
    // The TreeModel interface
    //

    public int getChildCount(Object node) {
        if (node instanceof PropertyTree) {
            PropertyTree loggerNode = ((PropertyTree)node);
            if (loggerNode.value() instanceof CellHistory)
                return getChildCount(loggerNode.value());
            else
                return loggerNode.childCount();
        } else if (node instanceof CellHistory) {
            CellHistory clientNode = ((CellHistory)node);
            int childCount = 0;
            Iterator it = clientNode.contract().attributes();
            for (; it.hasNext(); childCount++) it.next();
            it = clientNode.contract().queries();
            for (; it.hasNext(); childCount++) it.next();
            it =clientNode.contract().methods();
            for (; it.hasNext(); childCount++) it.next();
            return childCount;
        } else if (node instanceof AttributeImpl) {
            // children depend on whether the current value is collection
            // or if type is a contract.
            AttributeImpl attrNode = (AttributeImpl)node;
            if (attrNode.type() instanceof Contract) {
                String contractName = ((Contract)attrNode.type()).name();
                PropertyTree cellNode = ((PropertyTree)root).find("default."+contractName);
                int cnt = getChildCount(cellNode.value());
                return cnt;
            } else {
                if (attrNode.type() instanceof Collection) {
                    return 0;
                } else {
                    return 0;
                }
            }
            //return 0;
        } else if (node instanceof QueryImpl) {
            QueryImpl clientNode = ((QueryImpl)node);
            int childCount = 0;
            Iterator it = clientNode.parameters();
            for (; it.hasNext(); childCount++) it.next();
            return childCount;
        } else if (node instanceof MethodImpl) {
            MethodImpl clientNode = ((MethodImpl)node);
            int childCount = 0;
            Iterator it = clientNode.parameters();
            for (; it.hasNext(); childCount++) it.next();
            return childCount;
        } else {
            return 0;
        }
    }

    public Object getChild(Object node, int i) { 
        return getChildren(node)[i]; 
    }

    public boolean isLeaf(Object node) {
        if (node instanceof PropertyTree) {
            PropertyTree propNode = (PropertyTree)node;
            if (propNode.value() instanceof CellHistory)
                return isLeaf(propNode.value());
            return propNode.isLeaf();
        } else if (node instanceof CellHistory) {
            return false;
        } else if (node instanceof AttributeImpl) {
            AttributeImpl attrNode = (AttributeImpl)node;
            if (attrNode.type() instanceof Contract) {
                // depends on whether there's data or not...
                return false;
            } else {
                return !(attrNode.type() instanceof Collection);
            }
        } else if (node instanceof QueryImpl) {
            return (getChildCount(node)==0);
        } else if (node instanceof MethodImpl) {
            return (getChildCount(node)==0);
        } else if (node instanceof ParameterImpl) {
            return true;
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

    protected PropertyTree mediatorRoot;
    protected CellHistory lastCell;
    protected Object loadPathValue(TreePath treePath, Object param)
    {
        if (treePath == null) {
            System.out.println("loadPathValue hit NULL");
            return null;
        }
        Object node = treePath.getLastPathComponent();
        if (node instanceof PropertyTree) {
            Object nodeValue = ((PropertyTree)node).value();
            // Data for an attribute always comes from the parent.
            if (nodeValue instanceof HashMap) {
                mediatorRoot = (PropertyTree)node;
                return null;
            }
            // If that parent is a CellHistory, we're at the root.
            if (nodeValue instanceof CellHistory) {
                loadPathValue(treePath.getParentPath(), null);
                CellHistory ch = (CellHistory)nodeValue;
                ch.loadInstanceValue(ch.getRoot());
                lastCell = ch;
                return ch.get((String)param);
            }
        }
        // Otherwise, it's another attribute, for which we get
        // the key from its parent, load that instance
        // into the CellHistory for that contract, and request
        // the value.
        AttributeImpl attr = (AttributeImpl)node;
        Object instanceId = loadPathValue(treePath.getParentPath(), attr.name());
        //if (attr.type() == null) {
        //    return instanceId;
        //}
        Object och = mediatorRoot.find(((Contract)attr.type()).name()).value();
        CellHistory ch = (CellHistory)och;
        ch.loadInstanceValue(instanceId);
        lastCell = ch;
        if (param == null) {
            return "<cannot display>";
        }
        return ch.get((String)param);
    }

    public Object getPathValueAt(TreePath treePath, int column) {
        Object terminalNode = treePath.getLastPathComponent();
        if (terminalNode instanceof AttributeImpl) {
            if (column == 2)
                return loadPathValue(treePath.getParentPath(),
                                     ((AttributeImpl)terminalNode).name());
            return null;
        } else {
            return getValueAt(terminalNode, column);
        }
    }

    public Object getValueAt(Object node, int column) {
        if (node instanceof PropertyTree) {
            PropertyTree propNode = (PropertyTree)node;
            switch(column) {
                case 0:
                    return propNode.name();
                case 1:
                    if (propNode.value() instanceof CellHistory) {
                        CellHistory clientNode = (CellHistory)propNode.value();
                        return clientNode.getRoot();
                    }
                    return null;
                default:
                    return null;
            }
        } else if (node instanceof CellHistory) {
            CellHistory clientNode = (CellHistory)node;
            switch(column) {
                case 0:
                    return clientNode.contract().name();
                case 1:
                    return clientNode.getRoot();
                default:
                    return null;
            }
        } else if (node instanceof CellStore) {
            CellStore clientNode = (CellStore)node;
            switch(column) {
                case 0:
                    return clientNode.contract().name();
                case 1:
                    return "cellStore";
                default:
                    return null;
            }
        } else if (node instanceof AttributeImpl) {
            AttributeImpl attrNode = (AttributeImpl)node;
            switch(column) {
                case 0:
                    return attrNode.name();
                case 1:
                    if (attrNode.type() instanceof Contract) {
                        return ((Contract)attrNode.type()).name();
                    } else if (attrNode.type() == null) {
                        return "<untyped>";
                    } else {
                        return ((Class)attrNode.type()).getName();
                    }
                case 2:
                default:
                    return null;
            }
        } else if (node instanceof QueryImpl) {
            QueryImpl queryNode = (QueryImpl)node;
            switch(column) {
                case 0:
                    return queryNode.name();
                case 1:
                    return "query";
                default:
                    return null;
            }
        } else if (node instanceof MethodImpl) {
            MethodImpl methodNode = (MethodImpl)node;
            switch(column) {
                case 0:
                    return methodNode.name();
                case 1:
                    return "method";
                default:
                    return null;
            }
        } else if (node instanceof ParameterImpl) {
            ParameterImpl parameterNode = (ParameterImpl)node;
            switch(column) {
                case 0:
                    return parameterNode.name();
                case 1:
                    return parameterNode.type();
                default:
                    return null;
            }
        } else {
            return null;
        }
        /*
        return node.getClass().getName();
        //Logger logger = getLogger(node); 
        try {
            switch(column) {
                case 0:
                    return ((PropertyTree)node).name();
                case 1:
                    if (logger==null) return null;
                    return getApplicableLevel(logger);
            }
        }
        catch  (SecurityException se) { }
        
        return null; 
        */
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
                        if (value instanceof PropertyTree)
                            return super.getTreeCellRendererComponent(tree,
                                                                  ((PropertyTree)value).name(),selected,expanded,leaf,row,hasFocus);
                        else if (value instanceof AttributeImpl)
                            return super.getTreeCellRendererComponent(tree,
                                                                  ((AttributeImpl)value).name(),selected,expanded,leaf,row,hasFocus);
                        else if (value instanceof QueryImpl)
                            return super.getTreeCellRendererComponent(tree,
                                                                  ((QueryImpl)value).name(),selected,expanded,leaf,row,hasFocus);
                        else if (value instanceof MethodImpl)
                            return super.getTreeCellRendererComponent(tree,
                                                                  ((MethodImpl)value).name(),selected,expanded,leaf,row,hasFocus);
                        else if (value instanceof ParameterImpl)
                            return super.getTreeCellRendererComponent(tree,
                                                                  ((ParameterImpl)value).name(),selected,expanded,leaf,row,hasFocus);
                        else
                            return super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
                    }
                };
    }

    public String toString()
    {
        return ""+localNode;
    }
}
