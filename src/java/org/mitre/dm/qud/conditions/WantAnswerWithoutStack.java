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

public class WantAnswerWithoutStack extends Condition
{
    Object q;
    Object qf;
    /**
     * Creates a new <code>WantAnswer</code> instance.
     *
     * @param qq question for which answer may be desired
     * @param qqf plan-centric form of question, with proper variable names
     */
    public WantAnswerWithoutStack(Object qq,Object qqf)
    {
        super();
        q = qq;
        qf = qqf;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object plan = infoState.cell("is").cell("private").get("plan");
        LinkedList args = new LinkedList();
        args.add(plan);
        args.add(q);
        args.add(qf);
        return infoState.cell("domain").query("wants_answer").query(args,bindings);
    }
}

