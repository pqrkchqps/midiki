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
 * Refills the system agenda from various sources,
 * including user questions and the current plan.
 * 
 */
 
public class RefillAgenda extends RuleSet
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.rules.RefillAgenda");
    private Logger discourseLogger = null;
    Result consequence;

    /*
     * Variables
     */
    private Variable anon;
    private Variable action;
    private Variable as;
    private Variable m;
    private Variable p;
    private Variable p1;
    private Variable q;
    private Variable q1;
    private Variable len;
    private Variable r;
    private Variable type;
    private Variable x;
    private Variable move;
    private Variable task;
    private Variable tasks;

    /*
     * Predicates
     */
    private Predicate reqRep_und;
    private Predicate reqRep_rel;
    private Predicate reqRep_type;
    private Predicate ack_p;
    private Predicate ack_x;
    private Predicate repeat_m;
    private Predicate respond_q;
    private Predicate respond_len;
    private Predicate answer_r;
    private Predicate answer_task_anon;
    private Predicate ask_q;
    private Predicate findout_tasks;
    private Predicate raise_q;
    private Predicate task_findout_tasks;
    private Predicate task_anon;
    private Predicate task_x;
    private Predicate ask_task_x;
    private Predicate alts_x;
    private Predicate alts_q1_anon;
    private Predicate alts_q1_as;
    private Predicate alts_q1_x;
    private Predicate respond_alts_q1_x;

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

    public RefillAgenda(Logger theLogger)
    {
        super();
        discourseLogger = theLogger;
        consequence = new Result();
        consequence.setDiscourseLogger(discourseLogger);

        anon = new Variable();
        action = new Variable("Action");
        as = new Variable("As");
        m = new Variable("M");
        p = new Variable("P");
        p1 = new Variable("P1");
        q = new Variable("Q");
        q1 = new Variable("Q1");
        len = new Variable("Len");
        r = new Variable("R");
        type = new Variable("Type");
        x = new Variable("X");
        move = new Variable("Move");
        task = new Variable("Task");
        tasks = new Variable("Tasks");

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
        args.add(q);
        respond_q = new Predicate("respond",args);

        args = new LinkedList();
        args.add(len);
        respond_len = new Predicate("respond",args);

        args = new LinkedList();
        args.add(r);
        answer_r = new Predicate("answer",args);

        args = new LinkedList();
        args.add(task);
        args.add(anon);
        answer_task_anon = new Predicate("answer",args);

        args = new LinkedList();
        args.add(q);
        ask_q = new Predicate("ask",args);

        args = new LinkedList();
        args.add(tasks);
        findout_tasks = new Predicate("findout",args);

        args = new LinkedList();
        args.add(q);
        raise_q = new Predicate("raise",args);

        args = new LinkedList();
        args.add(findout_tasks);
        task_findout_tasks = new Predicate("task",args);

        args = new LinkedList();
        args.add(anon);
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
        args.add(anon);
        alts_q1_anon = new Predicate("alts",args);

        args = new LinkedList();
        args.add(q1);
        args.add(as);
        alts_q1_as = new Predicate("alts",args);

        args = new LinkedList();
        args.add(q1);
        args.add(x);
        alts_q1_x = new Predicate("alts",args);
        args = new LinkedList();
        args.add(alts_q1_x);
        respond_alts_q1_x = new Predicate("respond",args);

        buildRuleSet();
    }

    
    /**
     * Populates the rule set. Every rule in 'update.mks' becomes
     * a subclass of ExistsRule, every submodule becomes a RuleSet.
     *
     */
    protected void buildRuleSet()
    {
        /*
         * Find an answer to a user question and store it in
         * system's private beliefs. This is an alternatives
         * question, where user asks for alternative answers
         * to question on QUD.
         */
        Condition findAnswerC = new Condition();
        findAnswerC.extend(new FirstOnAgenda(respond_alts_q1_x));
        findAnswerC.extend(new IsNotABelief(alts_q1_anon));
        findAnswerC.extend(new FindAllAnswers(q1,as));
        ExistsRule findAnswer =
            new ExistsRule(findAnswerC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.addBelief(alts_q1_as, infoState, bindings);
                        return true;
                    }
                };
        add(findAnswer);

        /*
         * If an answer is relevant to more than one task,
         * ask which task the user wants to pursue unless
         * one of the tasks is already in common ground.
         */
        Condition clarifyTaskC = new Condition();
        clarifyTaskC.extend(new SpeakerIs("usr"));
        clarifyTaskC.extend(new MoveIsNotIntegrated(move));
        clarifyTaskC.extend(new IsRelevantToTasks(move,tasks));
        clarifyTaskC.extend(new ListLength(tasks,len));
        clarifyTaskC.extend(new GreaterThan(len,new Integer(1)));
        clarifyTaskC.extend(new IsNot(move,answer_task_anon));
        clarifyTaskC.extend(new NotFirstOnAgenda(findout_tasks));
        Condition ismember = new IsMember(tasks,task);
        ismember.extend(new IsCommonGround(task));
        clarifyTaskC.extend(new Never(ismember));
        ExistsRule clarifyTask =
            new ExistsRule(clarifyTaskC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.pushAgenda(findout_tasks, infoState, bindings);
                        return true;
                    }
                };
        add(clarifyTask);

        /*
         * Refill the agenda with the next action on the plan.
         */
        Condition refillAgendaFromPlanC = new Condition();
        refillAgendaFromPlanC.extend(new AgendaIsEmpty());
        refillAgendaFromPlanC.extend(new NextPlanStep(action));
        ExistsRule refillAgendaFromPlan =
            new ExistsRule(refillAgendaFromPlanC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        logger.logp(Level.FINER,"org.mitre.dm.qud.rules.RefillAgenda","refillAgendaFromPlan.execute","refilling from plan",bindings);
                        consequence.pushAgenda(action, infoState, bindings);
                        return true;
                    }
                };
        add(refillAgendaFromPlan);

        /*
         * Refill the agenda by resuming a previous plan, if possible.
         * To ensure we avoid infinite loops, need a condition to see if
         * the plan stack is empty.
         */
        Condition refillAgendaFromPlanStackC = new Condition();
        refillAgendaFromPlanStackC.extend(new AgendaIsEmpty());
        refillAgendaFromPlanStackC.extend(new NoNextStep(q));
        refillAgendaFromPlanStackC.extend(new PlanStackNotEmpty());
        ExistsRule refillAgendaFromPlanStack =
            new ExistsRule(refillAgendaFromPlanStackC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        logger.logp(Level.FINER,"org.mitre.dm.qud.rules.RefillAgenda","refillAgendaFromPlanStack.execute","refilling from plan stack",bindings);
                        consequence.popPlan(infoState, bindings);
                        //consequence.advancePlan(infoState, bindings);
                        return true;
                    }
                };
        add(refillAgendaFromPlanStack);

       }
}
