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

import org.mitre.dm.*;
import org.mitre.dm.qud.conditions.*;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;

import java.util.*;
import java.util.logging.*;

/**
 * Implements the integration rules for the update phase. 
 * Integration rules for system moves match perceived moves
 * against system intentions. System moves never have any
 * real effect until integration time.
 * 
 */
 
public class Integrate extends RuleSet
{
    private Logger logger =
        Logger.getLogger("org.mitre.dm.qud.rules.Integrate");
    private Logger discourseLogger = null;
    Result consequence;

    private boolean logByMove = true;

    public void init(Object config)
    {
        if (config instanceof PropertyTree) {
             Object obj = ((PropertyTree)config).find("dme.integrate.logByMove");
             logByMove = (obj == null ? true : obj.equals("true"));
        } else {
            System.out.println("Integrate got non-PropertyTree");
        }
    }

    /*
     * Variables
     */

    private Variable anonymous;
    private Variable m;
    private Variable p;
    private Variable p1;
    private Variable q;
    private Variable q1;
    private Variable qnew;
    private Variable r;
    private Variable type;
    private Variable x;
    private Variable move;
    private Variable plan;
    private Variable cause;

    /*
     * Predicates
     */
    private Predicate reqRep_und;
    private Predicate reqRep_rel;
    private Predicate reqRep_type;
    private Predicate ack_p;
    private Predicate ack_x;
    private Predicate repeat_m;
    private Predicate inform_p;
    private Predicate respond_q;
    private Predicate respond_qnew;
    private Predicate respond_alts_q_r;
    private Predicate answer_r;
    private Predicate answer_q_r;
    private Predicate answer_alts_q_r;
    private Predicate assert_q_r;
    private Predicate ask_q;
    private Predicate findout_q;
    private Predicate raise_q;
    private Predicate task_findout_q;
    private Predicate task_anon;
    private Predicate task_x;
    private Predicate ask_task_x;
    private Predicate alts_x;
    private Predicate alts_q1_x;
    private Predicate alts_q;
    private Predicate alts_q_r;
    private Predicate query_q_r;
    private Predicate method_q_r;
    private Predicate bind_q_r;
    private Predicate inform_invalid_answer_r;
    private Predicate quit_cause;
    private Predicate quit_atrequest;
    private Predicate forget_r;

    /**
     * Returns a list of the contracts this rule set requires.
     * The <code>ContractDatabase</code> is outside the API;
     * something I'm using for the sample implementation.
     * It would probably be useful for most DM projects,
     * but the actual set of contracts would differ.
     *
     * @return a <code>Collection</code> value
     */
    public Collection metadata()
    {
        LinkedList contracts = new LinkedList();
        contracts.add(ContractDatabase.find("is"));
        contracts.add(ContractDatabase.find("rec"));
        contracts.add(ContractDatabase.find("domain"));
        contracts.add(ContractDatabase.find("database"));
        contracts.add(ContractDatabase.find("lexicon"));
        return contracts;
    }

    public Integrate(Logger theLogger)
    {
        super();
        discourseLogger = theLogger;
        consequence = new Result();
        consequence.setDiscourseLogger(discourseLogger);

        anonymous = new Variable();
        m = new Variable("M");
        p = new Variable("P");
        p1 = new Variable("P1");
        q = new Variable("Q");
        q1 = new Variable("Q1");
        qnew = new Variable("Qnew");
        r = new Variable("R");
        type = new Variable("Type");
        x = new Variable("X");
        move = new Variable("Move");
        plan = new Variable("Plan");
        cause = new Variable("Cause");

        LinkedList args;

        args = new LinkedList();
        args.add("understanding");
        reqRep_und = new Predicate("reqRep",args);

        args = new LinkedList();
        args.add("relevance");
        reqRep_rel = new Predicate("reqRep",args);

        args = new LinkedList();
        args.add(type);
        reqRep_type = new Predicate("reqRep",args);

        args = new LinkedList();
        args.add(p);
        ack_p = new Predicate("ack",args);

        args = new LinkedList();
        args.add(x);
        ack_x = new Predicate("ack",args);

        args = new LinkedList();
        args.add(m);
        repeat_m = new Predicate("repeat",args);

        args = new LinkedList();
        args.add(p);
        inform_p = new Predicate("inform",args);

        args = new LinkedList();
        args.add(q);
        respond_q = new Predicate("respond",args);

        args = new LinkedList();
        args.add(qnew);
        respond_qnew = new Predicate("respond",args);

        args = new LinkedList();
        args.add(q);
        args.add(r);
        alts_q_r = new Predicate("alts",args);
        args = new LinkedList();
        args.add(alts_q_r);
        respond_alts_q_r = new Predicate("respond",args);

        args = new LinkedList();
        args.add(r);
        answer_r = new Predicate("answer",args);

        args = new LinkedList();
        LinkedList args1 = new LinkedList();
        args.add(answer_r);
        args1.add(new Predicate("invalid",args));
        inform_invalid_answer_r = new Predicate("inform",args1);

        args = new LinkedList();
        args.add(q);
        args.add(r);
        answer_q_r = new Predicate("answer",args);

        args = new LinkedList();
        args.add(q);
        alts_q = new Predicate("alts",args);
        args = new LinkedList();
        args.add(alts_q);
        args.add(r);
        answer_alts_q_r = new Predicate("answer",args);

        args = new LinkedList();
        args.add(q);
        args.add(r);
        assert_q_r = new Predicate("assert",args);

        args = new LinkedList();
        args.add(q);
        ask_q = new Predicate("ask",args);

        args = new LinkedList();
        args.add(q);
        findout_q = new Predicate("findout",args);

        args = new LinkedList();
        args.add(q);
        raise_q = new Predicate("raise",args);

        args = new LinkedList();
        args.add(findout_q);
        task_findout_q = new Predicate("task",args);

        args = new LinkedList();
        args.add(anonymous);
        task_anon = new Predicate("task",args);

        args = new LinkedList();
        args.add(x);
        task_x = new Predicate("task",args);

        args = new LinkedList();
        args.add(task_x);
        ask_task_x = new Predicate("ask",args);

        args = new LinkedList();
        args.add(x);
        alts_x = new Predicate("alts",args);

        args = new LinkedList();
        args.add(q1);
        args.add(x);
        alts_q1_x = new Predicate("alts",args);

        args = new LinkedList();
        args.add(q);
        args.add(r);
        query_q_r = new Predicate("query",args);

        args = new LinkedList();
        args.add(q);
        args.add(r);
        method_q_r = new Predicate("method",args);

        args = new LinkedList();
        args.add(q);
        args.add(r);
        bind_q_r = new Predicate("bind",args);

        args = new LinkedList();
        args.add(cause);
        quit_cause = new Predicate("quit",args);

        args = new LinkedList();
        args.add("atrequest");
        quit_atrequest = new Predicate("quit",args);

        args = new LinkedList();
        args.add(r);
        forget_r = new Predicate("forget",args);

        buildRuleSet();
    }

