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
package org.mitre.midiki.impl.mitre;

import java.util.*;
import java.util.logging.*;

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.Bindings;
import org.mitre.midiki.logic.BindingsImpl;
import org.mitre.midiki.logic.Unifier;
import org.mitre.midiki.logic.Unify;

/**
 * Provides the facilities for modifying the information state,
 * as an extension of the read-only <code>ImmutableCompositeInfoState</code>.
 * Composite information states provide a unified gateway to
 * a collection of two or more individual information states.
 * When cells overlap, only the first is consulted for reads
 * or for query and method calls, but all overlapping cells
 * (i.e., cells defining the same contract accessed by the
 * same name in different info states) will receive changes.<p>
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see ImmutableCompositeInfoState
 */
public class CompositeInfoState extends InfoStateImpl implements InfoState
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.CompositeInfoState");
    protected LinkedList infoStates;

    public void addInfoState(InfoState is)
    {
        if (is==null) {
            throw new Error("Appending null infoState to composite");
        }
        infoStates.addLast(is);
    }

    public void lockViewers()
    {
        Iterator it = infoStates.iterator();
        while (it.hasNext()) {
            InfoStateImpl is = (InfoStateImpl)it.next();
            is.lockViewers();
        }
    }
    public void notifyViewers()
    {
        Iterator it = infoStates.iterator();
        while (it.hasNext()) {
            InfoStateImpl is = (InfoStateImpl)it.next();
            is.notifyViewers();
        }
    }
    public void unlockViewers()
    {
        Iterator it = infoStates.iterator();
        while (it.hasNext()) {
            InfoStateImpl is = (InfoStateImpl)it.next();
            is.unlockViewers();
        }
    }
    public void fire(InfoListener il)
    {
        throw new RuntimeException("Internal error: CompositeInfoState.fire() called");
    }
    /**
     * Describe <code>notifyAllInfoListeners</code> method here.
     * Note: this should probably appear in the DefaultInfoState,
     * since an InfoState is required as a parameter to the handlers
     * and the attr/value is not available at this point.
     *
     */
    public void notifyAllInfoListeners() {
        Iterator it = infoStates.iterator();
        while (it.hasNext()) {
            InfoStateImpl is = (InfoStateImpl)it.next();
            is.notifyAllInfoListeners();
        }
    }

    public CompositeInfoState(ImmutableCompositeInfoState view)
    {
        super(view);
        if (infoView == null) {
            throw new Error("Composite created with null view!");
        }
        infoStates = new LinkedList();
    }

    public void instantiateRootDataSet()
    {
        Iterator it = infoStates.iterator();
        while (it.hasNext()) {
            InfoStateImpl is = (InfoStateImpl)it.next();
            is.instantiateRootDataSet();
        }
    }

    /**
     * Get a read-only view of the information state.
     *
     * @return an <code>ImmutableCompositeInfoState</code> value
     */
    public ImmutableInfoState getView()
    {
        return infoView;
    }
    /**
     * Get a unification engine that works with this information state.
     *
     * @return an <code>Unifier</code> value
     */
    public Unifier getUnifier()
    {
        return Unify.getInstance();
    }
    /**
     * Get a reference to the specified <code>Cell</code>
     * attribute.
     *
     * @param fqName a <code>String</code> value
     * @return a <code>Cell</code> value
     */
    public Cell cell(String fqName)
    {
        Iterator it = infoStates.iterator();
        while (it.hasNext()) {
            InfoState is = (InfoState)it.next();
            if (is.hasCell(fqName)) {
                return is.cell(fqName);
            }
        }
        RuntimeException ex = new RuntimeException("No cell '"+fqName+"' in infostate");
        logger.throwing("org.mitre.midiki.impl.mitre.CompositeInfoState","cell",ex);
        throw ex;
    }
    /**
     * Return <code>true</code> if the specified <code>Cell</code>
     * is present.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>boolean</code> value
     */
    public boolean hasCell(String fqName)
    {
        return infoView.hasCell(fqName);
    }

    /**
     * Create a <code>Bindings</code> object for storage of
     * variable assignments.
     *
     * @return a <code>Bindings</code> value
     */
    public Bindings createBindings()
    {
        return new BindingsImpl();
    }
    /**
     * Releases the specified <code>Bindings</code> object.
     * Implementations of <code>CompositeInfoState</code> can take advantage
     * of this to recycle <code>Bindings</code>, if possible.
     *
     * @param bindings a <code>Bindings</code> value
     */
    public void releaseBindings(Bindings bindings)
    {
    }
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
    public void lock()
    {
        // first, flush the local data store to force reloading
        // of any data which may have changed. then lock all
        // of our clients.
        ((ImmutableCompositeInfoState)infoView).flushLocalState();
        Iterator it = infoStates.iterator();
        while (it.hasNext()) {
            InfoState is = (InfoState)it.next();
            is.lock();
        }
    }
    /**
     * Propagates any recent information state changes to the
     * other agents, and processes inputs that have arrived since
     * <code>lock</code> was called. For maximum system responsiveness,
     * locks should be held as briefly as is feasible. Automatically
     * called when control returns from an <code>InfoListener</code>.<p>
     * This method is primarily of interest when injecting data
     * into the information state in response to non-Midiki events.
     *
     * Viewers, closures, ghosted info states... none of them are given
     * lists of clients. In that way, they are able to execute this code
     * without sending excessive messages.
     */
    public void unlock()
    {
        Iterator it = infoStates.iterator();
        while (it.hasNext()) {
            InfoState is = (InfoState)it.next();
            is.unlock();
        }
    }

    /**
     * For a composite infostate, this should only ever be called
     * for the most recently added infoState. Assume this is true.
     *
     * @param mediator a <code>Mediator</code> value
     * @return a <code>boolean</code> value
     */
    public boolean connect(Mediator mediator)
    {
        return ((InfoStateImpl)infoStates.getLast()).connect(mediator);
    }

    public void declare_external_services(Collection services,
                                          Collection requests,
                                          Mediator mediatedBy)
    {
    }
}
