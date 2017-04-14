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
package org.mitre.dm;

import java.util.*;

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;

import org.mitre.dm.*;

/**
 * Tokenizes a string, breaking on whitespace and punctuation.
 * Does not attempt to construct a lattice, just a list.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see MethodHandler
 */
public class WhitespaceTokenizingInterpreter
{
    public WhitespaceTokenizingInterpreter()
    {
        super();
    }

    public CellHandlers initializeHandlers()
    {
        CellHandlers interpreterCell = new CellHandlers(ContractDatabase.find("interpreter"));
        MethodHandler transducer = new MethodHandler()
            {

                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: String to tokenize
                    // 3: wordlist result to unify with
                    Object intfid = null;
                    Object string = null;
                    Object wordlist = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) string = argIt.next();
                    if (argIt.hasNext()) wordlist = argIt.next();

                    if (string==null) {
                        return false;
                    }
                    
                    // Two approaches:
                    // 1. Assume only String objects are valid
                    // 2. Use toString() to extract the value
                    // I've picked the second path.
                    String str = string.toString();  // OAA needs to "unquote" strings
                    StringTokenizer st = new StringTokenizer(str);
                    LinkedList wl = new LinkedList();
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        wl.add(token);
                    }
                    if (Unify.getInstance().matchTerms(wordlist, wl, bindings)) {
                        // for a method, don't reset the bindings.
                        // you can't backtrack anyway!
                        //bindings.reset();
                        return true;
                    }
                    return false;
                }
            };
        interpreterCell.addMethodHandler("string2wordlist", transducer);
        return interpreterCell;
    }

}
