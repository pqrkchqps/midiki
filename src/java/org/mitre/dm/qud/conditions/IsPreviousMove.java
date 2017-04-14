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

public class IsPreviousMove extends Condition
{
    Object move;
    public IsPreviousMove(Object p)
    {
        super();
        move = p;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object moves = infoState.cell("is").get("moves_for_last_turn");
        return IsCommonGround.rec_in(moves,move,bindings);
    }
}

