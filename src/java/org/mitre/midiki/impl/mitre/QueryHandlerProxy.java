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

import org.mitre.midiki.logic.Bindings;
import org.mitre.midiki.state.*;
import java.util.*;

/**
 * Provides the executable code for a <code>Query</code>.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class QueryHandlerProxy implements QueryHandler
{
    protected Contract.Query query;
    protected ImmutableCellImpl cell;
    protected CellClient client;
    public QueryHandlerProxy(String name, ImmutableCellImpl ic, CellClient cl)
    {
        Iterator qit = ic.getContract().queries();
        while (qit.hasNext()) {
            Contract.Query q = (Contract.Query)qit.next();
            if (name.equals(q.name())) {
                query = q;
                break;
            }
        }
        cell = ic;
        client = cl;
    }
    /**
     * Executes the query method specified by the fully qualified name.
     * The specified arguments are passed to the query, and may be modified
     * by variable assignments within the current set of bindings. The
     * query may bind additional variables.
     *
     * @param arguments a <code>Collection</code> value
     * @param bindings a <code>Bindings</code> value
     * @return <code>true</code> if the query succeeded for at least one
     * set of variable assignments
     */
    public boolean query(Collection arguments,
                         Bindings bindings)
    {
        String tag = "query$"+cell.getContract().name()+"$"+query.name();
        LinkedList parameters = new LinkedList(arguments);
        if (client.mediatedBy.usesMidikiProtocol()) {
            if ((cell==null) || (cell.rootInstance==null)) {
                parameters.addFirst(null);
            } else {
                parameters.addFirst(cell.rootInstance.instanceId);
            }
        }
        Collection results = new LinkedList();
        results.addAll(bindings.marshalAll());
        boolean success = client.mediatedBy.useService(tag, parameters, results);
        bindings.unmarshalLatest(results);
        return success;
    }
}
