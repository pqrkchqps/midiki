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

public class IsRelevantToTasks extends Condition
{
    Object tasks;
    Object answer;
    public IsRelevantToTasks(Object a, Object t)
    {
        super();
        tasks = t;
        answer = a;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        LinkedList args = new LinkedList();
        args.add(answer);
        args.add(tasks);
        return infoState.cell("domain").query("relevant_to_tasks").query(args,bindings);
    }
}

