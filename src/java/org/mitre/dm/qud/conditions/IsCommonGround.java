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

public class IsCommonGround extends Condition
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.conditions.IsCommonGround");
    Object move;
    public IsCommonGround(Object p)
    {
        super();
        move = p;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object moves = infoState.cell("is").cell("shared").get("com");
        //System.out.println("IsCommonGround: com == "+moves+", move == "+move);
        return rec_in(moves,move,bindings);
    }
    /* rec.in
     * This routine needs to check the elements of a collection
     * to see if they unify with the specified move.
     * Every element that unifies needs to be added as an alternative
     * for backtracking.
     */
    static public boolean rec_in(Object coll, Object elem, Bindings bindings)
    {
        if (coll==null) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.IsCommonGround","rec_in","rec.in of null Collection");
            return false;
        }
        if (!(coll instanceof Collection)) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.IsCommonGround","rec_in","rec.in of non-Collection");
            return false;
        }
        logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.IsCommonGround","rec_in","collection",coll);
        logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.IsCommonGround","rec_in","element",elem);
        boolean success = false;
        Iterator it = ((Collection)coll).iterator();
        while (it.hasNext()) {
            Object o = it.next();
            //System.out.println("rec_in: match "+o+" to "+elem);
            if (Unify.getInstance().matchTerms(o, elem, bindings)) {
                //System.out.println("------- success!!!");
                success = true;
                bindings.reset();
            }
        }
        return success;
    }
}

