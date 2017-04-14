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

import org.mitre.midiki.logic.*;
import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;

import java.util.*;
import java.util.logging.*;

/**
 * Implements a basic ImmutableCell.
 * 
 */
 
public class ImmutableCellImpl implements ImmutableCell
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.ImmutableCellImpl");
    protected Contract cellContract;
    protected CellClient client;
    public void setClient(CellClient c)
    {
        client = c;
    }
    public CellClient getClient()
    {
        return client;
    }
    /**
     * Describe variable <code>cellAttributes</code> here.
     */
    protected HashMap cellAttributes;
    /**
     * The current set of attribute data associated with this cell;
     * '*thisptr'.
     */
    protected CellInstance rootInstance;
    protected HashMap localInstanceTable;
    /**
     * Describe variable <code>queries</code> here.
     */
    protected HashMap queryHandlers;
    protected ImmutableInfoState owningInfoState;
    /**
     * Creates a new <code>CellImpl</code> instance.
     *
     * @param c a <code>Contract</code> value
     */
    public ImmutableCellImpl(Contract c, ImmutableInfoState iis)
    {
        if (c==null) {
            logger.warning("Instantiating a cell with a null contract");
            return;
        }
        cellContract = c;
        owningInfoState = iis;
        cellAttributes = new HashMap();
        Iterator atIt = c.attributes();
        for (int index = 0; atIt.hasNext(); index++) {
            Contract.Attribute ca = (Contract.Attribute)atIt.next();
            cellAttributes.put(ca.name(), new Integer(index));
        }
        queryHandlers = new HashMap();
        localInstanceTable = new HashMap();
    }
    /**
     * Get the definition of this information state component.
     */
    public Contract getContract()
    {
        return cellContract;
    }
    /**
     * Get the value of the specified attribute.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>Object</code> value
     */
    public Object get(String fqName)
    {
        Object iobj = cellAttributes.get(fqName);
        if (iobj == null) {
            throw new RuntimeException("No attribute '"+fqName+"' in contract '"+cellContract.name()+"'");
        }
        Integer index = (Integer)iobj;
        if (rootInstance != null)
            return rootInstance.instanceData.get(index.intValue());
        else {
            System.out.println("no instance data for cell "+cellContract.name());
            return null;
        }
    }
    /**
     * Get an immutable reference to the specified <code>Cell</code> attribute.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>ImmutableCell</code> value
     */
    public ImmutableCell cell(String fqName)
    {

        if (((AttributeImpl)getContract().attribute(fqName)).type() instanceof Contract) {
            String contractName = ((Contract)((AttributeImpl)getContract().attribute(fqName)).type()).name();
            ImmutableCellImpl cell = (ImmutableCellImpl)owningInfoState.cell(contractName);
            Object instanceKey = get(fqName);
            if (instanceKey == null) {
                CellInstance newInstance = client.newInstance();
                logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","creating",newInstance);
                instanceKey = newInstance.instanceId;
                // put the new value. this is a departure from the typical
                // operation here; normally this is immutable, but to prevent
                // access to null cells we build a new blank cell here.
                // probably want to rethink this at leisure.
                Integer index = (Integer)cellAttributes.get(fqName);
                logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","adding",fqName);
                logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","instance",cell.rootInstance.instanceData);
                Object oldValue = rootInstance.instanceData.get(index.intValue());
                logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","old-value",oldValue);
                rootInstance.instanceData.set(index.intValue(), instanceKey);
                logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","new-instance",rootInstance.instanceData);
            }
            cell.loadInstance(instanceKey);
            return (ImmutableCell)cell;
        } else {
            RuntimeException ex =
                new RuntimeException("No contract for attribute '"+fqName+"'");
            logger.throwing("org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell",ex);
            throw ex;
        }

/*
        ImmutableCellImpl icell =
            (ImmutableCellImpl)owningInfoState.cell(fqName);
        Object instanceKey = get(fqName);
        if (instanceKey == null) {
            CellInstance newInstance = client.newInstance();
            instanceKey = newInstance.instanceId;
            // put the new value. this is a departure from the typical
            // operation here; normally this is immutable, but to prevent
            // access to null cells we build a new blank cell here.
            // probably want to rethink this at leisure.
            Integer index = (Integer)cellAttributes.get(fqName);
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","adding",fqName);
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","instance",icell.rootInstance.instanceData);
            Object oldValue = rootInstance.instanceData.get(index.intValue());
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","old-value",oldValue);
            rootInstance.instanceData.set(index.intValue(), instanceKey);
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","new-instance",rootInstance.instanceData);
        }
        icell.loadInstance(instanceKey);
        logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","fqname",fqName);
        logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","id",instanceKey);
        logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","cell","instance-data",icell.rootInstance.instanceData);
        return (ImmutableCell)icell;
*/
    }
    public void loadInstance(Object instanceKey)
    {
        /*
        try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        /**/
        if (instanceKey == null) {
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","loadInstance","null instanceKey");
            return;
        }
        logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","loadInstance",instanceKey.toString());
        Object found = localInstanceTable.get(instanceKey);
        if (found != null) {
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","loadInstance","found in local store");
            rootInstance = (CellInstance)found;
        } else {
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.ImmutableCellImpl","loadInstance","copying from client");
            rootInstance = client.findInstance(instanceKey).copy();
            localInstanceTable.put(instanceKey, rootInstance);
        }
        /**/
    }

    public void flushLocalInstanceTable()
    {
        localInstanceTable.clear();
        rootInstance = null;
    }

    /**
     * Locates the query method specified by the name.
     * Returns <code>null</code> if there is no query by that name
     * in the <code>Contract</code> provided by this <code>Cell</code>.
     *
     * @param fqName fully qualified name of the method to be called
     * @return a handler for the query
     */
    public QueryHandler query(String fqName)
    {
        Object q = queryHandlers.get(fqName);
        if (q==null) {
            RuntimeException ex =
                new RuntimeException("No query '"+fqName+"' in contract '"+cellContract.name()+"'");
            logger.throwing("org.mitre.midiki.impl.mitre.ImmutableCellImpl","query",ex);
            throw ex;
        }
        return (QueryHandler)q;
    }
    /**
     * Adds a <code>QueryHandler</code> for the specified query.
     *
     * @param n a <code>String</code> value
     * @param qh a <code>QueryHandler</code> value
     * @return a <code>boolean</code> value
     */
    public boolean addQueryHandler(String n, QueryHandler qh)
    {
        Iterator atIt = cellContract.queries();
        while (atIt.hasNext()) {
            Contract.Query q = (Contract.Query)atIt.next();
            if (q.name().equals(n)) {
                queryHandlers.put(n,qh);
                return true;
            }
        }
        return false;
    }
}
