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
package org.mitre.dm.qud.conditions;

import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;

import java.util.*;
import java.util.logging.*;

public class ListLength extends Condition
{
    private static String className =
        "org.mitre.dm.qud.conditions.ListLength";
    private static Logger logger = Logger.getLogger(className);

    Object list;
    Object length;
    public ListLength(Object l, Object i)
    {
        super();
        list = l;
        length = i;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        boolean success = false;
        if (length==null) {
            System.out.println("ListLength: length==null: fails");
            return false;
        }
        if (list==null) {
            System.out.println("ListLength: list==null");
            success = infoState.getUnifier().matchTerms(length, new Integer(0), bindings);
            bindings.reset();
            return success;
        }
        Object listDeref = infoState.getUnifier().deref(list, bindings);
        if (!(listDeref instanceof List)) {
            System.out.println("ListLength: list not a List: "+listDeref);
            success = infoState.getUnifier().matchTerms(length, new Integer(1), bindings);
            bindings.reset();
            return success;
        }
        success = infoState.getUnifier().matchTerms(length, new Integer(((List)listDeref).size()), bindings);
        bindings.reset();
        return success;
    }
}

