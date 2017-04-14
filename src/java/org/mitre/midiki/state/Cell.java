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
 * Provides metadata for an information state component.
 * The information state is a forest of typed feature structures,
 * where the features may be attributes or methods. Methods, in turn,
 * may be queries (which may return multiple alternate values but must
 * not alter the info state) or actions (which modify the info state
 * and may bind variables, but may not be backtracked over).
 * the <code>Cell</code> interface provides access to the
 * information state component specified by its <code>Contract</code>.<p>
 * In order to enforce the dichotomy between read-only access and
 * full access while still allowing the use of the same method names,
 * separate interfaces are required. This isn't ideal.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface Cell
{
    /* {src_lang=Java}*/

    /**
     * Get the definition of this information state component.
     */
    public Contract getContract();
    /**
     * Get a read-only view of this cell.
     *
     * @return an <code>ImmutableCell</code> value
     */
    public ImmutableCell getView();
    /**
     * Get the value of the specified attribute.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>Object</code> value
     */
    public Object get(String fqName);
    /**
     * Make a copy of the cell and return an instance handle.
     * Does not recursively copy any subordinate cells;
     * this is a shallow copy.
     *
     * @return an <code>Object</code> value
     */
    public Object copy();
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
                    Object value);
    /**
     * Get a reference to the specified <code>Cell</code> attribute.
     *
     * @param fqName a <code>String</code> value
     * @return a <code>Cell</code> value
     */
    public Cell cell(String fqName);
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
                                   InfoListener listener);
    /**
     * Locates the query method specified by the name.
     * Returns <code>null</code> if there is no query by that name
     * in the <code>Contract</code> provided by this <code>Cell</code>.
     *
     * @param fqName fully qualified name of the method to be called
     * @return a handler for the query
     */
    public QueryHandler query(String fqName);
    /**
     * Locates the handler for the action method specified by the name.
     * The specified arguments are passed to the method, and may be modified
     * by variable assignments within the current set of bindings. The
     * method may bind additional variables.
     *
     * @param fqName fully qualified name of the method to be called
     * @return a <code>MethodHandler</code> for the method
     */
    public MethodHandler method(String fqName);
}
