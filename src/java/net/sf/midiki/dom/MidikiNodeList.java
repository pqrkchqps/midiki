/**
 * Implements the NodeList interface for Midiki datatypes.
 */
package net.sf.midiki.dom;

import org.w3c.dom.*;

/*
 * Copyright (c) 2004 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

import java.util.*;

/**
 * The <code>NodeList</code> interface provides the abstraction of an ordered 
 * collection of nodes, without defining or constraining how this collection 
 * is implemented. <code>NodeList</code> objects in the DOM are live.
 * <p>The items in the <code>NodeList</code> are accessible via an integral 
 * index, starting from 0.
 * <p>See also the <a href='http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public class MidikiNodeList implements org.w3c.dom.NodeList
{
    private List _list;
    public MidikiNodeList()
    {
    }

    /**
     * Returns the <code>index</code>th item in the collection. If 
     * <code>index</code> is greater than or equal to the number of nodes in 
     * the list, this returns <code>null</code>.
     * @param index Index into the collection.
     * @return The node at the <code>index</code>th position in the 
     *   <code>NodeList</code>, or <code>null</code> if that is not a valid 
     *   index.
     */
    public Node item(int index) {
        if (_list==null) return null;
        if (index < 0) return null;
        if (index >= _list.size()) return null;
        return (Node)_list.get(index);
    }

    /**
     * The number of nodes in the list. The range of valid child node indices 
     * is 0 to <code>length-1</code> inclusive.
     */
    public int getLength() {
        if (_list==null) return 0;
        return _list.size();
    }

    public MidikiNode getFirst() {
        return (MidikiNode)item(0);
    }

    public MidikiNode getLast() {
        return (MidikiNode)item(getLength()-1);
    }

    protected int find(MidikiNode ref) {
        if (_list == null) return -1;
        Iterator it = _list.iterator();
        for (int idx=0; it.hasNext(); idx++) {
            MidikiNode next = (MidikiNode)it.next();
            if (ref.equals(next)) return idx;
        }
        return -1;
    }
    
    public MidikiNode getPrevious(MidikiNode ref) {
        int idx = find(ref);
        if (idx<0) return null;
        return (MidikiNode)item(idx-1);
    }

    public MidikiNode getNext(MidikiNode ref) {
        int idx = find(ref);
        if (idx<0) return null;
        return (MidikiNode)item(idx+1);
    }

    public MidikiNode append(MidikiNode ref) {
        if (_list==null) _list = new ArrayList();
        _list.add(ref);
        return ref;
    }

    public MidikiNode insertBefore(MidikiNode node, MidikiNode ref) {
        int idx = find(ref);
        if (idx==-1) idx=0;
        if (_list==null) _list = new ArrayList();
        _list.add(idx, node);
        return node;
    }

    public MidikiNode insertAfter(MidikiNode node, MidikiNode ref) {
        int idx = find(ref)+1;
        if (_list==null) _list = new ArrayList();
        if (idx >= getLength()) append(node);
        else _list.add(idx, node);
        return node;
    }
}

