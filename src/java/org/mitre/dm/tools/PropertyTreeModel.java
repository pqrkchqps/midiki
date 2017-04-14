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

import java.util.Date;
import java.util.Iterator;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * PropertyTreeModel is a TreeTableModel representing a hierarchical file 
 * system. Nodes in the PropertyTreeModel are FileNodes which, when they 
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

public class PropertyTreeModel extends AbstractTreeTableModel 
                             implements TreeTableModel {

    // Names of the columns.
    static protected String[]  cNames = {"Name", "Level"};

    // Types of the columns.
    static protected Class[]  cTypes = {TreeTableModel.class, Level.class};

    protected PropertyTree localNode;

    public PropertyTreeModel() {
        super(null); 
    }

    public PropertyTreeModel(PropertyTree prop) { 
        super(prop);
        localNode = prop;
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
        PropertyTree loggerNode = ((PropertyTree)node); 
        if ((childStorage == null) ||
            (childStorage.length < loggerNode.childCount())) {
            childStorage = new Object[loggerNode.childCount()];
        }
        Iterator ci = loggerNode.children();
        for (int i=0; ci.hasNext(); i++) {
            childStorage[i] = ci.next();
        }
        return childStorage;
    }

    //
    // The TreeModel interface
    //

    public int getChildCount(Object node) { 
        PropertyTree loggerNode = ((PropertyTree)node); 
        return loggerNode.childCount();
    }

    public Object getChild(Object node, int i) { 
        return getChildren(node)[i]; 
    }

    public boolean isLeaf(Object node) {
        return ((PropertyTree)node).isLeaf();
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

    public Object getPathValueAt(TreePath treePath, int column) {
        Object node = treePath.getLastPathComponent();
        return getValueAt(node, column);
    }

    public Object getValueAt(Object node, int column) {
        Logger logger = getLogger(node); 
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
                        if (value==null)
                            return super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
                        else
                            return super.getTreeCellRendererComponent(tree,
                                                                  ((PropertyTree)value).name(),selected,expanded,leaf,row,hasFocus);
                    }
                };
    }

    public String toString()
    {
        return ""+localNode;
    }
}
