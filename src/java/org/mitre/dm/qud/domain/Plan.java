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
package org.mitre.dm.qud.domain;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;

import java.util.*;

/**
 * Provides a representation of a plan as a collection of operations
 * related to the specified contract. Includes convenience routines
 * for constructing steps in the plan.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class Plan
{
    protected Contract task;
    protected List ops;

    public Plan(Contract t)
    {
        task = t;
        ops = new LinkedList();
    }

    public Contract getTask()
    {
        return task;
    }

    public Collection plan()
    {
        return ops;
    }

    protected int indexOf(Contract.Attribute item)
    {
        int index = 0;
        Iterator it = task.attributes();
        while (it.hasNext()) {
            Contract.Attribute attr = (Contract.Attribute)it.next();
            if (attr == item) return index;
            index++;
        }
        return -1;
    }

    protected int indexOf(String name)
    {
        int index = 0;
        Iterator it = task.attributes();
        while (it.hasNext()) {
            Contract.Attribute attr = (Contract.Attribute)it.next();
            if (attr.name().equals(name)) return index;
            index++;
        }
        return -1;
    }

    /**
     * Obtain an appropriately named <code>variable</code> for referring to
     * the named attribute in the context of this plan.
     *
     * @param name a <code>String</code> value
     * @return a <code>Variable</code> value
     */
    public Variable getVariableFor(String name)
    {
        return new Variable("X"+indexOf(name));
    }

    /**
     * Add the specified annotation to the most recently added plan step.
     * Throws an <code>IllegalStateException</code> if the plan is empty.
     *
     * @param ann an <code>Object</code> value
     */
    public void addAnnotation(Object ann)
    {
        if (ops == null) throw new IllegalStateException();
        if (ops.isEmpty()) throw new IllegalStateException();
        PlanOperation op = (PlanOperation)ops.get(ops.size()-1);
        op.setAnnotation(ann);
    }

    /*
     * FINDOUT moves: ask the user to provide a value for this item.
     */
    public void addFindout(Contract.Attribute item)
    {
        ArrayList vect = new ArrayList();
        vect.add(new Variable("X"+indexOf(item)));
        Predicate move = new Predicate(item.name(), vect);
        ArrayList val = new ArrayList();
        val.add(move);
        ops.add(new PlanOperation(new Predicate("findout", val)));
    }
    public void addFindout(String itemName)
    {
        ArrayList vect = new ArrayList();
        vect.add(new Variable("X"+indexOf(itemName)));
        Predicate move = new Predicate(itemName, vect);
        ArrayList val = new ArrayList();
        val.add(move);
        ops.add(new PlanOperation(new Predicate("findout", val)));
    }
    public void addFindoutTask(Contract task)
    {
        ArrayList vect = new ArrayList();
        vect.add(task.name());
        Predicate move = new Predicate("task", vect);
        ArrayList val = new ArrayList();
        val.add(move);
        ops.add(new PlanOperation(new Predicate("findout", val)));
    }
    public void addFindoutWhichTask(Collection tasks)
    {
        LinkedList movelist = new LinkedList();
        Iterator it = tasks.iterator();
        while (it.hasNext()) {
            Contract t = (Contract)it.next();
            ArrayList vect = new ArrayList();
            vect.add(t.name());
            Predicate move = new Predicate("task", vect);
            movelist.add(move);
        }
        ArrayList val = new ArrayList();
        val.add(movelist);
        ops.add(new PlanOperation(new Predicate("findout", val)));
    }
    /*
     * RAISE moves: expect the user to provide a value for this item,
     * but don't explicitly ask for it at this time.
     */
    public void addRaise(Contract.Attribute item)
    {
        ArrayList vect = new ArrayList();
        vect.add(new Variable("X"+indexOf(item)));
        Predicate move = new Predicate(item.name(), vect);
        ArrayList val = new ArrayList();
        val.add(move);
        ops.add(new PlanOperation(new Predicate("raise", val)));
    }
    public void addRaise(String itemName)
    {
        ArrayList vect = new ArrayList();
        vect.add(new Variable("X"+indexOf(itemName)));
        Predicate move = new Predicate(itemName, vect);
        ArrayList val = new ArrayList();
        val.add(move);
        ops.add(new PlanOperation(new Predicate("raise", val)));
    }
    public void addRaiseTask(Contract task)
    {
        ArrayList vect = new ArrayList();
        vect.add(task.name());
        Predicate move = new Predicate("task", vect);
        ArrayList val = new ArrayList();
        val.add(move);
        ops.add(new PlanOperation(new Predicate("raise", val)));
    }
    public void addRaiseWhichTask(Collection tasks)
    {
        LinkedList movelist = new LinkedList();
        Iterator it = tasks.iterator();
        while (it.hasNext()) {
            Contract t = (Contract)it.next();
            ArrayList vect = new ArrayList();
            vect.add(t.name());
            Predicate move = new Predicate("task", vect);
            movelist.add(move);
        }
        ArrayList val = new ArrayList();
        val.add(movelist);
        ops.add(new PlanOperation(new Predicate("raise", val)));
    }
    /*
     * REQUIRE moves: requires the value of an item to be specified value.
     * (specified slot must bind with at least one accessible value.)
     */
    public void addRequire(Contract.Attribute item, Object value)
    {
        ArrayList vect2 = new ArrayList();
        vect2.add(new Variable("X"+indexOf(item)));
        Predicate move2 = new Predicate(item.name(), vect2);
        ArrayList val = new ArrayList();
        val.add(move2);
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(item.name(), vect);
        val.add(move);
        ops.add(new PlanOperation(new Predicate("require", val)));
    }
    public void addRequire(String itemName, Object value)
    {
        ArrayList vect2 = new ArrayList();
        vect2.add(new Variable("X"+indexOf(itemName)));
        Predicate move2 = new Predicate(itemName, vect2);
        ArrayList val = new ArrayList();
        val.add(move2);
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(itemName, vect);
        val.add(move);
        ops.add(new PlanOperation(new Predicate("require", val)));
    }
    /*
     * BIND moves: sets the value of an item to specified value.
     * (specified slot will bind with at least the specified value.
     * multiple binds for a slot are permissible; this is not the
     * perfect way to link parameters across boundaries.)
     */
    public void addBind(Contract.Attribute item, Object value)
    {
        ArrayList vect2 = new ArrayList();
        vect2.add(new Variable("X"+indexOf(item)));
        Predicate move2 = new Predicate(item.name(), vect2);
        ArrayList val = new ArrayList();
        val.add(move2);
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(item.name(), vect);
        val.add(move);
        ops.add(new PlanOperation(new Predicate("bind", val)));
    }
    public void addBind(String itemName, Object value)
    {
        ArrayList vect2 = new ArrayList();
        vect2.add(new Variable("X"+indexOf(itemName)));
        Predicate move2 = new Predicate(itemName, vect2);
        ArrayList val = new ArrayList();
        val.add(move2);
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(itemName, vect);
        val.add(move);
        ops.add(new PlanOperation(new Predicate("bind", val)));
    }
    /*
     * FORGET moves: remove a specific value from common ground.
     */
    public void addForget(Contract.Attribute item)
    {
        ArrayList vect = new ArrayList();
        vect.add(new Variable("X"+indexOf(item)));
        Predicate move = new Predicate(item.name(), vect);
        ArrayList val = new ArrayList();
        val.add(move);
        ops.add(new PlanOperation(new Predicate("forget", val)));
    }
    public void addForget(String itemName)
    {
        ArrayList vect = new ArrayList();
        vect.add(new Variable("X"+indexOf(itemName)));
        Predicate move = new Predicate(itemName, vect);
        ArrayList val = new ArrayList();
        val.add(move);
        ops.add(new PlanOperation(new Predicate("forget", val)));
    }
    public void addForget(Object msgTag)
    {
        ArrayList val = new ArrayList();
        val.add(msgTag);
        ops.add(new PlanOperation(new Predicate("forget", val)));
    }
    public void addForget()
    {
        ops.add(new PlanOperation("forget")); // forget everything
    }
    /*
     * INFORM moves: tell the user about the value of an item,
     * or present some boilerplate text.
     */
    public void addInform(Contract.Attribute item)
    {
        ArrayList vect = new ArrayList();
        vect.add(new Variable("X"+indexOf(item)));
        Predicate move = new Predicate(item.name(), vect);
        ArrayList val = new ArrayList();
        val.add(move);
        ops.add(new PlanOperation(new Predicate("inform", val)));
    }
    public void addInform(String itemName)
    {
        ArrayList vect = new ArrayList();
        vect.add(new Variable("X"+indexOf(itemName)));
        Predicate move = new Predicate(itemName, vect);
        ArrayList val = new ArrayList();
        val.add(move);
        ops.add(new PlanOperation(new Predicate("inform", val)));
    }
    public void addInform(Object msgTag)
    {
        ArrayList val = new ArrayList();
        val.add(msgTag);
        ops.add(new PlanOperation(new Predicate("inform", val)));
    }
    public void addInformPredicate(String topicName, String topicTag)
    {
        ArrayList val = new ArrayList();
        val.add(new Variable("X"+indexOf(topicTag)));
        Predicate topic = new Predicate(topicName, val);
        val = new ArrayList();
        val.add(topic);
        ops.add(new PlanOperation(new Predicate("inform", val)));
    }
    /*
     * ASSERT moves: sets the value of an item to some value.
     * Does this by binding the appropriate variable
     */
    public void addAssert(Contract.Attribute item, Object value)
    {
        ArrayList vect2 = new ArrayList();
        vect2.add(new Variable("X"+indexOf(item)));
        Predicate move2 = new Predicate(item.name(), vect2);
        ArrayList val = new ArrayList();
        val.add(move2);
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(item.name(), vect);
        val.add(move);
        ops.add(new PlanOperation(new Predicate("assert", val)));
    }
    public void addAssert(String itemName, Object value)
    {
        ArrayList vect2 = new ArrayList();
        vect2.add(new Variable("X"+indexOf(itemName)));
        Predicate move2 = new Predicate(itemName, vect2);
        ArrayList val = new ArrayList();
        val.add(move2);
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(itemName, vect);
        val.add(move);
        ops.add(new PlanOperation(new Predicate("assert", val)));
    }
    /*
     * Method and query calls. Inserts a call to one of the
     * queries or methods defined in the task contract.
     * (The calls are mapped to cell calls at a later time.)
     * By default, parameters and task attributes are matched
     * by name, with an error if there is a mismatch.
     * The default can be overridden by specifying an array
     * of parameter specifications.
     *
     * At the moment, there is no real difference between a
     * query and a method in a task spec. That should change
     * as the linkages between tasks grows closer to BPEL etc.
     */
    public void addQueryCall(Contract.Query query)
    {
        ArrayList val = new ArrayList();
        val.add(query);
        ops.add(new PlanOperation(new Predicate("query", val)));
    }
    public void addQueryCall(String contractName, String queryName)
    {
        ArrayList val = new ArrayList();
        val.add(contractName);
        val.add(queryName);
        ops.add(new PlanOperation(new Predicate("query", val)));
    }
    public void addQueryCall(Contract.Query query, Object[] parameters)
    {
        ArrayList val = new ArrayList();
        val.add(query);
        val.add(parameters);
        ops.add(new PlanOperation(new Predicate("query", val)));
    }
    public void addQueryCall(String contractName, String queryName, Object[] parameters)
    {
        ArrayList val = new ArrayList();
        val.add(contractName);
        val.add(queryName);
        val.add(parameters);
        ops.add(new PlanOperation(new Predicate("query", val)));
    }
    public void addMethodCall(Contract.Method method)
    {
        ArrayList val = new ArrayList();
        val.add(method);
        ops.add(new PlanOperation(new Predicate("method", val)));
    }
    public void addMethodCall(String contractName, String methodName)
    {
        ArrayList val = new ArrayList();
        val.add(contractName);
        val.add(methodName);
        ops.add(new PlanOperation(new Predicate("method", val)));
    }
    public void addMethodCall(Contract.Method method, Object[] parameters)
    {
        ArrayList val = new ArrayList();
        val.add(method);
        val.add(parameters);
        ops.add(new PlanOperation(new Predicate("method", val)));
    }
    public void addMethodCall(String contractName, String methodName, Object[] parameters)
    {
        ArrayList val = new ArrayList();
        val.add(contractName);
        val.add(methodName);
        val.add(parameters);
        ops.add(new PlanOperation(new Predicate("method", val)));
    }
    /*
     * Miscellaneous moves: either defined within DM or not.
     * User specifies if they think it's a builtin, get corrected
     * if they are wrong. (All moves in a plan must be handled
     * by the dialogue move engine, otherwise processing will be
     * permanently hung up by a move that can't be cleared.
     * Specifying a move that is not builtin will add it to the
     * 'sysaction' list.)
     * (For the moment, 'builtin' is ignored.)
     */
    public void addMove(Object moveTag, boolean builtin)
    {
        ops.add(new PlanOperation(moveTag));
    }
    /*
     * Control flow
     */
    public void addIfThen(Contract.Attribute test,
                          Object value,
                          Plan thenPart)
    {
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(test.name(), vect);
        ArrayList val = new ArrayList();
        val.add(move);
        val.add(thenPart.plan());
        ops.add(new PlanOperation(new Predicate("if_then", val)));
    }
    public void addIfThen(String testName,
                          Object value,
                          Plan thenPart)
    {
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(testName, vect);
        ArrayList val = new ArrayList();
        val.add(move);
        val.add(thenPart.plan());
        ops.add(new PlanOperation(new Predicate("if_then", val)));
    }
    public void addIfThenElse(Contract.Attribute test,
                              Object value,
                              Plan thenPart,
                              Plan elsePart)
    {
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(test.name(), vect);
        ArrayList val = new ArrayList();
        val.add(move);
        val.add(thenPart.plan());
        val.add(elsePart.plan());
        ops.add(new PlanOperation(new Predicate("if_then_else", val)));
    }
    public void addIfThenElse(String testName,
                              Object value,
                              Plan thenPart,
                              Plan elsePart)
    {
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(testName, vect);
        ArrayList val = new ArrayList();
        val.add(move);
        val.add(thenPart.plan());
        val.add(elsePart.plan());
        ops.add(new PlanOperation(new Predicate("if_then_else", val)));
    }
    public void addDoWhile(Contract.Attribute test,
                           Object value,
                           Plan thenPart)
    {
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(test.name(), vect);
        ArrayList val = new ArrayList();
        val.add(move);
        val.add(thenPart.plan());
        ops.add(new PlanOperation(new Predicate("do_while", val)));
    }
    public void addDoWhile(String testName,
                           Object value,
                           Plan thenPart)
    {
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(testName, vect);
        ArrayList val = new ArrayList();
        val.add(move);
        val.add(thenPart.plan());
        ops.add(new PlanOperation(new Predicate("do_while", val)));
    }
    public void addDoUntil(Contract.Attribute test,
                           Object value,
                           Plan thenPart)
    {
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(test.name(), vect);
        ArrayList val = new ArrayList();
        val.add(move);
        val.add(thenPart.plan());
        ops.add(new PlanOperation(new Predicate("do_until", val)));
    }
    public void addDoUntil(String testName,
                           Object value,
                           Plan thenPart)
    {
        ArrayList vect = new ArrayList();
        vect.add(value);
        Predicate move = new Predicate(testName, vect);
        ArrayList val = new ArrayList();
        val.add(move);
        val.add(thenPart.plan());
        ops.add(new PlanOperation(new Predicate("do_until", val)));
    }
    public void addExec(Contract task)
    {
        ArrayList val = new ArrayList();
        val.add(task.name());
        ops.add(new PlanOperation(new Predicate("exec", val)));
    }

    public void addExec(String taskName)
    {
        ArrayList val = new ArrayList();
        val.add(taskName);
        ops.add(new PlanOperation(new Predicate("exec", val)));
    }

    public void addCall(Contract task)
    {
        ArrayList val = new ArrayList();
        val.add(task.name());
        ops.add(new PlanOperation(new Predicate("call", val)));
    }

    public void addCall(String taskName)
    {
        ArrayList val = new ArrayList();
        val.add(taskName);
        ops.add(new PlanOperation(new Predicate("call", val)));
    }
}
