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

import java.util.logging.*;

public class InputNotEmpty extends Condition
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.conditions.InputNotEmpty");
    public InputNotEmpty()
    {
        super();
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object input = infoState.cell("is").get("input");
        logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.InputNotEmpty","test","input not null",input);
        if (input == null) {
            return false;
        }
        if (!(input instanceof String)) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.InputNotEmpty","test","input not String");
            return false;
        }
        boolean success = (((String)input).length() != 0);
        return success;
    }
}

