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
 * With an <code>ImmutableInfoStateImpl</code>, the user can
 * access cell attributes and call query methods, but cannot
 * change the contents of the information state. (Provided,
 * of course, that all query methods obey the Midiki contract
 * for queries. It is outside the scope of this API to enforce
 * that contract.)
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class ImmutableInfoStateImpl implements ImmutableInfoState
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.ImmutableInfoStateImpl");
    protected HashMap cellMap;

    public ImmutableInfoStateImpl()
    {
        cellMap = new HashMap();
    }

    public void addImmutableCell(String name, ImmutableCell cell)
    {
        cellMap.put(name, cell);
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
        Object icobj = cellMap.get(fqName);
        if (icobj==null) {
            RuntimeException ex = new RuntimeException("No cell '"+fqName+"' in infostate (null value)");
            logger.throwing("org.mitre.midiki.impl.mitre.ImmutableInfoStateImpl","cell",ex);
            throw ex;
        }
        ImmutableCellImpl cell = (ImmutableCellImpl)icobj;
        cell.loadInstance(cell.client.rootInstanceId());
        logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.ImmutableInfoStateImpl","cell","fqname="+fqName+",id="+cell.client.rootInstanceId(),cell.rootInstance.instanceData);
        return (ImmutableCell)cell;
    }

    /**
     * Return all of the <code>Contract</code>s available in this infostate.
     *
     * @return a <code>Collection</code> value
     */
    public Set getContracts()
    {
        Set contracts = new HashSet();
        Iterator it = cellMap.values().iterator();
        while (it.hasNext()) {
            ImmutableCellImpl cit = (ImmutableCellImpl)it.next();
            contracts.add(cit.getContract());
        }
        return contracts;
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
        return (cellMap.get(fqName) != null);
    }

    public void flushLocalState()
    {
        Iterator it = cellMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry)it.next();
            ((ImmutableCellImpl)me.getValue()).flushLocalInstanceTable();
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
