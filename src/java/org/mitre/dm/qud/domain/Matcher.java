/****************************************************************************
 *
 * Copyright (C) 2005. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 * Consult the LICENSE file in the root of the distribution for terms and restrictions.
 *
 *       Release: 1.0
 *       Date: 13-January-2005
 *       Author: Carl Burke
 *
 *****************************************************************************/
package org.mitre.dm.qud.domain;

import java.util.Collection;

/**
 * Describe interface <code>Matcher</code> here.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface Matcher
{
    /**
     * Retrieve any application-specific tags which might be used
     * in conjunction with this matcher. Typically used to help decide
     * whether this matcher should be applied to a particular input.
     *
     * @return an <code>Object</code> value
     */
    public Object getTags();
    /**
     * Get the <code>MatchSet</code> this <code>Matcher</code>
     * is defined in.
     *
     * @return a <code>MatchSet</code> value
     */
    public MatchSet getOwningSet();
    /**
     * Set the <code>MatchSet</code> this <code>Matcher</code>
     * is defined in.
     *
     * @param set a <code>MatchSet</code> value
     */
    public void setOwningSet(MatchSet set);
    /**
     * Attempts to match the input item, adding any resulting
     * interpretations to the existing results.
     *
     * @param input an <code>Object</code> to be matched.
     * Implementing classes may expect specific subclasses as input.
     * @param interpretations a <code>Collection</code> of values
     * transduced from the input.
     * @return <code>true</code> if this match succeeded.
     */
    public boolean match(Object input, Collection interpretations);
    /**
     * Check the match for problems with internal consistency.
     * Most common error: unbound variables in output.
     *
     * @return true if match is internally consistent, false otherwise.
     */
    public boolean checkMatchConsistency();
}
