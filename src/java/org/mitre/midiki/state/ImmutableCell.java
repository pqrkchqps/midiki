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

import org.mitre.midiki.logic.Bindings;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.HashMap;
/**
 * Provides access to an information state component.
 * The information state is a forest of typed feature structures,
 * where the features may be attributes or methods. Methods, in turn,
 * may be queries (which may return multiple alternate values but must
 * not alter the info state) or actions (which modify the info state
 * and may bind variables, but may not be backtracked over).
 * The <code>ImmutableCell</code> interface allows read-only access
 * to the data represented by its <code>Contract</code>.<p>
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface ImmutableCell
{
    /* {src_lang=Java}*/

    /**
     * Get the definition of this information state component.
     */
    public Contract getContract();
    /**
     * Get the value of the specified attribute.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>Object</code> value
     */
    public Object get(String fqName);
    /**
     * Get an immutable reference to the specified <code>Cell</code> attribute.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>ImmutableCell</code> value
     */
    public ImmutableCell cell(String fqName);
    /**
     * Locates the query method specified by the name.
     * Returns <code>null</code> if there is no query by that name
     * in the <code>Contract</code> provided by this <code>Cell</code>.
     *
     * @param fqName fully qualified name of the method to be called
     * @return a handler for the query
     */
    public QueryHandler query(String fqName);
}
