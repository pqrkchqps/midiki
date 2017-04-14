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
package org.mitre.midiki.agent;

import java.util.logging.Logger;

/**
 * A <code>DialogueSystem</code> encapsulates a set of <code>Agent</code>s
 * and their associated <code>InfoState</code>s.
 *
 * The functionality associated with a Midiki dialogue system
 * is still under investigation. It isn't clear, for example,
 * whether the interface should mandate exposure of the
 * agent configuration parameters, agent population, or other
 * non-infostate communication between agents. It is clear,
 * however, that it is difficult or impossible to ensure that
 * all of the dialogue-relevant log information for an instance
 * of a dialogue system (and only that instance) is logged in
 * the desired way without providing a standard logging interface
 * that all of the agents can access in an implementation-independent way.
 * This is particularly important for applications which run multiple
 * Midiki DMs in a single Java VM, or for systems which distribute
 * a single Midiki DM across multiple VMs/machines.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface DialogueSystem
{
    /**
     * Return the central point for logging dialogue system events.
     *
     * @return a <code>Logger</code> value
     */
    public Logger getLogger();
    /**
     * Return the central point for logging dialogue system events.
     * Allows logging with class-based logger names while still
     * maintaining cohesion of logs for a single system.
     *
     * @param name a <code>String</code> value
     * @return a <code>Logger</code> value
     */
    public Logger getLogger(String name);
    /**
     * Add the specified listener to the notification queue
     * for dialogue system events.
     *
     * @param dsl DialogueSystemListener to be added
     */    
    public void addDialogueSystemListener(DialogueSystemListener dsl);
    /**
     * Fetch the list of currently registered dialogue system listeners.
     *
     * @return
     */    
    public DialogueSystemListener[] getDialogueSystemListeners();
    /**
     * Remove the specified listener from the notification queue
     * for dialogue system events.
     *
     * @param dsl DialogueSystemListener to be removed
     */    
    public void removeDialogueSystemListener(DialogueSystemListener dsl);
}
