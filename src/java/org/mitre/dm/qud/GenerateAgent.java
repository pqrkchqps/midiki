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
 * Implements response generation.
 * 
 */
 
public class GenerateAgent implements Agent
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.GenerateAgent");
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
        m = new Variable("M");
        LinkedList args = new LinkedList();
        args.add(m);
        repeat_m = new Predicate("repeat",args);
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
         * Set the rule to be fired when system moves are ready for output.
         * Respond to new system moves. Shift 'next_moves' to 'latest_moves',
         * since they have now been executed, and set the latest_speaker
         * to the system. Any output generated here will be output elsewhere.
         * This is to allow moves that do not have associated output
         * to go back to the DME for integration anyway. The earlier
         * GoDiS design didn't permit that.
         */
        infoState.cell("is").addInfoListener("next_moves", new RuleBasedInfoListener(new GenerateRules()){});
        return true;
    }
    class GenerateRules extends RuleSet {
        public GenerateRules() {
            super();

            /* First condition: next_moves not empty, and previous move
             * was a repeat. Don't generate text for any of the repeated
             * moves.
             */
            Condition repeatedMovesC = new Condition();
            repeatedMovesC.extend(new NextMovesAreNotEmpty());
            repeatedMovesC.extend(new LatestMoveMatches(repeat_m));
            ExistsRule repeatedMoves =
                new ExistsRule(repeatedMovesC){
                        public boolean execute(InfoState infoState, Bindings bindings) {
                            StringBuffer sb = new StringBuffer();
                            Object moves = infoState.cell("is").get("next_moves");
                            if (moves instanceof List) {
                                processMoves((List)moves, sb, false, infoState, bindings);
                            }
                            //System.out.println("GenerateAgent moves: "+Unify.getInstance().deref(moves, bindings));
                            //System.out.println("GenerateAgent wordlist: "+Unify.getInstance().deref(wordlist, bindings));
                            outputMoves(sb, infoState, bindings);
                            infoState.cell("is").put("latest_speaker", "sys");
                            infoState.cell("is").put("next_moves", new LinkedList());
                            infoState.cell("is").put("latest_moves", moves);
                            return true;
                        }
                    };
            add(repeatedMoves);
            Condition movesC = new Condition();
            movesC.extend(new NextMovesAreNotEmpty());
            ExistsRule moves =
                new ExistsRule(movesC){
                        public boolean execute(InfoState infoState, Bindings bindings) {
                            StringBuffer sb = new StringBuffer();
                            Object moves = infoState.cell("is").get("next_moves");
                            if (moves instanceof List) {
                                processMoves((List)moves, sb, false, infoState, bindings);
                            }
                            //System.out.println("GenerateAgent moves: "+Unify.getInstance().deref(moves, bindings));
                            //System.out.println("GenerateAgent wordlist: "+Unify.getInstance().deref(wordlist, bindings));
                            outputMoves(sb, infoState, bindings);
                            infoState.cell("is").put("latest_speaker", "sys");
                            infoState.cell("is").put("next_moves", new LinkedList());
                            infoState.cell("is").put("latest_moves", moves);
                            return true;
                        }
                    };
            add(moves);
        }
    }
    private Predicate repeat_m;
    private Variable m;
    public void processMoves(List moves, StringBuffer sb, boolean repeating, InfoState infoState, Bindings bindings)
    {
        Object repeat = Unify.getInstance().deref(m, bindings);
        List repeatedMoves = null;
        if (!(repeat instanceof Variable)) {
            repeatedMoves = (List)repeat;
        }
        Iterator it = moves.iterator();
        while (it.hasNext()) {
            Object move = it.next();
            if (Unify.getInstance().matchTerms(repeat_m, move, bindings)) {
                // repeat the text for these moves
                processMoves(((List)Unify.getInstance().deref(m, bindings)), sb, true, infoState, bindings);
                continue;
            }
            if (!repeating && (repeatedMoves != null)) {
                if (IsCommonGround.rec_in(repeatedMoves,move,bindings)) {
                    System.out.println("skipping repeated move "+move);
                    continue;
                }
            }
            LinkedList singleMove = new LinkedList();
            singleMove.add(move);
            Variable wordlist = Variable.newVariable();
            ArrayList al = new ArrayList();
            al.add(singleMove);
            al.add(wordlist);
            infoState.cell("lexicon").method("movelist2words").invoke(al, bindings);
            Object result = Unify.getInstance().deref(wordlist, bindings);
            if ((result == null) ||
                (((String)result).length()==0)) {
                result = handleUnrecognizedMove(move);
            }
            sb.append((String)result);
        }
    }
    public void outputMoves(StringBuffer sb, InfoState infoState, Bindings bindings)
    {
        String output = sb.toString();
        if ((output != null) && (output.length() > 0)) {
            Integer output_index = (Integer)infoState.cell("output").get("output_index");
            infoState.cell("output").put("output_index", new Integer(output_index.intValue()+1));
            Integer output_turn = (Integer)infoState.cell("is").get("turn_for_moves");
            infoState.cell("output").put("output_for_turn", output_turn);
            infoState.cell("output").put("output", output);
            discourseLogger.logp(Level.INFO,"org.mitre.dm.qud.GenerateAgent","moves trigger","generated text, system turn "+output_turn.intValue()+" index "+(output_index.intValue()+1), output);
        }
    }
    /**
     * Provide user hook for handling moves for which
     * no output is found. It is not always an error for moves
     * to have no surface realization, but it could be.
     * The default implementation will log ask(), answer(),
     * and inform() moves of any arity sent into this method.
     *
     * @param move an <code>Object</code> value
     * @return a <code>String</code> value
     */
    public String handleUnrecognizedMove(Object move)
    {
        if (move instanceof Predicate) {
            String functor = ((Predicate)move).functor();
            if (functor.equals("ask") ||
                functor.equals("answer") ||
                functor.equals("inform")) {
                logger.warning("no text for content move: "+move);
            }
        }
        return "";
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
        return "generate_agent";
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
        contractSet.add(ContractDatabase.find("lexicon"));
        contractSet.add(ContractDatabase.find("output"));
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
        return null;
    }
}