    protected boolean inCommonGround(Collection coll, Object elem, Bindings bindings)
    {
System.out.println("inCommonGround "+coll+", looking for "+elem);
        boolean success = false;
        Iterator it = ((Collection)coll).iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (Unify.getInstance().matchTerms(o, elem, bindings)) {
                return true;
            }
        }
        return false;
    }

    protected boolean inCurrentPlan(InfoState infoState, Object plan, Object elem, Bindings bindings)
    {
        ArrayList args = new ArrayList();
        args.add(plan);
        args.add(elem);
        infoState.cell("domain").query("get_answer").query(args, bindings);
        Predicate pred = (Predicate)Unify.getInstance().deref(elem, bindings);
        Iterator it = pred.arguments();
        if (it.next() instanceof Variable) {
            System.out.println("inCurrentPlan: "+elem+" not found");
            return false;
        } else {
            System.out.println("inCurrentPlan: "+elem+" found as "+pred);
            return true;
        }
    }

    /**
     * Build arguments for a query by matching parameter formal names
     * with slots in the current task/plan. This assumes that there is
     * a direct mapping between the relevant contracts. A more general
     * approach using explicit mapping could be supported, using the
     * structures and concepts in the OWL-S ontology, but has not yet
     * been developed.
     *
     * Only supplies a single value to the query for each parameter.
     *
     * @param cellName a <code>String</code> value
     * @param queryName a <code>String</code> value
     * @param infoState an <code>InfoState</code> value
     * @param bindings a <code>Bindings</code> value
     * @return a <code>Collection</code> value
     */
    protected Collection buildQueryArguments(String cellName, String queryName, InfoState infoState, Bindings bindings)
    {
        LinkedList args = new LinkedList();
        Contract ctr = infoState.cell(cellName).getContract();
        Contract.Query qry = ctr.query(queryName);
        Iterator parmit = qry.parameters();
        while (parmit.hasNext()) {
            Contract.Parameter parm = (Contract.Parameter)parmit.next();
            ArrayList al = new ArrayList();
            al.add(Variable.newVariable());
            Predicate pred = new Predicate(parm.name(), al);
            Object preds = infoState.cell("is").cell("shared").get("com");
            Stack planStack = (Stack)infoState.cell("is").cell("private").get("plan");
            Object plan = Unify.getInstance().deref(planStack.peek(), bindings);
            //System.out.println("TOS is "+planPeek.getClass().getName()+" == "+planPeek);
            if (!(preds instanceof Collection)) continue;
            if (inCurrentPlan(infoState,plan,pred,bindings)) {
                args.add(infoState.getUnifier().deref(pred, bindings));
            } else if (inCommonGround((Collection)preds,pred,bindings)) {
                args.add(infoState.getUnifier().deref(pred, bindings));
            } else {
                args.add(pred);
            }
        }
        return args;
    }
    
    /**
     * Build arguments for a method by matching parameter formal names
     * with slots in the current task/plan. This assumes that there is
     * a direct mapping between the relevant contracts. A more general
     * approach using explicit mapping could be supported, using the
     * structures and concepts in the OWL-S ontology, but has not yet
     * been developed.
     *
     * Only supplies a single value to the method for each parameter.
     * The first value for a predicate encountered in COM will be used.
     *
     * @param cellName a <code>String</code> value
     * @param queryName a <code>String</code> value
     * @param infoState an <code>InfoState</code> value
     * @param bindings a <code>Bindings</code> value
     * @return a <code>Collection</code> value
     */
    protected Collection buildMethodArguments(String cellName, String methodName, InfoState infoState, Bindings bindings)
    {
        LinkedList args = new LinkedList();
        Contract ctr = infoState.cell(cellName).getContract();
        Contract.Method qry = ctr.method(methodName);
        Iterator parmit = qry.parameters();
        while (parmit.hasNext()) {
            Contract.Parameter parm = (Contract.Parameter)parmit.next();
            ArrayList al = new ArrayList();
            al.add(Variable.newVariable());
            Predicate pred = new Predicate(parm.name(), al);
            Object preds = infoState.cell("is").cell("shared").get("com");
            Stack planStack = (Stack)infoState.cell("is").cell("private").get("plan");
            Object plan = planStack.peek();
            if (!(preds instanceof Collection)) continue;
            if (inCurrentPlan(infoState,plan,pred,bindings)) {
                Object currentValue = infoState.getUnifier().deref(pred, bindings);
                System.out.println("adding argument "+currentValue+" from plan");
                args.add(currentValue);
            } else if (inCommonGround((Collection)preds,pred,bindings)) {
                Object currentValue = infoState.getUnifier().deref(pred, bindings);
                System.out.println("adding argument "+currentValue+" from com");
                args.add(currentValue);
            } else {
                System.out.println("adding argument "+pred);
                args.add(pred);
            }
        }
        return args;
    }
    
    /**
     * Takes the parameters returned from a query or method and
     * stores them in common ground, replacing the previous values
     * for those slots.
     *
     * It may be desirable for this routine to be expanded
     * so that it stores values in the current plan instance
     * as well. Really there should only be one place to store
     * slot information, but there are some undesirable interactions
     * between a stack of active tasks and a flat common ground,
     * and changing the structure of the common ground affects
     * many aspects of the DM.
     *
     * @param args a <code>Collection</code> value
     * @param infoState an <code>InfoState</code> value
     * @param bindings a <code>Bindings</code> value
     */
    protected void extractResults(Collection args, InfoState infoState, Bindings bindings)
    {
        //System.out.println("extractResults "+args);
        //System.out.println("extractResults bindings "+bindings);
        Iterator argit = args.iterator();
        while (argit.hasNext()) {
            Object arg = argit.next();
            if (!(arg instanceof Predicate)) continue;
            // all valid parameters are passed as Predicates.
            Predicate pred = (Predicate)arg;
            // first remove the existing value, if any.
            // then dereference the parameter and add it instead.
            // this version will add the dereferenced parameter
            // whether it was bound or not.
            // 2004/11/12: can't manipulate common ground like this.
            // information obtained from queries and methods is not
            // automatically part of the common ground, it is part
            // of the frame. it can't be considered common ground until
            // we've told the user about it.
            // that also implies that subsequent queries will start with
            // the same set of arguments, because we fill arguments from
            // common ground instead of from the frame. lays some bit of
            // groundwork for hypotheticals if we do it this way; there are
            // still problems with division of referent history this way.
            //consequence.removeCommonGround(pred, infoState, bindings);
            //consequence.addCommonGround(pred, infoState, bindings);
            consequence.unanswerPlanQuestion(pred, infoState, bindings);
            consequence.answerPlanQuestion(pred, infoState, bindings);
        }
    }
        
    /**
     * Populates the rule set. Every rule in 'update.mks' becomes
     * a subclass of ExistsRule, every submodule becomes a RuleSet.
     *
     */
    protected void buildRuleSet()
    {
        /*
         * Integration of system moves
         */

        /*
         * Integrate a system 'ask' move performed in response to a findout.
         */
        Condition integrateSysAskFindoutC = new Condition();
        integrateSysAskFindoutC.extend(new SpeakerIs("sys"));
        integrateSysAskFindoutC.extend(new MoveIsNotIntegrated(ask_q));
        integrateSysAskFindoutC.extend(new FirstOnAgenda(findout_q));
        ExistsRule integrateSysAskFindout =
            new ExistsRule(integrateSysAskFindoutC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.pushQUD(q, infoState, bindings);
                        consequence.markIntegrated(ask_q, infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysAskFindout);

        /*
         * Integrate a system 'ask' move performed in response to a raise.
         */
        Condition integrateSysAskRaiseC = new Condition();
        integrateSysAskRaiseC.extend(new SpeakerIs("sys"));
        integrateSysAskRaiseC.extend(new MoveIsNotIntegrated(ask_q));
        integrateSysAskRaiseC.extend(new FirstOnAgenda(raise_q));
        integrateSysAskRaiseC.extend(new NotFirstQuestionUnderDiscussion(q));
        ExistsRule integrateSysAskRaise =
            new ExistsRule(integrateSysAskRaiseC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.pushQUD(q, infoState, bindings);
                        consequence.popAgenda(infoState, bindings);
                        consequence.markIntegrated(ask_q, infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysAskRaise);

        /*
         * Integrate a system 'answer' move performed in response to a respond.
         */
        Condition integrateSysAnswerC = new Condition();
        integrateSysAnswerC.extend(new SpeakerIs("sys"));
        integrateSysAnswerC.extend(new MoveIsNotIntegrated(answer_q_r));
        integrateSysAnswerC.extend(new FirstOnAgenda(respond_q));
        integrateSysAnswerC.extend(new Reduces(q,r,p));
        ExistsRule integrateSysAnswer =
            new ExistsRule(integrateSysAnswerC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.popAgenda(infoState, bindings);
                        consequence.addCommonGround(p, infoState, bindings);
                        consequence.markIntegrated(answer_q_r, infoState, bindings);
                        consequence.answerPlanQuestion(p, infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysAnswer);

        /*
         * Integrate a system 'answer' move performed in response to a respond.
         */
        Condition integrateSysAlternativesAnswerC = new Condition();
        integrateSysAlternativesAnswerC.extend(new SpeakerIs("sys"));
        integrateSysAlternativesAnswerC.extend(new MoveIsNotIntegrated(answer_alts_q_r));
        integrateSysAlternativesAnswerC.extend(new FirstOnAgenda(respond_alts_q_r));
        ExistsRule integrateSysAlternativesAnswer =
            new ExistsRule(integrateSysAlternativesAnswerC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.popAgenda(infoState, bindings);
                        consequence.popQUD(infoState, bindings);
                        consequence.markIntegrated(answer_alts_q_r, infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysAlternativesAnswer);

        /*
         * Integrate a system 'inform' move performed in response to an inform.
         */
        Condition integrateSysInformC = new Condition();
        integrateSysInformC.extend(new SpeakerIs("sys"));
        integrateSysInformC.extend(new MoveIsNotIntegrated(inform_p));
        integrateSysInformC.extend(new FirstOnAgenda(inform_p));
        ExistsRule integrateSysInform =
            new ExistsRule(integrateSysInformC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.popAgenda(infoState, bindings);
                        consequence.addCommonGround(p, infoState, bindings);
                        consequence.markIntegrated(inform_p, infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysInform);

        /*
         * Integrate a system 'repeat' move performed in response to an repeat.
         */
        Condition integrateSysRepeatC = new Condition();
        integrateSysRepeatC.extend(new SpeakerIs("sys"));
        integrateSysRepeatC.extend(new MoveIsNotIntegrated(repeat_m));
        integrateSysRepeatC.extend(new FirstOnAgenda(repeat_m));
        ExistsRule integrateSysRepeat =
            new ExistsRule(integrateSysRepeatC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        //consequence.addUnintegratedMove(m, infoState, bindings);
                        consequence.markIntegrated(repeat_m, infoState, bindings);
                        consequence.popAgenda(infoState, bindings);
                        // we need to set our moves to match last utterance.
                        return true;
                    }
                };
        add(integrateSysRepeat);

        /*
         * Integrate a system 'reqRep' move.
         */
        Condition integrateSysReqRepC = new Condition();
        integrateSysReqRepC.extend(new SpeakerIs("sys"));
        integrateSysReqRepC.extend(new MoveIsNotIntegrated(reqRep_type));
        ExistsRule integrateSysReqRep =
            new ExistsRule(integrateSysReqRepC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.markIntegrated(reqRep_type, infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysReqRep);

        /*
         * Integrate a system 'ack' move.
         */
        Condition integrateSysAckC = new Condition();
        integrateSysAckC.extend(new SpeakerIs("sys"));
        integrateSysAckC.extend(new MoveIsNotIntegrated(ack_p));
        ExistsRule integrateSysAck =
            new ExistsRule(integrateSysAckC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.markIntegrated(ack_p, infoState, bindings);
                        consequence.popAgenda(infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysAck);

        /*
         * Integrate a system 'greet' move performed in response to an greet.
         */
        Condition integrateSysGreetC = new Condition();
        integrateSysGreetC.extend(new SpeakerIs("sys"));
        integrateSysGreetC.extend(new MoveIsNotIntegrated("greet"));
        integrateSysGreetC.extend(new FirstOnAgenda("greet"));
        ExistsRule integrateSysGreet =
            new ExistsRule(integrateSysGreetC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.popAgenda(infoState, bindings);
                        consequence.markIntegrated("greet", infoState, bindings);
                        consequence.clearCommonGround(infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysGreet);

        /*
         * Integrate a system 'assert' move.
         */
        Condition integrateSysAssertC = new Condition();
        integrateSysAssertC.extend(new SpeakerIs("sys"));
        integrateSysAssertC.extend(new FirstOnAgenda(assert_q_r));
        ExistsRule integrateSysAssert =
            new ExistsRule(integrateSysAssertC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.popAgenda(infoState, bindings);
                        Unify.getInstance().matchTerms(q,r,bindings);
                        consequence.answerPlanQuestion(r, infoState, bindings);
                        consequence.advancePlan(infoState, bindings);  // managePlan misses it
                        return true;
                    }
                };
        add(integrateSysAssert);

        /*
         * Integrate a system 'forget' move performed in response to an forget.
         * (It isn't clear to me why there are versions of this for plan management
         * as well as for integration. There should be only one copy. It should probably
         * be part of Integrate since, among other reasons, it failed to activate
         * for Midiki-EMMA when processing a forget(P).)
         */
        Condition integrateSysForgetC = new Condition();
        integrateSysForgetC.extend(new SpeakerIs("sys"));
        integrateSysForgetC.extend(new FirstOnAgenda("forget"));
        ExistsRule integrateSysForget =
            new ExistsRule(integrateSysForgetC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.popAgenda(infoState, bindings);
                        consequence.advancePlan(infoState, bindings);  // managePlan misses it
                        consequence.clearCommonGround(infoState, bindings);
                        consequence.clearMoves(infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysForget);

        /*
         * Integrate a system 'forget' move performed in response to an forget.
         * This version handles forgetting of a single item.
         */
        Condition integrateSysForgetItemC = new Condition();
        integrateSysForgetItemC.extend(new SpeakerIs("sys"));
        integrateSysForgetItemC.extend(new FirstOnAgenda(forget_r));
        ExistsRule integrateSysForgetItem =
            new ExistsRule(integrateSysForgetItemC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        //System.out.println("forget "+Unify.getInstance().deref(r, bindings));
                        consequence.popAgenda(infoState, bindings);
                        consequence.advancePlan(infoState, bindings);
                        consequence.removeCommonGround(r, infoState, bindings);
                        consequence.unanswerPlanQuestion(r, infoState, bindings);
                        consequence.removeCommonGround(answer_r, infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysForgetItem);

        /*
         * Integrate a system 'query' move.
         */
        Condition integrateSysQueryC = new Condition();
        integrateSysQueryC.extend(new SpeakerIs("sys"));
        integrateSysQueryC.extend(new FirstOnAgenda(query_q_r));
        ExistsRule integrateSysQuery =
            new ExistsRule(integrateSysQueryC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        String cellName =
                            (String)infoState.getUnifier().deref(q, bindings);
                        String queryName =
                            (String)infoState.getUnifier().deref(r, bindings);
                        boolean safe = true;
                        //System.out.println(">>>>> Query cell "+cellName+
                        //                   " query "+queryName);
                        try {
                            Object cell = infoState.cell(cellName);
                            if (cell == null) {
                                System.out.println(">>>>> cell "+cellName+" not found");
                                safe = false;
                            }
                        } catch (RuntimeException re) {
                            System.out.println(">>>>> cell "+cellName+" not found");
                            safe = false;
                        }
                        try {
                            Object query = infoState.cell(cellName).query(queryName);
                            if (query == null) {
                                System.out.println(">>>>> query "+queryName+" not found");
                                safe = false;
                            }
                        } catch (RuntimeException re) {
                            System.out.println(">>>>> query "+queryName+" not found");
                            safe = false;
                        }
                        //System.out.println("query safe? "+safe);
                        if (safe) {
                            // for each parameter, get its value from context.
                            // in the current model, that means common ground.
                            Collection arguments =
                                buildQueryArguments(cellName, queryName,
                                                     infoState, bindings);
                            //System.out.println("outgoing arguments = "+arguments);
                            infoState.cell(cellName).query(queryName).query(arguments, bindings);
                            //System.out.println("incoming bindings = "+bindings);
                            // update common ground with new parameter values.
                            extractResults(arguments, infoState, bindings);
                        }
                        consequence.popAgenda(infoState, bindings);
                        consequence.advancePlan(infoState, bindings);  // managePlan misses it
                        consequence.markIntegrated(query_q_r, infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysQuery);

        /*
         * Integrate a system 'method' move.
         */
        Condition integrateSysMethodC = new Condition();
        integrateSysMethodC.extend(new SpeakerIs("sys"));
        integrateSysMethodC.extend(new FirstOnAgenda(method_q_r));
        ExistsRule integrateSysMethod =
            new ExistsRule(integrateSysMethodC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        //System.out.println("integrateSysMethod: bindings == "+bindings);
                        String cellName =
                            (String)infoState.getUnifier().deref(q, bindings);
                        String methodName =
                            (String)infoState.getUnifier().deref(r, bindings);
                        boolean safe = true;
                        //System.out.println(">>>>> Method cell "+cellName+
                        //                   " method "+methodName);
                        try {
                            Object cell = infoState.cell(cellName);
                            if (cell == null) {
                                System.out.println(">>>>> cell "+cellName+" not found");
                                safe = false;
                            }
                        } catch (RuntimeException re) {
                            System.out.println(">>>>> cell "+cellName+" not found");
                            safe = false;
                        }
                        try {
                            Object method = infoState.cell(cellName).method(methodName);
                            if (method == null) {
                                System.out.println(">>>>> method "+methodName+" not found");
                                safe = false;
                            }
                        } catch (RuntimeException re) {
                            System.out.println(">>>>> method "+methodName+" not found");
                            safe = false;
                        }
                        if (safe) {
                            // for each parameter, get its value from context.
                            // in the current model, that means common ground.
                            // make two copies?
                            Collection arguments =
                                buildMethodArguments(cellName, methodName,
                                                     infoState, bindings);
                            //System.out.println("outgoing arguments = "+arguments);
                            infoState.cell(cellName).method(methodName).invoke(arguments, bindings);
                            //System.out.println("incoming bindings = "+bindings);
                            // update common ground with new parameter values.
                            extractResults(arguments, infoState, bindings);
                        }
                        consequence.popAgenda(infoState, bindings);
                        consequence.advancePlan(infoState, bindings);  // managePlan misses it
                        consequence.markIntegrated(method_q_r, infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysMethod);

        /*
         * Integrate a system 'bind' move.
         */
        Condition integrateSysBindC = new Condition();
        integrateSysBindC.extend(new SpeakerIs("sys"));
        integrateSysBindC.extend(new MoveIsNotIntegrated(bind_q_r));
        integrateSysBindC.extend(new FirstOnAgenda(bind_q_r));
        ExistsRule integrateSysBind =
            new ExistsRule(integrateSysBindC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.answerPlanQuestion(r, infoState, bindings);
                        consequence.markIntegrated(bind_q_r, infoState, bindings);
                        consequence.popAgenda(infoState, bindings);
                        consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysBind);

        /*
         * Integrate a system 'quit' move performed in response to
         * a scripted quit.
         */
        Condition integrateSysQuitC = new Condition();
        integrateSysQuitC.extend(new SpeakerIs("sys"));
        integrateSysQuitC.extend(new MoveIsNotIntegrated("quit"));
        integrateSysQuitC.extend(new FirstOnAgenda("quit"));
        ExistsRule integrateSysQuit =
            new ExistsRule(integrateSysQuitC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        System.out.println("Integrating system quit move");
                        consequence.popAgenda(infoState, bindings);
                        infoState.cell("is").put("program_state", "quitting");
                        consequence.markIntegrated("quit", infoState, bindings);
                        ArrayList arguments = new ArrayList();
                        infoState.cell("is_control").method("shutdown").invoke(arguments, bindings);
                        return true;
                    }
                };
        add(integrateSysQuit);

        /*
         * Integrate a system 'quit' move performed in response to
         * an unscripted quit; that is, one with a cause argument.
         */
        Condition integrateSysQuitCauseC = new Condition();
        integrateSysQuitCauseC.extend(new SpeakerIs("sys"));
        integrateSysQuitCauseC.extend(new MoveIsNotIntegrated(quit_cause));
        integrateSysQuitCauseC.extend(new FirstOnAgenda(quit_cause));
        ExistsRule integrateSysQuitCause =
            new ExistsRule(integrateSysQuitCauseC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        System.out.println("Integrating system quit move");
                        consequence.popAgenda(infoState, bindings);
                        infoState.cell("is").put("program_state", "quitting");
                        consequence.markIntegrated(quit_cause, infoState, bindings);
                        ArrayList arguments = new ArrayList();
                        infoState.cell("is_control").method("shutdown").invoke(arguments, bindings);
                        return true;
                    }
                };
        add(integrateSysQuitCause);

        /*
         * Integrate a system 'thank' move performed in response to an thank.
         */
        Condition integrateSysThankC = new Condition();
        integrateSysThankC.extend(new SpeakerIs("sys"));
        integrateSysThankC.extend(new MoveIsNotIntegrated("thank"));
        integrateSysThankC.extend(new FirstOnAgenda("thank"));
        ExistsRule integrateSysThank =
            new ExistsRule(integrateSysThankC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.popAgenda(infoState, bindings);
                        consequence.markIntegrated("thank", infoState, bindings);
                        return true;
                    }
                };
        add(integrateSysThank);

        /*
         * Integration of user moves
         */

        /*
         * Integrate a user ask in response to the most recent question.
         * This version of the ruleset assumes you are asking for the
         * available choices (alts(X)), which expands to alts(Ques, List).
         * Ought to use the domain to bind the possible alternatives.
         */
        Condition integrateUsrAskC = new Condition();
        integrateUsrAskC.extend(new SpeakerIs("usr"));
        integrateUsrAskC.extend(new MoveIsNotIntegrated(ask_q));
        integrateUsrAskC.extend(new FirstQuestionUnderDiscussion(q1));
        // the Unifies conditions here make this an alternatives question
        // remove those conditions to switch to normal questions
        integrateUsrAskC.extend(new Unifies(q,alts_x));
        integrateUsrAskC.extend(new FindAllAnswers(q1,x));
        integrateUsrAskC.extend(new Unifies(qnew,alts_q1_x));
        ExistsRule integrateUsrAsk =
            new ExistsRule(integrateUsrAskC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        if (logByMove) discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrAsk",
                                             "user utterance type",
                                             "question regarding available choices");
                        consequence.markIntegrated(ask_q, infoState, bindings);
                        consequence.pushQUD(qnew, infoState, bindings);
                        consequence.pushAgenda(respond_qnew, infoState, bindings);
                        return true;
                    }
                };
        add(integrateUsrAsk);

        /*
         * Integrate a user answer in response to a system task question,
         * as long as we have a plan for accomplishing the task.
         */
        Condition integrateUsrTaskAnswerC = new Condition();
        integrateUsrTaskAnswerC.extend(new SpeakerIs("usr"));
        integrateUsrTaskAnswerC.extend(new MoveIsNotIntegrated(answer_r));
        integrateUsrTaskAnswerC.extend(new FirstQuestionUnderDiscussion(q));
        integrateUsrTaskAnswerC.extend(new IsRelevantAnswer(q,r));
        integrateUsrTaskAnswerC.extend(new NotAlreadyAnswered(q));
        integrateUsrTaskAnswerC.extend(new Reduces(q,r,p));
        integrateUsrTaskAnswerC.extend(new Unifies(task_x,r));
        integrateUsrTaskAnswerC.extend(new HavePlan(x,plan));
        ExistsRule integrateUsrTaskAnswer =
            new ExistsRule(integrateUsrTaskAnswerC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        if (logByMove) discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrTaskAnswer",
                                             "user utterance type",
                                             "answer regarding task selection");
                        consequence.markIntegrated(answer_r, infoState, bindings);
                        consequence.answerPlanQuestion(p, infoState, bindings);
                        consequence.popQUD(infoState, bindings);
                        consequence.addCommonGround(p, infoState, bindings);

                        consequence.popPlan(infoState, bindings);
                        consequence.instantiatePlan(plan, infoState, bindings);
                        // pushAgenda(ack_p, infoState, bindings); // if needed
                        return true;
                    }
                };
        add(integrateUsrTaskAnswer);

        /*
         * Integrate a user answer without system prompting,
         * as long as we have a plan for accomplishing the task.
         * Defer actual instantiation of the plan to the
         * 'integrateUsrTaskChange' rule.
         */
        Condition integrateUsrTaskRequestC = new Condition();
        integrateUsrTaskRequestC.extend(new SpeakerIs("usr"));
        integrateUsrTaskRequestC.extend(new MoveIsNotIntegrated(answer_r));
        integrateUsrTaskRequestC.extend(new Unifies(task_x,r));
        integrateUsrTaskRequestC.extend(new HavePlan(x,plan));
        ExistsRule integrateUsrTaskRequest =
            new ExistsRule(integrateUsrTaskRequestC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        if (logByMove) discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrTaskRequest",
                                             "user utterance type",
                                             "system action request");
                        consequence.markIntegrated(answer_r, infoState, bindings);
                        //consequence.pushAgenda(task_x, infoState, bindings); // if needed
                        consequence.shiftTopic(task_x, infoState, bindings); // if needed
                        return true;
                    }
                };
        add(integrateUsrTaskRequest);

        /*
         * Integrate a user answer in response to a system question.
         */
        Condition integrateUsrAnswerC = new Condition();
        integrateUsrAnswerC.extend(new SpeakerIs("usr"));
        integrateUsrAnswerC.extend(new MoveIsNotIntegrated(answer_r));
        integrateUsrAnswerC.extend(new FirstQuestionUnderDiscussion(q));
        integrateUsrAnswerC.extend(new IsRelevantAnswer(q,r));
        integrateUsrAnswerC.extend(new NotAlreadyAnswered(q));
        integrateUsrAnswerC.extend(new Reduces(q,r,p));
        ExistsRule integrateUsrAnswer =
            new ExistsRule(integrateUsrAnswerC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        if (logByMove) discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrAnswer",
                                             "user utterance type",
                                             "answer");
                        consequence.markIntegrated(answer_r, infoState, bindings);
                        consequence.answerPlanQuestion(p, infoState, bindings);
                        consequence.popQUD(infoState, bindings);
                        consequence.addCommonGround(p, infoState, bindings);
                        // pushAgenda(ack_p, infoState, bindings); // if needed
                        return true;
                    }
                };
        add(integrateUsrAnswer);



        /*
         * Integrate a user-proposed topic change.
         * This is typically triggered by task accommodation.
         */
        Condition integrateUsrTopicChangeC = new Condition();
        integrateUsrTopicChangeC.extend(new SpeakerIs("usr"));
        integrateUsrTopicChangeC.extend(new NewTopic(task_x));
        integrateUsrTopicChangeC.extend(new AgendaIsEmpty());
        ExistsRule integrateUsrTopicChange =
            new ExistsRule(integrateUsrTopicChangeC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrTopicChange",
                                             "accepting topic change",
                                             Unify.getInstance().deref(x, bindings));
                        consequence.pushAgenda(task_x, infoState, bindings);
                        consequence.shiftTopic(null, infoState, bindings);
                        return true;
                    }
                };
        add(integrateUsrTopicChange);

        /*
         * Integrate a user answer in response to a task change.
         * This is typically triggered by task accommodation; we put it
         * after data item accommodation so that a 'task' slot can
         * override task initiation.
         */
        Condition integrateUsrTaskChangeC = new Condition();
        integrateUsrTaskChangeC.extend(new SpeakerIs("usr"));
        //integrateUsrTaskChangeC.extend(new MoveIsNotIntegrated(answer_r));
        integrateUsrTaskChangeC.extend(new FirstOnAgenda(task_x));
        //integrateUsrTaskChangeC.extend(new Unifies(task_x,r));
        integrateUsrTaskChangeC.extend(new HavePlan(x, plan));
        ExistsRule integrateUsrTaskChange =
            new ExistsRule(integrateUsrTaskChangeC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrTaskChange",
                                             "changing system task",
                                             Unify.getInstance().deref(x, bindings));
                        //consequence.markIntegrated(answer_r, infoState, bindings);
                        consequence.popAgenda(infoState, bindings);
                        //consequence.addCommonGround(task_task, infoState, bindings);
                        consequence.instantiatePlan(plan, infoState, bindings);
                        //consequence.clearAgenda(infoState, bindings);
                        return true;
                    }
                };
        add(integrateUsrTaskChange);

        /*
         * Integrate a user answer in response to a system question.
         * This version will succeed if the answer is relevant but
         * improperly formatted, blocking recognition of the answer
         * as invalid. Accommodation will need to fix the format;
         * we do nothing here.
         */
        Condition integrateValidUsrAnswerC = new Condition();
        integrateValidUsrAnswerC.extend(new SpeakerIs("usr"));
        integrateValidUsrAnswerC.extend(new MoveIsNotIntegrated(answer_r));
        integrateValidUsrAnswerC.extend(new FirstQuestionUnderDiscussion(q));
        integrateValidUsrAnswerC.extend(new IsRelevantAnswer(q,r));
        integrateValidUsrAnswerC.extend(new NotAlreadyAnswered(q));
        ExistsRule integrateValidUsrAnswer =
            new ExistsRule(integrateValidUsrAnswerC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        return false;
                    }
                };
        add(integrateValidUsrAnswer);

        /*
         * Integrate an invalid user answer in response to a system question.
         * Triggered if answer is correctly formatted but not relevant
         * for the available domain resources.
         */
        Condition integrateInvalidUsrAnswerC = new Condition();
        integrateInvalidUsrAnswerC.extend(new SpeakerIs("usr"));
        integrateInvalidUsrAnswerC.extend(new MoveIsNotIntegrated(answer_r));
        integrateInvalidUsrAnswerC.extend(new FirstQuestionUnderDiscussion(q));
        //integrateInvalidUsrAnswerC.extend(new IsRelevantAnswer(q,r));
        integrateInvalidUsrAnswerC.extend(new NotAlreadyAnswered(q));
        integrateInvalidUsrAnswerC.extend(new Reduces(q,r,p));
        ExistsRule integrateInvalidUsrAnswer =
            new ExistsRule(integrateInvalidUsrAnswerC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateInvalidUsrAnswer",
                                             "answer not relevant",
                                             Unify.getInstance().deref(r, bindings));
                        consequence.markIntegrated(answer_r, infoState, bindings);
                        //consequence.answerPlanQuestion(p, infoState, bindings);
                        //consequence.popQUD(infoState, bindings);
                        //consequence.addCommonGround(p, infoState, bindings);
                        consequence.pushAgenda(inform_invalid_answer_r, infoState, bindings); // if needed
                        return true;
                    }
                };
        add(integrateInvalidUsrAnswer);

        /*
         * Integrate a user reqRep, when we just repeated content last turn.
         * Simple strategy: repeat it again. Need to catch this case to prevent
         * infinite regress of 'repeat' clauses, requiring special handling to
         * extract the proper moves.
         */
        Condition integrateUsrReqRepAgainC = new Condition();
        integrateUsrReqRepAgainC.extend(new SpeakerIs("usr"));
        integrateUsrReqRepAgainC.extend(new MoveIsNotIntegrated(reqRep_type));
        integrateUsrReqRepAgainC.extend(new IsPreviousMove(repeat_m));
        ExistsRule integrateUsrReqRepAgain =
            new ExistsRule(integrateUsrReqRepAgainC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        if (logByMove) discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrReqRepAgain",
                                             "user utterance type",
                                             "dialog action request");
                        Unify.getInstance().matchTerms(m, infoState.cell("is").get("moves_for_last_turn"), bindings);
                        consequence.pushAgenda(repeat_m, infoState, bindings);
                        consequence.markIntegrated(reqRep_type, infoState, bindings);
                        // restore the entire cell from the last utterance
                        // (note: this no longer does what Godis expected
                        // it to, because there can be and often are multiple
                        // decision cycles within a single system move,
                        // and thus multiple state changes/calls to Store.)
                        //infoState.cell("is").put("shared",
                        //    infoState.cell("is").cell("private").get("tmp"));
                        return true;
                    }
                };
        add(integrateUsrReqRepAgain);

        /*
         * Integrate a user reqRep. We are not repeating content again.
         */
        Condition integrateUsrReqRepC = new Condition();
        integrateUsrReqRepC.extend(new SpeakerIs("usr"));
        integrateUsrReqRepC.extend(new MoveIsNotIntegrated(reqRep_type));
        ExistsRule integrateUsrReqRep =
            new ExistsRule(integrateUsrReqRepC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        if (logByMove) discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrReqRep",
                                             "user utterance type",
                                             "dialog action request");
                        Unify.getInstance().matchTerms(m, infoState.cell("is").get("moves_for_last_turn"), bindings);
                        consequence.pushAgenda(repeat_m, infoState, bindings);
                        consequence.markIntegrated(reqRep_type, infoState, bindings);
                        // restore the entire cell from the last utterance
                        // (note: this no longer does what Godis expected
                        // it to, because there can be and often are multiple
                        // decision cycles within a single system move,
                        // and thus multiple state changes/calls to Store.)
                        infoState.cell("is").put("shared",
                            infoState.cell("is").cell("private").get("tmp"));
                        return true;
                    }
                };
        add(integrateUsrReqRep);

        /*
         * Integrate a "failed" move, indicating that we couldn't
         * parse the user input. Treat it as a request to repeat.
         * This condition fires if our last turn included a repeat.
         */
        Condition integrateUsrFailedAgainC = new Condition();
        integrateUsrFailedAgainC.extend(new SpeakerIs("usr"));
        integrateUsrFailedAgainC.extend(new MoveIsNotIntegrated("failed"));
        integrateUsrFailedAgainC.extend(new IsPreviousMove(repeat_m));
        ExistsRule integrateUsrFailedAgain =
            new ExistsRule(integrateUsrFailedAgainC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrFailedAgain",
                                             "user utterance type",
                                             "dialog action request (implied)");
                        Unify.getInstance().matchTerms(m, infoState.cell("is").get("moves_for_last_turn"), bindings);
                        consequence.pushAgenda(repeat_m, infoState, bindings);
                        consequence.markIntegrated("failed", infoState, bindings);
                        // restore the entire cell from the last utterance
                        // (note: this no longer does what Godis expected
                        // it to, because there can be and often are multiple
                        // decision cycles within a single system move,
                        // and thus multiple state changes/calls to Store.)
                        infoState.cell("is").put("shared",
                            infoState.cell("is").cell("private").get("tmp"));
                        return true;
                    }
                };
        add(integrateUsrFailedAgain);

        /*
         * Integrate a "failed" move, indicating that we couldn't
         * parse the user input. Treat it as a request to repeat.
         */
        Condition integrateUsrFailedC = new Condition();
        integrateUsrFailedC.extend(new SpeakerIs("usr"));
        integrateUsrFailedC.extend(new MoveIsNotIntegrated("failed"));
        ExistsRule integrateUsrFailed =
            new ExistsRule(integrateUsrFailedC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrFailed",
                                             "user utterance type",
                                             "dialog action request (implied)");
                        Unify.getInstance().matchTerms(m, infoState.cell("is").get("moves_for_last_turn"), bindings);
                        consequence.pushAgenda(repeat_m, infoState, bindings);
                        consequence.markIntegrated("failed", infoState, bindings);
                        // restore the entire cell from the last utterance
                        // (note: this no longer does what Godis expected
                        // it to, because there can be and often are multiple
                        // decision cycles within a single system move,
                        // and thus multiple state changes/calls to Store.)
                        infoState.cell("is").put("shared",
                            infoState.cell("is").cell("private").get("tmp"));
                        return true;
                    }
                };
        add(integrateUsrFailed);

        /*
         * Integrate a user greet.
         */
        Condition integrateUsrGreetC = new Condition();
        integrateUsrGreetC.extend(new SpeakerIs("usr"));
        integrateUsrGreetC.extend(new MoveIsNotIntegrated("greet"));
        ExistsRule integrateUsrGreet =
            new ExistsRule(integrateUsrGreetC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        if (logByMove) discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrGreet",
                                             "user utterance type",
                                             "greeting");
                        consequence.pushAgenda("greet", infoState, bindings);
                        consequence.markIntegrated("greet", infoState, bindings);
                        return true;
                    }
                };
        add(integrateUsrGreet);

        /*
         * Integrate a user quit.
         */
        Condition integrateUsrQuitC = new Condition();
        integrateUsrQuitC.extend(new SpeakerIs("usr"));
        integrateUsrQuitC.extend(new MoveIsNotIntegrated("quit"));
        ExistsRule integrateUsrQuit =
            new ExistsRule(integrateUsrQuitC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        System.out.println("Integrating user quit move");
                        consequence.pushAgenda(quit_atrequest, infoState, bindings);
                        consequence.markIntegrated("quit", infoState, bindings);
                        return true;
                    }
                };
        add(integrateUsrQuit);

        /*
         * Integrate a user ack in response to my planned inform.
         * The acknowledgement move is arity 0.
         * This setup surrenders the turn to user after system inform.
         */
        Condition integrateUsrAckInformC = new Condition();
        integrateUsrAckInformC.extend(new SpeakerIs("usr"));
        integrateUsrAckInformC.extend(new MoveIsNotIntegrated("ack"));
        integrateUsrAckInformC.extend(new NextPlanStep(inform_p));
        integrateUsrAckInformC.extend(new IsCommonGround(p));
        ExistsRule integrateUsrAckInform =
            new ExistsRule(integrateUsrAckInformC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        if (logByMove) discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrAckInform",
                                             "user utterance type",
                                             "action acknowledgement");
                        consequence.markIntegrated("ack", infoState, bindings);
                        consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(integrateUsrAckInform);

        /*
         * Integrate a user ack in response to unknown stimulus.
         * The acknowledgement move is arity 0.
         */
        Condition integrateUsrAckUnknownC = new Condition();
        integrateUsrAckUnknownC.extend(new SpeakerIs("usr"));
        integrateUsrAckUnknownC.extend(new MoveIsNotIntegrated("ack"));
        ExistsRule integrateUsrAckUnknown =
            new ExistsRule(integrateUsrAckUnknownC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        if (logByMove) discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Integrate",
                                             "integrateUsrAckInform",
                                             "user utterance type",
                                             "action acknowledgement (inferred)");
                        consequence.markIntegrated("ack", infoState, bindings);
                        return true;
                    }
                };
        add(integrateUsrAckUnknown);

        /*
         * Integrate a user's failure to move. The intent is that the
         * integrated condition will be recognized elsewhere as a prompt
         * for presentation of available choices to the user.
         */
        Condition integrateUsrNoMoveC = new Condition();
        integrateUsrNoMoveC.extend(new SpeakerIs("usr"));
        integrateUsrNoMoveC.extend(new MoveIsNotIntegrated("no_move"));
        ExistsRule integrateUsrNoMove =
            new ExistsRule(integrateUsrNoMoveC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.markIntegrated("no_move", infoState, bindings);
                        return true;
                    }
                };
        add(integrateUsrNoMove);
                }
}

