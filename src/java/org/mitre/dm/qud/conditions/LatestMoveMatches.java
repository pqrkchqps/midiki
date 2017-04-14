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

public class LatestMoveMatches extends Condition
{
    Predicate move;
    public LatestMoveMatches(Predicate p)
    {
        super();
        move = p;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        boolean success = false;
        Object latest_moves = infoState.cell("is").get("latest_moves");
        if (latest_moves == null) return false;
        if (!(latest_moves instanceof List)) {
            success = infoState.getUnifier().matchTerms(latest_moves,move,bindings);
        } else {
            if (((List)latest_moves).isEmpty()) {
                System.out.println("LatestMoveMatches called with empty list");
                return false;
            }
            Object fst = ((List)latest_moves).get(0);
            success = infoState.getUnifier().matchTerms(fst,move,bindings);
        }
        bindings.reset();
        return success;
    }
}

