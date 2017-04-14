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
 * An <code>MKContext</code> describes a scope for
 * symbol binding. It may or may not have a name.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class MKContext implements Serializable
{
    private MKSymbol _name;
    private MKInstruction _ref;
    private MKContext _parent;
    private LinkedList _symbols;

    public MKSymbol getName() {return _name;}
    public MKInstruction getRef() {return _ref;}
    public MKContext getParent() {return _parent;}
    public void setName(MKSymbol n) {_name = n;}
    public void setRef(MKInstruction r) {_ref = r;}
    public void setParent(MKContext p) {_parent = p;}

    public MKContext()
    {
        _symbols = new LinkedList();
    }

    public MKContext(MKSymbol n, MKContext p, MKInstruction r)
    {
        this();
        setName(n);
        setRef(r);
        setParent(p);
    }

    public void bindSymbol(MKSymbol binding)
    {
        _symbols.add(binding);
    }

    public Iterator symbols()
    {
        return _symbols.iterator();
    }

    static public MKType contextType;
    static public MKType ctxtType;
    static {
        contextType = new MKType("$context");
        ctxtType = new MKType("$contextRef");
    }
}
