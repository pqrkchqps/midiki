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

/**
 * Provides a simple pairing of input sequences to output sequences
 * within a specified "lexicon", and convenience methods for constructing
 * those sequences. 
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @deprecated
 */
public class Match implements Matcher
{
    /**
     * Attempts to match the input item, adding any resulting
     * interpretations to the existing results.
     *
     * @param input an <code>Object</code> to be matched.
     * Implementing classes may expect specific subclasses as input.
     * @param interpretations a <code>Collection</code> of values
     * transduced from the input.
     * @return <code>true</code> if this match succeeded.
     */
    public boolean match(Object input, Collection interpretations)
    {
        return false;  // to be overridden, replaced by other classes
    }
    /**
     * Get the <code>MatchSet</code> this <code>Matcher</code>
     * is defined in.
     *
     * @return a <code>MatchSet</code> value
     */
    public MatchSet getOwningSet()
    {
        return _owningSet;
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

    protected LinkedList _tags;
    protected LinkedList _input;
    protected LinkedList _output;
    protected LexiconImplementation _parent;

    public Match(LexiconImplementation parent)
    {
        _parent = parent;
        _tags = new LinkedList();
        _input = new LinkedList();
        _output = new LinkedList();
    }

    public LinkedList tags() {return _tags;}
    public LinkedList input() {return _input;}
    public LinkedList output() {return _output;}

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
        _input.addAll(_parent.tokenize(string));
    }

    public void addOptionalInputTokens(String string)
    {
        _input.add(new Predicate("optional", _parent.tokenize(string)));
    }

