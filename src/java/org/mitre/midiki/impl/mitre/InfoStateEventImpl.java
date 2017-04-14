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

import org.mitre.midiki.state.*;

/**
 * Encapsulates information about an <code>InfoState</code> event.
 * In Midiki 1.0, those reflect changes in attribute values.
 * Future versions may implement additional event types.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class InfoStateEventImpl implements InfoStateEvent
{
    /**
     * Get the fully qualified name of the attribute that changed.
     * This will correspond to an attribute name that was registered
     * with the <code>InfoState</code>.
     *
     * @return a <code>String</code> value
     */
    public String getAttribute()
    {
        return attribute;
    }
    /**
     * Get a reference to the <code>InfoState</code>.
     *
     * @return an <code>InfoState</code> value
     */
    public InfoState getInfoState()
    {
        return infoState;
    }
    protected String attribute;
    protected InfoState infoState;
    public InfoStateEventImpl(String a, InfoState is)
    {
        attribute = a;
        infoState = is;
    }
    public void setEvent(String a, InfoState is)
    {
        attribute = a;
        infoState = is;
    }
}
