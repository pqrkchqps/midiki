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

public class NotFirstQuestionUnderDiscussion extends Condition
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.conditions.FirstQuestionUnderDiscussion");
    Object move;
    public NotFirstQuestionUnderDiscussion(Object p)
    {
        super();
        move = p;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object qud = infoState.cell("is").cell("shared").get("qud");
        if (qud == null) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.FirstQuestionUnderDiscussion","test","qud null");
            return true;
        }
        if (!(qud instanceof Stack)) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.FirstQuestionUnderDiscussion","test","qud not stack");
            return true;
        }
        if (((Stack)qud).isEmpty()) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.FirstQuestionUnderDiscussion","test","list empty");
            return true;
        }
        Object fst = ((Stack)qud).peek();
        return !infoState.getUnifier().matchTerms(fst,move,bindings);
    }
}

