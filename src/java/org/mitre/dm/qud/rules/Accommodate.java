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

import org.mitre.dm.*;
import org.mitre.dm.qud.conditions.*;

import java.util.*;
import java.util.logging.*;

/**
 * Implements the integration rules for the update phase. 
 * Integration rules for system moves match perceived moves
 * against system intentions. System moves never have any
 * real effect until integration time.
 * 
 */
 
public class Accommodate extends RuleSet
{
    private Logger logger =
        Logger.getLogger("org.mitre.dm.qud.rules.Accommodate");
    private Logger discourseLogger = null;
    Result consequence;
    /*
     * Variables
     */
    private Variable anonymous;
    private Variable a;
    private Variable m;
    private Variable oldp;
    private Variable p;
    private Variable p1;
    private Variable q;
    private Variable qf;
    private Variable q1;
    private Variable qnew;
    private Variable r;
    private Variable type;
    private Variable task;
    private Variable tasks;
    private Variable oldtask;
    private Variable x;
    private Variable move;
    private Variable plan;

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
    private Predicate respond_qnew;
    private Predicate answer_a;
    private Predicate answer_r;
    private Predicate answer_q_r;
    private Predicate ask_q;
    private Predicate findout_q;
    private Predicate raise_q;
    private Predicate task_findout_q;
    private Predicate task_anon;
    private Predicate task_x;
    private Predicate task_task;
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

