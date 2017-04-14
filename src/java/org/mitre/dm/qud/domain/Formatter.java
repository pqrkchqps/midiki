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

import java.util.List;

/**
 * Defines an operation to assemble list elements into a string
 * along application-dependent lines.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface Formatter
{
    /**
     * Assembles the list elements into a single text string.
     *
     * @param l a <code>List</code> value
     * @return a <code>String</code> value
     */
    public String format(List l);
}
