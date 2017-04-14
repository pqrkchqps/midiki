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
 * Implements a <code>Cell</code> which is controlled locally.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see CellStore
 */
public class CellImpl implements Cell
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.CellImpl");
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
     * Describe variable <code>cellContract</code> here.
     */
    protected Contract cellContract;
    /**
     * Describe variable <code>cellView</code> here.
     */
    protected ImmutableCellImpl cellView;
    protected InfoState owningInfoState;
    /**
     * Describe variable <code>methodHandlers</code> here.
     */
    protected HashMap methodHandlers;
    /**
     * Surrogate for <code>ImmutableCell</code> handler storage.
     * When an agent creates this cell, it needs to attach
     * query handlers before the information state is constructed.
     * The API probably ought to be changed, but let's run with this
     * for now.
     */
    protected HashMap queryHandlers;

    /**
     * Creates a new <code>CellImpl</code> instance.
     * NOTE: need a way to get the mediator and infostate
     * down into the CellStore
     *
     * @param c a <code>Contract</code> value
     */
    public CellImpl(Contract c, InfoState is)
    {
        owningInfoState = is;
        cellContract = c;
        methodHandlers = new HashMap();
        queryHandlers = new HashMap();
    }

    /**
     * Get the definition of this information state component.
     */
    public Contract getContract()
    {
        return cellContract;
    }
    /**
     * Get a read-only view of this cell.
     *
     * @return an <code>ImmutableCell</code> value
     */
    public ImmutableCell getView()
    {
        return cellView;
    }
    /**
     * Get the value of the specified attribute.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>Object</code> value
     */
    public Object get(String fqName)
    {
        return cellView.get(fqName);
    }
    /**
     * Make a copy of the cell and return an instance handle.
     * Recursively copies any subordinate cells as well.
     *
     * @return an <code>Object</code> value
     */
    public Object copy()
    {
        /*
        if (value instanceof Cell) {
            cellToCopy = (Cell)value;
            if (cellToCopy.getContract().equals(getContract())) {
                // for each attribute/cell, store a clone if possible.
                // if Cloneable not supported, just store the value.
                Iterator it = getContract().attributes();
                while (it.hasNext()) {
                    AttributeImpl attr = (AttributeImpl)it.next();
                    Object copyValue = cellToCopy.get(attr.name());
                    if (attr.type() instanceof Contract) {
                        // subordinate cell; copy recursively
                        cell(attr.name());
                    }
                    if (copyValue instanceof Cloneable) {
                        try {
                            copyValue = copyValue.clone();
                        } catch (CloneNotSupportedException cnse) {
                        }
                    }
                    put(attr.name(), copyValue);
                }
            } else {
                RuntimeException ex =
                    new RuntimeException("Incompatible contract '"+cellToCopy.getContract.name()+"' for copy into '"+cellContract.name()+"'");
                logger.throwing("org.mitre.midiki.impl.mitre.CellImpl","put",ex);
                throw ex;
            }
        }
         */
        CellInstance newInstance = cellView.client.newInstance();
        CellInstance oldInstance = cellView.rootInstance.copy();
        newInstance.instanceData = oldInstance.instanceData;
        logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellImpl","copy","creating",newInstance);
        // need to iterate over attributes, cloning where possible.
        // otherwise items like lists will still be changed in both
        // master and copy.
        Object instanceKey = newInstance.instanceId;
        return instanceKey;
    }
    /**
     * Set the <code>ImmutableCell</code> used for this cell's
     * attribute storage.
     *
     * @param cv an <code>ImmutableCellImpl</code> value
     * @return a <code>boolean</code> value
     */
    public boolean setView(ImmutableCellImpl cv)
    {
        if (cellContract != cv.getContract())
            return false;
        cellView = cv;
        Iterator qIt = queryHandlers.keySet().iterator();
        while (qIt.hasNext()) {
            String n = (String)qIt.next();
            cellView.addQueryHandler(n, (QueryHandler)queryHandlers.get(n));
        }
        queryHandlers.clear();
        return true;
    }
    /**
     * Assign a value to an attribute of the information state
     * specified by the fully qualified name. If the attribute
     * is typed, the value must be of a compatible type (or a
     * <code>ClassCastException</code> will be thrown).
     *
     * @param fqName a <code>String</code> value
     * @param value an <code>Object</code> value
     */
    public void put(String fqName,
                    Object value)
    {
        if (((AttributeImpl)getContract().attribute(fqName)).type() instanceof Contract) {
            // we have one fewer reference to the previous instance.
            // modify instance reference counts appropriately.
            String contractName = ((Contract)((AttributeImpl)getContract().attribute(fqName)).type()).name();
            CellImpl cell = (CellImpl)owningInfoState.cell(contractName);
            Object instanceKey = get(fqName);
            CellInstance inst;
            if (instanceKey != null) {
                // decrement the old instance
                inst = cell.cellView.client.findInstance(instanceKey);
                inst.dropRef();
            }
            inst = cell.cellView.client.findInstance(value);
            if (inst != null) inst.addRef();
        }
        Integer index = (Integer)cellView.cellAttributes.get(fqName);
        logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellImpl","put","attribute",fqName);
        logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellImpl","put","value",value);
        if (cellView.rootInstance != null) {
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.CellImpl","put","instance",cellView.rootInstance.instanceData);
            Object oldValue = cellView.rootInstance.instanceData.get(index.intValue());
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.CellImpl","put","fetched-value",oldValue);
            cellView.rootInstance.instanceData.set(index.intValue(), value);
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.CellImpl","put","new-instance",cellView.rootInstance.instanceData);
            cellView.client.recordChange(oldValue, value, cellView.rootInstance);
        } else {
            RuntimeException ex =
                new RuntimeException("No accessible instance of contract '"+cellContract.name()+"'");
            logger.throwing("org.mitre.midiki.impl.mitre.CellImpl","put",ex);
            throw ex;
        }
    }
    /**
     * Get a reference to the specified <code>Cell</code> attribute.
     * Loads the data for that instance.
     *
     * @param fqName a <code>String</code> value
     * @return a <code>Cell</code> value
     */
    public Cell cell(String fqName)
    {
        if (((AttributeImpl)getContract().attribute(fqName)).type() instanceof Contract) {
            // we have one fewer reference to the previous instance.
            // modify instance reference counts appropriately.
            String contractName = ((Contract)((AttributeImpl)getContract().attribute(fqName)).type()).name();
            CellImpl cell = (CellImpl)owningInfoState.cell(contractName);
            Object instanceKey = get(fqName);
            if (instanceKey == null) {
                CellInstance newInstance = cell.cellView.client.newInstance();
                logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellImpl","cell","creating",newInstance);
                instanceKey = newInstance.instanceId;
                put(fqName,instanceKey);
            }
            cell.cellView.loadInstance(instanceKey);
            return (Cell)cell;
        } else {
            RuntimeException ex =
                new RuntimeException("No contract for attribute '"+fqName+"'");
            logger.throwing("org.mitre.midiki.impl.mitre.CellImpl","cell",ex);
            throw ex;
        }
    }
    /**
     * Register interest in changes to the attribute specified
     * by the fully qualified attribute name <code>fqName</code>.
     * When that attribute changes, the specified handler will be
     * invoked. It is the responsibility of the handler to ensure
     * that the new value of the attribute meets the conditions.
     *
     * @param fqName a <code>String</code> value
     * @param listener a <code>InfoListener</code> value
     * @return a <code>boolean</code> value
     */
    public boolean addInfoListener(String fqName,
                                   InfoListener listener)
    {
        client.addInfoListener(fqName, listener, (InfoStateImpl)owningInfoState);
        return true;
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
        return cellView.query(fqName);
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
                if (cellView == null) {
                    queryHandlers.put(n,qh);
                } else {
                    cellView.addQueryHandler(n,qh);
                }
                return true;
            }
        }
        return false;
    }
    /**
     * Locates the handler for the action method specified by the name.
     * The specified arguments are passed to the method, and may be modified
     * by variable assignments within the current set of bindings. The
     * method may bind additional variables.
     *
     * @param fqName fully qualified name of the method to be called
     * @return a <code>MethodHandler</code> for the method
     */
    public MethodHandler method(String fqName)
    {
        Object q = methodHandlers.get(fqName);
        if (q==null) {
            RuntimeException ex =
                new RuntimeException("No method '"+fqName+"' in contract '"+cellContract.name()+"'");
            logger.throwing("org.mitre.midiki.impl.mitre.CellImpl","method",ex);
            throw ex;
        }
        return (MethodHandler)q;
    }
    /**
     * Adds a <code>MethodHandler</code> for the specified method.
     *
     * @param n a <code>String</code> value
     * @param qh a <code>MethodHandler</code> value
     * @return a <code>boolean</code> value
     */
    public boolean addMethodHandler(String n, MethodHandler qh)
    {
        Iterator atIt = cellContract.methods();
        while (atIt.hasNext()) {
            Contract.Method q = (Contract.Method)atIt.next();
            if (q.name().equals(n)) {
                methodHandlers.put(n,qh);
                return true;
            }
        }
        return false;
    }
}
