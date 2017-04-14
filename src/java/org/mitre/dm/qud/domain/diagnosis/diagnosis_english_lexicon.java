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
package org.mitre.dm.qud.domain.diagnosis;

import java.util.*;

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;
import org.mitre.dm.qud.domain.*;

/**
 * Concrete implementation of the diagnosis lexicon.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see LexiconCell
 */
public class diagnosis_english_lexicon
    extends LexiconCell
{

protected void initializeOutputMatches()
{
    AtomMatcher work;
    ArrayList args;

    /*
    work = newMatch();
    work.addInputNestedPredicate("ask", "task", new Variable("T"));
    work.addOutputTokens("Do you want to ");
    work.addOutputPredicate("task", "T");
    work.addOutputString("?");
    addOutputMatcher(work);

    work = newMatch();
    work.addInputPredicate("ask", new Variable("Tasklist"));
    work.addOutputTokens("Do you want to ");
    work.addOutputAlternatives("Tasklist", ",", "or");
    work.addOutputString("?");
    addOutputMatcher(work);
    */

    work = newOutputMatcher();
    work.addInputPredicate("ack", new Variable("P"));
    work.addOutputString("OK, ");
    work.addOutputVariable("P");
    work.addOutputString(".");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputString("greet");
    work.addOutputTokens("Welcome to the autodoc. I will try to diagnose your problem.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputString("quit");
    work.addOutputTokens("Thank you for your visit!");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("reqRep", "understanding");
    work.addOutputTokens("I didnt understand what you said. Please rephrase.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("reqRep", "relevance");
    work.addOutputTokens("What do you mean by that?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputString("thank");
    work.addOutputTokens("Thank you very much");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "query", new Variable("X"));
    work.addOutputTokens("Please enter your query:");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "headache", new Variable("X"));
    work.addOutputTokens("Do you have a headache?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "fever", new Variable("X"));
    work.addOutputTokens("Do you have a fever?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "oconus", new Variable("X"));
    work.addOutputTokens("Have you been travelling outside the country recently?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "malarial", new Variable("X"));
    work.addOutputTokens("Was that in a tropical area?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "swimfresh", new Variable("X"));
    work.addOutputTokens("Did you do any swimming or bathing in fresh water, like a river or lake?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "hematouria", new Variable("X"));
    work.addOutputTokens("Is there any blood in your urine?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "dust", new Variable("X"));
    work.addOutputTokens("Were you exposed to airborne dust?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("task", "diagnose");
    work.addOutputTokens("diagnose an affliction");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("respond", "_");
    work.addOutputTokens("Sorry, I could not find a disease matching your symptoms.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("inform", "stumped");
    work.addOutputTokens("Hmmm... I'm stumped. Your symptoms do not match any diseases I know about.");
    addOutputMatch(work);

    Variable p = new Variable("P");
    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "query", p);
    work.addOutputTokens("The query input was:");
    work.addOutputVariable("P");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "disease", "none");
    work.addOutputTokens("You aren't sick. Slacker.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "disease", "flu");
    work.addOutputTokens("I think you may have the flu. Get plenty of rest, and drink plenty of fluids. You should get better on your own within a few days.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "disease", "allergies");
    work.addOutputTokens("I think you have mild allergies. I recommend some anti-histamines.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "disease", "schistos");
    work.addOutputTokens("Looks like you may have schistosomiasis. I'm going to refer you to a tropical disease specialist, who can confirm the diagnosis and prescribe a course of praziquantel.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "disease", "coccid");
    work.addOutputTokens("Looks like you may have coccidioidomycosis. Most people with this disease do not require any treatment.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "disease", "malaria");
    work.addOutputTokens("Looks like you may have malaria. Since the course of treatment varies depending on a large number of factors, I'm going to refer you to a tropical disease specialist.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("repeat", new Variable("Move"));
    work.addOutputVariable("Move");
    addOutputMatch(work);

}

protected void initializeSynonyms()
{
}

protected void initializeInputMatches()
{
    ListMatcher work;
    ArrayList args;

    args = new ArrayList();
    args.add(Variable.newVariable());
    Predicate queryVar = new Predicate("query", args);
    args = new ArrayList();
    args.add(queryVar);
    Predicate findoutQuery = new Predicate("findout", args);
    addInputMatch(new AgendaMatcher(inputMatches, infoState, findoutQuery));
    
    work = newInputMatcher();
    work.addInputTokens("hello");
    work.addOutputString("greet");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("bye");
    work.addOutputString("quit");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("quit");
    work.addOutputString("quit");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("what did you say");
    work.addOutputString("reqRep");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("what?");
    work.addOutputString("reqRep");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("sorry");
    work.addOutputString("reqRep");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("pardon");
    work.addOutputString("reqRep");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("yes");
    work.addOutputPredicate("answer", "yes");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("no");
    work.addOutputPredicate("answer", "no");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("okay");
    work.addOutputString("ack");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("ok");
    work.addOutputString("ack");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("headache");
    work.addOutputNestedPredicate("answer", "headache", "yes");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("fever");
    work.addOutputNestedPredicate("answer", "fever", "yes");
    addInputMatch(work);

}

    public diagnosis_english_lexicon()
    {
        super();
    }
    static public void main(String[] args)
    {
        diagnosis_english_lexicon lex =
            new diagnosis_english_lexicon();
        LinkedList al = new LinkedList();
        for (int i=0; i<args.length; i++) {
            al.add(args[i]);
        }
        lex.doInput(al);
    }
}
