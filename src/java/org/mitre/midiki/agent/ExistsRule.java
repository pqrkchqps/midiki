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

import org.mitre.midiki.state.ImmutableInfoState;
import org.mitre.midiki.logic.Bindings;
import org.mitre.midiki.logic.BindingsImpl;
import org.mitre.midiki.state.InfoState;

import java.util.logging.*;

/**
 * Provides a <code>Condition</code>-based <code>Rule</code> which
 * executes actions no more than once.<p>
 * A <code>Condition</code> may succeed for more than one assignment
 * of <code>Variable</code>s to values. An <code>ExistsRule</code>
 * will succeed for the first assignment found, if any, and will
 * ignore any others. The values of those <code>Variable</code>s are
 * passed to the <code>execute</code> method through <code>Bindings</code>.<p>
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see Rule
 */
public abstract class ExistsRule implements Rule
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.agent.ExistsRule");
    /**
     * The <code>Condition</code> whose success or failure is tested.
     * The standard condition type is a call to a query method in the
     * information state, or a relational expression.
     *
     * {transient=false, volatile=false}
     */
    private Condition condition;
    /**
     * Holds the result of executing the rule.
     */
    private boolean success;

    /**
     * Evaluates the supplied <code>Condition</code>, and calls
     * <code>execute</code> at most once upon success.
     *
     * @param infoState the current <code>InfoState</code>
     * @return <code>true</code> if the <code>Condition</code> succeeded
     * and the <code>execute</code> method returned <code>true</code>.
     */
    final public boolean evaluate(InfoState infoState) {
        success = false;
        logger.entering(getClass().getName(),"evaluate");
        if (condition==null) {
            logger.logp(Level.WARNING,getClass().getName(),"evaluate","condition null!");
            return false;
        }
        Bindings binds = infoState.createBindings();
        if (condition.accept(infoState.getView(), binds)) {
            logger.logp(Level.FINER,getClass().getName(),"evaluate","executing");
            logger.logp(Level.FINER,getClass().getName(),"evaluate","succeeded");
            success = execute(infoState, binds);
            infoState.releaseBindings(binds);
            return true;
        } else {
            logger.logp(Level.FINER,getClass().getName(),"evaluate","failed");
            infoState.releaseBindings(binds);
            return false;
        }
    }
    public boolean succeeded()
    {
        return success;
    }
    /**
     * Provides actions to be performed when condition is accepted.<p>
     * It is unusual for this method to return anything other than
     * <code>true</code>. However, we cannot anticipate all of the
     * possible control structures users may devise, so we allow
     * a return of <code>false</code> to deny the firing of this
     * <code>Rule</code>.
     *
     * @param infoState the current mutable <code>InfoState</code>
     * @param bindings the <code>Bindings</code> returned by the
     * <code>Condition</code>
     * @return <code>true</code> if the actions succeeded
     */
    abstract protected boolean execute(InfoState infoState,
                                       Bindings bindings);
    /**
     * Creates a new <code>ExistsRule</code> instance. Since this
     * constructor does not take a <code>Condition</code>, and there is
     * no other way to specify one, this constructor should only be used
     * for object serialization.
     *
     */
    public ExistsRule() {
    }
    /**
     * Creates a new <code>ExistsRule</code> instance with the
     * specified <code>Condition</code>.
     *
     * @param cond a <code>Condition</code> to be tested
     */
    public ExistsRule(Condition cond) {
        condition = cond;
    }
}
