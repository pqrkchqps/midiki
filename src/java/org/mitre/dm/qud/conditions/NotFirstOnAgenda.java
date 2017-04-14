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

public class NotFirstOnAgenda extends Condition
{
    private static String className = 
        "org.mitre.dm.qud.conditions.NotFirstOnAgenda";
    private static Logger logger = Logger.getLogger(className);
    Object move;
    public NotFirstOnAgenda(Object p)
    {
        super();
        move = p;
    }
    /**
     * Returns <code>true</code> is the specified move is not the
     * first item on the agenda. The negation of <code>FirstOnAgenda</code>.
     *
     * @param infoState an <code>ImmutableInfoState</code> value
     * @param bindings a <code>Bindings</code> value
     * @return a <code>boolean</code> value
     */
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        boolean success = false;
        Object agenda = infoState.cell("is").cell("private").get("agenda");
        if (agenda == null) {
            logger.logp(Level.FINER,className,"test","agenda null");
            return true;
        }
        if (!(agenda instanceof List)) {
            logger.logp(Level.FINER,className,"test","agenda not list");
            return true;
        }
        if (((List)agenda).isEmpty()) {
            logger.logp(Level.FINER,className,"test","agenda is empty");
            return true;
        }
        Object fst = ((List)agenda).get(0);
        logger.logp(Level.FINER,className,"test","pattern",move);
        logger.logp(Level.FINER,className,"test","first on agenda",fst);
        success = !infoState.getUnifier().matchTerms(fst,move,bindings);
        return success;
    }
}

