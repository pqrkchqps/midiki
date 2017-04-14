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
package org.mitre.midiki.logic;

import java.util.*;
import java.util.logging.*;

/**
 * The Unify class provides methods for the unification of 
 * expressions and the substitution of values for variables in expressions.
 * This code is adapted from LISP code which contained the following comment:
 *
 * <pre>
 *   Unification and Substitutions (aka Binding Lists)
 *
 *   This code is borrowed from "Paradigms of AI Programming: Case Studies
 *   in Common Lisp", by Peter Norvig, published by Morgan Kaufmann, 1992.
 *   The complete code from that book is available for ftp at mkp.com in
 *   the directory "pub/Norvig".  Note that it uses the term "bindings"
 *   rather than "substitution" or "theta".  The meaning is the same.
 * </pre>
 *
 * As specified at the author's website (www.norvig.com), that code is
 * open source freeware available under <a href="http://www.norvig.com/license.html">this license.</a>
 * For convenience, a copy of that license can be found in the root directory
 * of this distribution. Modified to fit the Midiki API by Carl Burke.
 *
 * @author Carl D. Burke
 * @author Michael S. Braverman
 * @author Peter Norvig
 * @see Variable
 * @see Bindings
 */

public class Unify implements Unifier
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.logic.Unify");
    /**
     * Returns a new term which is the unification of the
     * two terms. If any variables must be bound for this
     * to take place, they are only bound long enough to
     * generate a result.
     *
     * The Norvig name for this function is <code>unifier</code>.
     *
     * @param term1 an <code>Object</code> value
     * @param term2 an <code>Object</code> value
     * @return an <code>Object</code> value
     */
    public Object unify(Object term1,
                        Object term2)
    {
        return unify(term1, term2, new MinimalBindings());
    }
    /**
     * Returns a new term which is the unification of the
     * two terms, using the existing variable bindings and
     * storing any additional bindings required.
     *
     * The Norvig name for this function is <code>unifier</code>.
     *
     * @param term1 an <code>Object</code> value
     * @param term2 an <code>Object</code> value
     * @param bindings a <code>Bindings</code> value
     * @return an <code>Object</code> value
     */
    public Object unify(Object term1,
                        Object term2,
                        Bindings bindings)
    {
        if (matchTerms(term1, term2, bindings)) {
            return deref(term1, bindings);
        } else {
            return null;
        }
    }
    /**
     * Compares the two terms, generating candidate variable
     * bindings that would allow them to unify. Modifies initial
     * bindings with additions that make the input parameters match.
     *
     * The Norvig name for this function is <code>unify</code>.
     *
     * @param term1 an <code>Object</code> to unify
     * @param term2 an <code>Object</code> to unify
     * @param bindings an initial set of <code>Bindings</code>
     * @return <code>true</code> if the terms can be unified
     */
    public boolean matchTerms(Object term1,
                              Object term2,
                              Bindings bindings)
    {
        if (bindings == null) {
            return false;
        } else if (term1 == null) {
            return (term1==term2);
        } else if (term1.equals(term2)) {
            return true;
        } else if (term1 instanceof Variable) {
            return unifyVar((Variable) term1,term2,bindings);
        } else if (term2 instanceof Variable) {
            return unifyVar((Variable) term2,term1,bindings);
        } else if (term1 instanceof Predicate) {
            if (!(term2 instanceof Predicate)) return false;
            Predicate pterm1 = (Predicate)term1;
            Predicate pterm2 = (Predicate)term2;
            if (!pterm1.functor().equals(pterm2.functor())) return false;
            boolean unified = true;
            Iterator it1 = pterm1.arguments();
            Iterator it2 = pterm2.arguments();
            while (unified && it1.hasNext() && it2.hasNext()) {
                Object obj1 = it1.next();
                Object obj2 = it2.next();
                unified = unified && matchTerms(obj1, obj2, bindings);
            }
            if (it1.hasNext() != it2.hasNext()) unified = false;
            return unified;
        } else if ((term1 instanceof Collection) &&
                   (term2 instanceof Collection) &&
                   (term1.getClass().equals(term2.getClass()))) {
            boolean unified = true;
            Iterator it1 = ((Collection)term1).iterator();
            Iterator it2 = ((Collection)term2).iterator();
            while (unified && it1.hasNext() && it2.hasNext()) {
                Object obj1 = it1.next();
                Object obj2 = it2.next();
                unified = unified && matchTerms(obj1, obj2, bindings);
            }
            if (it1.hasNext() != it2.hasNext()) unified = false;
            return unified;
        } else {
            return false;
        }
    }
    /**
     * Generates a copy of the logical term after substituting
     * all applicable variable bindings into the original term.
     * Note: this does not necessarily generate a copy. A copy
     * is only generated if there are variable bindings that affect
     * the value of the expression.<p>
     *
     * The Norvig name for this function is <code>substBindings</code>.
     *
     * @param term an <code>Object</code> value
     * @param bindings a <code>Bindings</code> value
     * @return an <code>Object</code> value
     */
    public Object deref(Object term,
                        Bindings bindings)
    {
        if (bindings == null) {
            return term;
        } else if (bindings.isEmpty()) {
            return term;
        } else if ((term instanceof Variable) && 
                   (bindings.get((Variable) term) != null)) {
            Object tgt = bindings.get((Variable) term);
            if (term.equals(tgt)) {
                logger.logp(Level.FINER,"org.mitre.midiki.logic.Unify","deref","deref var to itself trivial success");
                return term;
            } else {
                logger.logp(Level.FINER,"org.mitre.midiki.logic.Unify","deref","term",term);
                logger.logp(Level.FINER,"org.mitre.midiki.logic.Unify","deref","tgt",tgt);
            }
            return deref(tgt,
                         bindings);
        } else if (term instanceof Predicate) {
            Predicate p = (Predicate)term;
            LinkedList newArgs = new LinkedList();
            Iterator it = p.arguments();
            while (it.hasNext()) {
                newArgs.add(deref(it.next(), bindings));
            }
            return new Predicate(p.functor(), newArgs);
        } else if (term instanceof Collection) {
            Class classX = term.getClass();
            Collection collX = (Collection)term;
            Collection newCollX = null;
            try {
                newCollX = (Collection)classX.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage());
            }
            Iterator it = collX.iterator();
            while (it.hasNext()) {
                newCollX.add(deref(it.next(), bindings));
            }
            return newCollX;
        } else {
            return term;
        } 
    }

    private class MinimalBindings extends HashMap implements Bindings
    {
        public MinimalBindings()
        {
            super();
        }
        /**
         * Describe <code>get</code> method here.
         *
         * @param variable a <code>String</code> value
         * @return an <code>Object</code> value
         */
        public Object get(String variable)
        {
            return super.get(variable);
        }
        /**
         * Describe <code>get</code> method here.
         *
         * @param variable a <code>Variable</code> value
         * @return an <code>Object</code> value
         */
        public Object get(Variable variable)
        {
            return get(variable.name());
        }
        /**
         * Unbinds any variables bound by the current context and attempts to
         * rebind them to the next alternative. Fails if there are no remaining
         * alternatives.
         *
         * @return <code>true</code> if another alternate binding is available
         */
        public boolean backtrack()
        {
            // noop
            return false;
        }
        /**
         * Marks the start of a new binding scope.
         *
         * @return a <code>boolean</code> value
         */
        public boolean enterScope()
        {
            // noop
            return false;
        }
        /**
         * Marks the end of a binding scope. Any variables bound since the
         * last call to <code>enterScope</code> will be unbound, and the
         * scope stack will be popped.
         *
         * @return a <code>boolean</code> value
         */
        public boolean exitScope()
        {
            // noop
            return false;
        }
        /**
         * Resets all variable bindings for the current scope,
         * defining the start of a new alternative solution.
         *
         */
        public void reset()
        {
            // noop
        }
        public Collection marshalAll()
        {
            logger.logp(Level.WARNING,"org.mitre.midiki.logic.Unify.MinimalBindings","marshalAll","unimplemented");
            return null;
        }
        public Collection marshalLatest()
        {
            logger.logp(Level.WARNING,"org.mitre.midiki.logic.Unify.MinimalBindings","marshalLatest","unimplemented");
            return null;
        }
        public void unmarshalAll(Collection c)
        {
            logger.logp(Level.WARNING,"org.mitre.midiki.logic.Unify.MinimalBindings","unmarshalAll","unimplemented");
        }
        public void unmarshalLatest(Collection c)
        {
            logger.logp(Level.WARNING,"org.mitre.midiki.logic.Unify.MinimalBindings","unmarshalLatest","unimplemented");
        }
    }

    /** 
     * Determines if the given <code>Variable</code> occurs anywhere inside
     * the given expression, subject to the bindings contained in the given 
     * <code>Bindings</code> object.
     *
     * @return true iff <code>var</code> is contained is <code>x</code> subject
     * to <code>bindings</code>
     * @param var <code>Variable</code> for which to search.
     * @param x An expression (atomic or compound) to search.
     * @param bindings An set of bindings on the variables in <code>x</code>
     * @see Bindings
     * @see Variable
     */
    /*
      (defun occurs-in? (var x bindings)
      "Does var occur anywhere inside x?"
      (cond ((eq var x) t)
        ((and (variable? x) (get-binding x bindings))
         (occurs-in? var (lookup x bindings) bindings))
        ((consp x) (or (occurs-in? var (first x) bindings)
                       (occurs-in? var (rest x) bindings)))
        (t nil)))
    */
    public boolean occursIn(Variable var, Object x, 
                            Bindings bindings) {
        if (var.equals(x)) {
            return true;
        } else if ((x instanceof Variable) &&
                   (bindings.get((Variable) x) != null)) {

            if (bindings.get((Variable) x).equals(x)) {
                logger.logp(Level.FINER,"org.mitre.midiki.logic.Unify","occursIn","occursIn check for var bound to itself");
                return true;
            }

            return occursIn(var, bindings.get((Variable) x), 
                            bindings);
        } else if (x instanceof Predicate) {
            boolean occurs = false;
            Iterator it = ((Predicate)x).arguments();
            while (it.hasNext()) {
                occurs = occurs || occursIn(var, it.next(), bindings);
            }
            return occurs;
        } else if (x instanceof Collection) {
            boolean occurs = false;
            Iterator it = ((Collection)x).iterator();
            while (it.hasNext() && !occurs) {
                occurs = occurs || occursIn(var, it.next(), bindings);
            }
            return occurs;
        } else {
            return false;
        }
    }
  
    /** 
     * Creates an isomorphic expression to the given expression in which all
     * the variables have been replaced with new ones.  If a variable appears
     * more than once in the expression, then it is given the same replacement
     * at each position in which it occurs.
     *
     * @return An isomorphic expression with new <code>Variables</code>.
     * @param x An expression (atomic or compound) to process.
     * @see Variable
     */
    /*
     * (defun rename-variables (x)
     *   "Replace all variables in x with new ones."
     *   (sublis (mapcar #'(lambda (var) (make-binding var (new-variable var)))
     *    (variables-in x))
     *  x))
     */
    public Object renameVariables(Object x) {
        // I didn't want to reimplement sublis right now, so instead I build
        // a map from existing variables to new variables, and then just use
        // deref to rename the variables in the Object passed.
        Bindings b = buildNewVarMap(x, new MinimalBindings());
        return deref(x, b);
    }

    /** 
     * Unifies the given <code>Variable</code> with the given expression,
     * using (and maybe extending) the given <code>Bindings</code> object.
     *
     * @return A <code>Bindings</code> object that would make the
     * variable match the expression, or the <code>Bindings</code> object that
     * represents failure if they do not match.
     * @param var A <code>Variable</code> to unify.
     * @param x An expression (atomic or compound) to unify.
     * @param bindings An initial set of bindings to begin with.
     * @see Bindings
     * @see Variable
     */
    /*
     * (defun unify-var (var x bindings)
     *   "Unify var with x, using (and maybe extending) bindings [p 303]."
     *   (cond ((get-binding var bindings)
     *          (unify (lookup var bindings) x bindings))
     *         ((and (variable? x) (get-binding x bindings))
     *          (unify var (lookup x bindings) bindings))
     *         ((occurs-in? var x bindings)
     *          +fail+)
     *         (t (extend-bindings var x bindings))))
     */
    private boolean unifyVar(Variable var,Object x,
                             Bindings bindings) {
        Object value = bindings.get(var);
        if (var.equals(value)) {
            return true;
        } else if (value != null) {
            return matchTerms(value, x, bindings);
        } else if ((x instanceof Variable) &&
                   (bindings.get((Variable) x) != null)) {
            return matchTerms(var, bindings.get((Variable) x), bindings);
        } else if (occursIn(var,x,bindings)) {
            return false;
        } else {
            // this is the one place that variables are bound to values.
            // test the constraint on var. if x is also an unbound variable,
            // test its constraint. if constraints are to be propagated,
            // this is where that propagation must happen. (I haven't yet
            // checked the algorithm to discover if it ensures testing
            // of constraints, or where else that must occur.)
            if (var.isConstrained()) {
                if (!var.constraint().test(x,bindings)) return false;
            }
            if (x instanceof Variable) {
                if (((Variable)x).isConstrained()) {
                    if (!((Variable)x).constraint().test(var,bindings))
                        return false;
                }
            }
            bindings.put(var, x);
            return true;
        }
    }

    /** 
     * Searches the given (compound or atomic) expression to find all the
     * <code>Variable</code>s it contains and returns a <code>Bindings</code>
     * object mapping from each <code>Variable</code> in the expression to 
     * a new, previously non-existant, <code>Variable</code>.
     *
     * @return A <code>Bindings</code> map from old to new 
     * <code>Variable</code>s.
     * @param x An expression (atomic or compound) to process.
     * @param varMap A mapping from old to new <code>Variable</code>s to extend.
     * @see Bindings
     * @see Variable
     */
    private Bindings buildNewVarMap(Object x, Bindings varMap) {
        if (x instanceof Variable) {
            if (varMap.get((Variable) x) == null) {
                Variable xvar = (Variable)x;
                if (xvar.isConstrained()) {
                    varMap.put(xvar, Variable.newVariable(xvar.constraint()));
                } else {
                    varMap.put(xvar, Variable.newVariable());
                }
            }
        } else if (x instanceof Predicate) {
            Iterator it = ((Predicate)x).arguments();
            while (it.hasNext()) {
                varMap = buildNewVarMap(it.next(), varMap);
            }
        } else if (x instanceof Collection) {
            Iterator it = ((Collection)x).iterator();
            while (it.hasNext()) {
                varMap = buildNewVarMap(it.next(), varMap);
            }
        }
        return varMap;
    }

    /**
     * Placeholder to keep Unify objects from being created.
     */
    private Unify() {
        // This keeps Unify objects from being created.
    }

    static Unify theInstance;
    static public Unify getInstance()
    {
        if (theInstance == null) {
            theInstance = new Unify();
        }
        return theInstance;
    }
    static {
        theInstance = null;
    }
}

