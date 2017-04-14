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

/**
 * Provides a toolkit of rule effects used in porting GoDiS
 * to Midiki. Rules tend to share common effects. To improve
 * the legibility of rules and reduce potential errors, we've
 * pulled individual rule effects into separate routines, and
 * pulled them into a separate class so that individual rule sets
 * don't need their own copies of the code.<p>
 * There is no API requirement to put effects in separate routines,
 * nor are we claiming that this is a standard set of effects.<p>
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class ResultWithoutPlanStack
{
    private static String className =
        "org.mitre.dm.qud.conditions.Result";
    private static Logger logger = Logger.getLogger(className);
    private Logger discourseLogger;
    public void setDiscourseLogger(Logger dl)
    {
        discourseLogger = dl;
    }
    static Variable x;
    static Variable ann;
    static {
        x = new Variable("X");
        ann = new Variable("Ann");
    }
    public void popQUD(InfoState infoState, Bindings bindings)
    {
        Stack qud = 
            (Stack)infoState.cell("is").cell("shared").get("qud");
        Object qudPop = qud.pop();
        logger.logp(Level.FINER,className,"popQUD","popping QUD",qudPop);
        discourseLogger.logp(Level.FINER,className,"popQUD","popping QUD",qudPop);
        infoState.cell("is").cell("shared").put("qud", qud);
    }

    public Object removeUnifyingInList(InfoState infoState, List l, Object o)
    {
        Bindings bindings = infoState.createBindings();
        Iterator it = l.iterator();
        while (it.hasNext()) {
            Object lo = it.next();
            Object un = infoState.getUnifier().unify(o, lo, bindings);
            if (un != null) {
                it.remove();
                return infoState.getUnifier().deref(un, bindings);
            }
        }
        return o;
    }

    public void pushQUD(Object q, InfoState infoState, Bindings bindings)
    {
        Stack qud = 
            (Stack)infoState.cell("is").cell("shared").get("qud");
        if (qud == null) {
            qud = new Stack();
        }
        Object newqud = removeUnifyingInList(infoState, qud, infoState.getUnifier().deref(q,bindings));
        qud.push(newqud);
        logger.logp(Level.FINER,className,"pushQUD","pushing QUD",newqud);
        discourseLogger.logp(Level.FINER,className,"pushQUD","pushing QUD",newqud);
        infoState.cell("is").cell("shared").put("qud", qud);
    }

    public void popAgenda(InfoState infoState, Bindings bindings)
    {
        Stack agenda = 
            (Stack)infoState.cell("is").cell("private").get("agenda");
        logger.logp(Level.FINER,className,"popAgenda","popping agenda",agenda.pop());
        infoState.cell("is").cell("private").put("agenda", agenda);
    }

    public void pushAgenda(Object q, InfoState infoState, Bindings bindings)
    {
        Stack agenda = 
            (Stack)infoState.cell("is").cell("private").get("agenda");
        if (agenda == null) {
            agenda = new Stack();
        }
        Object newagenda = infoState.getUnifier().deref(q,bindings);
        logger.logp(Level.FINER,className,"pushAgenda","pushing agenda",newagenda);
        agenda.push(newagenda);
        infoState.cell("is").cell("private").put("agenda", agenda);
    }

    public void clearAgenda(InfoState infoState, Bindings bindings)
    {
        logger.logp(Level.FINER,className,"clearAgenda","clearing agenda");
        Object o = infoState.cell("is").cell("private").get("agenda");
        if (o == null) {
            infoState.cell("is").cell("private").put("agenda", new Stack());
        } else {
            Stack agenda = (Stack)o;
            agenda.clear();
            infoState.cell("is").cell("private").put("agenda", agenda);
        }
    }

    public void addBelief(Object p, InfoState infoState, Bindings bindings)
    {
        List bel = 
            (List)infoState.cell("is").cell("private").get("bel");
        if (bel == null) {
            bel = new LinkedList();
        }
        Object newbelief = infoState.getUnifier().deref(p,bindings);
        if (newbelief == null) return;
        logger.logp(Level.FINER,className,"addBelief","adding belief",newbelief);
        bel.add(newbelief);
        infoState.cell("is").cell("private").put("bel", bel);
    }

    public void addCommonGround(Object p, InfoState infoState, Bindings bindings)
    {
        List com = 
            (List)infoState.cell("is").cell("shared").get("com");
        if (com == null) {
            com = new LinkedList();
        }
        Object newcom = infoState.getUnifier().deref(p,bindings);
        logger.logp(Level.FINER,className,"addCommonGround","adding item to common ground",newcom);
        com.add(newcom);
        infoState.cell("is").cell("shared").put("com", com);
    }

    public void clearCommonGround(InfoState infoState, Bindings bindings)
    {
        logger.logp(Level.FINER,className,"clearCommonGround","clearing common ground");
        Object o = infoState.cell("is").cell("shared").get("com");
        if (o == null) {
            infoState.cell("is").cell("shared").put("com", new LinkedList());
        } else {
            List com = (List)o;
            com.clear();
            infoState.cell("is").cell("shared").put("com", com);
        }
    }

    public void removeCommonGround(Object p, InfoState infoState, Bindings bindings)
    {
        List com = 
            (List)infoState.cell("is").cell("shared").get("com");
        if (com == null) return;
        Object oldcom = infoState.getUnifier().deref(p,bindings);
        logger.logp(Level.FINER,className,"removeCommonGround","removing item from common ground",oldcom);
        discourseLogger.logp(Level.FINER,className,"removeCommonGround","removing item from common ground",oldcom);
        com.remove(oldcom);
        infoState.cell("is").cell("shared").put("com", com);
    }

    public void storeMove(Object p, InfoState infoState, Bindings bindings)
    {
        List nim = 
            (List)infoState.cell("is").cell("shared").get("nim");
        if (nim == null) {
            nim = new LinkedList();
        }
        Object newnim = infoState.getUnifier().deref(p,bindings);
        logger.logp(Level.FINER,className,"storeMove","storing new non-integrated move",newnim);
        discourseLogger.logp(Level.FINER,className,"storeMove","storing new non-integrated move",newnim);
        nim.add(newnim);
        infoState.cell("is").cell("shared").put("nim", nim);
    }

    public void removeAgedNIM(Object p, InfoState infoState, Bindings bindings)
    {
        List nim = 
            (List)infoState.cell("is").cell("shared").get("nim");
        Object oldnim = infoState.getUnifier().deref(p,bindings);
        logger.logp(Level.FINER,className,"removeAgedNIM","removing old non-integrated move",oldnim);
        discourseLogger.logp(Level.FINER,className,"removeAgedNIM","removing old non-integrated move",oldnim);
        nim.remove(oldnim);
        infoState.cell("is").cell("shared").put("nim", nim);
    }

    public void clearMoves(InfoState infoState, Bindings bindings)
    {
        logger.logp(Level.FINER,className,"clearMoves","clearing moves");
        Object o = infoState.cell("is").cell("shared").cell("lu").get("moves");
        if (o == null) {
            infoState.cell("is").cell("shared").cell("lu").put("moves", new LinkedList());
        } else {
            List moves = (List)o;
            moves.clear();
            infoState.cell("is").cell("shared").cell("lu").put("moves",moves);
        }
    }

    public void answerPlanQuestion(Object p, InfoState infoState, Bindings bindings)
    {
        Object plan = infoState.cell("is").cell("private").get("plan");
        Object answer = infoState.getUnifier().deref(p,bindings);
        logger.logp(Level.FINER,className,"answerPlanQuestion","answering question",answer);
        discourseLogger.logp(Level.FINER,className,"answerPlanQuestion","answering question",answer);
        LinkedList args = new LinkedList();
        args.add(plan);
        args.add(p);
        args.add(x);
        infoState.cell("domain").method("set_answer").invoke(args,bindings);
    }

    public void answerPlanQuestion(Object p, Object q, InfoState infoState, Bindings bindings)
    {
        Object answer = infoState.getUnifier().deref(p,bindings);
        logger.logp(Level.FINER,className,"answerPlanQuestion","answering question",answer);
        discourseLogger.logp(Level.FINER,className,"answerPlanQuestion","answering question",answer);
        Object plan = infoState.cell("is").cell("private").get("plan");
        LinkedList args = new LinkedList();
        args.add(plan);
        args.add(p);
        args.add(q);
        infoState.cell("domain").method("set_answer").invoke(args,bindings);
    }

    public void advancePlan(InfoState infoState, Bindings bindings)
    {
        logger.logp(Level.FINER,className,"advancePlan","advancing plan");
        Object plan = infoState.cell("is").cell("private").get("plan");
        LinkedList args = new LinkedList();
        args.add(plan);
        args.add(x);
        args.add(ann);
        infoState.cell("domain").method("advance").invoke(args,bindings);
        Object annotations = Unify.getInstance().deref(ann, bindings);
        if (annotations instanceof List) {
            List annList = (List)annotations;
            if (!annList.isEmpty()) {
                discourseLogger.info("plan annotations: "+annList);
            }
        }
    }

    public void instantiatePlan(Object plan, InfoState infoState, Bindings bindings)
    {
        logger.logp(Level.FINER,className,"instantiatePlan","instantiating plan");
        discourseLogger.logp(Level.FINER,className,"instantiatePlan","instantiating plan");
        Variable planref = new Variable("PlanRef");
        LinkedList args = new LinkedList();
        args.add(plan);
        args.add(planref);
        infoState.cell("domain").method("instantiate_plan").invoke(args,bindings);
        infoState.cell("is").cell("private").put("plan", infoState.getUnifier().deref(planref,bindings));
    }

    public boolean recSetAssoc(Object moves, Object move, Object flag, Bindings bindings)
    {
        move = Unify.getInstance().deref(move,bindings);
        if (moves instanceof List) {
            ListIterator it = ((List)moves).listIterator();
            while (it.hasNext()) {
                if (matchAssoc(it.next(), move, bindings)) {
                    ArrayList al = new ArrayList();
                    al.add(move);
                    al.add(flag);
                    it.set(new Predicate("assoc", al));
                    return true;
                }
            }
            ArrayList al = new ArrayList();
            al.add(move);
            al.add(flag);
            ((List)moves).add(new Predicate("assoc", al));
            return true;
        } else {
            System.out.println("setAssoc called with non-list moves");
            //return setAssoc(moves, move, flag, bindings);
        }
        return false;
    }
    public boolean matchAssoc(Object mv, Object m, Bindings b)
    {
        if (mv instanceof Predicate) {
            Predicate ap = (Predicate)mv;
            if (ap.functor().equals("assoc") && (ap.arity()==2)) {
                Iterator argit = ap.arguments();
                Object apm = argit.next();
                Object fit = argit.next();
                if (Unify.getInstance().matchTerms(apm, m, b)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public void markIntegrated(Object move, InfoState infoState, Bindings bindings)
    {
        Object moves = infoState.cell("is").cell("shared").cell("lu").get("moves");
        move = infoState.getUnifier().deref(move,bindings);
        logger.logp(Level.FINER,className,"markIntegrated","marking move as integrated:"+move);
        recSetAssoc(moves,move,new Boolean(true),bindings);
        infoState.cell("is").cell("shared").cell("lu").put("moves",moves);
    }

    public void addUnintegratedMove(Object move, InfoState infoState, Bindings bindings)
    {
        Object moves = infoState.cell("is").cell("shared").cell("lu").get("moves");
        move = infoState.getUnifier().deref(move,bindings);
        logger.logp(Level.FINER,className,"addUnintegratedMove","adding move, flagged as unintegrated",move);
        recSetAssoc(moves,move,new Boolean(false),bindings);
        infoState.cell("is").cell("shared").cell("lu").put("moves",moves);
    }

}