    public void addOutputTokens(String string)
    {
        _output.addAll(_parent.tokenize(string));
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

    public boolean optionalMatchInput(Object wt,
                                      ListIterator wi, 
                                      Iterator ipi,
                                      Bindings bindings)
    {
        int prevIndex = wi.previousIndex();  // where to return to
        boolean getNext = false;
        boolean matched = true;
        while (ipi.hasNext()) {
            Object ipt = ipi.next();
            //System.out.println("optionalMatchInput, "+wt+", "+ipt);
            // we're passing in first item, since already fetched.
            // therefore don't pull from wordlist.
            if (getNext) {
                // fail to match if no remaining input
                if (!wi.hasNext()) {
                    //System.out.println("end of input but not end of pattern");
                    return false;
                }
                wt = wi.next();
            } else
                getNext = true;
            // check this entry against the input string, brute force
            if (ipt instanceof Predicate) {
                Iterator subpat = ((Predicate)ipt).arguments();
                int prevRecurseIndex = wi.previousIndex();
                matched = optionalMatchInput(wt, wi, subpat, bindings);
                // if success, fine. if not, just ignore pattern element
                // after rewinding the wordlist.
                if (!matched) {
                    while (wi.previousIndex() >= prevRecurseIndex) {
                        //System.out.println("opt rewind "+wi.previousIndex());
                        wi.previous();
                    }
                    //wi.previous();
                    /*
                    if (wi.hasNext()) {
                        Object wiNext = wi.next();
                        //System.out.println("opt advancing to "+wiNext);
                    }
                    */
                    matched = true;
                    continue;
                } else {
                }
            } else if (!Unify.getInstance().matchTerms(wt, ipt, bindings)) {
                return false;
            } else {
            }
        }
        return true;
    }
    public boolean anchoredMatchInput(ListIterator wi, 
                                      List inputPattern,
                                      Bindings bindings,
                                      Collection result,
                                      Collection interpretations)
    {
        bindings.enterScope(); // prepare to back out of match if error occurs
        int prevIndex = wi.previousIndex();  // where to return to
        //System.out.println("prevIndex = "+prevIndex);
        boolean matched = true;
        Iterator ipi = inputPattern.iterator();
        while (ipi.hasNext()) {
            // fail to match if no remaining input
            if (!wi.hasNext()) {
                //System.out.println("end of input but not end of pattern");
                matched = false;
                break;
            }
            Object wt = wi.next();
            // check this entry against the input string, brute force
            Object ipt = ipi.next();
            //System.out.print("matching "+wt+" to "+ipt+"...");
            if ((ipt instanceof Predicate) &&
                (((Predicate)ipt).functor().equals("optional"))) {
                //System.out.println("pattern element predicate "+ipt);
                int prevRecurseIndex = wi.previousIndex();
                Iterator subpat = ((Predicate)ipt).arguments();
                matched = optionalMatchInput(wt, wi, subpat, bindings);
                //System.out.println("matched == "+matched);
                // if success, fine. if not, just ignore pattern element
                // after rewinding the wordlist.
                if (!matched) {
                    while (wi.previousIndex() >= prevRecurseIndex) {
                        //System.out.println("opt2 rewind "+wi.previousIndex());
                        wi.previous();
                    }
                    //wi.previous();
                    /*
                      if (wi.hasNext()) {
                      Object wiNext = wi.next();
                      //System.out.println("opt2 advancing to "+wiNext);
                      }
                    */
                    matched = true; // still match if optional part fails
                    continue;
                } else {
                }
                if (matched) wi.remove();
                /*
            } else if ((ipt instanceof Predicate) &&
                       (((Predicate)ipt).functor().equals("typed"))) {
                int prevRecurseIndex = wi.previousIndex();
                wi.previous();
                Iterator subpat = ((Predicate)ipt).arguments();
                Object bindTo = subpat.next();
                Object ofType = subpat.next();
                matched = matchTypedInput(wi, bindTo, ofType, bindings, interpretations);
                // matching tokens already removed, including current one.
                break;
                */
            } else if (!Unify.getInstance().matchTerms(wt, ipt, bindings)) {
                //System.out.println("failed");
                matched = false;
                break;
            } else {
                //System.out.println("matched");
                wi.remove();
            }
        }
        if (matched) {
            //System.out.println(result);
            Object deref = Unify.getInstance().deref(result, bindings);
            //System.out.println("Appending dereferenced result "+deref);
            interpretations.addAll((Collection)deref);
        } else {
            //System.out.println("no result generated");
        }
        if (!matched) bindings.exitScope(); // back out any new bindings
        // rewind wordlist
        while (wi.previousIndex() > prevIndex) {
            //System.out.println("rewind "+wi.previousIndex());
            wi.previous();
            if (matched) wi.remove();
        }
        return matched;
    }
    public void matchInput(List words, 
                           Collection interpretations)
    {
        //System.out.println("inputPattern = "+inputPattern);
        //System.out.println("result = "+result);
        List inputPattern = input();
        Collection result = output();
        Bindings bindings = new BindingsImpl();
        ListIterator wi = words.listIterator();
        while (wi.hasNext()) {
            bindings.clear();
            boolean matched = anchoredMatchInput(wi, inputPattern, bindings, result, interpretations);
            if (wi.hasNext()) {
                Object wiNext = wi.next();
                //System.out.println("advancing to "+wiNext);
            }
        }
    }
    /*
    public boolean matchTypedInput(ListIterator wi, 
                                   Object bindTo,
                                   Object ofType,
                                   Bindings bindings,
                                   Collection interpretations)
    {
        // in the initial "lexicon"/parser, the domain resource was
        // consulted directly for oblique input (elements that match
        // a particular type). However, that tends to push some parsing
        // code into the domain, which we don't want. Also, it doesn't
        // support slightly more sophisticated parsing with typed phrase
        // structures. In this routine, parsing will start at the current
        // word, but only for patterns matching a "type". Type could be
        // checked as output value, or as tag on phrase. I'm going to go
        // with tag, so that this is a subgrammar match. There is an
        // additional constraint that if bindTo is non-null, the result
        // must successfully bind with it. (However, at the present time
        // that isn't being done!)
        // Because the input is to be fixed at the left end, we will use
        // the anchored full matcher. Note that there could be additional
        // recursion within that match, so must be prepared to handle
        // unwinding of multiple binding scopes.
        LinkedList localInterpretations = new LinkedList();
        Iterator mit = inputMatches.iterator();
        while (mit.hasNext()) {
            Match match = (Match)mit.next();
            boolean matched = false;
            if (match.tags().contains(ofType)) {
                System.out.println("match == "+match);
                if (anchoredMatchInput(wi, (List)match.input(), bindings, (List)match.output(), localInterpretations)) {
                    if (Unify.getInstance().matchTerms(bindTo, localInterpretations, bindings)) {
                        interpretations.addAll(localInterpretations);
                        System.out.println("matchedTypedInput "+ofType+" "+interpretations);
                        return true;
                    } else {
                        // might want to exit and reenter scope instead...
                        System.out.println("failed to match TypedInput "+ofType);
                        return false;
                    }
                } else {
                    System.out.println("typed match failed");
                }
            }
        }
        return false;
    }
    */
    /**
     * Check the match for problems with internal consistency.
     * Most common error: unbound variables in output.
     *
     * @return true if match is internally consistent, false otherwise.
     */
    public boolean checkMatchConsistency()
    {
        return true;
    }
}
