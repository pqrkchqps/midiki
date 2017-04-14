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

import org.mitre.midiki.state.*;

import java.util.*;

/**
 * Implements a structure similar to a <code>class</code>,
 * but with an as-yet-unspecified type system. Many systems
 * for computational linguistics use some form of feature
 * structure, based on a list of named attributes with types
 * and values. Our implementation extends feature structures
 * with method signatures, providing declarative cell definitions
 * compatible with Trindikit, but does not yet specify a type
 * inheritance system and stores attribute values elsewhere.
 * This is likely to be enhanced in future releases of Midiki.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class ContractImpl implements Contract
{


    private String myName;
    private LinkedList myAttributes;
    private LinkedList myMethods;
    private LinkedList myQueries;

    public ContractImpl(String name)
    {
        myName = name;
        myAttributes = new LinkedList();
        myMethods = new LinkedList();
        myQueries = new LinkedList();
    }

    public String toString()
    {
        String name = myName;
        String attrs = null;
        Iterator it = attributes();
        while (it.hasNext()) {
            if (attrs == null) {
                attrs = "[";
            } else {
                attrs = attrs + ",";
            }
            attrs = attrs+it.next();
        }
        attrs = attrs + "]";
        String queries = null;
        it = queries();
        while (it.hasNext()) {
            if (queries == null) {
                queries = "[";
            } else {
                queries = queries + ",";
            }
            queries = queries+it.next();
        }
        queries = queries + "]";
        String methods = null;
        it = methods();
        while (it.hasNext()) {
            if (methods == null) {
                methods = "[";
            } else {
                methods = methods + ",";
            }
            methods = methods+it.next();
        }
        methods = methods + "]";
        return name + "{\nAttr="+attrs+"\nQueries="+queries+"\nMethods="+methods+"}";
    }

    public String name()
    {
        return myName;
    }

    public boolean addAttribute(AttributeImpl a)
    {
        return myAttributes.add(a);
    }

    public boolean addMethod(MethodImpl a)
    {
        return myMethods.add(a);
    }

    public boolean addQuery(QueryImpl a)
    {
        return myQueries.add(a);
    }

    /**
     * Returns an <code>Iterator</code> over the attributes.
     *
     * @return an <code>Iterator</code> of <code>Attribute</code>
     */
    public Iterator attributes()
    {
        return myAttributes.iterator();
    }
    /**
     * Returns an <code>Iterator</code> over the queries.
     *
     * @return a <code>Iterator</code> of <code>Query</code>
     */
    public Iterator queries()
    {
        return myQueries.iterator();
    }
    /**
     * Returns an <code>Iterator</code> over the queries.
     *
     * @return a <code>Iterator</code> of <code>Method</code>
     */
    public Iterator methods()
    {
        return myMethods.iterator();
    }
    /**
     * Return the <code>Method</code> with the specified name,
     * or null if it doesn't exist.
     *
     * @param name a <code>String</code> value
     * @return an <code>Method</code> value
     */
    public Method method(String name)
    {
        Iterator it = methods();
        while (it.hasNext()) {
            Method m = (Method)it.next();
            if (m.name().equals(name)) return m;
        }
        return null;
    }
    /**
     * Return the <code>Query</code> with the specified name,
     * or null if it doesn't exist.
     *
     * @param name a <code>String</code> value
     * @return an <code>Query</code> value
     */
    public Query query(String name)
    {
        Iterator it = queries();
        while (it.hasNext()) {
            Query m = (Query)it.next();
            if (m.name().equals(name)) return m;
        }
        return null;
    }
    /**
     * Return the <code>Attribute</code> with the specified name,
     * or null if it doesn't exist.
     *
     * @param name a <code>String</code> value
     * @return an <code>Attribute</code> value
     */
    public Attribute attribute(String name)
    {
        Iterator it = attributes();
        while (it.hasNext()) {
            Attribute m = (Attribute)it.next();
            if (m.name().equals(name)) return m;
        }
        return null;
    }
}
