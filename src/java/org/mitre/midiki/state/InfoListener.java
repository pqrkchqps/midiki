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

/**
 * Provides a listener interface for monitoring changes to
 * information state attributes. Instances of <code>InfoListener</code>
 * are registered with an <code>InfoState</code> through calls to
 * <code>addInfoListener</code>.<p>
 * When <code>infoChanged</code> is called from the <code>InfoState</code>,
 * external changes will be queued for later processing, and changes
 * made to the local <code>InfoState</code> will not be broadcast
 * until you return from the call.<p>
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface InfoListener
{
    /* {src_lang=Java}*/

    /**
     * Called when the associated attribute is changed.<p>
     *
     * NOTE: this interface may be changed to remove the value
     * argument, as it adds little functionality at increased cost.
     *
     * @param event an <code>InfoStateEvent</code> describing
     * the event
     * @return a <code>boolean</code> value
     */
    public void infoChanged(InfoStateEvent event);
}
