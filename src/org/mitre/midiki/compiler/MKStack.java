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
package org.mitre.midiki.compiler;

import java.util.*;

import org.mitre.midiki.compiler.parser.*;

/**
 * An <code>MKStack</code> provides a stack of value/type tuples
 * which the <code>MKLangspecMachine</code> executes against.
 * It also provides a stub for executing a statement against
 * the value of the stack when the statement is recognized.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class MKStack
{
    /*
     * Stack for use during compilation
     */
    static private final int MAX_STACK_SIZE = 20;
    private Object[] _vstack;
    private MKType[] _tstack;
    private int _stkPtr;

    /*
     * Symbol table for entry of contexts, etc.
     */
    private MKSymbolTable _symbols;
    private int _contextCount;

    public MKStack()
    {
        buildStack(MAX_STACK_SIZE);
        _symbols = new MKSymbolTable();
        _contextCount = 0;
    }

    private void buildStack(int size)
    {
        _vstack = new Object[size];
        _tstack = new MKType[size];
        reset();
    }

    public void reset()
    {
        for (int i=0; i<_vstack.length; i++) {
            _vstack[i] = null;
            _tstack[i] = null;
        }
        _stkPtr = 0;
    }

    public void push()
    {
        _stkPtr++;
    }

    public void push(Object v, MKType t)
    {
        push();
        setValue(v);
        setType(t);
    }

    public void pop()
    {
        _stkPtr--;
    }

    public MKContext openContext(MKInstruction contextRef)
    {
        MKContext ctx = _symbols.openContext("@"+_contextCount);
        _symbols.topContext().setRef(contextRef);
        _contextCount++;
        return ctx;
    }

    public void closeContext()
    {
        _symbols.closeContext();
    }

    public Object getValue() {return _vstack[_stkPtr-1];}
    public MKType getType() {return _tstack[_stkPtr-1];}
    public Object getValue(int ptr) {return _vstack[ptr];}
    public MKType getType(int ptr) {return _tstack[ptr];}
    public int getDepth() {return _stkPtr;}
    public MKSymbolTable getSymbolTable() {return _symbols;}

    public void setValue(Object v) {_vstack[_stkPtr-1]=v;}
    public void setType(MKType t) {_tstack[_stkPtr-1]=t;}

    public void recognize(String statementType)
    {
    }

    public void reportError(Exception e)
    {
    }

    static public MKType _nameType;
    static public MKType _designatorType;
    static public MKType _identifierType;
    static public MKType _variableType;
    static public MKType _expressionType;
    static public MKType _stringType;
    static public MKType _charType;
    static public MKType _integerType;
    static public MKType _realType;
    static public MKType _listType;
    static public MKType _functionType;
    static public MKType _statementType;
    static public MKType _blockType;
    static {
        _nameType = new MKType("$name");
        _designatorType = new MKType("$designator");
        _identifierType = new MKType("$identifier");
        _variableType = new MKType("$variable");
        _expressionType = new MKType("$expression");
        _stringType = new MKType("$string");
        _charType = new MKType("$char");
        _integerType = new MKType("$integer");
        _realType = new MKType("$real");
        _listType = new MKType("$list");
        _functionType = new MKType("$function");
        _statementType = new MKType("$statement");
        _blockType = new MKType("$block");
    }
}
