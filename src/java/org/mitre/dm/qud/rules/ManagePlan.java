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
 
public class ManagePlan extends RuleSet
{
    private Logger discourseLogger = null;
    Result consequence;

    /*
     * Variables
     */
    private Variable anonymous;
    private Variable a;
    private Variable m;
    private Variable p;
    private Variable p1;
    private Variable q;
    private Variable q1;
    private Variable qnew;
    private Variable r;
    private Variable type;
    private Variable t;
    private Variable task;
    private Variable alttask;
    private Variable knowntask;
    private Variable tasklist;
    private Variable x;
    private Variable move;
    private Variable step;
    private Variable plan;
    private Variable shared_belief;

    /*
     * Predicates
     */
    private Predicate reqRep_und;
    private Predicate reqRep_rel;
    private Predicate reqRep_type;
    private Predicate ack_p;
    private Predicate ack_x;
    private Predicate assert_q_r;
    private Predicate repeat_m;
    private Predicate inform_p;
    private Predicate respond_p;
    private Predicate respond_q;
    private Predicate respond_qnew;
    private Predicate answer_r;
    private Predicate answer_q_r;
    private Predicate ask_q;
    private Predicate findout_q;
    private Predicate findout_tasklist;
    private Predicate raise_q;
    private Predicate exec_t;
    private Predicate call_t;
    private Predicate forget_r;
    private Predicate query_q;
    private Predicate method_q;
    private Predicate task_findout_q;
    private Predicate task_alttask;
    private Predicate task_knowntask;
    private Predicate task_anon;
    private Predicate task_x;
    private Predicate task_t;
    private Predicate ask_task_x;
    private Predicate alts_x;
    private Predicate alts_q1_x;

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

    public ManagePlan(Logger theLogger)
    {
        super();
        discourseLogger = theLogger;
        consequence = new Result();
        consequence.setDiscourseLogger(discourseLogger);

        anonymous = new Variable();
        a = new Variable("A");
        m = new Variable("M");
        p = new Variable("P");
        p1 = new Variable("P1");
        q = new Variable("Q");
        q1 = new Variable("Q1");
        qnew = new Variable("Qnew");
        r = new Variable("R");
        t = new Variable("T");
        task = new Variable("Task");
        alttask = new Variable("AltTask");
        knowntask = new Variable("KnownTask");
        tasklist = new Variable("TaskList");
        type = new Variable("Type");
        x = new Variable("X");
        move = new Variable("Move");
        step = new Variable("Step");
        plan = new Variable("Plan");
        shared_belief = new Variable("SharedBelief");

        LinkedList args;

        args = new LinkedList();
        args.add("understanding");
        Predicate reqRep_und = new Predicate("reqRep",args);

        args = new LinkedList();
        args.add("relevance");
        Predicate reqRep_rel = new Predicate("reqRep",args);

        args = new LinkedList();
        args.add(type);
        Predicate reqRep_type = new Predicate("reqRep",args);

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
        args.add(p);
        respond_p = new Predicate("respond",args);

        args = new LinkedList();
        args.add(q);
        respond_q = new Predicate("respond",args);

        args = new LinkedList();
        args.add(qnew);
        respond_qnew = new Predicate("respond",args);

        args = new LinkedList();
        args.add(r);
        answer_r = new Predicate("answer",args);

        args = new LinkedList();
        args.add(q);
        args.add(r);
        answer_q_r = new Predicate("answer",args);

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
        args.add(tasklist);
        findout_tasklist = new Predicate("findout",args);

        args = new LinkedList();
        args.add(q);
        raise_q = new Predicate("raise",args);

        args = new LinkedList();
        args.add(t);
        exec_t = new Predicate("exec",args);

        args = new LinkedList();
        args.add(t);
        call_t = new Predicate("call",args);

        args = new LinkedList();
        args.add(r);
        forget_r = new Predicate("forget",args);

        args = new LinkedList();
        args.add(q);
        query_q = new Predicate("query",args);

        args = new LinkedList();
        args.add(q);
        method_q = new Predicate("method",args);

        args = new LinkedList();
        args.add(findout_q);
        task_findout_q = new Predicate("task",args);

        args = new LinkedList();
        args.add(anonymous);
        task_anon = new Predicate("task",args);

        args = new LinkedList();
        args.add(t);
        task_t = new Predicate("task",args);

        args = new LinkedList();
        args.add(alttask);
        task_alttask = new Predicate("task",args);

        args = new LinkedList();
        args.add(knowntask);
        task_knowntask = new Predicate("task",args);

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

        buildRuleSet();
    }

    public ExistsRule findPlan;  // callable separately

    /**
     * Populates the rule set. Every rule in 'update.mks' becomes
     * a subclass of ExistsRule, every submodule becomes a RuleSet.
     *
     */
    protected void buildRuleSet()
    {

        /*
         * Find a new plan if we have a task but no more plan steps
         * (or no plan).
         * (Should only apply to incomplete tasks)
         */
        Condition findPlanC = new Condition();
        findPlanC.extend(new NoNextStep(step));
        findPlanC.extend(new IsCommonGround(task_t));
        findPlanC.extend(new HavePlan(t,plan));
        findPlan =
            new ExistsRule(findPlanC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.instantiatePlan(plan, infoState, bindings);
                        return true;
                    }
                };
        //add(findPlan); // I think this is a separate rule

