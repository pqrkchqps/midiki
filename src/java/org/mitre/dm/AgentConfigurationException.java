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
package org.mitre.dm;

/**
 * Thrown when the system is unable to resolve an agent class.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see Exception
 */
public class AgentConfigurationException extends Exception {

    public AgentConfigurationException(String msg)
    {
        super(msg);
    }

    public AgentConfigurationException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    public AgentConfigurationException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Returns the underlying <code>Exception</code>, if any.
     * Legacy call, superceded by Exception.getCause().
     *
     * @deprecated
     * @return an <code>Exception</code> value
     */
    public Exception getException() {
        return (Exception)getCause();
    }
}
