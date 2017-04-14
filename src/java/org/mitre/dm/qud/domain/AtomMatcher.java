/****************************************************************************
 *
 * Copyright (C) 2005. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 * Consult the LICENSE file in the root of the distribution for terms and restrictions.
 *
 *       Release: 1.0
 *       Date: 13-January-2005
 *       Author: Carl Burke
 *
 *****************************************************************************/
package org.mitre.dm.qud.domain;

import java.util.*;
import org.mitre.midiki.logic.*;
import java.util.logging.*;

/**
 * Interface for a <code>Matcher</code> that accepts an atom (Object)
 * as input.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class AtomMatcher implements Matcher
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.domain.AtomMatcher");
    /**
     * Retrieve any application-specific tags which might be used
     * in conjunction with this matcher. Typically used to help decide
     * whether this matcher should be applied to a particular input.
     *
     * @return an <code>Object</code> value
     */
    public Object getTags()
    {
        return _tags;
    }
    /**
     * Get the <code>MatchSet</code> this <code>Matcher</code>
     * is defined in.
     *
     * @return a <code>MatchSet</code> value
     */
    public MatchSet getOwningSet()
    {
        return null;
    }
    /**
     * Set the <code>MatchSet</code> this <code>Matcher</code>
     * is defined in.
     *
     * @param set a <code>MatchSet</code> value
     */
    public void setOwningSet(MatchSet set)
    {
        _owningSet = set;
    }


    protected MatchSet _owningSet;
    protected LinkedList _tags;
    protected LinkedList _input;
    protected LinkedList _output;
    protected Tokenizer _tokenizer;

    public AtomMatcher(MatchSet set)
    {
        _owningSet = set;
        _tags = new LinkedList();
        _input = new LinkedList();
        _output = new LinkedList();
    }

    public LinkedList tags() {return _tags;}
    public LinkedList input() {return _input;}
    public LinkedList output() {return _output;}
    public Tokenizer getTokenizer() {return _tokenizer;}
    public void setTokenizer(Tokenizer t) {_tokenizer = t;}

    public void addTag(Object tag)
    {
        _tags.add(tag);
    }

    public void addInputString(String string)
    {
        _input.add(string);
    }

    public void addOptionalInputString(String string)
    {
        ArrayList al = new ArrayList();
        al.add(string);
        _input.add(new Predicate("optional", al));
    }

    public void addOutputString(String string)
    {
        _output.add(string);
    }

    public void addInputTokens(String string)
    {
        _input.addAll(_tokenizer.tokenize(string));
    }

    public void addOptionalInputTokens(String string)
    {
        _input.add(new Predicate("optional", _tokenizer.tokenize(string)));
    }

    public void addOutputTokens(String string)
    {
        _output.addAll(_tokenizer.tokenize(string));
    }

    public void addInputVariable(String string)
    {
        _input.add(new Variable(string));
    }

    public void addTypedInputVariable(String string, String typeName)
    {
        // need a type constraint to be instantiated here
        // note that this seems a lot like the nestedMatch functionality,
        // but fits better with the use of the API.
        ArrayList al = new ArrayList();
        al.add(new Variable(string));
        al.add(typeName);
        _input.add(new Predicate("typed", al));
    }

    //
    // A useful capability is the ability to specify optional sequences.
    // Perhaps as a typed input variable with defaults.
    // An intriguing approach to the solution.
    public void addTypedDefaultInputVariable(String string,
                                             String typeName,
                                             Object defaultPredicate)
    {
        // need a type constraint to be instantiated here
        // note that this seems a lot like the nestedMatch functionality,
        // but fits better with the use of the API.
        // note that if unification with the constrained variable fails,
        // the default value will be bound instead. this requires additional
        // mechanisms beyond the constraint, but not a sophisticated one.
        //
        // this could cause a problem with backtracking; basically, there is
        // no failure from this node, so your grammar has to be tested.
        ArrayList al = new ArrayList();
        al.add(new Variable(string));
        al.add(typeName);
        al.add(defaultPredicate);
        _input.add(new Predicate("typed", al));
    }

    public void addOutputVariable(String string)
    {
        _output.add(new Variable(string));
    }

    public void addInputBoundVariable(String string)
    {
        _input.add(new Variable(string,new BoundConstraint()));
    }

    public void addOutputBoundVariable(String string)
    {
        _output.add(new Variable(string,new BoundConstraint()));
    }

    public void addInputPredicate(String string)
    {
        ArrayList args = new ArrayList();
        _input.add(new Predicate(string, args));
    }

    public void addOutputPredicate(String string)
    {
        ArrayList args = new ArrayList();
        _output.add(new Predicate(string, args));
    }

    public void addInputPredicate(String string, Object arg1)
    {
        ArrayList args = new ArrayList();
        args.add(arg1);
        _input.add(new Predicate(string, args));
    }

    public void addOutputPredicate(String string, Object arg1)
    {
        ArrayList args = new ArrayList();
        args.add(arg1);
        _output.add(new Predicate(string, args));
    }

    public void addInputNestedPredicate(String string, String pred2, Object arg1)
    {
        ArrayList args = new ArrayList();
        args.add(arg1);
        Predicate pred = new Predicate(pred2, args);
        ArrayList args2 = new ArrayList();
        args2.add(pred);
        _input.add(new Predicate(string, args2));
    }

    public void addOutputNestedPredicate(String string, String pred2, Object arg1)
    {
        ArrayList args = new ArrayList();
        args.add(arg1);
        Predicate pred = new Predicate(pred2, args);
        ArrayList args2 = new ArrayList();
        args2.add(pred);
        _output.add(new Predicate(string, args2));
    }

    public void addInputPredicate(String string, Object arg1, Object arg2)
    {
        ArrayList args = new ArrayList();
        args.add(arg1);
        args.add(arg2);
        _input.add(new Predicate(string, args));
    }

    public void addOutputPredicate(String string, Object arg1, Object arg2)
    {
        ArrayList args = new ArrayList();
        args.add(arg1);
        args.add(arg2);
        _output.add(new Predicate(string, args));
    }
    /*
     * in order to process _nestedMatch on input, need some mechanism
     * for recursion. should be like either doInput() or doOutput().
     * doOutput() gets a term which is unified with current bindings
     * before being matched; the equivalent on input should take a
     * variable, possibly constrained, and will bind the results of
     * any recursive match (starting at that point) to that variable.
     * Possibly simpler approach would be to supply any generic term
     * which must unify in order to match.
     *
     * Parameter signature will need to change to support this.
     */
    public void addInputAlternatives(String string, String alt1, String alt2)
    {
        ArrayList alts = new ArrayList();
        alts.add(alt1);
        alts.add(alt2);
        ArrayList args = new ArrayList();
        args.add(new Variable(string));
        args.add(alts);
        _input.add(new Predicate("_nestedMatch", args));
    }

    public void addOutputAlternatives(String string, String alt1, String alt2)
    {
        ArrayList alts = new ArrayList();
        alts.add(alt1);
        alts.add(alt2);
        ArrayList args = new ArrayList();
        args.add(new Variable(string));
        args.add(alts);
        _output.add(new Predicate("_nestedMatch", args));
    }

    public void addInputMove(Object inputItem)
    {
        _input.add(inputItem);
    }

    public void addOutputMove(Object outputItem)
    {
        _output.add(outputItem);
    }

    public void addFilteredOutputMove(Object outputItem, Filter filter)
    {
        ArrayList args = new ArrayList();
        args.add(outputItem);
        args.add(filter);
        _output.add(new Predicate("_filter", args));
    }

    public void setInput(List inputItems)
    {
        _input.addAll(inputItems);
    }

    public void setOutput(Collection outputItems)
    {
        _output.addAll(outputItems);
    }

    public String toString()
    {
        return "Match(tags="+tags()+",input="+input()+",output="+output()+")";
    }

    /**
     * Attempts to match the input item, adding any resulting
     * interpretations to the existing results.
     *
     * @param interpretation an <code>Object</code> to be matched.
     * Implementing classes may expect specific subclasses as input.
     * @param interpretations a <code>Collection</code> of values
     * transduced from the input.
     * @return <code>true</code> if this match succeeded.
     */
    public boolean match(Object interpretation,
                         Collection interpretations)
    {
        Object outputPattern = input();
        List outputSequence = output();
        Bindings bindings = new BindingsImpl();

        //System.out.println("matching "+interpretation+" to "+outputPattern);
        if (outputPattern == null) {
            // the result is a concatenation without any testing.
        } else {
            if (outputPattern instanceof List) {
                List outputPatList = (List)outputPattern;
                if (outputPatList.isEmpty()) {
                    outputPattern = "";
                } else {
                    outputPattern = outputPatList.get(0);
                }
            }
            if (!Unify.getInstance().matchTerms(interpretation, outputPattern, bindings)) {
                //System.out.println("failed");
                return false;
            }
        }
        outputSequence = (List)Unify.getInstance().deref(outputSequence, bindings);
        // the output predicate was matched. Assemble the result string.
        if (!(outputSequence instanceof List)) {
            interpretations.add(outputSequence);
            return true;
        }
        int childCount = outputSequence.size();
        for (int i=0; i < childCount; i++) {
            // emit the next value in the sequence
            Object nextOutput = outputSequence.get(i);
            nextOutput = Unify.getInstance().deref(nextOutput, bindings);
            if (!(nextOutput instanceof Predicate)) {
                interpretations.add(nextOutput);
            } else {
                if (((Predicate)nextOutput).functor().equals("_conjoinedText")) {
                    /*
                     * Building a conjunction.
                     * Argument 1: list of terms.
                     * Argument 2: first coordinator, separating list elements.
                     * Argument 3: final coordinator, separating last two list elements (optional).
                     */
                    Iterator argIt = ((Predicate)nextOutput).arguments();
                    Object argTerm = argIt.next();
                    if (argTerm instanceof List) {
                        String sep1 = "";
                        String sep2 = "";
                        if (argIt.hasNext()) {
                            sep1 = (String)argIt.next();
                        }
                        if (argIt.hasNext()) {
                            sep2 = (String)argIt.next();
                        }
                        conjoinedTerms(argTerm, sep1, sep2, interpretations);
                    } else {
                        interpretations.add(argTerm);
                    }
                } else if (((Predicate)nextOutput).functor().equals("_nestedMatch")) {
                    Iterator argIt = ((Predicate)nextOutput).arguments();
                    Object argTerm = argIt.next();
                    Object separators = null;
                    String sep1 = "";
                    String sep2 = "";
                    if (argIt.hasNext()) {
                        separators = argIt.next();
                        if (separators != null) {
                            Iterator sepIt = ((List)separators).iterator();
                            if (sepIt.hasNext()) sep1 = (String)sepIt.next();
                            if (sepIt.hasNext()) sep2 = (String)sepIt.next();
                        }
                    }
                    nestedMatch(argTerm, sep1, sep2, interpretations);
                } else if (((Predicate)nextOutput).functor().equals("_filter")) {
                    Iterator argIt = ((Predicate)nextOutput).arguments();
                    Object argTerm = argIt.next();
                    Object filter = null;
                    if (argIt.hasNext()) {
                        filter = argIt.next();
                        if (filter != null) {
                            nextOutput = ((Filter)filter).filter(argTerm);
                            interpretations.add(nextOutput);
                        }
                    }
                } else {
                    interpretations.add(nextOutput);
                }
            }
        }
        return true;
    }
    public boolean conjoinedTerms(Object input, String sep1, String sep2,
                                  Collection interpretations)
    {
        if (input instanceof List) {
            int count = ((List)input).size();
            Iterator it2 = ((List)input).iterator();
            while (it2.hasNext()) {
                Object nestedMove = it2.next();
                interpretations.add(nestedMove);
                if (count > 2) {
                    interpretations.add(sep1);
                } else if (count > 1) {
                    interpretations.add(sep2);
                }
                count--;
            }
        } else {
            return false;
        }
        return true;
    }
    public boolean nestedMatch(Object input, String sep1, String sep2,
                        Collection interpretations)
    {
        boolean matched = true;
        if (input instanceof List) {
            int count = ((List)input).size();
            Iterator it2 = ((List)input).iterator();
            while (it2.hasNext()) {
                Object nestedMove = it2.next();
                matched = matched&&_owningSet.match(nestedMove, null, interpretations);
                if (count > 2) {
                    interpretations.add(sep1);
                } else if (count > 1) {
                    interpretations.add(sep2);
                }
                count--;
            }
        } else {
            // goal is to output a single move
            matched = match(input, interpretations);
        }
        return matched;
    }
    /**
     * Check the match for problems with internal consistency.
     * Most common error: unbound variables in output.
     *
     * @return true if match is internally consistent, false otherwise.
     */
    public boolean checkMatchConsistency()
    {
        boolean problems = false;
        // build list of bound variables in input
        LinkedList inputVars = new LinkedList();
        LinkedList outputVars = new LinkedList();
        appendInternalVariables(input(), inputVars);
        appendInternalVariables(output(), outputVars);
        Iterator it = outputVars.iterator();
        while (it.hasNext()) {
            Object var = it.next();
            boolean found = false;
            Iterator it2 = inputVars.iterator();
            while (it2.hasNext()) {
                if (((Variable)var).name().equals(((Variable)it2.next()).name())) {found=true; break;}
            }
            if (!found) {
                System.out.println("WARNING: AtomMatcher "+input()+"==>"+output()+" has unbound variable "+var);
                problems = true;
            }
        }
        return !problems;
    }
    protected void appendInternalVariables(Object move, List inputVars)
    {
        if (move instanceof Variable) {
            inputVars.add(move);
        } else if (move instanceof Predicate) {
            Predicate pred = (Predicate)move;
            Iterator it = pred.arguments();
            while (it.hasNext()) {
                appendInternalVariables(it.next(), inputVars);
            }
        } else if (move instanceof Collection) {
            Iterator it = ((Collection)move).iterator();
            while (it.hasNext()) {
                appendInternalVariables(it.next(), inputVars);
            }
        }
    }
}
