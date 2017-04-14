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

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.Unifier;
import org.mitre.midiki.logic.Unify;

import java.util.*;
import java.util.logging.*;

/**
 * Provides read-only access to the information state.
 * With an <code>ImmutableCompositeInfoState</code>, the user can
 * access cell attributes and call query methods, but cannot
 * change the contents of the information state. (Provided,
 * of course, that all query methods obey the Midiki contract
 * for queries. It is outside the scope of this API to enforce
 * that contract.)<p>
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
 */
public class ImmutableCompositeInfoState extends ImmutableInfoStateImpl implements ImmutableInfoState
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.ImmutableCompositeInfoState");
    protected LinkedList infoStates;

    public ImmutableCompositeInfoState()
    {
        infoStates = new LinkedList();
    }

    public void addImmutableInfoState(ImmutableInfoState is)
    {
        if (is==null) {
            throw new Error("Appending null infoState to composite");
        }
        infoStates.addLast(is);
    }

    /**
     * Get a read-only reference to the specified <code>Cell</code>
     * attribute.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>ImmutableCell</code> value
     */
    public ImmutableCell cell(String fqName)
    {
        Iterator it = infoStates.iterator();
        while (it.hasNext()) {
            ImmutableInfoState is = (ImmutableInfoState)it.next();
            if (is.hasCell(fqName)) {
                return is.cell(fqName);
            }
        }
        RuntimeException ex = new RuntimeException("No cell '"+fqName+"' in infostate");
        logger.throwing("org.mitre.midiki.impl.mitre.ImmutableCompositeInfoState","cell",ex);
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
        Iterator it = infoStates.iterator();
        while (it.hasNext()) {
            ImmutableInfoState is = (ImmutableInfoState)it.next();
            if (is.hasCell(fqName)) {
                return true;
            }
        }
        return false;
    }

    public void flushLocalState()
    {
        Iterator it = infoStates.iterator();
        while (it.hasNext()) {
            ImmutableInfoStateImpl is = (ImmutableInfoStateImpl)it.next();
            is.flushLocalState();
        }
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
}
