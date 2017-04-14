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

public class NewTopic extends Condition
{
    Object topic;
    public NewTopic(Object s)
    {
        super();
        topic = s;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object newTopic = infoState.cell("is").cell("private").get("topic_shift");
        if (newTopic == null) return false;
        if (infoState.getUnifier().matchTerms(topic,newTopic,bindings)) {
            bindings.reset();
            return true;
        } else {
            return false;
        }
    }
}

