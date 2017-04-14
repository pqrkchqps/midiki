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

public class NotAlreadyAnswered extends Condition
{
    Object q;
    public NotAlreadyAnswered(Object question)
    {
        super();
        q = question;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object ground = infoState.cell("is").cell("shared").get("com");
        if (ground == null) return true;
        if (!(ground instanceof List)) return true;
        LinkedList args = new LinkedList();
        boolean success = true;
        Iterator it = ((List)ground).iterator();
        while (it.hasNext()) {
            Object question = infoState.getUnifier().deref(q,bindings);
            Object cg = infoState.getUnifier().deref(it.next(),bindings);
            args.clear();
            args.add(question);
            args.add(cg);
            if (infoState.cell("domain").query("relevant_answer").query(args,bindings)) {
                // we already know something that answers the question
                success = false;
            }
        }
        return success;
    }
}

