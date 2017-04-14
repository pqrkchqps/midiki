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
package org.mitre.dm.qud;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;

import org.mitre.dm.*;
import org.mitre.dm.qud.conditions.*;

import java.util.*;
import java.util.logging.*;

/**
 * Implements a tokenizing and pattern-matching agent.
 * 
 */
 
public class InterpretAgent implements Agent
{
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
        /*
         * Set the rule to be fired when input is available.
         */
        Condition movesC = new Condition();
        movesC.extend(new InputNotEmpty());
        ExistsRule moves =
            new ExistsRule(movesC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        Object input = infoState.cell("is").get("input");
                        Variable wordlist = new Variable("WordList");
                        Variable moves = new Variable("Moves");
                        ArrayList al = new ArrayList();
                        al.add(input);
                        al.add(wordlist);
                        infoState.cell("interpreter").method("string2wordlist").invoke(al, bindings);
                        Object wl = Unify.getInstance().deref(wordlist, bindings);
                        if ((wl instanceof List) &&
                            (((List)wl).isEmpty())) {
                            LinkedList noMoveList = new LinkedList();
                            noMoveList.add("no_move");
                            bindings.put(moves, noMoveList);
                        } else {
                            Variable movelist0 = new Variable("MoveList0");
                            Variable movelist = new Variable("MoveList");
                            ArrayList al1 = new ArrayList();
                            al1.add(wordlist);
                            al1.add(movelist0);
                            infoState.cell("lexicon").method("wordlist2moves").invoke(al1, bindings);
                            ArrayList al2 = new ArrayList();
                            al2.add(movelist0);
                            al2.add(movelist);
                            infoState.cell("lexicon").method("remove_duplicates").invoke(al2, bindings);
                            Object ml = Unify.getInstance().deref(movelist, bindings);
                            if ((ml instanceof List) &&
                                (((List)ml).isEmpty())) {
                                LinkedList failedList = new LinkedList();
                                failedList.add("failed");
                                Unify.getInstance().matchTerms(moves, failedList, bindings);
                            } else {
                                Unify.getInstance().matchTerms(moves, movelist, bindings);
                            }
                        }
                        infoState.cell("is").put("latest_speaker", "usr");
                        infoState.cell("is").put("latest_moves", Unify.getInstance().deref(moves, bindings));
                        infoState.cell("is").put("input", "");
                        return true;
                    }
                };
        infoState.cell("is").addInfoListener("input", new RuleBasedInfoListener(moves){});

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
        return "interpret_agent";
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
        contractSet.add(ContractDatabase.find("is"));
        contractSet.add(ContractDatabase.find("shared"));
        contractSet.add(ContractDatabase.find("private"));
        contractSet.add(ContractDatabase.find("lu"));
        //contractSet.add(ContractDatabase.find("tmp"));
        // non-IS contracts
        contractSet.add(ContractDatabase.find("interpreter"));
        contractSet.add(ContractDatabase.find("lexicon"));
        contractSet.add(ContractDatabase.find("domain"));
        return contractSet;
    }
    /**
     * Get the <code>Set</code> of <code>Cell</code>s that this
     * <code>Agent</code> can provide to the <code>InfoState</code>.
     * The actual <code>InfoState</code> must include these.
     *
     * @return a <code>Set</code> value
     */
    public Set getProvidedCells()
    {
        Set cellSet = new HashSet();
        WhitespaceTokenizingInterpreter wti =
            new WhitespaceTokenizingInterpreter();
        cellSet.add(wti.initializeHandlers());
        return cellSet;
    }
}
