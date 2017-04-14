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

/**
 * Encapsulates a test for the completion of a task.
 * This test should be the responsibility of the domain,
 * returning true if values have been obtained for
 * certain special task slots, but Godis handled it
 * as a macro and I have not yet generalized it.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see Condition
 */
public class GoalAchieved extends Condition
{
    Object task;
    public GoalAchieved(Object o)
    {
        super();
        task = o;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        return false;
    }
}

