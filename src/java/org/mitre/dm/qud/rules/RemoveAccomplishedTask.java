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
 * If goalAchieved and both plan and agenda are empty,
 * then delete task from is.shared.com
 * and push findout(task) onto agenda
 * 
 */
 
public class RemoveAccomplishedTask extends ExistsRule
{
    private Logger discourseLogger = null;
    Result consequence;
    public void setLogger(Logger theLogger)
    {
        discourseLogger = theLogger;
        consequence.setDiscourseLogger(discourseLogger);
    }
    static public RemoveAccomplishedTask newInstance()
    {
        Variable step = new Variable("Step");
        Variable task = new Variable("Task");
        LinkedList args = new LinkedList();
        args.add(task);
        Predicate task_task = new Predicate("task", args);
        Condition removeAccomplishedTaskC = new SpeakerIs("sys");
        removeAccomplishedTaskC.extend(new IsCommonGround(task_task));
        removeAccomplishedTaskC.extend(new GoalAchieved(task));
        removeAccomplishedTaskC.extend(new NoNextStep(step));
        removeAccomplishedTaskC.extend(new AgendaIsEmpty());
        return new RemoveAccomplishedTask(removeAccomplishedTaskC);
    }

    public RemoveAccomplishedTask()
    {
        super();
        consequence = new Result();
    }

    public RemoveAccomplishedTask(Condition c)
    {
        super(c);
        consequence = new Result();
    }

    public boolean execute(InfoState infoState, Bindings bindings) {
        Variable x = new Variable("X");
        LinkedList args = new LinkedList();
        args.add(x);
        Predicate task_x = new Predicate("task", args);
        args = new LinkedList();
        args.add(task_x);
        Predicate findout_task_x = new Predicate("findout", args);
        consequence.pushAgenda(findout_task_x, infoState, bindings);
        return true;
    }
}

