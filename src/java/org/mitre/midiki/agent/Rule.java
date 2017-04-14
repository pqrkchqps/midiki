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
package org.mitre.midiki.agent;

import org.mitre.midiki.state.InfoState;

/**
 * Represents an object which can be evaluated for success
 * or failure, and which performs a user-defined action
 * when it succeeds. With respect to the information state,
 * the evaluation of a <code>Rule</code> is an atomic event.<p>
 * It is expected that most rules will need to modify the
 * information state, so specification of an <code>ImmutableInfoState</code>
 * may be questioned. However, the expected processing of a <code>Rule</code>
 * consists of two phases:<p>
 * <ul><li>evaluation, during which the state must remain consistent,</li>
 * <li>execution, during which the state may change.</li></ul><p>
 * The evaluation phase can always succeed with an <code>ImmutableInfoState</code>.
 * At the start of the execution phase, the design supports casting
 * to a full and mutable <code>InfoState</code>.<p>
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface Rule
{
    /**
     * Evaluates the <code>Rule</code> in the context of the
     * current information state. Returns <code>true</code> if the
     * <code>Rule</code> can be applied to the information state.
     *
     * @param infoState an <code>ImmutableInfoState</code> view
     * @return <code>true</code> if the <code>Rule</code> fired
     */
    public boolean evaluate(InfoState infoState);
    /**
     * Returns <code>true</code> if the last execution of this rule
     * was successful. Only has a meaningful value if the last
     * call to <code>evaluate</code> returned <code>true</code>.
     * In most cases, it should return <code>true</code>. Sometimes,
     * however, it is desirable to have a rule which is applicable
     * (evaluated to true) but not successful, e.g. to force the
     * failure of a rule set. Should only be overridden in special cases.
     *
     * @return a <code>boolean</code> value for rule success
     */
    public boolean succeeded();
}
