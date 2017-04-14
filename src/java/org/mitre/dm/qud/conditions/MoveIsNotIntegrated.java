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
package org.mitre.dm.qud.conditions;

import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;

import java.util.*;
import java.util.logging.*;

public class MoveIsNotIntegrated extends Condition
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.conditions.MoveIsNotIntegrated");
    Object move;
    public MoveIsNotIntegrated(Object o)
    {
        super();
        move = o;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object moves = infoState.cell("is").cell("shared").cell("lu").get("moves");
        logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","test","move="+move);
        logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","test","is.shared.lu.moves="+moves);
        boolean success = recAssoc(moves,move,new Boolean(false),bindings);
        logger.logp(Level.FINER,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","test",(success ? "success" : "failure"));
        return success;
    }
    static public boolean recAssoc(Object moves, Object move, Object flag, Bindings bindings)
    {
        boolean success = false;
        if (moves instanceof List) {
            logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","recAssoc","moves is list");
            Iterator it = ((List)moves).iterator();
            while (it.hasNext()) {
                if (matchAssoc(it.next(), move, flag, bindings)) {
                    logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","recAssoc","success",bindings);
                    bindings.reset();
                    success = true;
                } else {
                    // clear any bindings for this scope, since they can
                    // only be from the recAssoc and must not be saved.
                    ((BindingsImpl)bindings).clearCurrentScope();
                }
            }
        } else {
            logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","recAssoc","moves is not list");
            if (matchAssoc(moves, move, flag, bindings)) {
                logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","recAssoc","success",bindings);
                bindings.reset();
                success = true;
            }
        }
        return success;
    }
    static public boolean matchAssoc(Object mv, Object m, Object f, Bindings b)
    {
        if (mv instanceof Predicate) {
            logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","matchAssoc","mv is predicate");
            Predicate ap = (Predicate)mv;
            if (ap.functor().equals("assoc") && (ap.arity()==2)) {
                logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","matchAssoc","ap matches assoc/2");
                Iterator argit = ap.arguments();
                Object apm = argit.next();
                Object fit = argit.next();
                if (Unify.getInstance().matchTerms(apm, m, b)) {
                    logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","matchAssoc","associated move matches input move",m);
                    if (Unify.getInstance().matchTerms(fit, f, b)) {
                        logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","matchAssoc","associated flag matches input flag",f);
                        return true;
                    } else {
                        logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","matchAssoc","associated flag does not match input flag",f);
                        return false;
                    }
                } else {
                    logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","matchAssoc","associated move does not match input move",m);
                    return false;
                }
            } else {
                logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","matchAssoc","ap does not match assoc/2");
                return false;
            }
        } else {
            logger.logp(Level.FINEST,"org.mitre.dm.qud.conditions.MoveIsNotIntegrated","matchAssoc","mv is not predicate; failure");
            return false;
        }
    }
}

