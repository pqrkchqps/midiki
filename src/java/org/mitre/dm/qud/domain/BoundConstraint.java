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
package org.mitre.dm.qud.domain;

import org.mitre.midiki.logic.*;

/**
 * Permits unification if the value is anything but a free variable.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class BoundConstraint implements Constraint
{
    /**
     * Called when the constrained <code>Variable</code> is about
     * to be bound. Returning <code>false</code> will prevent unification
     * from succeeding. This is not intended to substitute for a complete
     * constraint programming system, but merely to provide a simple
     * declarative way of including occasional simple constraints.
     * One anticipated application is matching output moves in the
     * lexicon, where matching may dependent upon whether a variable
     * is free, bound, or bound to a <code>Collection</code>.
     *
     * @param value an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean test(Object value, Bindings bindings)
    {
        return !(value instanceof Variable);
    }
}
