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

import java.util.*;
/**
 * Provides handlers for an information state component cell.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class CellHandlers
{
    /**
     * Describe variable <code>cellContract</code> here.
     */
    protected Contract cellContract;
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
     * Creates a new <code>RemoteCellImpl</code> instance.
     * NOTE: need a way to get the mediator and infostate
     * down into the CellStore
     *
     * @param c a <code>Contract</code> value
     */
    public CellHandlers(Contract c)
    {
        cellContract = c;
        methodHandlers = new HashMap();
        queryHandlers = new HashMap();
    }
    /**
     * Returns a string representation of this object.
     * Default is to return the name in the handled contract.
     */
    public String toString()
    {
        return cellContract.name();
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

    /**
     * Get the definition of this information state component.
     */
    public Contract getContract()
    {
        return cellContract;
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
        return (QueryHandler)queryHandlers.get(fqName);
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
        return (MethodHandler)methodHandlers.get(fqName);
    }
}
