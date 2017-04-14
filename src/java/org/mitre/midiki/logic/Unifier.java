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

/**
 * Provides support for unification of logical terms.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface Unifier
{
    /**
     * Returns a new term which is the unification of the
     * two terms. If any variables must be bound for this
     * to take place, they are only bound long enough to
     * generate a result.
     *
     * @param term1 an <code>Object</code> value
     * @param term2 an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object unify(Object term1,
                        Object term2);
    /**
     * Returns a new term which is the unification of the
     * two terms, using the existing variable bindings and
     * storing any additional bindings required.
     *
     * @param term1 an <code>Object</code> value
     * @param term2 an <code>Object</code> value
     * @param bindings a <code>Bindings</code> value
     * @return an <code>Object</code> value
     */
    public Object unify(Object term1,
                        Object term2,
                        Bindings bindings);
    /**
     * Compares the two terms, generating candidate variable
     * bindings that would allow them to unify.
     *
     * @param term1 an <code>Object</code> value
     * @param term2 an <code>Object</code> value
     * @param bindings a <code>Bindings</code> value
     * @return a <code>boolean</code> value
     */
    public boolean matchTerms(Object term1,
                              Object term2,
                              Bindings bindings);
    /**
     * Generates a copy of the logical term after substituting
     * all applicable variable bindings.
     *
     * @param term an <code>Object</code> value
     * @param bindings a <code>Bindings</code> value
     * @return an <code>Object</code> value
     */
    public Object deref(Object term,
                        Bindings bindings);
}
