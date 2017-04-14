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

import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Coordinates the application of one or more <code>Matcher</code>s.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class MatchSet
{
    protected LinkedList matches;
    public MatchSet()
    {
        matches = new LinkedList();
    }
    /**
     * Returns true if the specified Matcher is already contained
     * within this matchset. Ultimately relies on the implementation
     * of equals() for the classes.
     */
    public boolean contains(Matcher match)
    {
        return matches.contains(match);
    }
    /**
     * Add a <code>Matcher</code> to be considered during matching.
     *
     * @param match a <code>Matcher</code> value
     */
    public void addMatcher(Matcher match)
    {
        if (matches.contains(match)) return;
        matches.add(match);
    }
    /**
     * Return an <code>Iterator</code> over all available <code>Matcher</code>s.
     *
     * @return an <code>Iterator</code> value
     */
    public Iterator matchers()
    {
        return matches.iterator();
    }
    public boolean fitsCriteria(Matcher match, Object criteria)
    {
        return true;
    }
    /**
     * Apply all of the matchers in this set to the supplied input,
     * provided they fit the supplied application-specific criteria.
     * Transductions of the input are appended to the interpretations.
     *
     * @param input an <code>Object</code> value
     * @param criteria an <code>Object</code> value
     * @param interpretations a <code>Collection</code> value
     * @return a <code>boolean</code> value
     */
    public boolean match(Object input, Object criteria, Collection interpretations)
    {
        boolean matched = false;
        Iterator mit = matchers();
        while (mit.hasNext()) {
            Matcher m = (Matcher)mit.next();
            if (fitsCriteria(m, criteria)) {
                boolean test = m.match(input, interpretations);
                matched = matched || test;
            }
        }
        return matched;
    }
}
