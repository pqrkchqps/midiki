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

public class ConsultDatabase extends Condition
{
    Object query;
    Object sharedBelief;
    Object result;
    public ConsultDatabase(Object q, Object s, Object r)
    {
        super();
        query = q;
        sharedBelief = s;
        result = r;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        LinkedList args = new LinkedList();
        args.add(query);
        args.add(sharedBelief);
        args.add(result);
        //return infoState.cell("database").query("consultDB").query(args,bindings);
        return true;
    }
}

