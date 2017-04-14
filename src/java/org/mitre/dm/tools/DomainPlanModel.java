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

import org.mitre.dm.qud.domain.Plan;
import org.mitre.dm.qud.domain.PlanOperation;
import org.mitre.midiki.logic.*;

/**
 * DomainPlanModel is a TreeTableModel representing a set of task plans.
 * Each plan is a heirarchical collection of operations.
 * 
 * @version %I% %G%
 *
 * @author Philip Milne
 * @author Scott Violet
 */

import org.mitre.midiki.impl.mitre.*;
import java.util.*;
import java.util.logging.*;

public class DomainPlanModel extends AbstractTreeTableModel 
                             implements TreeTableModel {

    // Names of the columns.
    static protected String[]  cNames = {"Op", "Arguments", "Annotation"};

    // Types of the columns.
    static protected Class[]  cTypes = {TreeTableModel.class, Object.class, String.class};

    protected PropertyTree localNode;

    public DomainPlanModel() {
        super(new PropertyTree("task plans",null)); 
        localNode = (PropertyTree)getRoot();
    }

    //
    // Some convenience methods. 
    //

    protected void planToTree(PropertyTree tree, Collection plan) {
        int idx = 0;
        Iterator plit = plan.iterator();
        while (plit.hasNext()) {
            PlanOperation planop = (PlanOperation)plit.next();
            ArrayList al = new ArrayList();
            PropertyTree thisNode = new PropertyTree(""+idx, al);
            tree.addChild(thisNode);
            Object taskop = planop.getOperation();
            Object annotation = planop.getAnnotation();
            if (taskop instanceof Predicate) {
                al.add(((Predicate)taskop).functor());
                if (((Predicate)taskop).functor().equals("if_then")) {
                    al.add(null);
                    Iterator it = ((Predicate)taskop).arguments();
                    Object cond = it.next();
                    Object thenPart = it.next();
                    ArrayList althen = new ArrayList();
                    althen.add("if");
                    althen.add(cond);
                    althen.add(null);
                    PropertyTree pt = new PropertyTree("0",althen);
                    planToTree(pt, (Collection)thenPart);
                    thisNode.addChild(pt);
                } else if (((Predicate)taskop).functor().equals("if_then_else")) {
                    al.add(null);
                    Iterator it = ((Predicate)taskop).arguments();
                    Object cond = it.next();
                    Object thenPart = it.next();
                    Object elsePart = it.next();
                    ArrayList althen = new ArrayList();
                    althen.add("if");
                    althen.add(cond);
                    althen.add(null);
                    PropertyTree pt = new PropertyTree("0",althen);
                    planToTree(pt, (Collection)thenPart);
                    thisNode.addChild(pt);
                    ArrayList alelse = new ArrayList();
                    alelse.add("if not");
                    alelse.add(cond);
                    alelse.add(null);
                    PropertyTree pt2 = new PropertyTree("1",alelse);
                    planToTree(pt2, (Collection)elsePart);
                    thisNode.addChild(pt2);
                } else if (((Predicate)taskop).functor().equals("do_while")) {
                    al.add(null);
                    Iterator it = ((Predicate)taskop).arguments();
                    Object cond = it.next();
                    Object thenPart = it.next();
                    ArrayList althen = new ArrayList();
                    althen.add("while");
                    althen.add(cond);
                    althen.add(null);
                    PropertyTree pt = new PropertyTree("0",althen);
                    planToTree(pt, (Collection)thenPart);
                    thisNode.addChild(pt);
                } else if (((Predicate)taskop).functor().equals("do_until")) {
                    al.add(null);
                    Iterator it = ((Predicate)taskop).arguments();
                    Object cond = it.next();
                    Object thenPart = it.next();
                    ArrayList althen = new ArrayList();
                    althen.add("until");
                    althen.add(cond);
                    althen.add(null);
                    PropertyTree pt = new PropertyTree("0",althen);
                    planToTree(pt, (Collection)thenPart);
                    thisNode.addChild(pt);
                } else {
                    LinkedList ll = new LinkedList();
                    Iterator argit = ((Predicate)taskop).arguments();
                    while (argit.hasNext()) {
                        ll.add(argit.next());
                    }
                    al.add(ll);
                }
                al.add(annotation);
            } else {
                al.add(taskop);
                al.add(null);
                al.add(annotation);
            }
            idx++;
        }
    }
    
    public void put(Object task, Object plan, Bindings bindings)
    {
        task = Unify.getInstance().deref(task, bindings);
        plan = Unify.getInstance().deref(plan, bindings);
        // make a new property tree for this plan
        PropertyTree taskTree = new PropertyTree(task.toString(), "");
        planToTree(taskTree, ((Plan)plan).plan());
        localNode.addChild(taskTree);
    }
    protected Object getArrayList(Object node) {
        PropertyTree planStepNode = ((PropertyTree)node); 
        return planStepNode.value();
    }

    protected Object[] childStorage = null;
    protected Object[] getChildren(Object node) {
        if (node == null) return null;
        PropertyTree planStepNode = ((PropertyTree)node); 
        if ((childStorage == null) ||
            (childStorage.length < planStepNode.childCount())) {
            childStorage = new Object[planStepNode.childCount()];
        }
        Iterator ci = planStepNode.children();
        for (int i=0; ci.hasNext(); i++) {
            childStorage[i] = ci.next();
        }
        return childStorage;
    }

    //
    // The TreeModel interface
    //

    public int getChildCount(Object node) { 
        PropertyTree planStepNode = ((PropertyTree)node); 
        return planStepNode.childCount();
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
 
    public Object getPathValueAt(TreePath treePath, int column) {
        Object node = treePath.getLastPathComponent();
        return getValueAt(node, column);
    }

    public Object getValueAt(Object node, int column) {
        Object nodeValue = getArrayList(node);
        if (!(nodeValue instanceof ArrayList)) {
            if (column > 0) return null;
            return ((PropertyTree)node).name();
        }
        ArrayList al = (ArrayList)nodeValue; 
        try {
            switch(column) {
                case 0:
                    return al.get(0);
                case 1:
                    if (al==null) return null;
                    return al.get(1);
                case 2:
                    if (al==null) return null;
                    return al.get(2);
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
                        else {
                            PropertyTree node = (PropertyTree)value;
                            if (node.value() instanceof ArrayList) {
                                return super.getTreeCellRendererComponent(tree,
                                                                  ((ArrayList)node.value()).get(0),selected,expanded,leaf,row,hasFocus);
                                
                            } else {
                                return super.getTreeCellRendererComponent(tree,
                                                                  node.name(),selected,expanded,leaf,row,hasFocus);
                            }
                        }
                    }
                };
    }

    public String toString()
    {
        return ""+localNode;
    }
}
