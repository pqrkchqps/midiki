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

public class MoveIsNotStored extends Condition
{
    Object move;
    public MoveIsNotStored(Object o)
    {
        super();
        move = o;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object moves = infoState.cell("is").cell("shared").get("nim");
        return !IsCommonGround.rec_in(moves,move,bindings);
    }
}

