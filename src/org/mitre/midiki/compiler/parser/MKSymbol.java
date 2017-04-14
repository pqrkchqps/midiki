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
package org.mitre.midiki.compiler.parser;

import java.util.*;
import java.io.Serializable;

/**
 * An <code>MKSymbol</code> is a symbol table entry.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class MKSymbol implements Serializable
{
    /**
     * Unqualified name of this symbol.
     */
    private String _name;
    /**
     * The type of entity this name represents.
     */
    private MKType _type;
    /**
     * The value bound to this symbol, if any.
     */
    private Object _value;
    /**
     * The most recent symbol by this name, hidden
     * by this entry.
     */
    private MKSymbol _hidden;
    /**
     * The context in which this name is declared.
     */
    private MKContext _context;

    public String getName() {return _name;}
    public MKType getType() {return _type;}
    public Object getValue() {return _value;}
    public MKSymbol getHidden() {return _hidden;}
    public MKContext getContext() {return _context;}
    public void setName(String n) {_name = n;}
    public void setType(MKType t) {_type = t;}
    public void setValue(Object v) {_value = v;}
    public void setHidden(MKSymbol h) {_hidden = h;}
    public void setContext(MKContext c) {_context = c;}

    public String getQualifiedName()
    {
        String name = getName();
        MKContext scope = getContext();
        if (scope==null) return name;
        MKSymbol scopeName = scope.getName();
        if (scopeName == null) return "<anonymous>."+name;
        return scopeName.getQualifiedName()+"."+name;
    }

    public MKSymbol()
    {
    }

    public MKSymbol(String n, MKType t, Object v, MKContext c)
    {
        this();
        setName(n);
        setType(t);
        setValue(v);
        setContext(c);
    }
}
