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

public class LatestMoveDoesNotMatch extends Condition
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.conditions.LatestMoveDoesNotMatch");
    Object move;
    public LatestMoveDoesNotMatch(Object o)
    {
        super();
        move = o;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object latest_moves = infoState.cell("is").get("latest_moves");
        if (latest_moves == null) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.LatestMoveDoesNotMatch","test","no latest move, so no match");
            return true;
        }
        if (!(latest_moves instanceof List)) {
            return !infoState.getUnifier().matchTerms(latest_moves,move,bindings);
        }
if (((List)latest_moves).isEmpty()) {
    System.out.println("LatestMoveDoeNotMatch called with empty list");
    return true;
}
        Object fst = ((List)latest_moves).get(0);
        return !infoState.getUnifier().matchTerms(fst,move,bindings);
    }
}

