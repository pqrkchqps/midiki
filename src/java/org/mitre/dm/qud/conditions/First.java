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

public class First extends Condition
{
    Object list;
    Object first;
    public First(Object l, Object i)
    {
        super();
        list = l;
        first = i;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        boolean success = false;
        if (first==null) return false;
        if (list==null) return false;
        Object listDeref = infoState.getUnifier().deref(list, bindings);
        if (!(listDeref instanceof List)) {
            success = infoState.getUnifier().matchTerms(listDeref, first, bindings);
        } else {
            success = infoState.getUnifier().matchTerms(((List)listDeref).get(0), first, bindings);
        }
        bindings.reset();
        return success;
    }
}

