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

import org.mitre.midiki.compiler.parser.*;

public class MKLangspecStack extends MKStack
{
    public MKLangspecStack()
    {
        super();
    }

    public void recognize(String statementType)
    {
        System.out.println(statementType+"\t"+
                           getDepth()+"\t"+
                           getValue(0)+":"+
                           getType(0)+"\t"+
                           getValue(1)+":"+
                           getType(1));
        getSymbolTable().bindSymbol((String)getValue(0), MKContext.ctxtType, getValue(1));
    }

    public void reportError(Exception e)
    {
    }
}
