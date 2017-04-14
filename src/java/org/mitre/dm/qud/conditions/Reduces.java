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

public class Reduces extends Condition
{
    Object q;
    Object r;
    Object p;
    public Reduces(Object qq, Object rr, Object pp)
    {
        super();
        q = qq;
        r = rr;
        p = pp;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        LinkedList args = new LinkedList();
        args.add(q);
        args.add(r);
        args.add(p);
        return infoState.cell("domain").query("reduce").query(args,bindings);
    }
}

