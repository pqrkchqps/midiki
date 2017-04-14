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
import org.mitre.midiki.state.InfoState;

import java.util.logging.*;

/**
 * Provides a <code>Condition</code>-based <code>Rule</code> which
 * executes actions as many times as possible.<p>
 * A <code>Condition</code> may succeed for more than one assignment
 * of <code>Variable</code>s to values. A <code>ForAllRule</code>
 * will succeed for all assignments found, if any. The values of those
 * <code>Variable</code>s are passed to the <code>execute</code> method
 * through <code>Bindings</code>, one assignment at a time.<p>
 * [for ease of backtracking, probably need an internal Condition class
 * that will extend the supplied condition and forbid further extension.
 * that internal class will then call execute when evaluated.]
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see Rule
 */
public abstract class ForAllRule implements Rule
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.agent.ForAllRule");
    /**
     * Internal <code>Condition</code> that will extend the supplied
     * <code>Condition</code> and forbid further extension. When this
     * rule is evaluated, the terminal condition will force backtracking
     * regardless of the success of the <code>execute</code> method.
     */
    private class TerminalCondition extends Condition
    {
        ForAllRule target;
        InfoState targetInfoState;

        public TerminalCondition(ForAllRule t)
        {
            target = t;
        }
        public void extend(Condition cond) {
            throw new IllegalStateException();
        }
        public boolean test(ImmutableInfoState infoState,
                            Bindings bindings) {
            logger.logp(Level.FINER,super.getClass().getName(),"terminalCondition","backtracking");
            target.success =
                target.success || target.execute(targetInfoState, bindings);
            return false;
        }
    }
    private TerminalCondition terminalCondition;
    /**
     * The <code>Condition</code> whose success or failure is tested.
     * The standard condition type is a call to a query method in the
     * information state, or a relational expression.
     *
     * {transient=false, volatile=false}
     */
    public Condition condition;

    /**
     * Maintains state during backtracking. Set to <code>true</code> if
     * <code>execute</code> is ever called during evaluation.
     * Initialized to <code>false</code>.
     */
    private boolean success;

    /**
     * Evaluates the supplied <code>Condition</code>, and calls
     * <code>execute</code> for each success. The return value of
     * <code>accept</code> is ignored; see design issue discussion
     * under <code>execute</code>.
     *
     * @param infoState the current <code>ImmutableInfoState</code>
     * @return <code>true</code> if the <code>Condition</code> succeeded
     * and the <code>execute</code> method returned <code>true</code>.
     */
    final public boolean evaluate(InfoState infoState) {
        success = false;
        logger.entering(getClass().getName(),"evaluate");
        if (terminalCondition==null) {
            logger.logp(Level.WARNING,getClass().getName(),"evaluate","terminalCondition null!");
            return false;
        }
        Bindings binds = infoState.createBindings();
        terminalCondition.targetInfoState = infoState;
        condition.accept(infoState.getView(), binds);
        infoState.releaseBindings(binds);
        logger.logp(Level.FINER,getClass().getName(),"evaluate",(success ? "succeeded" : "failed"));
        return success;
    }
    /**
     * Returns <code>true</code> if the last rule execution was successful.
     *
     * @return a <code>boolean</code> value
     */
    public boolean succeeded()
    {
        return success;
    }
    /**
     * Provides actions to be performed on success.<p>
     * It is unusual for this method to return anything other than
     * <code>true</code>. However, we cannot anticipate all of the
     * possible control structures users may devise, so we allow
     * a return of <code>false</code> to deny the firing of this
     * <code>Rule</code>.<p>
     * [Design issue: returning false from execute requires special
     * handling in this class, because multiple calls to execute
     * are expected. Returning false can have no effect, can set a flag
     * to return false after executing all possibilities, can halt
     * evaluation with the current set returning false, or can halt
     * evaluation with the current set and return true overall.
     * at present, this rule isn't used in operational DMs, so we haven't
     * worked out the best solution. default action is to ignore a
     * false result.]
     *
     * @param infoState the current mutable <code>InfoState</code>
     * @param bindings the <code>Bindings</code> returned by the
     * <code>Condition</code>
     * @return <code>true</code> if the actions succeeded
     */
    abstract protected boolean execute(InfoState infoState,
                                       Bindings bindings);
    /**
     * Creates a new <code>ForAllRule</code> instance. Since this
     * constructor does not take a <code>Condition</code>, and there is
     * no other way to specify one, this constructor should only be used
     * for object serialization.<p>
     * An implementation concern is that any <code>Condition</code> attached
     * to this rule following this constructor cannot be guaranteed to
     * have the correct terminating <code>Condition</code>. Without the
     * expected terminator, this rule will execute no actions.
     *
     */
    public ForAllRule() {
    }
    /**
     * Creates a new <code>ForAllRule</code> instance with the
     * specified <code>Condition</code>. The supplied <code>Condition</code>
     * will be <code>extend</code>ed with a special <code>Condition</code>
     * that prohibits further extension, always fails, and calls
     * <code>execute</code>.
     *
     * @param cond a <code>Condition</code> to be tested
     */
    public ForAllRule(Condition cond) {
        condition = cond;
        terminalCondition = new TerminalCondition(this);
        condition.extend(terminalCondition);
    }
}
