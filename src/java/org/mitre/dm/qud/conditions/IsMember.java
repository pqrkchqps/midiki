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

public class IsMember extends Condition
{
    Object list;
    Object item;
    public IsMember(Object l, Object i)
    {
        super();
        list = l;
        item = i;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        if (list==null) return false;
        if (item==null) return false;
        Object listDeref = infoState.getUnifier().deref(list, bindings);
        if (!(listDeref instanceof List)) return false;
        boolean haveSolution = false;
        Iterator items = ((List)listDeref).iterator();
        while (items.hasNext()) {
            Object nextItem = items.next();
            if (infoState.getUnifier().matchTerms(nextItem, item, bindings)) {
                haveSolution = true;
                bindings.reset();  // call reset() after each set of bindings
            }
        }
        return haveSolution;
    }
}

