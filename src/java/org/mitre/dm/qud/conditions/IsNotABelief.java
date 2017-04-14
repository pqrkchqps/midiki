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

public class IsNotABelief extends Condition
{
    Object arg;
    public IsNotABelief(Object o)
    {
        super();
        arg = o;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object beliefs = infoState.cell("is").cell("private").get("bel");
        return !IsCommonGround.rec_in(beliefs,arg,bindings);
    }
}

