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

public class IsRelevantCategory extends Condition
{
    Object task;
    Object category;
    public IsRelevantCategory(Object t, Object a)
    {
        super();
        task = t;
        category = a;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        LinkedList args = new LinkedList();
        args.add(Unify.getInstance().deref(task, bindings));
        args.add(Unify.getInstance().deref(category, bindings));
        return infoState.cell("domain").query("relevant_category").query(args,bindings);
    }
}

