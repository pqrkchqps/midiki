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
 * Invokes <code>domain.abstract</code> for a specified triplet
 * (Question, Answer, Predicate). Note: this may not be implemented
 * in the default DomainImplementation, as it is not used in the DME.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see Condition
 */
public class Abstracts extends Condition
{
    Object q;
    Object r;
    Object p;
    public Abstracts(Object qq, Object rr, Object pp)
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
        return infoState.cell("domain").query("abstract").query(args,bindings);
    }
}

