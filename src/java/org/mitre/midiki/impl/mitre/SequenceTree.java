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
package org.mitre.midiki.impl.mitre;

import java.util.*;
import java.io.*;

/**
 * Provides a named property which may have a value,
 * descendant nodes, or both. Not as widely applicable as the
 * <code>PropertyTree</code> class.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class SequenceTree
{
    protected SequenceTree nodeParent;
    protected String nodeName;
    protected Object nodeValue;
    protected LinkedList nodeChildren;

    public String toString()
    {
        String result = name()+":";
        if (value() != null) result=result+value();
        if (!isLeaf()) {
            result = result + " [";
            Iterator it = children();
            while (it.hasNext()) {
                SequenceTree ptn = (SequenceTree)it.next();
                result = result + "\n" + ptn;
            }
            result = result + "]";
        }
        return result;
    }

    public SequenceTree()
    {
    }

    public SequenceTree(String name, Object value)
    {
        //System.out.println("adding child "+name+", value "+value);
        nodeName=name;
        nodeValue=value;
    }

    /**
     * Describe <code>name</code> method here.
     *
     */
    public String name()
    {
        return nodeName;
    }
    /**
     * Describe <code>value</code> method here.
     *
     */
    public Object value()
    {
        return nodeValue;
    }
    /**
     * Returns the parent of this node (the node claiming this node
     * as one of its children.)
     */
    public SequenceTree parent()
    {
        return nodeParent;
    }
    /**
     * Describe <code>setValue</code> method here.
     *
     */
    public Object setValue(Object value)
    {
        nodeValue=value;
        return nodeValue;
    }
    /**
     * Describe <code>children</code> method here.
     *
     */
    public Iterator children()
    {
        if (nodeChildren==null) return null;
        return nodeChildren.iterator();
    }

    public int childCount()
    {
        if (nodeChildren==null) return 0;
        return nodeChildren.size();
    }

    /**
     * Describe <code>isLeaf</code> method here.
     *
     */
    public boolean isLeaf()
    {
        if (nodeChildren==null) return true;
        if (nodeChildren.isEmpty()) return true;
        return false;
    }
    /**
     * Describe <code>addChild</code> method here.
     *
     */
    public boolean addChild(SequenceTree child)
    {
        if (nodeChildren==null) {
            nodeChildren = new LinkedList();
        }
        nodeChildren.add(child);
        child.nodeParent = this;
        return true;
    }
    /**
     * Describe <code>removeChild</code> method here.
     *
     */
    public boolean removeChild(String childName)
    {
        Iterator chit = children();
        if (chit==null) {
            return false;
        }
        while (chit.hasNext()) {
            SequenceTree seq = (SequenceTree)chit.next();
            if (seq.name().equals(childName)) {
                chit.remove();
                return true;
            }
        }
        return false;
    }

}