    public Accommodate(Logger theLogger)
    {
        super();
        discourseLogger = theLogger;
        consequence = new Result();
        consequence.setDiscourseLogger(discourseLogger);

        anonymous = new Variable();
        a = new Variable("A");
        m = new Variable("M");
        oldp = new Variable("OldP");
        p = new Variable("P");
        p1 = new Variable("P1");
        q = new Variable("Q");
        q = new Variable("Qf");
        q1 = new Variable("Q1");
        qnew = new Variable("Qnew");
        r = new Variable("R");
        type = new Variable("Type");
        task = new Variable("Task");
        tasks = new Variable("Tasks");
        oldtask = new Variable("OldTask");
        x = new Variable("X");
        move = new Variable("Move");
        plan = new Variable("Plan");

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
        args.add(qnew);
        respond_qnew = new Predicate("respond",args);

        args = new LinkedList();
        args.add(a);
        answer_a = new Predicate("answer",args);

        args = new LinkedList();
        args.add(r);
        answer_r = new Predicate("answer",args);

        args = new LinkedList();
        args.add(q);
        args.add(r);
        answer_q_r = new Predicate("answer",args);

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
        args.add(task);
        task_task = new Predicate("task",args);

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

    /**
     * Populates the rule set. Every rule in 'update.mks' becomes
     * a subclass of ExistsRule, every submodule becomes a RuleSet.
     *
     */
    protected void buildRuleSet()
    {

        /*
         * If we're trying to shift topics, skip accommodation.
         * We can try again when we have finished shifting.
         */
        Condition shiftingTopicC = new Condition();
        shiftingTopicC.extend(new NewTopic(q));
        ExistsRule shiftingTopic=
            new ExistsRule(shiftingTopicC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        return false;
                    }
                };
        add(shiftingTopic);

        /*
         * Accommodate a question that has already been answered,
         * for which we have received a different answer.
         */
        Condition reaccommodateQuestionC = new Condition();
        reaccommodateQuestionC.extend(new SpeakerIs("usr"));
        reaccommodateQuestionC.extend(new MoveIsNotIntegrated(answer_a));
        reaccommodateQuestionC.extend(new IsCommonGround(oldp));
        reaccommodateQuestionC.extend(new Abstracts(a,oldp,q));
        reaccommodateQuestionC.extend(new IsNotRelevantCategory(q,"yesno"));
        ExistsRule reaccommodateQuestion =
            new ExistsRule(reaccommodateQuestionC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Accommodate",
                                             "reaccommodateQuestion",
                                             "change existing data",
                                             infoState.getUnifier().deref(q, bindings));
                        consequence.removeCommonGround(oldp, infoState, bindings);
                        consequence.pushQUD(q, infoState, bindings);
                        return true;
                    }
                };
        add(reaccommodateQuestion);

        /*
         * Accommodate a supplied answer with a question in the plan,
         * by making that question the QUD.
         */
        Condition accommodateQuestionWithPlanC = new Condition();
        accommodateQuestionWithPlanC.extend(new SpeakerIs("usr"));
        accommodateQuestionWithPlanC.extend(new MoveIsNotIntegrated(answer_a));
        accommodateQuestionWithPlanC.extend(new IsRelevantAnswer(q,a));
        accommodateQuestionWithPlanC.extend(new IsNotRelevantCategory(q,"yesno"));
        accommodateQuestionWithPlanC.extend(new WantAnswer(q,qf));
        ExistsRule accommodateQuestionWithPlan =
            new ExistsRule(accommodateQuestionWithPlanC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Accommodate",
                                             "answerToUnaskedQuestion",
                                             "volunteered answer fits plan",
                                             infoState.getUnifier().deref(q, bindings));
                        consequence.pushQUD(q, infoState, bindings);
                        consequence.answerPlanQuestion(q, qf, infoState, bindings);
                        return true;
                    }
                };
        add(accommodateQuestionWithPlan);

        /*
         * Accommodate the QUD with a question in the agenda.
         */
        Condition accommodateQuestionWithAgendaC = new Condition();
        accommodateQuestionWithAgendaC.extend(new SpeakerIs("usr"));
        accommodateQuestionWithAgendaC.extend(new MoveIsNotIntegrated(answer_a));
        accommodateQuestionWithAgendaC.extend(new FirstOnAgenda(findout_q));
        accommodateQuestionWithAgendaC.extend(new IsRelevantAnswer(q,a));
        accommodateQuestionWithAgendaC.extend(new IsNotRelevantCategory(q,"yesno"));
        ExistsRule accommodateQuestionWithAgenda =
            new ExistsRule(accommodateQuestionWithAgendaC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Accommodate",
                                             "answerToAskedQuestion",
                                             "volunteered answer fits agenda",
                                             infoState.getUnifier().deref(q, bindings));
                        consequence.pushQUD(q, infoState, bindings);
                        consequence.popAgenda(infoState, bindings);
                        consequence.answerPlanQuestion(q, infoState, bindings);
                        return true;
                    }
                };
        add(accommodateQuestionWithAgenda);

        /*
         * Accommodate the QUD with a question in the agenda.
         */
        Condition accommodateUnqualifiedQuestionWithAgendaC = new Condition();
        accommodateUnqualifiedQuestionWithAgendaC.extend(new SpeakerIs("usr"));
        accommodateUnqualifiedQuestionWithAgendaC.extend(new MoveIsNotIntegrated(answer_a));
        accommodateUnqualifiedQuestionWithAgendaC.extend(new FirstOnAgenda(findout_q));
        accommodateUnqualifiedQuestionWithAgendaC.extend(new IsUnqualifiedAnswer(a));
        ExistsRule accommodateUnqualifiedQuestionWithAgenda =
            new ExistsRule(accommodateUnqualifiedQuestionWithAgendaC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Accommodate",
                                             "answerToAskedQuestion",
                                             "volunteered but unrecognized answer assumed to fit agenda",
                                             infoState.getUnifier().deref(q, bindings));
                        // make a qualified answer containing a, using q's functor.
                        // mark input move as integrated, appending new unintegrated move
                        Object q2 = Unify.getInstance().deref(q, bindings);
                        if (q2 instanceof Predicate) {
                            ArrayList al = new ArrayList();
                            al.add(Unify.getInstance().deref(a, bindings));
                            Predicate p2 = new Predicate(((Predicate)q2).functor(), al);
                            al = new ArrayList();
                            Predicate p3 = new Predicate("answer", al);
                            consequence.markIntegrated(answer_a, infoState, bindings);
                            consequence.addUnintegratedMove(p3, infoState, bindings);
                        } else {
                            discourseLogger.logp(Level.WARNING,
                                             "org.mitre.dm.qud.rules.Accommodate",
                                             "answerToAskedQuestion",
                                             "findout argument not a predicate!",
                                             infoState.getUnifier().deref(q, bindings));
                        }
                        return true;
                    }
                };
        add(accommodateUnqualifiedQuestionWithAgenda);

        /*
         * Accommodate a NIM answer with a question in the plan.
         * NIMs are moves from previous utterances that were not
         * integrated.
         */
        Condition accommodateQuestionNIMWithPlanC = new Condition();
        accommodateQuestionNIMWithPlanC.extend(new AgedMoveIsNotIntegrated(answer_a));
        accommodateQuestionNIMWithPlanC.extend(new IsRelevantAnswer(q,a));
        accommodateQuestionNIMWithPlanC.extend(new IsNotRelevantCategory(q,"yesno"));
        accommodateQuestionNIMWithPlanC.extend(new WantAnswer(findout_q,qf));
        ExistsRule accommodateQuestionNIMWithPlan =
            new ExistsRule(accommodateQuestionNIMWithPlanC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Accommodate",
                                             "resolvePreviousIrrelevance",
                                             "previously volunteered answer fits plan",
                                             infoState.getUnifier().deref(q, bindings));
                        consequence.pushQUD(q, infoState, bindings);
                        consequence.answerPlanQuestion(q, qf, infoState, bindings);
                        consequence.removeAgedNIM(answer_a, infoState, bindings);
                        consequence.addUnintegratedMove(answer_a, infoState, bindings);
                        return true;
                    }
                };
        add(accommodateQuestionNIMWithPlan);

        /*
         * Accommodate a NIM answer with a question in the agenda.
         * NIMs are moves from previous utterances that were not
         * integrated.
         */
        Condition accommodateQuestionNIMWithAgendaC = new Condition();
        accommodateQuestionNIMWithAgendaC.extend(new AgedMoveIsNotIntegrated(answer_a));
        accommodateQuestionNIMWithAgendaC.extend(new FirstOnAgenda(findout_q));
        accommodateQuestionNIMWithAgendaC.extend(new IsRelevantAnswer(q,a));
        accommodateQuestionNIMWithAgendaC.extend(new IsNotRelevantCategory(q,"yesno"));
        ExistsRule accommodateQuestionNIMWithAgenda =
            new ExistsRule(accommodateQuestionNIMWithAgendaC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Accommodate",
                                             "resolvePreviousIrrelevance",
                                             "previously volunteered answer fits agenda",
                                             infoState.getUnifier().deref(q, bindings));
                        consequence.popAgenda(infoState, bindings);
                        consequence.pushQUD(q, infoState, bindings);
                        consequence.answerPlanQuestion(q, infoState, bindings);
                        consequence.removeAgedNIM(answer_a, infoState, bindings);
                        consequence.addUnintegratedMove(answer_a, infoState, bindings);
                        return true;
                    }
                };
        add(accommodateQuestionNIMWithAgenda);

        // task_task needs rethinking for variables...

        /*
         * Accommodate a task question.
         * Case 1: there is only one relevant task.
         */
        Condition accommodateTaskSingleC = new Condition();
        accommodateTaskSingleC.extend(new SpeakerIs("usr"));
        accommodateTaskSingleC.extend(new MoveIsNotIntegrated(answer_a));
        accommodateTaskSingleC.extend(new IsRelevantToTasks( answer_a, tasks )); // list of task(T)
        accommodateTaskSingleC.extend(new ListLength(tasks,new Integer(1)));
        accommodateTaskSingleC.extend(new First(tasks, task));
        // there is no known task which does not dominate the new task
        // i.e. the new task is dominated by every known task
        Condition dominatedC = new IsCommonGround(oldtask);
        dominatedC.extend(new NotDominatedBy(oldtask, task));
        accommodateTaskSingleC.extend(new Never(dominatedC));
        ExistsRule accommodateTaskSingle =
            new ExistsRule(accommodateTaskSingleC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Accommodate",
                                             "accommodateUnambiguousTask",
                                             "volunteered answer fits new task",
                                             infoState.getUnifier().deref(task, bindings));
                        //consequence.pushAgenda(task, infoState, bindings);
                        consequence.shiftTopic(task, infoState, bindings);
                        //consequence.addCommonGround(task, infoState, bindings);
                        //consequence.instantiatePlan(plan, infoState, bindings);
                        //consequence.clearAgenda(infoState, bindings);
                        return true;
                    }
                };
        add(accommodateTaskSingle);

        /*
         * Accommodate a task question.
         * Case 2: the question is relevant to the default task.
         *         Other relevant tasks are ignored.
         */
        Condition accommodateTaskDefaultC = new Condition();
        accommodateTaskDefaultC.extend(new SpeakerIs("usr"));
        accommodateTaskDefaultC.extend(new MoveIsNotIntegrated(answer_a));
        accommodateTaskDefaultC.extend(new DefaultTask(task));
        accommodateTaskDefaultC.extend(new IsRelevantToTask( answer_a, task, plan ));
        // there is no known task which does not dominate the new task
        // i.e. the new task is dominated by every known task
        Condition dominatedDefC = new IsCommonGround(task_x);
        dominatedDefC.extend(new NotDominatedBy(task_x, task));
        accommodateTaskDefaultC.extend(new Never(dominatedDefC));
        add(new AnswerToDefaultTask(accommodateTaskDefaultC));

    }

    /*
     * Accommodate a task question.
     * Case 2: the question is relevant to the default task.
     *         Other relevant tasks are ignored.
     */
    class AnswerToDefaultTask extends ExistsRule
    {
        public AnswerToDefaultTask(Condition c) {super(c);}
        public boolean execute(InfoState infoState, Bindings bindings) {
            discourseLogger.logp(Level.INFO,
                                 "org.mitre.dm.qud.rules.Accommodate",
                                 "accommodateDefaultTask",
                                 "volunteered answer fits default task",
                                 infoState.getUnifier().deref(task, bindings));
            //consequence.pushAgenda(task_task /* a */, infoState, bindings);
            consequence.shiftTopic(task_task /* a */, infoState, bindings);
            //consequence.addCommonGround(task_x, infoState, bindings);
            //consequence.instantiatePlan(plan, infoState, bindings);
            //consequence.clearAgenda(infoState, bindings);
            return true;
        }
    }
}
