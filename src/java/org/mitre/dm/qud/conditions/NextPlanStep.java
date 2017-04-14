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

public class NextPlanStep extends Condition
{
    Object step;
    public NextPlanStep(Object s)
    {
        super();
        step = s;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object planStackObj = infoState.cell("is").cell("private").get("plan");
        Stack planStack = (Stack)planStackObj;
        if (planStack.isEmpty()) return false;
        Object plan = planStack.peek();
        LinkedList args = new LinkedList();
        args.add(plan);
        args.add(step);
        return infoState.cell("domain").query("next_step").query(args,bindings);
    }
}

