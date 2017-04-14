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

import java.util.logging.*;

/**
 * Encapsulates a boolean expression which may bind variables.
 * The <code>Condition</code> may be composed of a series of
 * <code>Condition</code>s; evaluation of the top-level <code>Condition</code>
 * is only successful if all of the component <code>Conditions</code>
 * are successful.<p>
 * A <code>Condition</code> may evaluate to <code>true</code>
 * for multiple values, and those values may be iterated by
 * backtracking. Backtracking is automatic, provided that the
 * condition developer takes it into consideration when writing
 * code for the <code>test</code> method.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class Condition
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.agent.Condition");
    /**
     * Refers to the next <code>Condition</code> in this chain.
     * {transient=false, volatile=false}
     */
    private Condition next;

    /**
     * Return the next <code>Condition</code> in this chain.
     *
     * @return a <code>Condition</code> value
     */
    public Condition getNextCondition()
    {
        return next;
    }

    /**
     * Flag to indicate if this condition should attempt to backtrack
     * on failure. Default is <code>true</code>.
     */
    private boolean mayBacktrack = true;

    /**
     * Disables backtracking on failure for this instance of condition.
     *
     */
    public void disableBacktracking()
    {
        mayBacktrack = false;
    }

    /**
     * Returns <code>true</code> if the current information state
     * is acceptable. Acceptability is determined by the user-defined
     * <code>test</code> method and the <code>Variable</code> bindings
     * in effect at evaluation time.
     *
     * @param infoState an <code>ImmutableInfoState</code> value
     * @param bindings a <code>Bindings</code> value
     * @return a <code>boolean</code> value
     */
    final public boolean accept(ImmutableInfoState infoState,
                                Bindings bindings) {
        if (test(infoState, bindings)) {
            logger.logp(Level.FINER,getClass().getName(),"accept","passed");
            //System.out.println("accept "+getClass().getName()+" bindings "+bindings);
            ((BindingsImpl)bindings).initializeBacktracking();
            //System.out.println("accept 2 "+getClass().getName()+" bindings "+bindings);
            if (next != null) {
                while (true) {
                    bindings.enterScope();
                    if (next.accept(infoState, bindings)) {
                        logger.logp(Level.FINER,getClass().getName(),"accept","accepted");
                        return true;
                    } else {
                        bindings.exitScope();
                        if (next.mayBacktrack && bindings.backtrack()) {
                            logger.logp(Level.FINER,getClass().getName(),
                                        "accept","backtracked");
                        } else {
                            logger.logp(Level.FINER,getClass().getName(),
                                        "accept","failed to backtrack");
                            break;
                        }
                    }
                }
            } else {
                logger.logp(Level.FINER,getClass().getName(),"accept","complete");
                return true;
            }
        } else {
            logger.logp(Level.FINER,getClass().getName(),"accept","failed");
        }
        return false;
    }
    /**
     * Appends a new <code>Condition</code> to the chain which
     * this <code>Condition</code> participates in.
     *
     * @param cond a <code>Condition</code> value
     */
    public void extend(Condition cond) {
        if (next != null)
            next.extend(cond);
        else
            next = cond;
    }
    /**
     * Returns <code>true</code> if the <code>infoState</code>
     * meets some calculated conditions. Default returns <code>true</code>;
     * override this to provide interesting behavior.<p>
     * When this routine returns, <code>bindings</code> is assumed to
     * have access to all of the possible successful bindings for this
     * condition given the input state. How this is done depends on the
     * available backtracking implementations. In the first release of
     * the Midiki API, <code>ImmutableInfoState.resetBindings()</code>
     * must be called once for every alternate binding; the system can
     * call <code>ImmutableInfoState.backtrack()</code> once for each
     * call to <code>resetBindings</code>.<p>
     * If there is only one possible setting of variables
     * that gives a successful result, you do not need to call
     * <code>resetBindings</code>. If your test encapsulates a call
     * to <code>ImmutableInfoState.query</code>, you should not call
     * <code>resetBindings</code> because the query developer has already
     * taken backtracking into account.
     *
     * @param infoState an <code>ImmutableInfoState</code> value
     * @param bindings a <code>Bindings</code> value
     * @return a <code>boolean</code> value
     */
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings) {
        return true;
    }
    /**
     * Creates a new <code>Condition</code> instance.
     *
     */
    public Condition() {
    }
}
