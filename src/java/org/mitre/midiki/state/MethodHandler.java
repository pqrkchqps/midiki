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

/**
 * Provides the executable code for a <code>Method</code>.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface MethodHandler
{
    /**
     * Executes the query method specified by this handler.
     * The specified arguments are passed to the method, and may be modified
     * by variable assignments within the current set of bindings. The
     * method may bind additional variables.
     *
     * @param arguments a <code>Collection</code> value
     * @param bindings a <code>Bindings</code> value
     * @return <code>true</code> if the method succeeded
     */
    public boolean invoke(Collection arguments,
                          Bindings bindings);
}
