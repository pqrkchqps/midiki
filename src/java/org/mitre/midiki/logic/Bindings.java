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
package org.mitre.midiki.logic;

import java.util.Map;
import java.util.Collection;

/**
 * Provides a means for binding variables to values.
 * The map is extended to support binding scopes
 * and multiple alternative bindings, encapsulating
 * backtracking operations.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface Bindings extends Map
{
    /**
     * Describe <code>get</code> method here.
     *
     * @param variable a <code>String</code> value
     * @return an <code>Object</code> value
     */
    public Object get(String variable);
    /**
     * Describe <code>get</code> method here.
     *
     * @param variable a <code>Variable</code> value
     * @return an <code>Object</code> value
     */
    public Object get(Variable variable);
    /**
     * Unbinds any variables bound by the current context and attempts to
     * rebind them to the next alternative. Fails if there are no remaining
     * alternatives.
     *
     * @return <code>true</code> if another alternate binding is available
     */
    public boolean backtrack();
    /**
     * Marks the start of a new binding scope.
     *
     * @return a <code>boolean</code> value
     */
    public boolean enterScope();
    /**
     * Marks the end of a binding scope. Any variables bound since the
     * last call to <code>enterScope</code> will be unbound, and the
     * scope stack will be popped.
     *
     * @return a <code>boolean</code> value
     */
    public boolean exitScope();
    /**
     * Resets all variable bindings for the current scope,
     * defining the start of a new alternative solution.
     *
     */
    public void reset();
    /**
     * Convert the bindings into a form that can be transmitted
     * to other agents. This form marshals all scopes for transmittal.
     *
     * @return a <code>Collection</code> value
     */
    public Collection marshalAll();
    /**
     * Convert the bindings into a form that can be transmitted
     * to other agents. This form marshals only the latest scope.
     *
     * @return a <code>Collection</code> value
     */
    public Collection marshalLatest();
    /**
     * Convert the incoming <code>Collection</code>, transmitted
     * from another agent, into bindings. Unpacks as many scopes
     * as were provided. Requires that the input format matches
     * the output format of <code>marshallAll()</code>
     *
     * @param c a <code>Collection</code> value
     */
    public void unmarshalAll(Collection c);
    /**
     * Convert the incoming <code>Collection</code>, transmitted
     * from another agent, into bindings. Unpacks a single scope.
     * Requires that the input format matches the output format
     * of <code>marshalLatest()</code>.
     *
     * @param c a <code>Collection</code> value
     */
    public void unmarshalLatest(Collection c);
}
