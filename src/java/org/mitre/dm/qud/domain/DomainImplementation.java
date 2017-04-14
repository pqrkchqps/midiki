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

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;
import org.mitre.midiki.impl.mitre.*;

/**
 * Provides a simple implementation of the Midiki domain contract.
 * The specific details of the domain must be filled in within three
 * abstract initialization routines that establish attributes, tasks,
 * and questions as data structures in memory.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
abstract public class DomainImplementation
{
    private static final String className =
        "org.mitre.dm.qud.domain.DomainImplementation";
    private static Logger logger = Logger.getLogger(className);
    public DomainImplementation()
    {
    }
    public void init(Object configurationData)
    {
        //super();
        planMap = new HashMap();
        attributeMap = new HashMap();
        taskMap = new HashMap();
        keyMap = new HashMap();
        questionTypes = new HashMap();
        questionAnswers = new HashMap();
        // Declare local data required for our solvables
    }

    public void connect(InfoState is)
    {
        //infoState = is;
        initializeTasks();
        initializeAttributes();
        // once attributes have been initialized, can migrate to more complete
        // checking of types, value compatability.
        checkDomainConsistency(is);
        calculateTaskDominationAndRelevance();
    }

    protected boolean stackHoldsPlanInstances=true;
    protected boolean initializedAttributes=false;
    protected HashMap attributeMap;
    protected HashMap planMap;
    static protected int nextPlanId = 0;
    abstract protected void initializeAttributes();
    protected boolean initializedTasks=false;
    protected HashMap taskMap;
    protected HashMap keyMap;  // at one point I think this was storing Contracts, but now it just maps strings to themselves. will revisit this.
    protected Object defaultTask;
    protected Object initialTask;
    protected boolean[][] domination;
    protected boolean[][] relevance;
    protected boolean[] taskRelevance;
    protected void calculateTaskDominationAndRelevance()
    {
System.out.println("calculating dominance and relevance");
        if (taskMap.keySet().size() > 0) {
            int dim = taskMap.keySet().size();
            domination = new boolean[dim][dim];
            taskRelevance = new boolean[dim];
            int qdim = 0;
            if (questionTypes.keySet().size() > 0) {
                qdim = questionTypes.keySet().size();
                relevance = new boolean[dim][qdim];
            }
            int idx = 0;
            Iterator it = taskMap.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                Object plan = (Object)taskMap.get(key);
                Iterator plit;
                if (plan instanceof Plan) {
                    plit = ((Plan)plan).plan().iterator();
                } else {
                    plit = ((Collection)plan).iterator();
                }
                while (plit.hasNext()) {
                    PlanOperation taskop = (PlanOperation)plit.next();
                    dominance_and_relevance(idx, taskop);
                }
                idx++;
            }
            // now we have all the direct "dominates" relations between tasks.
            // calculate the transitive closure.
            boolean changed = true;
            while (changed) {
                changed = false;
                for (int i=0; i<dim; i++) {
                    for (int j=0; j<dim; j++) {
                        if (i==j) continue;
                        if (!domination[i][j]) continue;
                        for (int k=0; k<dim; k++) {
                            if (domination[j][k] && !domination[i][k]) {
                                domination[i][k] = true;
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
    }
    protected void dominance_and_relevance(int idx, PlanOperation planop)
    {
        int idx2;
        Object taskop = planop.getOperation();
        if (taskop instanceof Predicate) {
            if (((Predicate)taskop).functor().equals("findout")) {
                Object sub = ((Predicate)taskop).arguments().next();
                if (sub == null) return;
                String funct = "";
                if (sub instanceof Predicate) {
                    funct = ((Predicate)sub).functor();
                    if (funct.equals("task")) {
                        idx2 = 0;
                        Iterator it2 = taskMap.keySet().iterator();
                        while (it2.hasNext()) {
                            Object k2 = keyMap.get(it2.next());
                            if (Unify.getInstance().unify(sub,k2) != null) {
                                domination[idx][idx2] = true;
                                break;
                            }
                            idx2++;
                        }
                    } else {
                        // functor ought to be a question
                        Object qt = questionTypes.get(funct);
                        if (qt == null) {
                            // unregistered question type
                            System.out.println("*** Unregistered question type "+funct);
                        } else {
                            // mark the question as relevant to this task
                            idx2 = 0;
                            Iterator it2 = questionTypes.keySet().iterator();
                            while (it2.hasNext()) {
                                String ques = (String)it2.next();
                                if (ques.equals(funct)) {
                                    relevance[idx][idx2] = true;
                                //} else if (ques.equals(qt)) {
                                //    relevance[idx][idx2] = true;
                                }
                                idx2++;
                            }
                        }
                    }
                } else {
                    // functor expected to be a yesno question (no arguments)
                    // (note that it's easier to keep formats identical...)
                    funct = sub.toString();
                    Object qt = questionTypes.get(funct);
                    if (qt == null) {
                        // unregistered question type
                        System.out.println("dom&rel: unregistered question type "+funct);
                    } else if (qt instanceof String) {
                        if (((String)qt).equals("yesno")) {
                            // mark the question as relevant to this task
                            idx2 = 0;
                            Iterator it2 = questionTypes.keySet().iterator();
                            while (it2.hasNext()) {
                                String ques = (String)it2.next();
                                if (ques.equals(funct)) {
                                    relevance[idx][idx2] = true;
                                }
                                idx2++;
                            }
                        } else {
                            // invalid type for arity 0 question
                            System.out.println("invalid type for arity 0 question: "+qt);
                        }
                    } else {
                        // question type not string, not sure how to handle here
                        System.out.println("question type not string: "+qt.getClass().getName());
                    }
                }
            } else if (((Predicate)taskop).functor().equals("if_then")) {
                Iterator it = ((Predicate)taskop).arguments();
                Object cond = it.next();
                Object thenPart = it.next();
                Iterator plit = ((Collection)thenPart).iterator();
                while (plit.hasNext()) {
                    PlanOperation subop = (PlanOperation)plit.next();
                    dominance_and_relevance(idx, subop);
                }
            } else if (((Predicate)taskop).functor().equals("if_then_else")) {
                Iterator it = ((Predicate)taskop).arguments();
                Object cond = it.next();
                Object thenPart = it.next();
                Object elsePart = it.next();
                Iterator plit = ((Collection)thenPart).iterator();
                while (plit.hasNext()) {
                    PlanOperation subop = (PlanOperation)plit.next();
                    dominance_and_relevance(idx, subop);
                }
                plit = ((Collection)elsePart).iterator();
                while (plit.hasNext()) {
                    PlanOperation subop = (PlanOperation)plit.next();
                    dominance_and_relevance(idx, subop);
                }
            } else if (((Predicate)taskop).functor().equals("do_while")) {
                Iterator it = ((Predicate)taskop).arguments();
                Object cond = it.next();
                Object thenPart = it.next();
                Iterator plit = ((Collection)thenPart).iterator();
                while (plit.hasNext()) {
                    PlanOperation subop = (PlanOperation)plit.next();
                    dominance_and_relevance(idx, subop);
                }
            } else if (((Predicate)taskop).functor().equals("do_until")) {
                Iterator it = ((Predicate)taskop).arguments();
                Object cond = it.next();
                Object thenPart = it.next();
                Iterator plit = ((Collection)thenPart).iterator();
                while (plit.hasNext()) {
                    PlanOperation subop = (PlanOperation)plit.next();
                    dominance_and_relevance(idx, subop);
                }
            }
        } else if (taskop instanceof Collection) {
            // if this is a list, recurse.
            Iterator plit = ((Collection)taskop).iterator();
            while (plit.hasNext()) {
                PlanOperation taskop2 = (PlanOperation)plit.next();
                dominance_and_relevance(idx, taskop2);
            }
        } else if (taskop instanceof String) {
            // system move, ignore this
        } else {
            System.out.println("Unexpected taskop type == "+taskop.getClass().getName());
        }
    }
    abstract protected void initializeTasks();
    protected boolean initializedQuestions=false;
    protected Object defaultQuestion;
    protected HashMap questionTypes;
    protected HashMap questionAnswers;
    protected void initializeQuestions()
    {

        /*
         * generates slot list from plans.
         * compare to mandated slot list for consistency?
         */

        Iterator qiter = taskMap.entrySet().iterator();
        while (qiter.hasNext()) {
            Map.Entry me = (Map.Entry)qiter.next();
            Plan p = (Plan)me.getValue();
            Iterator atit = p.getTask().attributes();
            while (atit.hasNext()) {
                AttributeImpl at = (AttributeImpl)atit.next();
                String name = at.name();
                Object type = at.type();  // may be String or java.lang.Class
                questionTypes.put(name, type);
            }
        }

        initializedQuestions = true;
    }

    /**
     * Identifies common domain problems and corrects them.
     * Outputs warning messages to support correction by programmer.
     */
    public void checkDomainConsistency(InfoState is)
    {
System.out.println("checking domain consistency");
        boolean problem = false;
        // ensure all methods and query arguments are named attributes
        Iterator it = taskMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry)it.next();
            Plan p = (Plan)me.getValue();
            Iterator mit = p.getTask().methods();
            while (mit.hasNext()) {
                Contract.Method method = (Contract.Method)mit.next();
                Iterator ait = method.parameters();
                while (ait.hasNext()) {
                    Contract.Parameter param = (Contract.Parameter)ait.next();
                    if (p.getTask().attribute(param.name()) == null) {
                        System.out.println("WARNING: task "+p.getTask().name()+" method "+method.name()+" parameter "+param.name()+" not in task. ADDING");
                        ((ContractImpl)p.getTask()).addAttribute(new AttributeImpl(param.name(), null, null));
                        if (questionTypes != null) questionTypes.put(param.name(), null);
                    }
                }
            }
            Iterator qit = p.getTask().queries();
            while (mit.hasNext()) {
                Contract.Query query = (Contract.Query)mit.next();
                Iterator ait = query.parameters();
                while (ait.hasNext()) {
                    Contract.Parameter param = (Contract.Parameter)ait.next();
                    if (p.getTask().attribute(param.name()) == null) {
                        System.out.println("WARNING: task "+p.getTask().name()+" query "+query.name()+" parameter "+param.name()+" not in task. ADDING");
                        ((ContractImpl)p.getTask()).addAttribute(new AttributeImpl(param.name(), null, null));
                        if (questionTypes != null) questionTypes.put(param.name(), null);
                    }
                }
            }
            boolean error = checkPlanConsistency(p.getTask(), p.plan(), is);
            problem = problem || error;
        }
        if (problem) {
            System.out.println("WARNING: Please correct these problems to ensure correct plan operation.");
        }
    }

    /**
     * Identifies plan operations whose arguments are not consistent with the
     * default semantics of those operations. Override this method if your
     * version of plans use different operations and/or different semantics.
     */
    protected boolean checkPlanConsistency(Contract task, Collection ops, InfoState is)
    {
        boolean problem = false;
        // ensure all moves have named attributes or tasks:
        // findout, findout(task), raise, raise(task), require, bind, if_then, do_while, do_until, exec, call...
        // forget, inform, assert can be other than attributes
        // query, method must refer to an actual query or method
        Iterator pit = ops.iterator();
        while (pit.hasNext()) {
            Object op = pit.next();
            if (!(op instanceof PlanOperation)) continue;
            op = ((PlanOperation)op).getOperation();
            if (!(op instanceof Predicate)) continue;
            Predicate pop = (Predicate)op;
            if (pop.functor().equals("findout")) {
                Object question = pop.arguments().next();
                if (question instanceof Predicate) {  // better be task or attribute
                    Predicate taskPred = (Predicate)question;
                    if (task.attribute(taskPred.functor()) == null) {
                        if (taskPred.functor().equals("task")) {
                            question = taskPred.arguments().next();
                            if (taskMap.get(question) == null) {
                                System.out.println("ERROR: task "+task.name()+" findout for unknown task "+question+".");
                                problem = true;
                            }
                        } else {
                            System.out.println("WARNING: task "+task.name()+" findout for unrecognized functor "+taskPred.functor()+".");
                            ((ContractImpl)task).addAttribute(new AttributeImpl(taskPred.functor(), null, null));
                            if (questionTypes != null) questionTypes.put(question, null);
                            problem = true;
                        }
                    }
                } else if (question instanceof Collection) {
                    // disambiguating from a list
                    // should check that all are comparable, but for now just let it slide.
                } else {  // must be attribute
                    if (task.attribute((String)question) == null) {
                        System.out.println("WARNING: task "+task.name()+" findout for non-attribute "+question+". ADDING");
                        ((ContractImpl)task).addAttribute(new AttributeImpl((String)question, null, null));
                        if (questionTypes != null) questionTypes.put(question, null);
                        problem = true;
                    }
                }
            } else if (pop.functor().equals("raise")) {
                Object question = pop.arguments().next();
                if (question instanceof Predicate) {  // better be task
                    Predicate taskPred = (Predicate)question;
                    if (taskPred.functor().equals("task")) {
                        question = taskPred.arguments().next();
                        if (taskMap.get(question) == null) {
                            System.out.println("ERROR: task "+task.name()+" raise for unknown task "+question+".");
                            problem = true;
                        }
                    } else {
                        System.out.println("WARNING: task "+task.name()+" raise for unrecognized functor "+taskPred.functor()+".");
                        problem = true;
                    }
                } else if (question instanceof Collection) {
                    // disambiguating from a list
                    // should check that all are comparable, but for now just let it slide.
                } else {  // must be attribute
                    if (task.attribute((String)question) == null) {
                        System.out.println("WARNING: task "+task.name()+" raise for non-attribute "+question+". ADDING");
                        ((ContractImpl)task).addAttribute(new AttributeImpl((String)question, null, null));
                        if (questionTypes != null) questionTypes.put(question, null);
                        problem = true;
                    }
                }
            } else if (pop.functor().equals("require")) {
                Object attr = pop.arguments().next();
                if (attr instanceof Predicate) {
                    if (task.attribute(((Predicate)attr).functor()) == null) {
                        System.out.println("ERROR: task "+task.name()+" require of unknown attribute "+((Predicate)attr).functor()+". ADDING");
                        ((ContractImpl)task).addAttribute(new AttributeImpl(((Predicate)attr).functor(), null, null));
                        if (questionTypes != null) questionTypes.put(((Predicate)attr).functor(), null);
                        problem = true;
                    }
                } else {
                    System.out.println("ERROR: task "+task.name()+" require uses unknown format.");
                    problem = true;
                }
            } else if (pop.functor().equals("bind")) {
                Object attr = pop.arguments().next();
                if (attr instanceof Predicate) {
                    if (task.attribute(((Predicate)attr).functor()) == null) {
                        System.out.println("ERROR: task "+task.name()+" bind of unknown attribute "+((Predicate)attr).functor()+". ADDING");
                        ((ContractImpl)task).addAttribute(new AttributeImpl(((Predicate)attr).functor(), null, null));
                        if (questionTypes != null) questionTypes.put(((Predicate)attr).functor(), null);
                        problem = true;
                    }
                } else {
                    System.out.println("ERROR: task "+task.name()+" bind uses unknown format.");
                    problem = true;
                }
            } else if (pop.functor().equals("if_then_else")) {
                Iterator popIt = pop.arguments();
                Object attr = null;
                Object thenPlan = null;
                Object elsePlan = null;
                try {
                    attr = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" if_then_else missing attr name and 'then' plan.");
                    return true;
                }
                try {
                    thenPlan = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" if_then_else("+attr+") missing 'then' and 'else' plan.");
                    return true;
                }
                try {
                    elsePlan = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" if_then_else("+attr+") missing 'else' plan.");
                    return true;
                }
                try {
                    if (task.attribute(((Predicate)attr).functor()) == null) {
                        System.out.println("ERROR: task "+task.name()+" if_then_else of unknown attribute "+((Predicate)attr).functor()+". ADDING");
                        ((ContractImpl)task).addAttribute(new AttributeImpl(((Predicate)attr).functor(), null, null));
                        if (questionTypes != null) questionTypes.put(((Predicate)attr).functor(), null);
                        problem = true;
                    }
                } catch (ClassCastException cce) {
                    System.out.println("ERROR: task "+task.name()+" if_then_else has non-Predicate attribute test.");
                }
                try {
                    Collection plan = (Collection)thenPlan;
                    boolean error = checkPlanConsistency(task, plan, is);
                    problem = problem || error;
                } catch (ClassCastException cce) {
                    System.out.println("ERROR: task "+task.name()+" if_then_else has non-Plan then-part.");
                }
                try {
                    Collection plan = (Collection)elsePlan;
                    boolean error = checkPlanConsistency(task, plan, is);
                    problem = problem || error;
                } catch (ClassCastException cce) {
                    System.out.println("ERROR: task "+task.name()+" if_then_else has non-Plan else-part.");
                }

            } else if (pop.functor().equals("if_then")) {
                Iterator popIt = pop.arguments();
                Object attr = null;
                Object thenPlan = null;
                try {
                    attr = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" if_then missing attr name and 'then' plan.");
                    return true;
                }
                try {
                    thenPlan = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" if_then("+attr+") missing 'then' plan.");
                    return true;
                }
                try {
                    if (task.attribute(((Predicate)attr).functor()) == null) {
                        System.out.println("ERROR: task "+task.name()+" if_then of unknown attribute "+((Predicate)attr).functor()+". ADDING");
                        ((ContractImpl)task).addAttribute(new AttributeImpl(((Predicate)attr).functor(), null, null));
                        if (questionTypes != null) questionTypes.put(((Predicate)attr).functor(), null);
                        problem = true;
                    }
                } catch (ClassCastException cce) {
                    System.out.println("ERROR: task "+task.name()+" if_then has non-Predicate attribute test.");
                }
                try {
                    Collection plan = (Collection)thenPlan;
                    boolean error = checkPlanConsistency(task, plan, is);
                    problem = problem || error;
                } catch (ClassCastException cce) {
                    System.out.println("ERROR: task "+task.name()+" if_then has non-Plan then-part.");
                }
            } else if (pop.functor().equals("do_while")) {
                Iterator popIt = pop.arguments();
                Object attr = null;
                Object thenPlan = null;
                try {
                    attr = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" do_while missing attr name and 'then' plan.");
                    return true;
                }
                try {
                    thenPlan = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" do_while("+attr+") missing 'then' plan.");
                    return true;
                }
                try {
                    if (task.attribute(((Predicate)attr).functor()) == null) {
                        System.out.println("ERROR: task "+task.name()+" do_while of unknown attribute "+((Predicate)attr).functor()+". ADDING");
                        ((ContractImpl)task).addAttribute(new AttributeImpl(((Predicate)attr).functor(), null, null));
                        if (questionTypes != null) questionTypes.put(((Predicate)attr).functor(), null);
                        problem = true;
                    }
                } catch (ClassCastException cce) {
                    System.out.println("ERROR: task "+task.name()+" do_while has non-Predicate attribute test.");
                }
                try {
                    Collection plan = (Collection)thenPlan;
                    boolean error = checkPlanConsistency(task, plan, is);
                    problem = problem || error;
                } catch (ClassCastException cce) {
                    System.out.println("ERROR: task "+task.name()+" do_while has non-Plan do-part.");
                }
            } else if (pop.functor().equals("do_until")) {
                Iterator popIt = pop.arguments();
                Object attr = null;
                Object thenPlan = null;
                try {
                    attr = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" do_until missing attr name and 'then' plan.");
                    return true;
                }
                try {
                    thenPlan = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" do_until("+attr+") missing 'then' plan.");
                    return true;
                }
                try {
                    if (task.attribute(((Predicate)attr).functor()) == null) {
                        System.out.println("ERROR: task "+task.name()+" do_until of unknown attribute "+((Predicate)attr).functor()+". ADDING");
                        ((ContractImpl)task).addAttribute(new AttributeImpl(((Predicate)attr).functor(), null, null));
                        if (questionTypes != null) questionTypes.put(((Predicate)attr).functor(), null);
                        problem = true;
                    }
                } catch (ClassCastException cce) {
                    System.out.println("ERROR: task "+task.name()+" do_until has non-Predicate attribute test.");
                }
                try {
                    Collection plan = (Collection)thenPlan;
                    boolean error = checkPlanConsistency(task, plan, is);
                    problem = problem || error;
                } catch (ClassCastException cce) {
                    System.out.println("ERROR: task "+task.name()+" do_until has non-Plan do-part.");
                }
            } else if (pop.functor().equals("exec")) {
                Object toTask = pop.arguments().next();
                if (taskMap.get((String)toTask) == null) {
                    System.out.println("ERROR: task "+task.name()+" exec of unknown task "+toTask+".");
                    problem = true;
                }
            } else if (pop.functor().equals("call")) {
                Object toTask = pop.arguments().next();
                if (taskMap.get((String)toTask) == null) {
                    System.out.println("ERROR: task "+task.name()+" call of unknown task "+toTask+".");
                    problem = true;
                }
            } else if (pop.functor().equals("query")) {
                Iterator popIt = pop.arguments();
                Object cell = null;
                Object query = null;
                Object proxy = null;
                try {
                    cell = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" query call missing cell and query name.");
                    return true;
                }
                try {
                    query = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" query call on cell "+cell+" missing query name.");
                    return true;
                }
                try {
                    proxy = is.cell((String)cell).query((String)query);
                } catch (Exception e) {
                }
                if (proxy == null) {
                    System.out.println("ERROR: task "+task.name()+" calls unknown query "+query+".");
                    problem = true;
                }
            } else if (pop.functor().equals("method")) {
                Iterator popIt = pop.arguments();
                Object cell = null;
                Object method = null;
                Object proxy = null;
                try {
                    cell = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" method call missing cell and method name.");
                    return true;
                }
                try {
                    method = popIt.next();
                } catch (Exception e) {
                    System.out.println("ERROR: task "+task.name()+" method call on cell "+cell+" missing method name.");
                    return true;
                }
                try {
                    proxy = is.cell((String)cell).method((String)method);
                } catch (Exception e) {
                }
                if (proxy == null) {
                    System.out.println("ERROR: task "+task.name()+" calls unknown method "+method+".");
                    problem = true;
                }
            }
        }
        return problem;
    }

    /*
     *'reduce' merges an input question and an input answer
     * to yield a proposition. this is the routine that answers
     * a question and asserts the result.
     */
    public boolean doQuery_domain_reduce(Object intfid,Object q,Object r,Object p, Bindings bindings)
    {
        q = Unify.getInstance().deref(q, bindings);
        r = Unify.getInstance().deref(r, bindings);
        boolean reduced = false;
        logger.logp(Level.FINER,className,"reduce","question",q);
        logger.logp(Level.FINER,className,"reduce","answer",r);
        //System.out.println("reducing "+q+", "+r+", "+p);
        //
        // is it a list of alternatives? (answer is one of the list)
        if (q instanceof List) {
            // verify that answer is member of list
            logger.logp(Level.FINEST,className,"reduce","q isa List");
        }
        // is it a yes-no question? (answers are yes, no, P, not P)
        // is it a predicate answered by a predicate? attempt unification.
        else if ((q instanceof Predicate) && (r instanceof Predicate)) {
            reduced = Unify.getInstance().matchTerms(q,r,bindings);
            logger.logp(Level.FINEST,className,"reduce",(reduced ? "both are predicates, unify == true" : "both are predicates, unify == false"));
        }
        // is it a predicate answered by an atom? attempt wrapping.
        else if (q instanceof Predicate) {
            ArrayList temp = new ArrayList();
            temp.add(r);
            Predicate pred = new Predicate(((Predicate)q).functor(), temp);
            reduced = Unify.getInstance().matchTerms(q,pred,bindings);
            logger.logp(Level.FINEST,className,"reduce",(reduced ? "wrapped answer unifies" : "wrapped answer does not unify"));
        } else {
            logger.logp(Level.FINEST,className,"reduce","q-isa",q.getClass());
            logger.logp(Level.FINEST,className,"reduce","r-isa",r.getClass());
        }
        // is it a predicate with arity greater than 1? ignore for now.
        if (reduced) {
            Object result = Unify.getInstance().deref(q, bindings);
            Unify.getInstance().matchTerms(p,result,bindings);
            logger.logp(Level.FINEST,className,"reduce","yields-bindings",bindings);
            bindings.reset();
            //System.out.println("reduction succeeded.");
            return true;
        } else {
            //System.out.println("reduction failed.");
            return false;
        }
    }
    public boolean packAnswers(Object intfid, Object q, String functor, Object value, String attribute, Bindings bindings)
    {
        logger.logp(Level.FINEST,className,"packAnswers","functor",functor);
        logger.logp(Level.FINEST,className,"packAnswers","value",value);
        logger.logp(Level.FINEST,className,"packAnswers","attribute",attribute);
        value = Unify.getInstance().deref(value, bindings);
        if (value instanceof Predicate) {
            if (functor.equals(((Predicate)value).functor())) {
                // the answer is a fully-qualified answer, which includes
                // the functor in question
                //value = ((Predicate)value).arguments().next();
                return packQualifiedAnswers(intfid,q,functor,value,attribute,bindings);
            } else {
                // the answer has the wrong functor, so cannot be used.
                logger.logp(Level.FINEST,className,"packAnswers","wrong-functor",((Predicate)value).functor());
                return false;
            }
        }
        // prepare the result predicate
        ArrayList args = new ArrayList();
        args.add(value);
        Predicate result = new Predicate(functor, args);
        // test the value of the attribute
        if (!initializedAttributes) initializeAttributes();
        Object tupleRef = attributeMap.get(attribute);
        ArrayList tuples = null;
        if (tupleRef == null) {
            //System.out.println("** Unspecified attribute type "+attribute);
            //logger.logp(Level.WARNING,className,"packAnswers","unspecified-attribute-type",attribute);
            //return false;
        } else if (tupleRef instanceof AttributeEvaluator) {
            if (((AttributeEvaluator)tupleRef).evaluate(value)) {
                if (Unify.getInstance().unify(q, result, bindings) != null) {
                    bindings.reset();
                    return true;
                }
            } else {
                return false;
            }
        } else if (tupleRef instanceof ArrayList)
            tuples = (ArrayList)tupleRef;
        else {
            try {
                tuples = new ArrayList((Collection)tupleRef);
            } catch (ClassCastException cce) {
                System.out.println("tupleRef class is "+tupleRef.getClass().getName());
            }
        }
        boolean success = false;
        if (tuples == null) {
            if (Unify.getInstance().unify(q, result, bindings) != null) {
                success = true;
                bindings.reset();
            }
        } else {
            Iterator it = tuples.iterator();
            while (it.hasNext()) {
                Object tuple = it.next();
            /* When setting up the bindings, all of them have to be
             * established within the same scope. So, we match q again
             * to ensure that it has a binding for each result set.
             */
                if (Unify.getInstance().matchTerms(value, tuple, bindings)) {
                    Unify.getInstance().matchTerms(q, result, bindings);
                    success = true;
                    bindings.reset();
                }
            }
        }
        return success;
    }
    public boolean packQualifiedAnswers(Object intfid, Object q, String functor, Object value, String attribute, Bindings bindings)
    {
        logger.logp(Level.FINEST,className,"packQualifiedAnswers","functor",functor);
        logger.logp(Level.FINEST,className,"packQualifiedAnswers","value",value);
        logger.logp(Level.FINEST,className,"packQualifiedAnswers","attribute",attribute);
        // Normalize the incoming answer and relevant answers.
        // Question should never be normalized -- either we're matching
        // internals of a given predicate, or we're matching the whole thing.
        if (!(value instanceof Predicate)) {
            ArrayList vll = new ArrayList();
            vll.add(value);
            value = new Predicate(functor, vll);
            logger.logp(Level.FINEST,className,"packQualifiedAnswers","normalized-value",value);
        }

        Variable v = Variable.newVariable();
        ArrayList vl = new ArrayList();
        vl.add(v);
        Predicate valtest = new Predicate(functor, vl);

        if (!initializedAttributes) initializeAttributes();
        Object tupleRef = attributeMap.get(attribute);
        ArrayList tuples = null;
        if (tupleRef == null) {
            System.out.println("** Unspecified attribute type "+attribute);
            logger.logp(Level.WARNING,className,"packQualifiedAnswers","unspecified-attribute-type",attribute);
        } else if (tupleRef instanceof AttributeEvaluator) {
            if (Unify.getInstance().matchTerms(valtest, value, bindings) &&
                ((AttributeEvaluator)tupleRef).evaluate(Unify.getInstance().deref(v, bindings))) {
                bindings.reset();
                return true;
            } else {
                return false;
            }
        } else if (tupleRef instanceof ArrayList)
            tuples = (ArrayList)tupleRef;
        else {
            try {
                tuples = new ArrayList((Collection)tupleRef);
            } catch (ClassCastException cce) {
                System.out.println("tupleRef class is "+tupleRef.getClass().getName());
            }
        }
        
        boolean success = false;
        if (tuples == null) {
            if (Unify.getInstance().matchTerms(valtest, value, bindings) &&
                Unify.getInstance().matchTerms(q, value, bindings)) {
                success = true;
                bindings.reset();
                //System.out.println("Untyped packQualifiedAnswers success: "+valtest+", "+q);
            } else {
                //System.out.println("Untyped packQualifiedAnswers failure: "+valtest+", "+q);
            }
        } else {
        Iterator it = tuples.iterator();
        while (it.hasNext()) {
            Object tuple = it.next();
            bindings.put(v, tuple);
            logger.logp(Level.FINEST,className,"packQualifiedAnswers","tuple",tuple);
            logger.logp(Level.FINEST,className,"packQualifiedAnswers","q",q);
            if (Unify.getInstance().matchTerms(valtest, value, bindings) &&
                Unify.getInstance().matchTerms(q, value, bindings)) {
                success = true;
                bindings.reset();
            }
            bindings.remove(v);
        }
        }
        return success;
    }
    public boolean packTaskAnswers(Object intfid, Object task, Object test, Bindings bindings)
    {
        if (!initializedAttributes) initializeAttributes();
        // since the question is a task question, we could be presented with
        // two types of answers: attribute answers, and task answers.
        if (test instanceof Predicate) {
            if (((Predicate)test).functor().equals("task")) {
                // this will succeed iff it unifies with the input task
                boolean matchedTask =
                    Unify.getInstance().matchTerms(task, test, bindings);
                if (matchedTask) bindings.reset();
                return matchedTask;
            }
        }
        // test term is not a task functor. must be an attribute.
        // test term is passed in as raw value, not as an answer.
        // must formulate it as an answer move before relevantTasks()
        // will want to process it.
        ArrayList testArgs = new ArrayList();
        testArgs.add(test);
        Object testPredicate = new Predicate("answer", testArgs);
        ArrayList tasks = relevantTasks(testPredicate);
        //System.out.println("packTaskAnswers(): tasks = "+tasks+" for "+test);
        if (tasks == null) {
            return false;
        }
        boolean success = false;
        Iterator it = tasks.iterator();
        while (it.hasNext()) {
            String taskName = (String)it.next();
            ArrayList taskArgs = new ArrayList();
            taskArgs.add(taskName);
            Object taskPredicate = new Predicate("task", taskArgs);
            // does this unify with the input task?
            boolean matchedTask =
                Unify.getInstance().matchTerms(task, taskPredicate, bindings);
            if (matchedTask) bindings.reset();
            success = success || matchedTask;
        }
        return success;
    }

    /*
     * Trindikit/Godis assumes questions are arity 1 predicates.
     * That does not have to be the case. However, if there are
     * higher arities, domain has to be enriched to handle this.
     * Answer can be bare value substituted for any position in
     * a question predicate, or it can be matched directly
     * against a question predicate. In both cases the result
     * must be compared against legal values.
     */
    public boolean doQuery_domain_relevant_answer(Object intfid,Object q,Object r, Bindings bindings)
    {
        logger.entering("org.mitre.dm.qud.domain.DomainImplementation","relevantAnswer");
        q = Unify.getInstance().deref(q, bindings);
        r = Unify.getInstance().deref(r, bindings);
        logger.logp(Level.FINEST,className,"relevantAnswer","q on entry",q);
        logger.logp(Level.FINEST,className,"relevantAnswer","r on entry",r);
        // q must be a struct corresponding to a defined question
        // NOTE: this can be a problem for some of the shortcut asks
        // like 'findout task(const)'
        Object f = null;
        if (q instanceof Predicate) {
            f = ((Predicate)q).functor();
            logger.logp(Level.FINEST,className,"relevantAnswer","q-functor",f);
            if (f.equals("task")) {
                logger.logp(Level.FINEST,className,"relevantAnswer","task functor gets special handling");
                return packTaskAnswers(intfid, q, r, bindings);
            }
        } else if (q instanceof List) {
            /*
             * q would be an list if we are processing a findout with
             * a set of possible answers smaller than the universe of
             * answers for the question.
             */
            logger.logp(Level.FINEST,className,"relevantAnswer","q-isList-vs",r);
            // process each list element separately
            boolean success = false;
            Iterator it = ((List)q).iterator();
            while (it.hasNext()) {
                Object elem = it.next();
                if (elem instanceof Predicate) {
                    f = ((Predicate)elem).functor();
                    if (f.equals("task")) {
                        logger.logp(Level.FINEST,className,"relevantAnswer","task functor gets special handling");
                        boolean matched = packTaskAnswers(intfid, (Object)elem, r, bindings);
                        success = success || matched;
                    } else {
                        logger.logp(Level.FINEST,className,"relevantAnswer","Improper list element for relevance");
                    }
                } else {
                    logger.logp(Level.FINEST,className,"relevantAnswer","Improper list element for relevance");
                }
            }
            return success;
        } else if (!(q instanceof Variable)) {
            logger.logp(Level.FINEST,className,"relevantAnswer","q neither predicate nor variable",q);
            return false;
        } else {
            logger.logp(Level.FINEST,className,"relevantAnswer","q is variable",q);
            f = new Variable("F");
        }
        return checkAnswerDomain(intfid, q, f, r, bindings);
    }
    public boolean checkAnswerType(Object intfid, Object q, String functor, Object value, java.lang.Class attribute, Bindings bindings)
    {
        logger.logp(Level.FINEST,className,"checkAnswerType","functor",functor);
        logger.logp(Level.FINEST,className,"checkAnswerType","value",value);
        logger.logp(Level.FINEST,className,"checkAnswerType","attribute",attribute.getName());
        //System.out.println("checkAnswerType "+functor+" "+value+" "+attribute.getName());
        value = Unify.getInstance().deref(value, bindings);
        if (value instanceof Predicate) {
            if (functor.equals(((Predicate)value).functor())) {
                // the answer is a fully-qualified answer, which includes
                // the functor in question
                // must extract the answer value, dereference it, and check the type
                Variable v = Variable.newVariable();
                ArrayList args = new ArrayList();
                args.add(v);
                Predicate probe = new Predicate(functor, args);
                boolean success = false;
                if (Unify.getInstance().matchTerms(value, probe, bindings)) {
                    value = Unify.getInstance().deref(v, bindings);
                    bindings.reset();
                } else {
                    return false; // didn't unify for whatever reason, so failed.
                }
            } else {
                // the answer has the wrong functor, so cannot be used.
                logger.logp(Level.FINEST,className,"packAnswers","wrong-functor",((Predicate)value).functor());
                return false;
            }
        }
         boolean typeCheck = attribute.isAssignableFrom(value.getClass());
         //System.out.println("checkAnswerType returns "+typeCheck+" for value "+value+" of "+value.getClass());
         return typeCheck;
    }
    /*
     *
     */
    protected boolean checkAnswerDomain(Object intfid, Object q, Object f, Object r, Bindings bindings)
    {
        q = Unify.getInstance().deref(q, bindings);
        f = Unify.getInstance().deref(f, bindings);
        r = Unify.getInstance().deref(r, bindings);
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.DomainImplementation","checkAnswerDomain","question",q);
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.DomainImplementation","checkAnswerDomain","functor",f);
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.DomainImplementation","checkAnswerDomain","answer",r);
        // if r is a variable, return all of the answers
        // otherwise verify that r is a member of the set

        if (!initializedQuestions) initializeQuestions();

        boolean success = false;
        boolean knownQuestionType = false;
        Iterator qit = questionTypes.entrySet().iterator();
        while (qit.hasNext()) {
            Map.Entry me = (Map.Entry)qit.next();
            String question = (String)me.getKey();
            Object questionCategory = me.getValue();
            if (f.equals(question) || (f instanceof Variable)) {
                if (questionCategory instanceof String) {
                    String category = (String)questionCategory;
                    knownQuestionType = true;
                    boolean found =
                            packAnswers(intfid, q, question, r, category, bindings);
                    success = success || found;
                    if (!(f instanceof Variable)) return success;
                } else if (questionCategory instanceof java.lang.Class) {
                    knownQuestionType = true;
                    boolean found =
                            checkAnswerType(intfid, q, question, r, (java.lang.Class)questionCategory, bindings);
                    success = success || found;
                    if (!(f instanceof Variable)) return success;
                } else if (questionCategory == null) {
                    System.out.println("checkAnswerDomain: untyped or unknown attribute "+question);
                } else {
                    // unrecognized attribute type specification
                    System.out.println("checkAnswerDomain: unrecognized attribute type spec, class "+questionCategory.getClass().getName());
                }
            }
        }
        if (!knownQuestionType) {
            // didn't find a matching type for this question.
            // might be a bad question name, might be a different attribute type,
            // or might really be undefined.
            // find the attribute in the/a contract
            // if (attr.hasCompatibleType(r)) { return bound value }
        }
        return success;
    }
    /**
     * Matches questions in the domain to the type of thier answers.
     *
     * @param intfid an <code>Object</code> value
     * @param q an <code>Object</code> value
     * @param r an <code>Object</code> value
     * @param bindings a <code>Bindings</code> value
     */
    public boolean doQuery_domain_relevant_category(
                           Object intfid,
                           Object q,
                           Object r, Bindings bindings)
    {
        q = Unify.getInstance().deref(q, bindings);
        r = Unify.getInstance().deref(r, bindings);
        logger.logp(Level.FINEST,className,"relevantCategory","q-initial",q);
        logger.logp(Level.FINEST,className,"relevantCategory","r-initial",r);
        // tests for type/structure assume that parameters have been dereffed
        // before the routine is called.
        boolean success = false;
        if (q instanceof Predicate) {
            logger.logp(Level.FINEST,className,"relevantCategory","q-is-predicate");
            // q is assumed to be a struct corresponding to a defined question.
            // To extract a category(ies) from an answer, you must first
            // find a relevant question, then the category can be extracted.
            Object attrType = questionTypes.get(((Predicate)q).functor());
            if (attrType==null) {
                logger.logp(Level.FINEST,className,"relevantCategory","q-is-unknown-question-type");
                return false;
            }
            //String attr = (String)attrType;  // may be unadorned java.lang.class
            logger.logp(Level.FINEST,className,"relevantCategory","attrType",attrType);
            Variable v = Variable.newVariable();
            ArrayList al = new ArrayList();
            al.add(v);
            Predicate qVar = new Predicate(((Predicate)q).functor(), al);
            logger.logp(Level.FINEST,className,"relevantCategory","qVar",qVar);
            if (Unify.getInstance().matchTerms(attrType,r,bindings) &&
                Unify.getInstance().matchTerms(q,qVar,bindings)) {
                success = true;
                bindings.reset();
            }
        } else {
            logger.logp(Level.FINEST,className,"relevantCategory","q-is-not-predicate");
            // q is not a predicate. It could be anything, although correct
            // use would stipulate a variable. Loop through all question types
            // and propose unifications.
            Variable w = Variable.newVariable();
            Iterator it = questionTypes.keySet().iterator();
            while (it.hasNext()) {
                String qname = (String)it.next();
                Object questionType = questionTypes.get(qname);
                //String qattr = (String)questionType;
                Variable v = Variable.newVariable();
                ArrayList al = new ArrayList();
                al.add(v);
                Predicate qVar = new Predicate(qname, al);
                logger.logp(Level.FINEST,className,"relevantCategory","qVar",qVar);
                if (Unify.getInstance().matchTerms(questionType,r,bindings) &&
                    Unify.getInstance().matchTerms(q,qVar,bindings)) {
                    success = true;
                    bindings.reset();
                }
            }
        }
        return success;
    }
    /* end for domain_relevant_category */

    public boolean doQuery_domain_abstract(Object intfid,Object a,Object  oldp,Object  q, Bindings bindings)
    {
        // abstract is the inverse of reduce, finding a question from
        // an asserted proposition and an answer. in the simple form
        // used in godis 2.0, all that is checked is functor identity.
        a = Unify.getInstance().deref(a,bindings);
        oldp = Unify.getInstance().deref(oldp,bindings);
        logger.logp(Level.FINEST,className,"abstract","a",a);
        logger.logp(Level.FINEST,className,"abstract","oldp",oldp);
        if (!(a instanceof Predicate)) {
            logger.logp(Level.FINEST,className,"abstract","a not predicate");
            return false;
        }
        if (!(oldp instanceof Predicate)) {
            logger.logp(Level.FINEST,className,"abstract","oldp not predicate");
            return false;
        }
        if (((Predicate)a).functor().equals(((Predicate)oldp).functor())) {
            // make a new predicate using the common funtor and bind to q
            ArrayList al = new ArrayList();
            al.add(Variable.newVariable());
            Predicate pred = new Predicate(((Predicate)a).functor(), al);
            Unify.getInstance().matchTerms(q,pred,bindings);
            bindings.reset();
        }
        return true;
    }
    //
    // relevantTasks must ignore yesno questions when calculating relevance,
    // unless the answer is fully qualified (possibly not then).
    // all other calls that ignore yesno questions have occasion to extract
    // the relevant question; this usage does not, and tasks could be
    // accommodated by mistake.
    protected ArrayList relevantTasks(Object move)
    {
        logger.logp(Level.FINEST,className,"relevantTasks","move",move);
        if (!(move instanceof Predicate)) {
            logger.logp(Level.FINEST,className,"relevantTasks","move is not predicate; cannot be answer");
            return null;  // can't answer anything
        }
        if (!((Predicate)move).functor().equals("answer")) {
            logger.logp(Level.FINEST,className,"relevantTasks","move is not answer()");
            return null;  // not an applicable move
        }
        //System.out.println("DomainImplementation.relevantTasks() running");
        // initialize the working array
        for (int i = 0; i < taskMap.keySet().size(); i++) {
            taskRelevance[i] = false;
        }
        ArrayList retval = new ArrayList();
        Iterator moveArgs = ((Predicate)move).arguments();
        if (!moveArgs.hasNext()) {
            System.out.println("Empty question move "+move+"!");
            return retval;
        }
        Object ques = moveArgs.next();
        // if this is still a struct, it looks like a question.
        // find the data for that question, then check the value against
        // the database.
        if (ques instanceof Predicate) {
            logger.logp(Level.FINEST,className,"relevantTasks","question is predicate");
            //System.out.println("relevantTasks: "+move+" question is predicate");
            String questionFunctor = ((Predicate)ques).functor();
            Object attrType = questionTypes.get(questionFunctor);
            //String attr = (String)attrType;
            String attr = (attrType==null ? "null" : attrType.toString());
            if (questionFunctor.equalsIgnoreCase("task")) {
                // the task functor is automatically relevant, but only to
                // tasks whose name unifies with the probe predicate.
                int tidx = 0;
                Iterator it = taskMap.keySet().iterator();
                while (it.hasNext()) {
                    String taskName = (String)it.next();
                    taskRelevance[tidx] = false;
                    // build task predicate for testing
                    ArrayList al = new ArrayList();
                    al.add(taskName);
                    Predicate taskProbe = new Predicate("task", al);
                    if (Unify.getInstance().unify(ques,taskProbe) != null) {
                        retval.add(taskName);
                    }
                    tidx++;
                }
            } else if (attrType == null) {
                logger.logp(Level.WARNING,className,"relevantTasks","could not get question type for this functor",questionFunctor);
                System.out.println("relevantTasks could not get question type for this functor");
                //return retval;
            } else if (attrType.equals("yesno")) {
                logger.logp(Level.FINEST,className,"relevantTasks","yesno questions can't be checked for relevance");
                return retval;
            }
            Object answerValue = ((Predicate)ques).arguments().next();
            logger.logp(Level.FINEST,className,"relevantTasks","category",attrType);
            logger.logp(Level.FINEST,className,"relevantTasks","question",questionFunctor);
            logger.logp(Level.FINEST,className,"relevantTasks","value",answerValue);
            //System.out.println("relevantTasks: category "+attr);
            //System.out.println("relevantTasks: question "+questionFunctor);
            // for now we assume this is a legal answer value.
            // find all tasks this question addresses.
            //
            int qidx = 0;
            Iterator qtIt = questionTypes.keySet().iterator();
            while (qtIt.hasNext()) {
                String qt = (String)qtIt.next();
                logger.logp(Level.FINEST,className,"relevantTasks","qt",qt);
                Object qtType = questionTypes.get(qt);
                //String qt2 = (String)qtType;
                String qt2 = (qtType==null ? "null" : qtType.toString());
                logger.logp(Level.FINEST,className,"relevantTasks","qtType",qtType);
                if (questionFunctor.equals(qt)) {
                    logger.logp(Level.FINEST,className,"relevantTasks","qt matched question functor");
                    //System.out.println("relevantTasks: "+qt+" matched question functor index "+qidx);
                    // then mark all tasks this question is relevant to
                    for (int i = 0; i < taskMap.keySet().size(); i++) {
                        if (relevance[i][qidx]) taskRelevance[i] = true;
                        //else System.out.println("task "+i+" not relevant");
                    }
                } else if ((attrType != null) && attrType.equals(qtType)) {
                    logger.logp(Level.FINEST,className,"relevantTasks","qtType matched category");
                    //System.out.println("relevantTasks: "+qt2+" matched category");
                    // then mark all tasks this question is relevant to
                    for (int i = 0; i < taskMap.keySet().size(); i++) {
                        if (relevance[i][qidx]) taskRelevance[i] = true;
                    }
                }
                qidx++;
            }
        } else {
            logger.logp(Level.FINEST,className,"relevantTasks","question is not predicate");
            // idea is to identify which attribute(s) this answer can be classified as.
            // from there, cross-reference back into plan set to find relevant task.
            Iterator it = attributeMap.keySet().iterator();
            while (it.hasNext()) {
                String attr = (String)it.next();
                ArrayList attrValues = null;
                try {
                    attrValues = (ArrayList)attributeMap.get(attr);
                } catch (NullPointerException npe) {
                    logger.logp(Level.FINER,className,"relevantTasks","unrecognized attribute",attr);
                    continue;
                } catch (ClassCastException cce) {
                    logger.logp(Level.FINER,className,"relevantTasks","attr value not enum (ArrayList)",attr);
                    continue;
                }
                Iterator valIt = attrValues.iterator();
                while (valIt.hasNext()) {
                    Object cand = valIt.next();
                    if (Unify.getInstance().unify(ques,cand) != null) {
                        // we can classify this value as this attribute.
                        // find all questions this attribute can answer
                        if (attr.equals("yesno")) {
                            logger.logp(Level.FINEST,className,"relevantTasks","value can be yesno; ignoring");
                            return retval;
                        }
                        int qidx = 0;
                        Iterator qtIt = questionTypes.keySet().iterator();
                        while (qtIt.hasNext()) {
                            String qt = (String)qtIt.next();
                            Object qtAttrType = questionTypes.get(qt);
                            //String qtAttr = (String)qtAttrType;
                            if (attr.equals(qtAttrType)) {
                                // then mark all tasks this question is relevant to
                                for (int i = 0; i < taskMap.keySet().size(); i++) {
                                    if (relevance[i][qidx]) taskRelevance[i] = true;
                                }
                            }
                            qidx++;
                        }
                        break;
                    }
                }
            }
        }
        int tidx = 0;
        Iterator it = taskMap.keySet().iterator();
        while (it.hasNext()) {
            String taskName = (String)it.next();
            if (taskRelevance[tidx]) {
                retval.add(taskName);
            }
            tidx++;
        }
        logger.logp(Level.FINEST,className,"relevantTasks","retval",retval);
        //System.out.println("relevantTasks: "+move+" ==> "+retval);
        return retval;
    }
    /**
     * Matches an input move to tasks which have a slot that
     * the move is relevant to. Move must be an input value
     * of answer(X), where:
     *    relevant_answer(Q, X),
     *   !relevant_category(Q, yesno),
     *    has_slot(Task, Q),
     *    has_plan(Task, Plan).
     *
     * @param intfid an <code>Object</code> value
     * @param move an <code>Object</code> value
     * @param task an <code>Object</code> value
     * @param plan an <code>Object</code> value
     * @param bindings a <code>Bindings</code> value
     */
    public boolean doQuery_domain_relevant_to_task(Object intfid,Object move,Object task,Object plan, Bindings bindings)
    {
        move = Unify.getInstance().deref(move,bindings);
        ArrayList tasklist = relevantTasks(move);
        System.out.println("relevant_to_task: move=="+move+", tasklist=="+tasklist);
        boolean success = false;
        Iterator it = tasklist.iterator();
        while (it.hasNext()) {
            String taskName = (String)it.next();
            Object pl = taskMap.get(taskName);
            if (pl != null) {
                if (Unify.getInstance().matchTerms(task,taskName,bindings)) {
                    Unify.getInstance().matchTerms(plan,pl,bindings);
                    bindings.reset();
                    success = true;
                }
            }
        }
        return success;
    }
    /**
     * Matches an input move to tasks which have a slot that
     * the move is relevant to. Move must be an input value
     * of answer(X), where:
     *    relevant_answer(Q, X),
     *   !relevant_category(Q, yesno),
     *    has_slot(Task, Q).
     * Unifies a list, possibly empty, with the 'tasks' parameter.
     * Also wraps each value in a task(X) predicate, which seems
     * an ungainly choice.
     *
     * @param intfid an <code>Object</code> value
     * @param move an <code>Object</code> value
     * @param tasks an <code>Object</code> value
     * @param bindings a <code>Bindings</code> value
     */
    public boolean doQuery_domain_relevant_to_tasks(Object intfid,Object move,Object tasks, Bindings bindings)
    {
        move = Unify.getInstance().deref(move,bindings);
        ArrayList tasklist = relevantTasks(move);
        if (tasklist == null) {
            logger.logp(Level.FINEST,className,"relevantToTasks","tasklist is empty");
            return false;
        }
        ArrayList task_temp = new ArrayList();
        Iterator it = tasklist.iterator();
        while (it.hasNext()) {
            String taskName = (String)it.next();
            ArrayList taskArgs = new ArrayList();
            taskArgs.add(taskName);
            Object taskPredicate = new Predicate("task", taskArgs);
            task_temp.add(taskPredicate);
        }
        if (!task_temp.isEmpty()) {
            Unify.getInstance().matchTerms(tasks,task_temp,bindings);
            logger.logp(Level.FINEST,className,"relevantToTasks","move",move);
            logger.logp(Level.FINEST,className,"relevantToTasks","tasks",tasks);
            logger.logp(Level.FINEST,className,"relevantToTasks","bindings",task_temp);
            bindings.reset();
            return true;
        } else {
            logger.logp(Level.FINEST,className,"relevantToTasks","no matching tasks");
            return false;
        }
    }
    /**
     * Matches the input to the default task, if any.
     *
     * @param intfid an <code>Object</code> value
     * @param task an <code>Object</code> value
     * @param bindings a <code>Bindings</code> value
     */
    public boolean doQuery_domain_default(Object intfid,Object task, Bindings bindings)
    {
        if (defaultTask != null) {
            if (Unify.getInstance().unify(task, defaultTask, bindings) != null) {
                bindings.reset();
                return true;
            }
        }
        return false;
    }
    public boolean doQuery_domain_dominates(Object intfid,Object oldTask,Object task, Bindings bindings)
    {
        oldTask = Unify.getInstance().deref(oldTask,bindings);
        task = Unify.getInstance().deref(task,bindings);
        logger.logp(Level.FINEST,className,"dominates","oldTask",oldTask);
        logger.logp(Level.FINEST,className,"dominates","task",task);
        boolean success = false;
        if (oldTask instanceof Variable) {
            // if oldTask is variable, and task is value, return all true dominations of task
            // if oldTask is variable and task is variable, return all dominations
            if (task instanceof Variable) {
                int oldIdx = 0;
                Iterator it = taskMap.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String)it.next();
                    int newIdx = 0;
                    Iterator it2 = taskMap.keySet().iterator();
                    while (it2.hasNext()) {
                        String key2 = (String)it2.next();
                        try {
                            if (domination[oldIdx][newIdx]) {
                                Unify.getInstance().matchTerms(oldTask,key,bindings);
                                Unify.getInstance().matchTerms(task,key2,bindings);
                                success = true;
                                bindings.reset();
                            }
                        } catch (Exception e) {
                        }
                        newIdx++;
                    }
                    oldIdx++;
                }
            } else {
                int newIdx = -1;
                int idx = 0;
                Iterator it = taskMap.keySet().iterator();
                while (it.hasNext()) {
                    Object key = keyMap.get(it.next());
                    if (Unify.getInstance().unify(task,key) != null) {
                        newIdx = idx;
                    }
                    idx++;
                }
                int oldIdx = 0;
                it = taskMap.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String)it.next();
                    try {
                        if (domination[oldIdx][newIdx]) {
                            Unify.getInstance().matchTerms(oldTask,key,bindings);
                            success = true;
                            bindings.reset();
                        }
                    } catch (Exception e) {
                    }
                    oldIdx++;
                }
            }
        } else {
            // if oldTask is value, and task is variable, return all true dominations by oldTask
            // if oldTask is value, and task is value, check if dominates is true
            if (task instanceof Variable) {
                int oldIdx = -1;
                int idx = 0;
                Iterator it = taskMap.keySet().iterator();
                while (it.hasNext()) {
                    Object key = keyMap.get(it.next());
                    if (Unify.getInstance().unify(oldTask,key) != null) {
                        oldIdx = idx;
                    }
                    idx++;
                }
                int newIdx = 0;
                it = taskMap.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String)it.next();
                    try {
                        if (domination[oldIdx][newIdx]) {
                            Unify.getInstance().matchTerms(task,key,bindings);
                            success = true;
                            bindings.reset();
                        }
                    } catch (Exception e) {
                    }
                    newIdx++;
                }
            } else {
                int oldIdx = -1;
                int newIdx = -1;
                int idx = 0;
                Iterator it = taskMap.keySet().iterator();
                while (it.hasNext()) {
                    Object key = keyMap.get(it.next());
                    if (Unify.getInstance().unify(oldTask,key) != null) {
                        oldIdx = idx;
                    }
                    if (Unify.getInstance().unify(task,key) != null) {
                        newIdx = idx;
                    }
                    idx++;
                }
                try {
                    if (domination[oldIdx][newIdx]) {
                        success = true;
                        bindings.reset();
                    }
                } catch (Exception e) {
                }
            }
        }
        logger.logp(Level.FINEST,className,"dominates","results",(success ? "success" : "failure"));
        return success;
    }
    public boolean doQuery_domain_plan(Object intfid,Object t,Object plan, Bindings bindings)
    {
        t = Unify.getInstance().deref(t,bindings);
        plan = Unify.getInstance().deref(plan,bindings);
        logger.logp(Level.FINEST,className,"plan","task",t);
        logger.logp(Level.FINEST,className,"plan","plan",plan);
        // if t is anything and p is not a variable, can't process this.
        if (!(plan instanceof Variable)) {
            logger.logp(Level.FINEST,className,"plan","'plan' is not a variable");
            System.out.println("domain_plan false 1, "+t+","+plan);
            return false;
        }
        // if t and p are variables, return all tasks, all plans.
        // if t is a value and p is a variable, return matching plan
        if (t instanceof Variable) {
            logger.logp(Level.FINEST,className,"plan","'task' is variable");
            Iterator it = taskMap.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                Object p = taskMap.get(key);
                Unify.getInstance().matchTerms(plan,p,bindings);
                bindings.reset();
            }
            System.out.println("domain_plan true 1, "+t+","+plan);
            return true;
        } else {
            logger.logp(Level.FINEST,className,"plan","'task' is not variable");
            Iterator it = taskMap.keySet().iterator();
            while (it.hasNext()) {
                Object key = keyMap.get(it.next());
                if (Unify.getInstance().unify(key, t) != null) {
                    Object p = taskMap.get(key.toString());
                    Unify.getInstance().matchTerms(plan,p,bindings);
                bindings.reset();
                    System.out.println("domain_plan true 2, "+t+","+plan+"=="+p);
                    //throw new RuntimeException("domain_plan true 2");
                    return true;
                }
            }
        }
        System.out.println("domain_plan false 2, "+t+","+plan);
        return false;
    }
    public boolean doQuery_domain_sysaction(Object intfid,Object a, Bindings bindings)
    {
        return false;
    }
    public boolean doQuery_domain_all_answers(Object intfid,Object question,Object answs, Bindings bindings)
    {
        question = Unify.getInstance().deref(question,bindings);
        if (!(question instanceof Predicate)) return false;
        Predicate pQuestion = (Predicate)question;
        String functor = pQuestion.functor();
        // if functor is task, get list of all tasks.
        if (functor.equalsIgnoreCase("task")) {
            LinkedList tasklist = new LinkedList();
            Iterator it = taskMap.keySet().iterator();
            while (it.hasNext()) {
                Object key = keyMap.get(it.next());
                tasklist.add(key);
            }
            boolean success = Unify.getInstance().matchTerms(answs,tasklist,bindings);
            bindings.reset();
            return success;
        }
        // if functor is a known question, get all answers for that question.
        Object attrType = questionTypes.get(functor);
        //String attr = (String)attrType;
        Object tupleRef = attributeMap.get(attrType);
        if (tupleRef != null) {
            LinkedList tuples = new LinkedList((Collection)tupleRef);
            boolean success = Unify.getInstance().matchTerms(answs,tuples,bindings);
            bindings.reset();
            return success;
        }
        // otherwise return false.
        return false;
    }

    public boolean doExec_domain_instantiate_plan(
                           Object intfid,
                           Object code,
                           Object planid, Bindings bindings) {
        code = Unify.getInstance().deref(code, bindings);
        planid = Unify.getInstance().deref(planid, bindings);
        PlanInstance plan = null;
        String key = "";
        if (planid instanceof Variable) {
            key = "planinstance"+nextPlanId;
            nextPlanId++;
        } else {
            key = planid.toString();
        }
        if (code instanceof List) {
            logger.logp(Level.FINEST,className,"instantiatePlan","creating plan", code);
            plan = new PlanInstance((Collection)code, key);
        } else if (code instanceof Plan) {
            logger.logp(Level.FINEST,className,"instantiatePlan","creating plan", code);
            plan = new PlanInstance((Plan)code, key);
        } else {
            // can't process a plan that isn't a list of task ops.
            logger.logp(Level.WARNING,className,"instantiatePlan","format failure: "+code.getClass().getName(), code.getClass().getName());
            return false;
        }
        planMap.remove(key);
        planMap.put(key,plan);
        if (stackHoldsPlanInstances) {
            Unify.getInstance().matchTerms(planid,plan,bindings);
        } else {
            Unify.getInstance().matchTerms(planid,key,bindings);
        }
        return true;
    }
    /* end for domain_instantiate_plan */

    protected PlanInstance getPlanInstance(Object plan, Bindings bindings) {
        PlanInstance planInstance = null;
        if (stackHoldsPlanInstances) {
           planInstance = (PlanInstance)plan;
        } else {
            String key = plan.toString();
            Object planobj = planMap.get(key);
            if (planobj == null) {
                logger.logp(Level.WARNING,className,"nextStep","plan instance not found",plan);
                return null;
            }
            planInstance = (PlanInstance)planobj;
        }
        return planInstance;
    }

    public boolean doQuery_domain_next_step(
                           Object  intfid,
                           Object plan,
                           Object step, Bindings bindings)
    {
        logger.logp(Level.FINEST,className,"nextStep","plan-instance", plan);
        logger.logp(Level.FINEST,className,"nextStep","step-template", step);
        if (plan == null) {
            logger.logp(Level.FINEST,className,"nextStep","plan is null");
            return false;
        }
        PlanInstance planInstance = getPlanInstance(plan, bindings);
        Object pc = planInstance.next_step();
        logger.logp(Level.FINEST,className,"nextStep","next-step", pc);
        if (!Unify.getInstance().matchTerms(pc, step, bindings)) {
            logger.logp(Level.FINEST,className,"nextStep","next step did not match template");
            return false;
        }
        bindings.reset();
        return (true);
    }
    /* end for domain_next_step */

    public boolean doExec_domain_advance(
                           Object intfid,
                           Object plan,
                           Object annotations, Bindings bindings) {
        PlanInstance planInstance = getPlanInstance(plan, bindings);
        planInstance.advance();
        Unify.getInstance().matchTerms(annotations, planInstance.getRecentAnnotations(), bindings);
        return true;
    }
    /* end for domain_advance */

    public boolean doExec_domain_reset(
                           Object intfid,
                           Object plan, Bindings bindings) {
        PlanInstance planInstance = getPlanInstance(plan, bindings);
        planInstance.reset();
        return true;
    }
    /* end for domain_reset */

    public boolean doExec_domain_set_answer(
                           Object intfid,
                           Object plan,
                           Object answer,
                           Object template, Bindings bindings) {
        plan = Unify.getInstance().deref(plan,bindings);
        PlanInstance planInstance = getPlanInstance(plan, bindings);
        answer = Unify.getInstance().deref(answer,bindings);
        if (template != null) {
            template = Unify.getInstance().deref(template,bindings);
            if (!(template instanceof Variable)) {
            boolean bound =
                Unify.getInstance().matchTerms(answer,template,planInstance.planBindings);
            logger.logp(Level.FINEST,className,"setAnswer","template",template);
            } else {
                // create a dummy variable and ask want_answer
                template = planInstance.wants_answer(answer);
                if (template != null) {
                    boolean bound =
                        Unify.getInstance().matchTerms(answer,template,planInstance.planBindings);
                    logger.logp(Level.FINEST,className,"setAnswer","implicit template",template);
                } else {
                    logger.logp(Level.FINEST,className,"setAnswer","plan doesn't want this answer");
                }
            }
        } else {
            // create a dummy variable and ask want_answer
            template = planInstance.wants_answer(answer);
            if (template != null) {
                boolean bound =
                    Unify.getInstance().matchTerms(answer,template,planInstance.planBindings);
                logger.logp(Level.FINEST,className,"setAnswer","implicit template",template);
            }
        }
        logger.logp(Level.FINEST,className,"setAnswer","plan",planInstance.theKey);
        logger.logp(Level.FINEST,className,"setAnswer","answer",answer);
        planInstance.set_answer(answer);
        return true;
    }
    /* end for domain_set_answer */

    public boolean doQuery_domain_get_answer(
                           Object  intfid,
                           Object plan,
                           Object question, Bindings bindings)
    {
        plan = Unify.getInstance().deref(plan,bindings);
        PlanInstance planInstance = getPlanInstance(plan, bindings);
        question = Unify.getInstance().deref(question,bindings);
        Object answer = planInstance.get_answer(question);
        if (answer != null) {
            boolean bound =
                Unify.getInstance().matchTerms(answer,question,bindings);
        }
        logger.logp(Level.FINEST,className,"getAnswer","plan",planInstance.theKey);
        logger.logp(Level.FINEST,className,"getAnswer","answer",answer);
        return true;
    }
    /* end for domain_get_answer */

    public boolean doExec_domain_retract_answer(
                           Object intfid,
                           Object plan,
                           Object question, Bindings bindings) {
        plan = Unify.getInstance().deref(plan,bindings);
        PlanInstance planInstance = getPlanInstance(plan, bindings);
        question = Unify.getInstance().deref(question,bindings);
        planInstance.retract_answer(question);
        logger.logp(Level.FINEST,className,"retractAnswer","plan",planInstance.theKey);
        logger.logp(Level.FINEST,className,"retractAnswer","answer",question);
        return true;
    }
    /* end for domain_retract_answer */

    public boolean doQuery_domain_wants_answer(
                           Object  intfid,
                           Object plan,
                           Object question,
                           Object template, Bindings bindings)
    {
        plan = Unify.getInstance().deref(plan, bindings);
        question = Unify.getInstance().deref(question, bindings);
        PlanInstance planInstance = getPlanInstance(plan, bindings);
        Object wantsAnswer = planInstance.wants_answer(question);
        logger.logp(Level.FINEST,className,"wantsAnswer",((wantsAnswer!=null) ? "wants answer for" : "does not want answer for"),question);
        if (template != null) {
            Unify.getInstance().matchTerms(template,wantsAnswer,bindings);
            bindings.reset();
        }
        return (wantsAnswer!=null);
    }

    public boolean doQuery_domain_plan_task(
                           Object  intfid,
                           Object plan,
                           Object task,
                           Bindings bindings)
    {
        plan = Unify.getInstance().deref(plan, bindings);
        task = Unify.getInstance().deref(task, bindings);
        // currently assumes we have a plan and want to get name.
        // should really be invertible.
        PlanInstance planInstance = getPlanInstance(plan, bindings);
        Object taskName = planInstance.getTaskName();
        if (taskName != null) {
            ArrayList al = new ArrayList();
            al.add(taskName);
            Predicate taskPred = new Predicate("task", al);
            Unify.getInstance().matchTerms(task,taskPred,bindings);
            bindings.reset();
            return true;
        }
        return false;
    }

    /* end for domain_wants_answer */

    /*
     * Building a domain.
     *
     * 1. Specify possible tasks
     * 2. Specify questions (slots) for tasks
     *    (a question is an arity/1 predicate, whose name is
     *     unique within a task.)
     *    (questions may be mapped to tabular data, e.g. java.sql
     *     tables, such that a question --> table.column.)
     * 3. Specify services provided by those tasks, if any
     *    (the service is a method call that takes an action
     *     and which may also bind variables.)
     * 4. Specify possible answers for those questions
     * 5. Specify plans to answer those questions
     *    (order of acquiring data, social boilerplate,
     *     actions taken beyond acquisition, linkages
     *     between tasks.)
     */
    /*
     * Specifying a service
     * - contract name as known to this agent
     * - Contract.Method for the method
     * - Map linking names of parameters to names of questions.
     *   Parameters that don't match questions are assumed to be
     *   output from the method call.
     */
}
