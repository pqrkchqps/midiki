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
package org.mitre.dm.qud.rules;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;

import java.util.*;
import java.util.logging.*;

/*
 *      Grounding (optimistic)
 *
 * Implements a version of grounding which assumes all input is
 * correctly read and interpreted by both parties. The list of
 * latest moves is converted into a list of (Move, Boolean) associations
 * and is stored as the current set of moves. Those moves will be
 * processed in later Update modules.
 */
public class Grounding extends RuleSet
{
    private Logger logger =
        Logger.getLogger("org.mitre.dm.qud.rules.Grounding");
    private Logger discourseLogger = null;
    public LinkedList recAddAllAssoc(Object tl, Object flag)
    {
        LinkedList assocs = new LinkedList();
        if (tl instanceof List) {
            Iterator it = ((List)tl).iterator();
            while (it.hasNext()) {
                Object term = it.next();
                ArrayList al = new ArrayList();
                al.add(term);
                al.add(flag);
                assocs.add(new Predicate("assoc", al));
            }
        } else {
            ArrayList al = new ArrayList();
            al.add(tl);
            al.add(flag);
            assocs.add(new Predicate("assoc", al));
        }
        return assocs;
    }
    public Grounding(Logger theLogger)
    {
        super();
        discourseLogger = theLogger;
        Condition assumeSysMovesGroundedC =
            new Condition(){
                    public boolean test(ImmutableInfoState iis,
                                        Bindings b) {
                        Object ls = iis.cell("is").get("latest_speaker");
                        boolean success = iis.getUnifier().matchTerms(ls, "sys", b);
                        logger.logp(Level.FINER,"org.mitre.dm.qud.rules.Grounding","assumeSysMovesGrounded.test",(success?"latest_speaker matched":"latest_speaker did not match"),ls);
                        return success;
                    }
                };
        ExistsRule assumeSysMovesGrounded =
            new ExistsRule(assumeSysMovesGroundedC){
                    public boolean execute(InfoState infoState, Bindings b) {
                        infoState.cell("is").cell("shared").cell("lu").put("speaker", "sys");
                        infoState.cell("is").cell("shared").cell("lu").put("moves",null);

                        LinkedList assocs = recAddAllAssoc(infoState.cell("is").get("latest_moves"), new Boolean(false));
                        infoState.cell("is").cell("shared").cell("lu").put("moves",assocs);
                        return true;
                    }
                };
        add(assumeSysMovesGrounded);

        Condition assumeUsrMovesGroundedC =
            new Condition(){
                    public boolean test(ImmutableInfoState iis,
                                        Bindings b) {
                        Object ls = iis.cell("is").get("latest_speaker");
                        boolean success = iis.getUnifier().matchTerms(ls, "usr", b);
                        logger.logp(Level.FINER,"org.mitre.dm.qud.rules.Grounding","assumeUsrMovesGrounded.test",(success?"latest_speaker matched":"latest_speaker did not match"),ls);
                        return success;
                    }
                };
        ExistsRule assumeUsrMovesGrounded =
            new ExistsRule(assumeUsrMovesGroundedC){
                    public boolean execute(InfoState infoState, Bindings b) {
                        infoState.cell("is").cell("shared").cell("lu").put("speaker", "usr");
                        infoState.cell("is").cell("shared").cell("lu").put("moves",null);

                        LinkedList assocs = recAddAllAssoc(infoState.cell("is").get("latest_moves"), new Boolean(false));

                        infoState.cell("is").cell("shared").cell("lu").put("moves",assocs);
                        return true;
                    }
                };
        add(assumeUsrMovesGrounded);
    }
}
