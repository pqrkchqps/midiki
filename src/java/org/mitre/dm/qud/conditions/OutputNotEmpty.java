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

public class OutputNotEmpty extends Condition
{
    public OutputNotEmpty()
    {
        super();
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object output = infoState.cell("output").get("output");
        if (output == null) {
            System.out.println("output null!");
            return false;
        }
        if (!(output instanceof String)) {
            System.out.println("Output not String, output "+output.getClass());
            return false;
        }
        return (((String)output).length() != 0);
    }
}

