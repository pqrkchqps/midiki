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
import java.util.LinkedList;

public class LatestMoveFailed extends Condition
{
    private LinkedList failedList;
    public LatestMoveFailed()
    {
        super();
        failedList = new LinkedList();
        failedList.add("failed");
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object latest_moves = infoState.cell("is").get("latest_moves");
        return infoState.getUnifier().matchTerms(latest_moves,failedList,bindings);
    }
}

