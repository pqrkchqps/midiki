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
package org.mitre.midiki.tools;

import java.util.*;
import java.util.logging.*;

/**
 * Provides Midiki support for filtering and viewing log events.
 * Particularly useful for transducing logging API events into
 * events which are meaningful to dialogue researchers.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see Handler
 */
public class LogEventHandler extends Handler
{
    public class EventList extends Observable {
        /**
         * List of events received by this handler. The list is synchronized.
         */
        public List events;
        public EventList()
        {
            super();
            events = new Vector(1000,1000);
        }
        public void clear()
        {
            events.clear();
            events = null;
        }
        public void publish(Object event)
        {
            if (events != null) {
                events.add(event);
                setChanged();
                notifyObservers(event);
            }
        }
    }
    public EventList el;

    /**
     * Close the Handler and free all associated resources. 
     *
     * The close method will perform a flush and then close the Handler.
     * After close has been called this Handler should no longer be used.
     * Method calls may either be silently ignored or may throw runtime
     * exceptions. 
     *
     * @exception SecurityException if an error occurs
     */
    public void close() throws SecurityException
    {
        flush();
        el.clear();
    }

    public void flush()
    {
    }

    /**
     * Publish a LogRecord. 
     *
     * The logging request was made initially to a Logger object, which
     * initialized the LogRecord and forwarded it here. 
     *
     * The Handler is responsible for formatting the message, when and if
     * necessary. The formatting should include localization. 
     *
     * @param logRecord description of the logged event
     */
    public void publish(LogRecord logRecord)
    {
        // store all incoming records. (storage must be threadsafe, cheap)
        el.publish(logRecord);
    }

    /**
     * Creates a new <code>LogEventHandler</code> instance.
     *
     */
    public LogEventHandler()
    {
        super();
        el = new EventList();
    }
}
