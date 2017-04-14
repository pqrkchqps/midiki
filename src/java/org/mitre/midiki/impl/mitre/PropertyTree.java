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
 * descendant nodes, or both.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class PropertyTree
{
    protected String nodeName;
    protected Object nodeValue;
    protected TreeMap nodeChildren;

    static protected String getPropertyName(String name)
    {
        if (name==null) return null;
        int dotIndex = name.indexOf('.');
        if (dotIndex < 0) {
            return name;
        } else {
            return name.substring(0, dotIndex);
        }
    }

    static protected String getPropertyResidue(String name)
    {
        if (name==null) return null;
        int dotIndex = name.indexOf('.');
        if (dotIndex < 0) {
            return null;
        } else {
            return name.substring(dotIndex+1);
        }
    }

    public PropertyTree find(String fqName)
    {
        if (fqName == null) return this;
        if (fqName.length() == 0) return this;
        String childName = getPropertyName(fqName);
        String residualName = getPropertyResidue(fqName);
        PropertyTree ch = child(childName);
        if (ch == null) return null;
        if (residualName == null) return ch;
        else return ch.find(residualName);
    }

    public String toString()
    {
        String result = name()+":";
        if (value() != null) result=result+value();
        if (!isLeaf()) {
            result = result + " [";
            Iterator it = children();
            while (it.hasNext()) {
                PropertyTree ptn = (PropertyTree)it.next();
                result = result + "\n" + ptn;
            }
            result = result + "]";
        }
        return result;
    }

    public PropertyTree()
    {
    }

    public PropertyTree(String name, Object value)
    {
        //System.out.println("adding child "+name+", value "+value);
        nodeName=getPropertyName(name);
        String residue = getPropertyResidue(name);
        if (residue==null) {
            nodeValue=value;
        } else {
            addChild(new PropertyTree(residue, value));
        }
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
     * Describe <code>setValue</code> method here.
     *
     */
    public Object setValue(Object value)
    {
        nodeValue=value;
        return nodeValue;
    }
    /**
     * Describe <code>child</code> method here.
     *
     */
    public PropertyTree child(String childName)
    {
        if (nodeChildren==null) return null;
        Object childNode = nodeChildren.get(childName);
        if (childNode ==null) return null;
        return (PropertyTree)childNode;
    }
    /**
     * Describe <code>children</code> method here.
     *
     */
    public Iterator children()
    {
        if (nodeChildren==null) return null;
        return nodeChildren.values().iterator();
    }

    public int childCount()
    {
        if (nodeChildren==null) return 0;
        return nodeChildren.values().size();
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
    public boolean addChild(PropertyTree child)
    {
        if (nodeChildren==null) {
            nodeChildren = new TreeMap();
            nodeChildren.put(child.name(), child);
            return true;
        }
        if (nodeChildren.containsKey(child.name())) {
            // merge child nodes
            PropertyTree existingChild = child(child.name());
            Iterator cit = child.children();
            if (cit==null) {
                // inserting leaf value at non-leaf node
                //System.out.println("Adding leaf for ..."+name()+"."+
                //                   child.name()+"="+child.value());
                existingChild.setValue(child.value());
                return true;
            }
            while (cit.hasNext()) {
                existingChild.addChild((PropertyTree)cit.next());
            }
            return true;
        }
        nodeChildren.put(child.name(), child);
        return true;
    }
    /**
     * Describe <code>removeChild</code> method here.
     *
     */
    public boolean removeChild(String childName)
    {
        if (nodeChildren==null) {
            return false;
        }
        return (nodeChildren.remove(childName) != null);
    }

    static public PropertyTree load(String fileName)
    {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            Properties props = new Properties();
            props.load(fis);
            PropertyTree root = new PropertyTree();
            Enumeration penum = props.propertyNames();
            while (penum.hasMoreElements()) {
                String propName = (String)penum.nextElement();
                root.addChild(new PropertyTree(propName, props.getProperty(propName)));
            }
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static public final void main(String[] args)
    {
        if (args.length==0) {
            System.out.println("Usage: java PropertyTree XXX.properties");
            return;
        }
        System.out.println(load(args[0]));
    }
}
