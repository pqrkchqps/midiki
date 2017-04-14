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

public class IsRelevantToTask extends Condition
{
    Object task;
    Object answer;
    Object plan;
    public IsRelevantToTask(Object a, Object t, Object p)
    {
        super();
        task = t;
        answer = a;
        plan = p;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        LinkedList args = new LinkedList();
        args.add(Unify.getInstance().deref(answer,bindings));
        args.add(Unify.getInstance().deref(task,bindings));
        args.add(Unify.getInstance().deref(plan,bindings));
        boolean relevance = infoState.cell("domain").query("relevant_to_task").query(args,bindings);
        System.out.println("IsRelevantToTask "+args+" "+bindings+" == "+relevance);
        return relevance;
    }
}

