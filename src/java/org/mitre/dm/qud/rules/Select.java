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
 * Implements the action-selection component of the DME.
 * Action-selection is implemented as a rule set applied
 * to the information state after update.
 *
 * This class has been translated into the Midiki Java API
 * from the 'select.mks' script. The translation was done
 * manually. 
 * 
 */
 
public class Select extends RuleSet
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.rules.Select");
    private Logger discourseLogger = null;
    /*
     * The conditions in Select refer to a small set of variables.
     * There's no need to define those more than once.
     */
    private Variable anonymous;
    private Variable m;
    private Variable p;
    private Variable q;
    private Variable r;
    private Variable x;
    private Variable move;
    private Variable cause;

    /*
     * Predicates, like Variables, can be defined in advance.
     * This can make the actual rule definition easier to read.
     */
    private Predicate reqRep_und;
    private Predicate reqRep_rel;
    private Predicate ack_p;
    private Predicate repeat_m;
    private Predicate respond_q;
    private Predicate alts_q;
    private Predicate alts_q_r;
    private Predicate respond_alts_q_r;
    private Predicate answer_q_r;
    private Predicate answer_alts_q_r;
    private Predicate ask_q;
    private Predicate findout_q;
    private Predicate raise_q;
    private Predicate task_findout_q;
    private Predicate task_anon;
    private Predicate task_x;
    private Predicate ask_task_x;
    private Predicate quit_cause;
    private Predicate inform_hello;

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

    public Select(Logger theLogger)
    {
        super();
        discourseLogger = theLogger;

        anonymous = new Variable();
        m = new Variable("M");
        p = new Variable("P");
        q = new Variable("Q");
        r = new Variable("R");
        x = new Variable("X");
        move = new Variable("Move");
        cause = new Variable("Cause");

        LinkedList args;

        args = new LinkedList();
        args.add("understanding");
        Predicate reqRep_und = new Predicate("reqRep",args);

        args = new LinkedList();
        args.add("relevance");
        Predicate reqRep_rel = new Predicate("reqRep",args);

        args = new LinkedList();
        args.add(p);
        ack_p = new Predicate("ack",args);

        args = new LinkedList();
        args.add(m);
        repeat_m = new Predicate("repeat",args);

        args = new LinkedList();
        args.add(q);
        respond_q = new Predicate("respond",args);

        args = new LinkedList();
        args.add(q);
        args.add(r);
        alts_q_r = new Predicate("alts",args);
        args = new LinkedList();
        args.add(alts_q_r);
        respond_alts_q_r = new Predicate("respond",args);

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
        args.add(cause);
        quit_cause = new Predicate("quit",args);

        args = new LinkedList();
        args.add("hello");
        inform_hello = new Predicate("inform",args);

        buildRuleSet();
    }

    /**
     * Populates the rule set. Every rule in 'select.mks' becomes
     * a subclass of ExistsRule, every submodule (there is only one
     * in select) becomes a RuleSet.
     *
     */
    protected void buildRuleSet()
    {
        /*
         * System could not interpret user's latest move
         */
        Condition selectReqRepUnderstandingC = new Condition();
        selectReqRepUnderstandingC.extend(new InputNotEmpty());
        selectReqRepUnderstandingC.extend(new LatestMoveFailed());
        ExistsRule selectReqRepUnderstanding =
            new ExistsRule(selectReqRepUnderstandingC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Select",
                                             "selectReqRepUnderstanding",
                                             "system utterance type",
                                             "signal misunderstanding");
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(reqRep_und);
                        infoState.cell("is").put("next_moves", next_moves);
                        return true;
                    }
                };
        add(selectReqRepUnderstanding);

        /*
         * System could not integrate user's latest move
         *
         * Case 1: there is a task clarification question on the agenda
         */
        Condition taskClarificationQuestionC = new Condition();
        taskClarificationQuestionC.extend(new InputNotEmpty());
        taskClarificationQuestionC.extend(new MoveIsNotIntegrated(move));
        taskClarificationQuestionC.extend(new LatestMoveDoesNotMatch(reqRep_rel));
        taskClarificationQuestionC.extend(new FirstOnAgenda(findout_q));
        taskClarificationQuestionC.extend(new Unifies(q, task_anon));
        ExistsRule taskClarificationQuestion =
            new ExistsRule(taskClarificationQuestionC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Select",
                                             "taskClarificationQuestion",
                                             "system utterance type",
                                             "question");
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(reqRep_rel);
                        next_moves.add(infoState.getUnifier().deref(ask_q,bindings));
                        infoState.cell("is").put("next_moves", next_moves);
                        return true;
                    }
                };
        
        /*
         * System could not integrate user's latest move
         *
         * Case 2: there is no task clarification question on the agenda
         */
        Condition noTaskClarificationQuestionC = new Condition();
        noTaskClarificationQuestionC.extend(new InputNotEmpty());
        noTaskClarificationQuestionC.extend(new MoveIsNotIntegrated(move));
        noTaskClarificationQuestionC.extend(new LatestMoveDoesNotMatch(reqRep_rel));
        ExistsRule noTaskClarificationQuestion =
            new ExistsRule(taskClarificationQuestionC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Select",
                                             "noTaskClarificationQuestion",
                                             "system utterance type",
                                             "signal misunderstanding");
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(reqRep_rel);
                        infoState.cell("is").put("next_moves", next_moves);
                        return true;
                    }
                };

        /*
         * System could not integrate latest user move.
         *
         * Assemble the rule set
         */
        RuleSet selectReqRepRelevance = new RuleSet();
        selectReqRepRelevance.add(taskClarificationQuestion);
        selectReqRepRelevance.add(noTaskClarificationQuestion);
        add(selectReqRepRelevance);

        /*
         * Select an acknowledgement
         */
        Condition selectAckC = new Condition();
        selectAckC.extend(new FirstOnAgenda(ack_p));
        selectAckC.extend(new LatestMoveDoesNotMatch(ack_p));
        ExistsRule selectAck =
            new ExistsRule(selectAckC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Select",
                                             "selectAck",
                                             "system utterance type",
                                             "action acknowledgement");
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(infoState.getUnifier().deref(ack_p,bindings));
                        infoState.cell("is").put("next_moves", next_moves);
                        return true;
                    }
                };
        add(selectAck);

        /*
         * Answer a question
         */
        Condition selectAnswerC = new Condition();
        selectAnswerC.extend(new FirstOnAgenda(respond_q));
        selectAnswerC.extend(new IsABelief(r));
        selectAnswerC.extend(new IsRelevantAnswer(q,r));
        selectAnswerC.extend(new LatestMoveDoesNotMatch(answer_q_r));
        ExistsRule selectAnswer =
            new ExistsRule(selectAnswerC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Select",
                                             "selectAnswer",
                                             "system utterance type",
                                             "answer");
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(infoState.getUnifier().deref(answer_q_r,bindings));
                        infoState.cell("is").put("next_moves", next_moves);
                        return true;
                    }
                };
        add(selectAnswer);

        /*
         * Answer an available alternatives question
         */
        Condition selectAvailableAnswersC = new Condition();
        selectAvailableAnswersC.extend(new FirstOnAgenda(respond_alts_q_r));
        selectAvailableAnswersC.extend(new LatestMoveDoesNotMatch(answer_alts_q_r));
        ExistsRule selectAvailableAnswers =
            new ExistsRule(selectAvailableAnswersC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Select",
                                             "selectAvailableAnswers",
                                             "system utterance type",
                                             "capabilities statement");
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(infoState.getUnifier().deref(answer_alts_q_r,bindings));
                        infoState.cell("is").put("next_moves", next_moves);
                        return true;
                    }
                };
        add(selectAvailableAnswers);

        /*
         * Ask questions triggered by findout or raise, but not if we've
         * already selected that move and not if we are repeating previous
         * moves. (Repetition avoidance needs to be handled in the generation
         * phase. As far as the DME is concerned, it is reasking the question;
         * if the 'ask' is not generated, the answer will not be integrated.
         * Non-yes/no answers might be accommodated, haven't tested that.)
         */
        Condition selectAskFindoutC = new Condition();
        selectAskFindoutC.extend(new FirstOnAgenda(findout_q));
        selectAskFindoutC.extend(new LatestMoveDoesNotMatch(ask_q));
        //selectAskFindoutC.extend(new NotInCurrentTurn(repeat_m));
        ExistsRule selectAskFindout =
            new ExistsRule(selectAskFindoutC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Select",
                                             "selectAskFindout",
                                             "system utterance type",
                                             "question");
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(infoState.getUnifier().deref(ask_q,bindings));
                        infoState.cell("is").put("next_moves", next_moves);
                        return true;
                    }
                };
        add(selectAskFindout);

        Condition selectAskRaiseC = new Condition();
        selectAskRaiseC.extend(new FirstOnAgenda(raise_q));
        selectAskRaiseC.extend(new NotFirstQuestionUnderDiscussion(q));
        selectAskRaiseC.extend(new NotAlreadyAnswered(q));
        selectAskRaiseC.extend(new LatestMoveDoesNotMatch(ask_q));
        //selectAskRaiseC.extend(new NotInCurrentTurn(repeat_m));
        ExistsRule selectAskRaise =
            new ExistsRule(selectAskRaiseC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Select",
                                             "selectAskRaise",
                                             "system utterance type",
                                             "question");
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(infoState.getUnifier().deref(ask_q,bindings));
                        infoState.cell("is").put("next_moves", next_moves);
                        return true;
                    }
                };
        add(selectAskRaise);

        /*
         * Match a question asked in response to a findout or raise.
         * Returns success, but makes no changes to the information state.
         * We are surrendering our turn.
         */
        Condition selectAskPendingFindoutC = new Condition();
        selectAskPendingFindoutC.extend(new FirstOnAgenda(findout_q));
        selectAskPendingFindoutC.extend(new LatestMoveMatches(ask_q));
        ExistsRule selectAskPendingFindout =
            new ExistsRule(selectAskPendingFindoutC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        return true;
                    }
                };
        add(selectAskPendingFindout);

        Condition selectAskPendingRaiseC = new Condition();
        selectAskPendingRaiseC.extend(new FirstOnAgenda(raise_q));
        selectAskPendingRaiseC.extend(new LatestMoveMatches(ask_q));
        ExistsRule selectAskPendingRaise =
            new ExistsRule(selectAskPendingRaiseC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        return true;
                    }
                };
        add(selectAskPendingRaise);


        /*
         * Select a greeting; distinguished for logging purposes only
         */
        Condition selectGreetC = new Condition();
        selectGreetC.extend(new FirstOnAgenda("greet"));
        selectGreetC.extend(new LatestMoveDoesNotMatch("greet"));
        ExistsRule selectGreet =
            new ExistsRule(selectGreetC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Select",
                                             "selectGreet",
                                             "system utterance type",
                                             "greeting");
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(infoState.getUnifier().deref("greet",bindings));
                        infoState.cell("is").put("next_moves", next_moves);
                        System.out.println("select greeting moves "+next_moves);
                        return true;
                    }
                };
        add(selectGreet);

        /*
         * Select a greeting; distinguished for logging purposes only
         * Handles the case where user move waas a greet and system is responding.
         * A kludge; true fix requires rethinking entire flow of DM.
         */
        Condition selectGreetUsrC = new Condition();
        selectGreetUsrC.extend(new FirstOnAgenda("greet"));
        selectGreetUsrC.extend(new SpeakerIs("usr"));
        ExistsRule selectGreetUsr =
            new ExistsRule(selectGreetUsrC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Select",
                                             "selectGreetUsr",
                                             "system utterance type",
                                             "greeting");
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(infoState.getUnifier().deref("greet",bindings));
                        infoState.cell("is").put("next_moves", next_moves);
                        System.out.println("select greeting moves "+next_moves);
                        return true;
                    }
                };
        add(selectGreetUsr);

        /*
         * Select any pending move not otherwise handled
         */
        Condition selectOtherC = new Condition();
        selectOtherC.extend(new FirstOnAgenda(m));
        selectOtherC.extend(new LatestMoveDoesNotMatch(m));
        ExistsRule selectOther =
            new ExistsRule(selectOtherC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(infoState.getUnifier().deref(m,bindings));
                        infoState.cell("is").put("next_moves", next_moves);
                        System.out.println("select other moves "+next_moves);
                        return true;
                    }
                };
        add(selectOther);

        /*
         * Ask for task clarification if agenda is empty
         */
        Condition selectAskForTaskC = new Condition();
        selectAskForTaskC.extend(new AgendaIsEmpty());
        selectAskForTaskC.extend(new LatestMoveDoesNotMatch(ask_task_x));
        ExistsRule selectAskForTask =
            new ExistsRule(selectAskForTaskC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        discourseLogger.logp(Level.INFO,
                                             "org.mitre.dm.qud.rules.Select",
                                             "selectAskForTask",
                                             "system utterance type",
                                             "question");
                        LinkedList next_moves = new LinkedList();
                        next_moves.add(infoState.getUnifier().deref(ask_task_x,bindings));
                        infoState.cell("is").put("next_moves", next_moves);
                        return true;
                    }
                };
        add(selectAskForTask);

        /*
         * Log an indication of quiescence
         */
        Condition selectQuiescenceC = new Condition();
        ExistsRule selectQuiescence =
            new ExistsRule(selectQuiescenceC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
System.out.println("quiescence");
                        return false;
                    }
                };
        add(selectQuiescence);
    }

}

