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
package org.mitre.dm.qud.domain.irs;

import java.util.*;

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;
import org.mitre.dm.qud.domain.*;

/**
 * Concrete implementation of the IRS toy lexicon.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see LexiconCell
 */
public class EnglishLexicon
    extends LexiconCell
{

protected void initializeOutputMatches()
{
    AtomMatcher work;
    ArrayList args;

    /*
     * Standard items generated for task questions, either specific
     * bound task, unknown task, or selection from a list of tasks.
     */
    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "task",
                                 new Variable("T",new BoundConstraint()));
    work.addOutputTokens("Do you want to ");
    work.addOutputPredicate("task", "T");   // must generate _nestedMatch
    work.addOutputString("?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "task",
                                 new Variable("T",new UnboundConstraint()));
    work.addOutputTokens("What do you want to do?");
    addOutputMatch(work);


    work = newOutputMatcher();
    work.addInputPredicate("ask", new Variable("Tasklist",
                                               new ListConstraint()));
    work.addOutputTokens("Do you want to ");
    work.addOutputAlternatives("Tasklist", ",", "or");  // _nestedMatch
    work.addOutputString("?");
    addOutputMatch(work);

    /*
     * Standard utterances for standard moves.
     */
    work = newOutputMatcher();
    work.addInputPredicate("ack", new Variable("P"));
    work.addOutputString("OK, ");
    work.addOutputVariable("P");
    work.addOutputString(".");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputString("greet");
    work.addOutputTokens("Welcome to the IRS automated help line.");
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

    /*
     * Domain-specific transductions.
     */
    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "domore", new Variable("X"));
    work.addOutputTokens("Would you like anything else?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "topic", new Variable("X"));
    work.addOutputTokens("What do you have a question about?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "action", new Variable("X"));
    work.addOutputTokens("What would you like to do?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "address_valid", new Variable("X"));
    work.addOutputTokens("I can use the address in our files if you have ever filed a return with us and are still at that address. Have you filed a return with the IRS, and if so, are you still at that address?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "ssn", new Variable("X"));
    work.addOutputTokens("Please enter your social security number.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "publication", new Variable("X"));
    work.addOutputTokens("Which publication would you like?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "address", new Variable("X"));
    work.addOutputTokens("What is your address?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "filing_status", new Variable("X"));
    work.addOutputTokens("What is your filing status?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "refund_amt", new Variable("X"));
    work.addOutputTokens("What is the amount of your expected refund?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "exemptions", new Variable("X"));
    work.addOutputTokens("How many exemptions did you claim?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "ctc", new Variable("X"));
    work.addOutputTokens("What was your child tax credit?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("task", "conversation");
    work.addOutputTokens("get automated assistance");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("task", "general_info");
    work.addOutputTokens("find general IRS information");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("task", "specific_info");
    work.addOutputTokens("find information specific to you");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("task", "send_publication");
    work.addOutputTokens("have a document mailed to you");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("task", "transfer_call");
    work.addOutputTokens("speak with a customer service representative");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "describe", "child_tax_credits");
    work.addOutputTokens("information about child tax credits.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "describe", "household_employer_taxes");
    work.addOutputTokens("information about household employer taxes.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "describe", "individual_tax_return");
    work.addOutputTokens("1040 information.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "describe", "wage_and_tax_statement");
    work.addOutputTokens("W-2 information.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "describe", "withholding_allowance_certificate");
    work.addOutputTokens("W-4 information.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "describe", "status_of_refund");
    work.addOutputTokens("Individuals expecting a refund can obtain the status of that refund by providing SSN, filing status, and the exact expected amount of the refund.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "describe", "filing_date");
    work.addOutputTokens("The filing date is the date by which your tax return must be mailed to avoid the possibility of penalties. For 2003, your return must be postmarked no later than 12:00 midnight on April 15.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "describe", "abandon_call");
    work.addOutputTokens("Sorry I was unable to help you.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "describe", "none");
    work.addOutputTokens("You have not selected a topic.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "ctc", new Variable("CTC"));
    work.addOutputTokens("Your child tax credit was");
    work.addOutputVariable("CTC");
    work.addOutputTokens(".");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "refund_status", new Variable("RS"));
    work.addOutputTokens("Your refund status is");
    work.addOutputVariable("RS");
    work.addOutputTokens(".");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "describe", new Variable("Qua"));
    work.addOutputTokens("Internal error -- describe topic is unrecognized.");
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

    /*
     * Standard transductions.
     */
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

    /*
     * Domain-specific transductions.
     */
    work = newInputMatcher();
    work.addInputTokens("send me");
    work.addOutputNestedPredicate("answer","action", "send_publication");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("general info");
    work.addOutputNestedPredicate("answer","action", "general_info");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("tell me about");
    work.addOutputNestedPredicate("answer","action", "general_info");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("specific info");
    work.addOutputNestedPredicate("answer","action", "specific_info");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("tell me my");
    work.addOutputNestedPredicate("answer","action", "specific_info");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("talk to");
    work.addOutputNestedPredicate("answer","action", "transfer_call");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("refund");
    work.addOutputNestedPredicate("answer","topic", "refund_status");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("household");
    work.addOutputNestedPredicate("answer","topic", "household_employer_taxes");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("maid");
    work.addOutputNestedPredicate("answer","topic", "household_employer_taxes");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("au pair");
    work.addOutputNestedPredicate("answer","topic", "household_employer_taxes");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("nanny");
    work.addOutputNestedPredicate("answer","topic", "household_employer_taxes");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("child tax credit");
    work.addOutputNestedPredicate("answer","topic", "child_tax_credits");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("child tax credits");
    work.addOutputNestedPredicate("answer","topic", "child_tax_credits");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("tax return");
    work.addOutputNestedPredicate("answer","topic", "individual_tax_return");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("taxes paid");
    work.addOutputNestedPredicate("answer","topic", "wage_and_tax_statement");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("withholding");
    work.addOutputNestedPredicate("answer","topic", "withholding_allowance_certificate");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("w4");
    work.addOutputNestedPredicate("answer","topic", "withholding_allowance_certificate");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("filing date");
    work.addOutputNestedPredicate("answer","topic", "filing_date");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("filing dates");
    work.addOutputNestedPredicate("answer","topic", "filing_date");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("when");
    work.addOutputNestedPredicate("answer","topic", "filing_date");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("my ssn is");
    work.addInputVariable("X");
    work.addOutputNestedPredicate("answer","ssn", new Variable("X"));
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("my refund is");
    work.addInputVariable("X");
    work.addOutputNestedPredicate("answer","refund_amt", new Variable("X"));
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("my exemptions are");
    work.addInputVariable("X");
    work.addOutputNestedPredicate("answer","exemptions", new Variable("X"));
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("single");
    work.addOutputNestedPredicate("answer","filing_status", "single");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("married filing jointly");
    work.addOutputNestedPredicate("answer","filing_status", "married_filing_jointly");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("married filing separately");
    work.addOutputNestedPredicate("answer","filing_status", "married_filing_separately");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("qualifying widower");
    work.addOutputNestedPredicate("answer","filing_status", "qualifying_widower");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("head of household");
    work.addOutputNestedPredicate("answer","filing_status", "head of household");
    addInputMatch(work);
}

    public EnglishLexicon()
    {
        super();
    }
    static public void main(String[] args)
    {
        EnglishLexicon lex =
            new EnglishLexicon();
        LinkedList al = new LinkedList();
        for (int i=0; i<args.length; i++) {
            al.add(args[i]);
        }
        lex.doInput(al);
    }
}
