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
 * Saved the state of the "shared" cell. This depends on the
 * information state handling puts on cells as deep copies rather
 * than copies of instance handles, which is the wrong way to do it.
 * Really need to have a "copy" operator on cells as well as
 * the "new" and "set" operators.
 * 
 */
 
public class Store extends ExistsRule
{
    private Logger discourseLogger = null;
    public void setLogger(Logger theLogger)
    {
        discourseLogger = theLogger;
    }
    static public Store newInstance()
    {
        Condition storeC = new SpeakerIs("sys");
        return new Store(storeC);
    }

    public Store()
    {
        super();
    }

    public Store(Condition c)
    {
        super(c);
    }

    public boolean execute(InfoState infoState, Bindings bindings) {
        // no longer needed, and not correctly implemented anyway.
        //infoState.cell("is").cell("private").put("tmp",infoState.cell("shared").copy());
        return true;
    }
}
