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

import java.util.Set;
import org.mitre.midiki.state.InfoState;

/**
 * Defines a major unit of functionality which operates
 * semi-autonomously. In Midiki, an <code>Agent</code>
 * is a significant dialogue system component which
 * accesses an information state and provides certain
 * capabilities. The information requirements are specified
 * by <code>Contract</code>s, while the capabilities the
 * <code>Agent</code> provides are specified by <code>Cell</code>s.
 * The union of those specifications defines the minimal
 * information state this <code>Agent</code> must be given.<p>
 * Packaging functions into agents is driven by concerns
 * outside the realm of dialogue management itself.
 * For example, speech recognition would often be performed
 * by an <code>Agent</code> that is separate from the
 * dialogue move engine <code>Agent</code>.<p>
 * Communication between <code>Agent</code>s occurs through
 * shared sections of the information state.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface Agent
{
    /**
     * Attach the <code>Agent</code> to a <code>DialogueSystem</code>.
     * There are occasional functions, such as logging, to which all
     * of the dialogue system components should present a common identity.
     * This common identity is presumed to extend beyond connection to
     * an information state, i.e. from before initialization to destruction.
     *
     * @param system the <code>DialogueSystem</code> this agent
     * instance belongs to.
     */
    public void attachTo(DialogueSystem system);
    /**
     * Performs one-time <code>Agent</code> initialization.
     * This processing is likely to include initialization
     * of the <code>Contract</code>s and <code>Cell</code>s
     * for the <code>Agent</code>, as well as non-Midiki
     * initialization.
     * @param config TODO
     *
     */
    public void init(Object config);
    /**
     * Release any resources which were created in <code>init</code>.
     * Following this routine, the agent will be terminated.
     *
     */
    public void destroy();
    /**
     * Connects the <code>Agent</code> to the provided information state.
     * In this routine, the <code>Agent</code> should register any
     * <code>Rule</code>s necessary for normal operation, and perform
     * any other <code>InfoState</code>-specific processing. 
     *
     * @param infoState a compatible information state
     * @return <code>true</code> if connection succeeded
     */
    public boolean connect(InfoState infoState);
    /**
     * Disconnects the <code>Agent</code> from the information state.
     * After this call, the <code>InfoState</code> is assumed to be
     * invalid, and no further processing should be performed
     * until another call to <code>connect</code>.
     * (The API does not require that all implementations
     * of <code>Agent</code> be able to <code>connect</code>
     * again following <code>disconnect</code>.)
     *
     */
    public void disconnect();
    /**
     * Get the value for the specified Midiki system property.
     *
     * @param key an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object getProperty(Object key);
    /**
     * Set the value for the specified Midiki system property.
     *
     * @param key an <code>Object</code> value
     * @param value an <code>Object</code> value
     * @return previous value for the property
     */
    public Object putProperty(Object key, Object value);
    /**
     * Get the system identifier for this <code>Agent</code>.
     * This is the name by which it is known to the system
     * as a whole, and should be unique.
     *
     * @return a <code>String</code> value
     */
    public String getId();
    /**
     * Get the name that this <code>Agent</code> calls itself.
     * A Midiki system might have several <code>Agent</code>s
     * with the same name, but each will have a unique id.
     *
     * @return a <code>String</code> value
     */
    public String getName();
    /**
     * Get the set of <code>Contract</code>s this <code>Agent</code>
     * must find in its <code>InfoState</code>. There can be more,
     * but these must be there.
     *
     * @return a <code>Set</code> value
     */
    public Set getRequiredContracts();
    /**
     * Get the <code>Set</code> of <code>CellHandlers</code>s that this
     * <code>Agent</code> can provide to the <code>InfoState</code>.
     * The actual <code>InfoState</code> must include these handlers
     * on local contracts.
     *
     * @return a <code>Set</code> value
     */
    public Set getProvidedCells();
}
