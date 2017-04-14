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
import java.io.Serializable;

import org.mitre.midiki.compiler.parser.*;

/**
 * An <code>MKSymbolTable</code> holds all of the symbol
 * definitions for an MK program. It supports static lexical
 * scoping, represented as a stack of contexts.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class MKSymbolTable implements Serializable
{
    private Stack _contexts;
    private HashMap _symbols;

    public Iterator symbols()
    {
        return _symbols.values().iterator();
    }

    public MKSymbolTable()
    {
        _contexts = new Stack();
        MKSymbol globalRef = new MKSymbol("$global",
                                          MKContext.ctxtType,
                                          null,
                                          null);
        MKContext globalScope = new MKContext(globalRef, null, null);
        globalRef.setValue(globalScope);
        _contexts.push(globalScope);
        _symbols = new HashMap();
        _symbols.put(globalRef.getName(), globalRef);
        // add global type symbols to global scope
        MKSymbol stringSymbol =
            new MKSymbol("string", MKType.typeType, null, globalScope);
        MKSymbol charSymbol =
            new MKSymbol("char", MKType.typeType, null, globalScope);
        MKSymbol intSymbol =
            new MKSymbol("int", MKType.typeType, null, globalScope);
        MKSymbol floatSymbol =
            new MKSymbol("float", MKType.typeType, null, globalScope);
        MKSymbol booleanSymbol =
            new MKSymbol("boolean", MKType.typeType, null, globalScope);
        MKSymbol typeSymbol =
            new MKSymbol("type", MKType.typeType, null, globalScope);
        _symbols.put(stringSymbol.getName(), stringSymbol);
        _symbols.put(charSymbol.getName(), charSymbol);
        _symbols.put(intSymbol.getName(), intSymbol);
        _symbols.put(floatSymbol.getName(), floatSymbol);
        _symbols.put(booleanSymbol.getName(), booleanSymbol);
        _symbols.put(typeSymbol.getName(), typeSymbol);
    }

    public MKSymbol getSymbol(String name)
    {
        //System.out.println("Checking existence of symbol '"+name+"'");
        if (_symbols.containsKey(name)) {
            Object binding = _symbols.get(name);
            //System.out.println("Symbol '"+name+"' found: "+binding);
            if (!(binding instanceof MKSymbol)) return null;
            return (MKSymbol)binding;
        } else {
            //System.out.println("Symbol '"+name+"' not found!");
            return null;
        }
    }

    public MKSymbol bindSymbol(String name, MKType type, Object value)
    {
        MKContext tos = null;
        if (_contexts.empty()) {
            //System.out.println("bindSymbol: no context stack");
            return null;    // can't bind a symbol if there's no context
        } else {
            tos = (MKContext)(_contexts.peek());
        }
        MKSymbol prev = getSymbol(name);
        // don't bind the context if the name already exists locally
        if (prev != null)
            if (prev.getContext() == tos) {
                //System.out.println("bindSymbol: "+name+
                //                   " already bound in scope");
                return null;
            }
        MKSymbol binding = new MKSymbol(name, type, value, tos);
        binding.setHidden(prev);
        if (prev != null) {
            _symbols.remove(name);
        }
        _symbols.put(name, binding);
        tos.bindSymbol(binding);
        //System.out.println("Bound "+name+" type "+type+" value "+value);
        return binding;
    }

    public MKContext topContext()
    {
        MKContext tos = null;
        if (!_contexts.empty()) {
            tos = (MKContext)(_contexts.peek());
        }
        return tos;
    }

    public MKContext openContext(String name)
    {
        MKContext tos = topContext();
        // symbol may be bound, but not in this scope.
        MKSymbol newContextRef = 
            bindSymbol(name, MKContext.contextType, null);
        MKContext newContext = new MKContext(newContextRef, tos, null);
        if (newContextRef==null)
        {
            //System.out.println("openContext: "+name+" binding failed");
            return null;
        }
        newContextRef.setValue(newContext);
        // push the new context onto the stack.
        //System.out.println("Pushing context "+name);
        _contexts.push(newContext);
        return newContext;
    }

    public void closeContext()
    {
        if (_contexts.empty()) return;
        MKContext tos = (MKContext)(_contexts.pop());
        //System.out.println("Popping context "+tos.getName());
        Iterator it=tos.symbols();
        while (it.hasNext()) {
            MKSymbol sym = (MKSymbol)(it.next());
            MKSymbol binding = getSymbol(sym.getName());
            _symbols.remove(sym.getName());
            MKSymbol hidden = binding.getHidden();
            binding.setHidden(null);
            _symbols.put(sym.getName(), hidden);
        }
    }
}
