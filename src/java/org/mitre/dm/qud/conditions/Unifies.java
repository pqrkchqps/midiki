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

public class Unifies extends Condition
{
    Object one;
    Object two;
    public Unifies(Object uno, Object dos)
    {
        super();
        one = uno;
        two = dos;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object unity = infoState.getUnifier().unify(one,two,bindings);
        bindings.reset();
        return (unity != null);
    }
}

