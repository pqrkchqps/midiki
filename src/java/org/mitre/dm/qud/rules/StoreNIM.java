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
package org.mitre.dm.qud.rules;

import org.mitre.dm.*;
import org.mitre.dm.qud.conditions.*;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;

import java.util.*;
import java.util.logging.*;

/**
 * Store all non-integrated user moves.
 * 
 */
 
public class StoreNIM extends ForAllRule
{
    private Logger discourseLogger = null;
    Result consequence;
    public void setLogger(Logger theLogger)
    {
        discourseLogger = theLogger;
        consequence.setDiscourseLogger(discourseLogger);
    }
    protected Variable move;
    static public StoreNIM newInstance(Variable move)
    {
        Condition storeNimC = new Condition();
        storeNimC.extend(new SpeakerIs("usr"));
        storeNimC.extend(new MoveIsNotIntegrated(move));
        storeNimC.extend(new MoveIsNotStored(move));
        return new StoreNIM(move, storeNimC);
    }

    public StoreNIM()
    {
        super();
        consequence = new Result();
    }

    public StoreNIM(Variable m, Condition c)
    {
        super(c);
        move = m;
        consequence = new Result();
    }

    public boolean execute(InfoState infoState, Bindings bindings) {
        consequence.storeMove(move, infoState, bindings);
        return true;
    }
}
