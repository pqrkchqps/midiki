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
package org.mitre.midiki.impl.mitre;

import java.util.*;

/**
 * Register an <code>ServiceHandler</code> to handle
 * events that arrive from the framework. The types of the
 * arguments aren't defined, because they are likely to be
 * framework dependent.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface ServiceHandler
{
    /**
     * Handle a specific framework event.
     *
     * @param event an <code>Object</code> describing the event
     * @param parameters an <code>Object</code> holding the event parameters
     * @param results an <code>Object</code> holding your results
     * @return <code>true</code> if this agent handled this event
     */
    public boolean handleEvent(String event,
                               List parameters,
                               Collection results);
}
