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

public class MKDFANode
{
    // canAccept
    // productionIndex
    // subGrammar
    // transitions
    // isStart
    // isEnd
    String transition;
    int productionIndex;
    Vector successors;
    MKDFA subGrammar;

    public MKDFANode()
    {
        productionIndex = -1;
    }

    public String toString()
    {
        String result =
            "MKDFANode transition="+transition+
            ", productionIndex="+productionIndex+
            " successors=[\r\n";
        if (successors == null)
        {
            result = result + "null\r\n]\r\n";
        }
        else
        {
            String succString = "";
            Enumeration enum = successors.elements();
            while (enum.hasMoreElements())
            {
                succString = succString + enum.nextElement();
            }
            if (succString.length() == 0)
                succString = "null\r\n";
            result = result + succString + "]\r\n";
        }
        if (subGrammar != null)
        {
            result = result + "* subgrammar: "+subGrammar.root;
        }
        return result;
    }
}

