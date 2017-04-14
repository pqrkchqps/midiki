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

import java.util.*;
import java.util.logging.*;

import org.mitre.midiki.impl.mitre.AttributeImpl;
import org.mitre.midiki.impl.mitre.ContractImpl;
import org.mitre.midiki.logic.*;
import org.mitre.midiki.state.Contract;

/**
 * Provides an instance of a specific plan viewed through an
 * iterator-like interface. Includes local copies of slot values.
 * Uses the model that a Plan is a sequence of operations for
 * performing a task on a Contract, and the Instance maintains
 * dynamic state for the task.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class PlanInstance
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.domain.PlanInstance");
    protected String theKey;
    protected Object[] machine;
    protected int pc;
    protected LinkedList answers;
    protected LinkedList annotations;  // accumulated on each advance()
    protected Plan thePlan;
    protected Bindings planBindings;

    /**
     * Intended for boolean queries; not entirely satisfactory in general.
     *
     * @param query an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    protected boolean answerKnown(Object query)
    {
        Iterator it = answers.iterator();
        while (it.hasNext()) {
            Object ans = it.next();
            logger.logp(Level.FINER,"org.mitre.dm.qud.domain.PlanInstance","answerKnown","query "+query);
            logger.logp(Level.FINER,"org.mitre.dm.qud.domain.PlanInstance","answerKnown","ans "+ans);
            if (Unify.getInstance().unify(ans,query) != null) {
                // all we care about is the existence of the answer
                logger.logp(Level.FINER,"org.mitre.dm.qud.domain.PlanInstance","answerKnown","answer is known");
                return true;
            }
        }
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.PlanInstance","answerKnown","answer is not known");
        return false;
    }

    protected int unpackPlan(List plan, Vector mach, int pc)
    {
        Iterator it = plan.iterator();
        while (it.hasNext()) {
            PlanOperation planOp = (PlanOperation)it.next();
            Object inst = planOp.getOperation();
            if (inst instanceof Predicate) {
                Predicate struct = (Predicate)inst;
                if (struct.functor().equals("do_while")) {
                    // model do_while with an if at the start, goto at the end.
                    Iterator ch = struct.arguments();
                    // first child expected to be condition
                    Object cond = ch.next();
                    // second child expected to be 'true' case
                    Object iftrue = ch.next();
                    int trueAddress = pc+1;
                    int falseAddress = pc+2;
                    if (iftrue instanceof List) {
                        Vector truevect = new Vector();
                        falseAddress = unpackPlan((List)iftrue,truevect,trueAddress)+1;
                        ArrayList args = new ArrayList();
                        args.add(cond);
                        args.add(new Integer(trueAddress));
                        args.add(new Integer(falseAddress));
                        mach.add(new PlanOperation(new Predicate("if_then", args), planOp.getAnnotation()));
                        mach.addAll(truevect);
                        ArrayList args2 = new ArrayList();
                        args2.add(new Integer(pc));
                        mach.add(new PlanOperation(new Predicate("goto", args2)));
                    } else {
                        ArrayList args = new ArrayList();
                        args.add(cond);
                        args.add(new Integer(trueAddress));
                        args.add(new Integer(falseAddress));
                        mach.add(new PlanOperation(new Predicate("if_then", args), planOp.getAnnotation()));
                        mach.add(new PlanOperation(iftrue));
                        ArrayList args2 = new ArrayList();
                        args2.add(new Integer(pc));
                        mach.add(new PlanOperation(new Predicate("goto", args2)));
                    }
                    pc = falseAddress;
                } else if (struct.functor().equals("do_until")) {
                    // model do_until with an if at the end.
                    Iterator ch = struct.arguments();
                    // first child expected to be condition
                    Object cond = ch.next();
                    // second child expected to be 'true' case
                    Object iftrue = ch.next();
                    int trueAddress = pc;
                    int falseAddress = pc+2;
                    if (iftrue instanceof List) {
                        Vector truevect = new Vector();
                        falseAddress = unpackPlan((List)iftrue,truevect,trueAddress)+1;
                        mach.addAll(truevect);
                        ArrayList args = new ArrayList();
                        args.add(cond);
                        args.add(new Integer(falseAddress));
                        args.add(new Integer(trueAddress));
                        mach.add(new PlanOperation(new Predicate("if_then", args), planOp.getAnnotation()));
                    } else {
                        mach.add(new PlanOperation(iftrue, planOp.getAnnotation()));
                        ArrayList args = new ArrayList();
                        args.add(cond);
                        args.add(new Integer(falseAddress));
                        args.add(new Integer(trueAddress));
                        mach.add(new PlanOperation(new Predicate("if_then", args), planOp.getAnnotation()));
                    }
                    pc = falseAddress;
                } else if (struct.functor().equals("if_then")) {
                    Iterator ch = struct.arguments();
                    //if (struct.getNumChildren() != 2) {
                    //    System.out.println("Children of if_then not == 2!");
                    //}
                    // first child expected to be condition
                    Object cond = ch.next();
                    // second child expected to be 'true' case
                    Object iftrue = ch.next();
                    int trueAddress = pc+1;
                    int falseAddress = pc+2;
                    if (iftrue instanceof List) {
                        Vector truevect = new Vector();
                        falseAddress = unpackPlan((List)iftrue,truevect,trueAddress);
                        ArrayList args = new ArrayList();
                        args.add(cond);
                        args.add(new Integer(trueAddress));
                        args.add(new Integer(falseAddress));
                        mach.add(new PlanOperation(new Predicate("if_then", args), planOp.getAnnotation()));
                        mach.addAll(truevect);
                    } else {
                        ArrayList args = new ArrayList();
                        args.add(cond);
                        args.add(new Integer(trueAddress));
                        args.add(new Integer(falseAddress));
                        mach.add(new PlanOperation(new Predicate("if_then", args), planOp.getAnnotation()));
                        mach.add(new PlanOperation(iftrue, planOp.getAnnotation()));
                    }
                    pc = falseAddress;
                } else if (struct.functor().equals("if_then_else")) {
                    Iterator ch = struct.arguments();
                    //if (struct.getNumChildren() != 3) {
                    //    System.out.println("Children of if_then_else != 3!");
                    //}
                    // first child expected to be condition
                    Object cond = ch.next();
                    // second child expected to be 'true/then' case
                    Object iftrue = ch.next();
                    // third child expected to be 'false/else' case
                    Object iffalse = ch.next();
                    int trueAddress = pc+1;
                    int falseAddress = pc+2;
                    int doneAddress = pc+4;
                    Vector truevect = new Vector();
                    Vector falsevect = new Vector();
                    if (iftrue instanceof List) {
                        falseAddress = unpackPlan((List)iftrue,truevect,trueAddress);
                        falseAddress++; // for 'goto' instruction
                    } else {
                        truevect.add(iftrue);
                    }
                    if (iffalse instanceof List) {
                        doneAddress = unpackPlan((List)iffalse,falsevect,falseAddress);
                    } else {
                        falsevect.add(iffalse);
                        doneAddress = falseAddress+1;
                    }
                    ArrayList args = new ArrayList();
                    args.add(cond);
                    args.add(new Integer(trueAddress));
                    args.add(new Integer(falseAddress));
                    mach.add(new PlanOperation(new Predicate("if_then_else", args), planOp.getAnnotation()));
                    mach.addAll(truevect);
                    ArrayList args2 = new ArrayList();
                    args2.add(new Integer(doneAddress));
                    mach.add(new PlanOperation(new Predicate("goto", args2)));
                    mach.addAll(falsevect);
                    pc = doneAddress;
                } else {
                    // complex but atomic move
                    mach.add(new PlanOperation(inst, planOp.getAnnotation()));
                    pc++;
                }
            } else {
                // simple move
                mach.add(new PlanOperation(inst, planOp.getAnnotation()));
                pc++;
            }
        }
        return pc;
    }

    protected void buildMachine(Object plan)
    {
        Vector mach = new Vector();
        pc = unpackPlan((List)plan,mach,0);
        machine = mach.toArray();
    }

    protected PlanInstance(String key)
    {
        theKey = key;
        answers = new LinkedList();
        annotations = new LinkedList();
        planBindings = new BindingsImpl();
    }

    public PlanInstance(Collection plan, String key)
    {
        // plan data passed in as List of moves
        this(key);
        // unpack into Vector of moves, converting control structures
        // into appropriate instructions.
        buildMachine(plan);
        pc = -1;  // reset to before first plan instruction
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.PlanInstance","planInstance","plan",plan);
        for (int i=0; i<machine.length; i++) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.domain.PlanInstance","planInstance","instruction #"+i,machine[i]);
        }
        advance();
    }

    public PlanInstance(Plan plan, String key)
    {
        // plan data passed in as Plan structure
        this(plan.plan(), key);
        thePlan = plan;
    }

    public Object next_step()
    {
        if ((pc < machine.length) && (pc >= 0)) {
            PlanOperation planOp = (PlanOperation)machine[pc];
            Object step = planOp.getOperation();
            // if answers have been set, may be able to deref this
            step = Unify.getInstance().deref(step,planBindings);
            //System.out.println("next_step "+pc+":"+step+", bindings=="+planBindings);
            return step;
        } else {
            return null;
        }
    }

    public Object annotation()
    {
        if ((pc < machine.length) && (pc >= 0)) {
            PlanOperation planOp = (PlanOperation)machine[pc];
            Object step = planOp.getAnnotation();
            // if answers have been set, may be able to deref this
            step = Unify.getInstance().deref(step,planBindings);
            return step;
        } else {
            return null;
        }
    }

    public void advance() {
        //clear accumulated list of annotations
        annotations.clear();
        // record starting location for later check
        int starting_pc = pc;
        // extract annotation for this step
        Object note = annotation();
        if (note != null) annotations.add(note);
        // move pc to next instruction
        pc++;
        // execute instructions until we reach one that requires intervention.
        boolean scanning = true;
        while (scanning) {
            if (starting_pc == pc) {
                throw new RuntimeException("plan iterator cycle!");
            }
            starting_pc = pc;
            Object next = next_step();
            logger.logp(Level.FINER,"org.mitre.dm.qud.domain.PlanInstance","advance","next-step",next);
            if (next == null) {
                return;
            }
            if (!(next instanceof Predicate)) {
                // simple moves always need intervention
                return;
            }
            Predicate move = (Predicate)next;
            // there are a limited number of control structures that
            // I care about: if_then, if_then_else, goto, and exec.
            // exec must be handled externally, unless I add a dependency
            // on the domain plan database in this class.
            // Can also automatically bypass findout nodes, but not necessary.
            logger.logp(Level.FINEST,"org.mitre.dm.qud.domain.PlanInstance","advance","instruction functor",move.functor());
            if (move.functor().equals("if_then")) {
                note = annotation();
                if (note != null) annotations.add(note);
                Iterator children = move.arguments();
                Object cond = children.next();
                Integer trueaddr = (Integer)children.next();
                Integer falseaddr = (Integer)children.next();
                if (answerKnown(cond)) {
                    //System.out.println("if_then: answer known");
                    logger.logp(Level.FINEST,"org.mitre.dm.qud.domain.PlanInstance","advance","if-then true");
                    pc = trueaddr.intValue();
                } else {
                    //System.out.println("if_then: answer not known");
                    logger.logp(Level.FINEST,"org.mitre.dm.qud.domain.PlanInstance","advance","if-then false");
                    pc = falseaddr.intValue();
                }
            } else if (move.functor().equals("if_then_else")) {
                note = annotation();
                if (note != null) annotations.add(note);
                Iterator children = move.arguments();
                Object cond = children.next();
                Integer trueaddr = (Integer)children.next();
                Integer falseaddr = (Integer)children.next();
                if (answerKnown(cond)) {
                    //System.out.println("if_then_else: answer known");
                    logger.logp(Level.FINEST,"org.mitre.dm.qud.domain.PlanInstance","advance","if-then-else true");
                    pc = trueaddr.intValue();
                } else {
                    //System.out.println("if_then_else: answer not known");
                    logger.logp(Level.FINEST,"org.mitre.dm.qud.domain.PlanInstance","advance","if-then-else false");
                    pc = falseaddr.intValue();
                }
            } else if (move.functor().equals("goto")) {
                note = annotation();
                if (note != null) annotations.add(note);
                Iterator children = move.arguments();
                Integer nextaddr = (Integer)children.next();
                logger.logp(Level.FINEST,"org.mitre.dm.qud.domain.PlanInstance","advance","goto taken");
                pc = nextaddr.intValue();
            } else {
                scanning = false;
            }
        }
    }

    public void reset() {
        answers.clear();
        pc = -1;
        //advance();
    }

    /**
     * Return the list of annotations encountered during the last
     * call to <code>advance()</code>.
     *
     * @return a <code>LinkedList</code> value
     */
    public LinkedList getRecentAnnotations()
    {
        return annotations;
    }

    /**
     * Add the specified answer to the internal set of answers
     * if it isn't already present. Does not make any check for
     * predicate, though, since we assume there might be multiple
     * values possible.
     *
     * @param answer an <code>Object</code> value
     */
    public void set_answer(Object answer) {
        //System.out.println("asserting "+answer);
        Iterator it = answers.iterator();
        while (it.hasNext()) {
            Object ans = it.next();
            if (ans.toString().equals(answer.toString())) {
                logger.logp(Level.FINER,"org.mitre.dm.qud.domain.PlanInstance","setAnswer","already answered "+answer);
                return;
            }
        }
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.PlanInstance","setAnswer","succeeded "+answer);
        answers.add(answer);
    }

    /**
     * Returns a matching answer in the plan. If no answer is known,
     * return null. For now, could simplify by using 'answerKnown()',
     * but in the general case there should be unification involved.
     *
     * @param question an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object get_answer(Object question)
    {
        if (!(question instanceof Predicate)) {
            ArrayList args = new ArrayList();
            args.add(Variable.newVariable());
            question = new Predicate(question.toString(), args);
        }
        //System.out.println("plan bindings on entry == "+planBindings);
        planBindings.enterScope();
        Iterator it = answers.iterator();
        while (it.hasNext()) {
            Object ans = Unify.getInstance().unify(it.next(), question, planBindings);
            if (ans != null) {
                planBindings.exitScope();
                //System.out.println("plan bindings on exit == "+planBindings);
                return ans;
            }
        }
        planBindings.exitScope();
        //System.out.println("plan bindings on exit == "+planBindings);
        return null;
    }

    /**
     * Remove the specified answer if it exists. Removal is done
     * through unification, so it is possible to remove multiple
     * answers.
     *
     * Note: design of plan instances currently expects a single binding
     * for each slot, so bindings are maintained for the plan instance
     * to avoid scanning the answer list each time. This is fine when
     * we're asserting, but causes problems when retracting. The variable
     * needs to be unbound, and the Bindings interface doesn't support
     * dynamic rebinding (nor should it). Assume that the supplied
     * question has been asserted, and build a new set of bindings
     * containing all bindings but the removed one.
     *
     * @param question an <code>Object</code> value
     */
    public void retract_answer(Object question) {
        //System.out.println("retracting "+question);
        planBindings.enterScope();
        Iterator it = answers.iterator();
        while (it.hasNext()) {
            Object ques = it.next();
            Object ans = Unify.getInstance().unify(ques, question);
            if (ans != null) {
                //System.out.println("retract matched "+ans);
                it.remove();
                Predicate pred = (Predicate)question;
                BindingsImpl pbi = (BindingsImpl)planBindings;
                if (pred.arity() == 1) {
                    Variable var = thePlan.getVariableFor(pred.functor());
                    if (var != null) {
                        pbi.remove(var);
                    } else {
                        //System.out.println("question has no var: "+pred);
                    }
                }
            } else {
                //System.out.println("retract fail "+ques);
            }
        }
        //System.out.println("answers after retract == "+answers);
        planBindings.exitScope();
        //System.out.println("plan bindings after retract == "+planBindings);
    }

    /**
     * Return true if the specified question can be unified with any
     * portion of any findout or raise move in the plan.
     *
     * @param question an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public Object wants_answer(Object question)
    {
        if (thePlan != null) {
            if (question instanceof Predicate) {
                Predicate q = (Predicate)question;
                int idx = thePlan.indexOf(q.functor());
                if (idx != -1) {
                    ArrayList al = new ArrayList();
                    al.add(new Variable("X"+idx));
                    Predicate master = new Predicate(q.functor(),al);
                    //System.out.println("plan set; master question "+master);
                    return master;
                } else {
                    //System.out.println("plan set, no attr "+q.functor());
                }
            } else {
                //System.out.println("plan set, question not predicate");
            }
        }
        for (int i=0; i<machine.length; i++) {
            if (machine[i]==null) continue;
            Object term = machine[i];
            if (!(term instanceof Predicate)) continue;
            Predicate struct = (Predicate)term;
            if (!struct.functor().equals("findout") &&
                !struct.functor().equals("raise")) continue;
            Iterator children = struct.arguments();
            Object ans = children.next();
            if (ans instanceof List) {
                Iterator answers = ((List)ans).iterator();
                while (answers.hasNext()) {
                    ans = answers.next();
                    if (Unify.getInstance().unify(question,ans) != null) {
                        return ans;
                    }
                }
            } else {
                if (Unify.getInstance().unify(question,ans) != null) {
                    return ans;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if the plan has an answer for the specified question.
     * For now, return the result of answerKnown, since we expect to only
     * have a known answer if the answer is affirmative. In the general
     * case, processing should be more complex.
     *
     * @param question an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean has_answer(Object question)
    {
        return answerKnown(question);
    }

    /**
     * Returns the name of the plan that this is an instance of.
     * Provides a means of associating a task name with an
     * instance of a process.
     *
     * @return a <code>String</code> value
     */
    public String getTaskName()
    {
        if (thePlan == null) return "*anonymous*";
        else return thePlan.getTask().name();
    }

    /**
     * Returns a string representation of this plan instance,
     * suitable for display.
     *
     * @return a <code>String</code> value
     */
    public String toString(){
    		String output = "";
    		if (thePlan != null){
    			
    			ContractImpl contract = (ContractImpl) thePlan.getTask();
    			output = output + contract.name() + ": [\n";

    			Iterator attributes = contract.attributes();
    			AttributeImpl attribute;
    			String attrName, ans;
    			
    			while(attributes.hasNext()){
    				attribute = (AttributeImpl) attributes.next();
    				attrName = attribute.name();
    				if(get_answer(attrName) != null){
    					ans = get_answer(attrName).toString();
    					output = output + "   "+ ans;
    					if (attributes.hasNext()) output = output+",";
    					output = output + "\n";
    				}
    			}
    			output = output + "]\n";
    			
    		}
    		return output;
    }
    
}
