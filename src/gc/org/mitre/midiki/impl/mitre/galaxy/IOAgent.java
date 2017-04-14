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
package org.mitre.midiki.impl.mitre.galaxy;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;

import org.mitre.dm.*;

import java.awt.event.*;

import java.util.*;
import java.util.logging.*;

/**
 * Implements an input and output agent that uses Communicator-based
 * services (the IOPodium from the Open Source Toolkit (OSTK)).
 * 
 */
 
public class IOAgent implements Agent
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.galaxy.IOAgent");
    private Logger discourseLogger = null;
    DialogueSystem dialogueSystem = null;
    public void attachTo(DialogueSystem system)
    {
        dialogueSystem = system;
        discourseLogger = system.getLogger();
    }

    /**
     * Performs one-time <code>Agent</code> initialization.
     * This processing is likely to include initialization
     * of the <code>Contract</code>s and <code>Cell</code>s
     * for the <code>Agent</code>, as well as non-Midiki
     * initialization.
     *
     */
    public void init(Object config)
    {
    }
    /**
     * Release any resources which were created in <code>init</code>.
     * Following this routine, the agent will be terminated.
     *
     */
    public void destroy()
    {
    }
    private InfoState myInfoState;
    /**
     * Set is.input to the specified string.
     *
     * @param input a <code>String</code> value
     */
    public boolean setInput(String input)
    {
        if (myInfoState == null) return false;
        return true;
    }
    /**
     * Connects the <code>Agent</code> to the provided information state.
     * In this routine, the <code>Agent</code> should register any
     * <code>Rule</code>s necessary for normal operation, and perform
     * any other <code>InfoState</code>-specific processing. 
     *
     * @param infoState a compatible information state
     * @return <code>true</code> if connection succeeded
     */
    public boolean connect(InfoState infoState)
    {
        logger.logp(Level.FINE,"org.mitre.midiki.impl.mitre.galaxy.IOAgent","connect","connecting to infoState",infoState);
        myInfoState = infoState;  // retain for later

        return true;
    }
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
    public void disconnect()
    {
    }
    /**
     * Get the value for the specified Midiki system property.
     *
     * @param key an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object getProperty(Object key)
    {
        return null;
    }
    /**
     * Set the value for the specified Midiki system property.
     *
     * @param key an <code>Object</code> value
     * @param value an <code>Object</code> value
     * @return previous value for the property
     */
    public Object putProperty(Object key, Object value)
    {
        return null;
    }
    /**
     * Get the system identifier for this <code>Agent</code>.
     * This is the name by which it is known to the system
     * as a whole, and should be unique.
     *
     * @return a <code>String</code> value
     */
    public String getId()
    {
        return getName();
    }
    /**
     * Get the name that this <code>Agent</code> calls itself.
     * A Midiki system might have several <code>Agent</code>s
     * with the same name, but each will have a unique id.
     *
     * @return a <code>String</code> value
     */
    public String getName()
    {
        return "io_agent";
    }
    /**
     * Get the set of <code>Contract</code>s this <code>Agent</code>
     * must find in its <code>InfoState</code>. There can be more,
     * but these must be there.
     *
     * @return a <code>Set</code> value
     */
    public Set getRequiredContracts()
    {
        Set contractSet = new HashSet();
        // IS and components
        contractSet.add(GalaxyContractDatabase.find("io_podium_input"));
        contractSet.add(GalaxyContractDatabase.find("io_podium_output"));
        return contractSet;
    }
    public MethodHandler outputCellProxy;
    public MethodHandler outputHandler;
    private int outputStringIndex = 0;
    /**
     * Get the <code>Set</code> of <code>Cell</code>s that this
     * <code>Agent</code> can provide to the <code>InfoState</code>.
     * The actual <code>InfoState</code> must include these.
     *
     * @return a <code>Set</code> value
     */
    public Set getProvidedCells()
    {
        CellHandlers ioPodiumOutputCell = new CellHandlers(GalaxyContractDatabase.find("io_podium_output"));
        MethodHandler fromIOPodiumTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    System.out.println("ioPodiumOutputCell invoked");
                    if (outputCellProxy != null) {
                        return outputCellProxy.invoke(arguments, bindings);
                    } else {
                        return true;
                    }
                }
            };
        ioPodiumOutputCell.addMethodHandler("FromIOPodium", fromIOPodiumTransducer);

        outputHandler = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // input arguments should be a string.
                    // increment the output id and add it.
                    outputStringIndex++;
                    arguments.add(new Integer(outputStringIndex));
                    // add '1' for clearing the input field
                    arguments.add(new Integer(1));
                    System.out.println("sending response to ioPodium: "+arguments);
                    return myInfoState.cell("io_podium_input").method("show_output_string").invoke(arguments, bindings);
                }
            };

        Set cellSet = new HashSet();
        cellSet.add(ioPodiumOutputCell);
        return cellSet;
    }
}