        /*
         * Remove a findout action when the question has been answered.
         */
        Condition removePlanFindoutAnsweredC = new Condition();
        removePlanFindoutAnsweredC.extend(new NextPlanStep(findout_q));
        removePlanFindoutAnsweredC.extend(new IsCommonGround(p));
        removePlanFindoutAnsweredC.extend(new IsRelevantAnswer(q,p));
        ExistsRule removePlanFindoutAnswered =
            new ExistsRule(removePlanFindoutAnsweredC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.answerPlanQuestion(p, infoState, bindings);
                        consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(removePlanFindoutAnswered);

        /*
         * Remove a findout action for a task question if a known task
         * is dominated by one of the alternatives (assumes all alternatives
         * are at the same level).
         */
        Condition removePlanFindoutDominatedC = new Condition();
        removePlanFindoutDominatedC.extend(new NextPlanStep(findout_tasklist));
        removePlanFindoutDominatedC.extend(new IsCommonGround(task_knowntask));
        removePlanFindoutDominatedC.extend(new IsMember(tasklist, task_alttask));
        removePlanFindoutDominatedC.extend(new Dominates(alttask,knowntask));
        ExistsRule removePlanFindoutDominated =
            new ExistsRule(removePlanFindoutDominatedC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(removePlanFindoutDominated); 

        /*
         * Remove a raise action if the question is already answered.
         */
        Condition removePlanRaiseC = new Condition();
        removePlanRaiseC.extend(new NextPlanStep(raise_q));
        removePlanRaiseC.extend(new IsCommonGround(p));
        removePlanRaiseC.extend(new IsRelevantAnswer(q,p));
        ExistsRule removePlanRaise =
            new ExistsRule(removePlanRaiseC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(removePlanRaise); 

        /*
         * Remove a respond action if the question is already answered.
         */
        Condition removePlanRespondC = new Condition();
        removePlanRespondC.extend(new NextPlanStep(respond_p));
        removePlanRespondC.extend(new IsCommonGround(p));
        ExistsRule removePlanRespond =
            new ExistsRule(removePlanRespondC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(removePlanRespond); 

        /*
         * Remove an inform action if the information was already provided.
         */
        Condition removePlanInformC = new Condition();
        removePlanInformC.extend(new NextPlanStep(inform_p));
        removePlanInformC.extend(new IsCommonGround(p));
        ExistsRule removePlanInform =
            new ExistsRule(removePlanInformC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(removePlanInform); 

        /*
         * Execute a chained task. Remove current plan before
         * pushing new plan, because we will not be returning.
         */
        Condition removePlanExecC = new Condition();
        removePlanExecC.extend(new NextPlanStep(exec_t));
        removePlanExecC.extend(new HavePlan(t,p));
        ExistsRule removePlanExec =
            new ExistsRule(removePlanExecC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.popPlan(infoState, bindings);
                        consequence.instantiatePlan(p, infoState, bindings);
                        return true;
                    }
                };
        add(removePlanExec); 

        /*
         * Execute a subroutine task. Advance current plan past the call
         * before pushing the new plan, so that we can resume at the
         * proper location.
         */
        Condition removePlanCallC = new Condition();
        removePlanCallC.extend(new NextPlanStep(call_t));
        removePlanCallC.extend(new HavePlan(t,p));
        ExistsRule removePlanCall =
            new ExistsRule(removePlanCallC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.advancePlan(infoState, bindings);
                        consequence.instantiatePlan(p, infoState, bindings);
                        return true;
                    }
                };
        add(removePlanCall); 

        /*
         * Remove "greet" once executed.
         */
        Condition removePlanGreetC = new Condition();
        removePlanGreetC.extend(new NextPlanStep("greet"));
        removePlanGreetC.extend(new MoveIsIntegrated("greet"));
        ExistsRule removePlanGreet =
            new ExistsRule(removePlanGreetC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(removePlanGreet); 

        /*
         * Remove "thank" once executed.
         */
        Condition removePlanThankC = new Condition();
        removePlanThankC.extend(new NextPlanStep("thank"));
        removePlanThankC.extend(new MoveIsIntegrated("thank"));
        ExistsRule removePlanThank =
            new ExistsRule(removePlanThankC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(removePlanThank); 

        /*
         * Remove "assert" once executed.
         */
        Condition removePlanAssertC = new Condition();
        removePlanAssertC.extend(new NextPlanStep("assert_q_r"));
        ExistsRule removePlanAssert =
            new ExistsRule(removePlanAssertC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(removePlanAssert); 

        /*
         * Remove "forget" once executed.
         */
        Condition removePlanForgetAllC = new Condition();
        removePlanForgetAllC.extend(new NextPlanStep("forget"));
        ExistsRule removePlanForgetAll =
            new ExistsRule(removePlanForgetAllC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.advancePlan(infoState, bindings);
                        consequence.clearCommonGround(infoState, bindings);
                        consequence.clearMoves(infoState, bindings);
                        return true;
                    }
                };
        add(removePlanForgetAll); 

        /*
         * Remove "forget" once executed. (Specific information)
         */
        Condition removePlanForgetItemC = new Condition();
        removePlanForgetItemC.extend(new NextPlanStep(forget_r));
        ExistsRule removePlanForgetItem =
            new ExistsRule(removePlanForgetItemC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        //System.out.println("forget (ManagePlan) "+Unify.getInstance().deref(r, bindings));
                        consequence.advancePlan(infoState, bindings);
                        consequence.removeCommonGround(r, infoState, bindings);
                        consequence.removeCommonGround(answer_r, infoState, bindings);
                        consequence.unanswerPlanQuestion(r, infoState, bindings);
                        return true;
                    }
                };
        add(removePlanForgetItem); 

        /*
         * Remove any other sysaction once executed.
         */
        Condition removePlanSysactionC = new Condition();
        removePlanSysactionC.extend(new NextPlanStep(a));
        removePlanSysactionC.extend(new IsSysaction(a));
        ExistsRule removePlanSysaction =
            new ExistsRule(removePlanSysactionC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(removePlanSysaction); 

    }
}
