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
 * Describe interface <code>Filter</code> here.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface Filter
{
    /**
     * Filters the item, performing any required translation.
     * Typically an output operation.
     *
     * @param input an <code>Object</code> to be filted.
     * Implementing classes may expect specific subclasses as input.
     * @param interpretations a <code>Collection</code> of values
     * transduced from the input.
     * @return <code>true</code> if this filt succeeded.
     */
    public Object filter(Object input);
}
