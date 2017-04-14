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
import java.util.logging.*;

public class GreaterThan extends Condition
{
    private static String className =
        "org.mitre.dm.qud.conditions.GreaterThan";
    private static Logger logger = Logger.getLogger(className);

    Object one;
    Object two;
    public GreaterThan(Object uno, Object dos)
    {
        super();
        one = uno;
        two = dos;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object oneDeref = infoState.getUnifier().deref(one, bindings);
        Object twoDeref = infoState.getUnifier().deref(two, bindings);
        logger.logp(Level.FINEST,className,"test","one",oneDeref);
        logger.logp(Level.FINEST,className,"test","two",twoDeref);
        if (!(oneDeref instanceof Integer)) {
            logger.logp(Level.FINEST,className,"test","one is non-Integer");
            return false;
        }
        if (!(twoDeref instanceof Integer)) {
            logger.logp(Level.FINEST,className,"test","two is non-Integer");
            return false;
        }
        return ((Integer)oneDeref).intValue() > ((Integer)twoDeref).intValue();
    }
}

