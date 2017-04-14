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
package org.mitre.midiki.state;

import java.util.Collection;
import org.mitre.midiki.logic.Bindings;
import org.mitre.midiki.logic.Unifier;

/**
 * Provides the facilities for modifying the information state,
 * as an extension of the read-only <code>ImmutableInfoState</code>.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see ImmutableInfoState
 */
public interface InfoState
{
    /* {src_lang=Java}*/

    /**
     * Get a read-only view of the information state.
     *
     * @return an <code>ImmutableInfoState</code> value
     */
    public ImmutableInfoState getView();
    /**
     * Get a unification engine that works with this information state.
     *
     * @return an <code>Unifier</code> value
     */
    public Unifier getUnifier();
    /**
     * Get a reference to the specified <code>Cell</code>
     * attribute.
     *
     * @param fqName a <code>String</code> value
     * @return a <code>Cell</code> value
     */
    public Cell cell(String fqName);
    /**
     * Return <code>true</code> if the specified <code>Cell</code>
     * is present.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>boolean</code> value
     */
    public boolean hasCell(String fqName);
    /**
     * Create a <code>Bindings</code> object for storage of
     * variable assignments.
     *
     * @return a <code>Bindings</code> value
     */
    public Bindings createBindings();
    /**
     * Releases the specified <code>Bindings</code> object.
     * Implementations of <code>InfoState</code> can take advantage
     * of this to recycle <code>Bindings</code>, if possible.
     *
     * @param bindings a <code>Bindings</code> value
     */
    public void releaseBindings(Bindings bindings);
    /**
     * Blocks external changes to the local copy of the
     * information state. Automatically called whenever
     * an <code>InfoListener</code> is invoked. Should be
     * followed by <code>unlock</code> as quickly as possible,
     * because the agent will not respond to any new events
     * or pass on its changes to the information state until
     * the lock is released.<p>
     * This method is primarily of interest when injecting data
     * into the information state in response to non-Midiki events.
     *
     */
    public void lock();
    /**
     * Propagates any recent information state changes to the
     * other agents, and processes inputs that have arrived since
     * <code>lock</code> was called. For maximum system responsiveness,
     * locks should be held as briefly as is feasible. Automatically
     * called when control returns from an <code>InfoListener</code>.<p>
     * This method is primarily of interest when injecting data
     * into the information state in response to non-Midiki events.
     *
     */
    public void unlock();
}
