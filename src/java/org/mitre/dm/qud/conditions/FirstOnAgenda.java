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

public class FirstOnAgenda extends Condition
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.conditions.FirstOnAgenda");
    Object move;
    public FirstOnAgenda(Object p)
    {
        super();
        move = p;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object agenda = infoState.cell("is").cell("private").get("agenda");
        if (agenda == null) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.FirstOnAgenda","test","agenda null");
            return false;
        }
        if (!(agenda instanceof Stack)) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.FirstOnAgenda","test","agenda not list");
            return false;
        }
        if (((Stack)agenda).isEmpty()) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.FirstOnAgenda","test","agenda empty");
            return false;
        }
        Object fst = ((Stack)agenda).peek();
        boolean matched = infoState.getUnifier().matchTerms(fst,move,bindings);
        bindings.reset();
        logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.FirstOnAgenda","test","first item on agenda",fst);
        logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.FirstOnAgenda","test",(matched ? "matches template" : "does not match template"),move);
        return matched;
    }
}

