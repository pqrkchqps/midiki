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

/**
 * Returns <code>true</code> if the plan stack is not empty.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see Condition
 */
public class CurrentPlan extends Condition
{
    Object plan;
    public CurrentPlan(Object p)
    {
        super();
        plan = p;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object planStack = infoState.cell("is").cell("private").get("plan");
        boolean success = infoState.getUnifier().matchTerms(plan,((Stack)planStack).peek(),bindings);
        bindings.reset();
        return success;
    }
}

