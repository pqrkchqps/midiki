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
 
public class DowndateQUD extends RuleSet
{
    private Logger discourseLogger = null;
    Result consequence;

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
    private Variable alts;

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
    private Predicate answer_q;
    private Predicate answer_r;
    private Predicate answer_q_r;
    private Predicate ask_q;
    private Predicate findout_q;
    private Predicate raise_q;
    private Predicate task_findout_q;
    private Predicate task_anon;
    private Predicate task_x;
    private Predicate ask_task_x;
    private Predicate alts_x;
    private Predicate alts_q1_x;
    private Predicate ask_alts;

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

    public DowndateQUD(Logger theLogger)
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
        alts = new Variable("Alts");

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
        args.add(q);
        answer_q = new Predicate("answer",args);

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
        args.add(alts);
        ask_alts = new Predicate("ask",args);

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
         * Downdate the QUD if it was answered.
         */
        Condition downdateQUDifAnsweredC = new Condition();
        downdateQUDifAnsweredC.extend(new FirstOnAgenda(findout_q));
        downdateQUDifAnsweredC.extend(new FirstQuestionUnderDiscussion(q));
        downdateQUDifAnsweredC.extend(new IsRelevantAnswer(q,p));
        downdateQUDifAnsweredC.extend(new IsCommonGround(p));
        ExistsRule downdateQUDifAnswered =
            new ExistsRule(downdateQUDifAnsweredC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.popQUD(infoState, bindings);
                        return true;
                    }
                };
        add(downdateQUDifAnswered);

        /*
         * Downdate the QUD if it was not answered in the current move,
         * unless the latest_move was no_move, an answer, or an
         * alternatives question.
         */
        Condition downdateQUDifNotAnsweredC = new Condition();
        downdateQUDifNotAnsweredC.extend(new FirstQuestionUnderDiscussion(q));
        downdateQUDifNotAnsweredC.extend(new IsNotCurrentMove(answer_q));
        downdateQUDifNotAnsweredC.extend(new IsNotCurrentMove("no_move"));
        downdateQUDifNotAnsweredC.extend(new IsNotCurrentMove(ask_alts));
        downdateQUDifNotAnsweredC.extend(new LatestMoveDoesNotMatch("failed"));
        ExistsRule downdateQUDifNotAnswered =
            new ExistsRule(downdateQUDifNotAnsweredC){
                    public boolean execute(InfoState infoState, Bindings bindings) {
                        consequence.popQUD(infoState, bindings);
                        return true;
                    }
                };
        add(downdateQUDifNotAnswered);

    }
}
