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
 * Provides a simple lexical pattern-matching facility.
 * Concrete subclasses should override three abstract
 * routines to initialize input matches, output matches,
 * and the table of synonyms.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
abstract public class LexiconImplementation implements Tokenizer
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.domain.LexiconImplementation");
    protected InfoState infoState;
    protected Bindings localBindings;
    public void connect(InfoState is)
    {
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","connect","connecting to infoState",is);
        infoState = is;
        localBindings = new BindingsImpl();
        synonymMaps = new LinkedList();
        inputMatches = new MatchSet();
        outputMatches = new MatchSet();
        initializeSynonyms();
        initializeInputMatches();
        initializeOutputMatches();
        checkLexiconConsistency();
    }

    public ListMatcher newInputMatcher()
    {
        ListMatcher match = new ListMatcher(inputMatches);
        match.setTokenizer(this);
        addInputMatch(match);
        return match;
    }

    public AtomMatcher newOutputMatcher()
    {
        AtomMatcher match = new AtomMatcher(outputMatches);
        match.setTokenizer(this);
        addOutputMatch(match);
        return match;
    }

    /**
     * Check match elements for most likely error, unbound variables in output.
     * Defer this check to each matcher in turn.
     */
    protected void checkLexiconConsistency()
    {
        boolean problem = false;
        Iterator it = inputMatches.matchers();
        while (it.hasNext()) {
            Matcher m = (Matcher)it.next();
            boolean ok = m.checkMatchConsistency();
            problem = problem || !ok;
        }
        it = outputMatches.matchers();
        while (it.hasNext()) {
            Matcher m = (Matcher)it.next();
            boolean ok = m.checkMatchConsistency();
            problem = problem || !ok;
        }
        if (problem) System.out.println("WARNING: Please correct lexicon errors and try the dialogue again.");
    }

    abstract protected void initializeSynonyms();
    abstract protected void initializeInputMatches();
    abstract protected void initializeOutputMatches();

    protected List doInput(List words)
    {
        // goal is to find all possible interpretations.
        // we create a single list for accumulating interpretations.
        // the matching routine uses that passed collection rather than
        // building a new one each time and copying contents.
        LinkedList interpretations = new LinkedList();
        Iterator sit = synonymMaps.iterator();
        while (sit.hasNext()) {
            Object[] match = (Object[])sit.next();
            substitute(words,(Collection)match[0],match[1]);
        }
        inputMatches.match(words, null, interpretations);
        List matched = matchObliqueInput(words, interpretations);
        return matched;
    }

    protected Formatter formatter = new SimpleTextFormatter(); // default
    public void setFormatter(Formatter f)
    {
        formatter = f;
    }

    public String doOutput(Object move)
    {
        StringBuffer result = new StringBuffer();
        String temp;

        LinkedList results = new LinkedList();
        boolean matched = outputMatches.match(move, null, results);
        if (matched) {
            return formatter.format(results);
        } else {
            return "";
        }
    }

    protected Collection synonymMaps;
    protected MatchSet inputMatches;
    protected MatchSet outputMatches;

    public void addSynonymMap(List words, Object term)
    {
        Object[] match = new Object[2];
        match[0] = words;
        match[1] = term;
        synonymMaps.add(match);
    }

    public void addInputMatch(List input, Collection move)
    {
        ListMatcher match = newInputMatcher();
        match.setInput(input);
        match.setOutput(move);
        if (inputMatches.contains(match)) return;
        inputMatches.addMatcher(match);
    }

    public void addOutputMatch(List move, Collection output)
    {
        AtomMatcher match = newOutputMatcher();
        match.setInput(move);
        match.setOutput(output);
        if (outputMatches.contains(match)) return;
        outputMatches.addMatcher(match);
    }

    public void addOutputMatch(Object move, Collection output)
    {
        AtomMatcher match = newOutputMatcher();
        LinkedList inputMoves = new LinkedList();
        inputMoves.add(move);
        match.setInput(inputMoves);
        match.setOutput(output);
        if (outputMatches.contains(match)) return;
        outputMatches.addMatcher(match);
    }

    public void addInputMatch(Matcher omatch)
    {
        if (inputMatches.contains(omatch)) return;
        inputMatches.addMatcher(omatch);
    }

    public void addOutputMatch(Matcher omatch)
    {
        if (outputMatches.contains(omatch)) return;
        outputMatches.addMatcher(omatch);
    }

    public List tokenize(String input)
    {
        /*
         * Assumes the existence of a string tokenizer somewhere
         * in the system.
         */
        Variable wl = new Variable("Wl");
        ArrayList al = new ArrayList();
        al.add(input);
        al.add(wl);
        localBindings.enterScope();
        infoState.cell("interpreter").method("string2wordlist").invoke(al, localBindings);
        Object result = Unify.getInstance().deref(wl, localBindings);
        localBindings.exitScope();
        return (List)result;
    }

    // synonym substitution could be replaced by a specialized Matcher.
    public void substitute(List words, Collection synonyms, Object lemma)
    {
        //System.out.println(synonyms);
        ListIterator wi = words.listIterator();
        while (wi.hasNext()) {
            int prevIndex = wi.previousIndex();  // where to return to
            Iterator si = synonyms.iterator();
            while (si.hasNext()) {
                // check this synonym against the input string, brute force
                Object st = si.next();
                if (st instanceof Collection) {
                    Iterator ci = ((Collection)st).iterator();
                    // try to match this synonym against this part
                    // of the wordlist
                    boolean matched = true;
                    while (ci.hasNext()) {
                        // fail to match if no remaining input
                        if (!wi.hasNext()) {
                            matched = false;
                            break;
                        }
                        Object ct = ci.next();
                        Object wt = wi.next();
                        //System.out.println("comparing "+wt+" to "+ct);
                        if (Unify.getInstance().unify(wt,ct) == null) {
                            matched = false;
                            break;
                        }
                    };
                    // if matched, perform the substitution
                    if (matched) {
                        //System.out.println("matched!");
                        while (wi.previousIndex() > prevIndex) {
                            //System.out.println("rewind "+wi.previousIndex());
                            wi.previous();
                            wi.remove();
                        }
                        wi.add(lemma);
                        //System.out.println(words);
                    }
                    // rewind wordlist
                    while (wi.previousIndex() > prevIndex) {
                        //System.out.println("rewind "+wi.previousIndex());
                        wi.previous();
                    }
                } else {
                    if (wi.hasNext()) {
                        Object wt = wi.next();
                        //System.out.println(wt+":"+st);
                        if (Unify.getInstance().unify(wt,st) != null) {
                            //System.out.println("matched!");
                            wi.remove();
                            wi.add(lemma);
                        } else {
                            wi.previous();
                        }
                    }
                }
            }
            // advance to next word, if any left
            if (wi.hasNext()) wi.next();
        }
    }

    // the following routine should be subsumed by a new Matcher type,
    // customized to match against items in a domain.
    public List matchObliqueInput(Collection words, List results)
    {
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","matchObliqueInput","words",words);
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","matchObliqueInput","prior results",results);
        Variable question = new Variable("Question");
        Iterator wi = words.iterator();
        while (wi.hasNext()) {
            Bindings lb = new BindingsImpl();
            ArrayList args = new ArrayList();
            args.add(question);
            Object word = wi.next();
            args.add(word);
System.out.println("calling relevant_answer with "+args);
            if (infoState.cell("domain").query("relevant_answer").query(args, lb)) {
System.out.println("is considered relevant");
                // The specific word answers at least one question.
                // However, we don't know for sure which one it is.
                // If we insert all of the answers, we are saying that
                // the user has made each of those moves, which is not
                // generally the case. Instead, just insert the raw
                // answer and leave it for the DME to resolve.
                // (For example, "march" might be appropriate for month(X)
                // or ret_month(X); insert "answer(march)" instead of
                // "answer(month(march))" and "answer(ret_month(march))".
                // The Accommodate module is written with that strategy
                // in mind.)
                ArrayList resultArgs = new ArrayList();
                resultArgs.add(word);
                Object result = new Predicate("answer", resultArgs);
                results.add(result);
                logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","matchObliqueInput","match="+result);
            } else {
System.out.println("is not considered relevant");
            }
        }
        return results;
    }
    public Collection removeDuplicates(Collection terms, Bindings b)
    {
        if (terms == null) {
            return terms;
        }
        if (terms.isEmpty()) {
            return terms;
        }
        LinkedList newTerms = new LinkedList();
        Iterator it = terms.iterator();
        while (it.hasNext()) {
            Object term = it.next();
            boolean found = false;
            ListIterator lit = newTerms.listIterator();
            while (lit.hasNext()) {
                Object existingTerm = lit.next();
                if (Unify.getInstance().matchTerms(term,existingTerm,b)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newTerms.add(term);
            }
        }
        return newTerms;
    }

    public boolean doExec_lexicon_wordlist2moves(
                           Object intfid,
                           Object wordlist,
                           Object moves,
                           Bindings bindings) {
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","wordlist2moves","wordlist"+wordlist);
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","wordlist2moves","bindings"+bindings);
        wordlist = Unify.getInstance().deref(wordlist, bindings);
        if (!(wordlist instanceof List)) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","wordlist2moves","type error; list expected.");
            return false;
        }
        List outputMoves = doInput((List)wordlist);
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","wordlist2moves","outputMoves"+outputMoves);
        if (outputMoves == null) {
            // error in the input; nothing recognized,
            // and didn't get an empty list
            logger.logp(Level.FINE,"org.mitre.dm.qud.domain.LexiconImplementation","wordlist2moves","nothing recognized, but non-empty list!");
            return false;
        }
        /*
         * Bind outputMoves to the computed list of moves.
         */
        return Unify.getInstance().matchTerms(moves, outputMoves, bindings);
    }

    public boolean doExec_lexicon_movelist2words(
                           Object intfid,
                           Object movelist,
                           Object words,
                           Bindings bindings) {
        movelist = Unify.getInstance().deref(movelist, bindings);
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","movelist2words","movelist"+movelist);
        logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","movelist2words","bindings"+bindings);
        if (!(movelist instanceof List)) {
            // type error; list expected.
            logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","movelist2words","failed; type error");
            return false;
        }
        StringBuffer outputWords = new StringBuffer();
        Iterator it = ((List)movelist).iterator();
        while (it.hasNext()) {
            Object nextTerm = it.next();
            if (nextTerm instanceof List) {
                Iterator it2 = ((List)nextTerm).iterator();
                while (it2.hasNext()) {
                    Object nestedTerm = it2.next();
                    outputWords.append(doOutput((Object)nestedTerm));
                }
            } else {
                outputWords.append(doOutput(nextTerm));
            }
        }
        /*
         * Bind outputWords to the computed list of words.
         */
        return Unify.getInstance().matchTerms(words, outputWords.toString(), bindings);
    }

    public boolean doExec_lexicon_remove_duplicates(Object intfid,
                           Object movesin,
                           Object movesout, Bindings bindings) {
        movesin = Unify.getInstance().deref(movesin, bindings);
        if (!(movesin instanceof List)) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.domain.LexiconImplementation","remove_duplicates","type error; list expected.");
            return false;
        }
        Collection outputMoves =
            removeDuplicates((Collection)movesin,bindings);
        return Unify.getInstance().matchTerms(movesout, outputMoves, bindings);
    }

    public LexiconImplementation()
    {
        super();
    }
}
