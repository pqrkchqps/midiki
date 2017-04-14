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
 * The <code>Never</code> condition states that it is true if all of the
 * possible invocations of its input condition fail. That is, there does
 * not exist a case where its input condition succeeds. The naive approach
 * is to simply check for the negation of its input; this isn't sufficient,
 * though, because the standard condition-processing code will backtrack
 * to find an alternative that succeeds. We need to override the default
 * processing for 'accept' to remove backtracking on this condition.
 * That in turn means that the 'accept' routine in the Midiki core
 * cannot be final, or we need a backtracking flag in every instance
 * of condition.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see Condition
 */
public class Never extends Condition
{
    Condition chain;
    public Never(Condition c)
    {
        super();
        disableBacktracking();
        chain = c;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        return !chain.accept(infoState, bindings);
    }
}
