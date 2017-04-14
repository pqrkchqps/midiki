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

public class SpeakerIs extends Condition
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.conditions.SpeakerIs");
    Object spkr;
    public SpeakerIs(Object o)
    {
        super();
        spkr = o;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object speaker = infoState.cell("is").cell("shared").cell("lu").get("speaker");
        logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.SpeakerIs","test","check is.shared.lu.speaker == "+spkr,speaker);
        return infoState.getUnifier().matchTerms(speaker,spkr,bindings);
    }
}

