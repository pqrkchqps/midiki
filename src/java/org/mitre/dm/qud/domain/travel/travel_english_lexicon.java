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
package org.mitre.dm.qud.domain.travel;

import java.util.*;

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;
import org.mitre.dm.qud.domain.*;

/**
 * Concrete implementation of the travel lexicon.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see LexiconCell
 */
public class travel_english_lexicon
    extends LexiconCell
{
    private LinkedList show_493_output;
    private Object temp_518;
    private Object temp_523;
    private Object temp_528;
    private ArrayList vect_538;
    private Object temp_536;
    private ArrayList vect_540;
    private Object temp_539;
    private ArrayList vect_542;
    private Object temp_541;
    private Object temp_543;
    private Object temp_548;
    private ArrayList vect_554;
    private Object temp_553;
    private Object temp_555;
    private Object temp_557;


    private LinkedList show_534_output;
    private Object temp_568;
    private Object temp_573;
    private ArrayList vect_573;
    private Object temp_577;
    private Object temp_581;

    private Object temp_583;

    private Object temp_590;
    private ArrayList vect_600;
    private Object temp_598;
    private ArrayList vect_602;
    private Object temp_601;
    private ArrayList vect_604;
    private Object temp_603;
    private Object temp_605;
    private Object temp_610;
    private ArrayList vect_616;
    private Object temp_615;
    private Object temp_617;
    private Object temp_619;


    private LinkedList show_596_output;
    private Object temp_630;
    private Object temp_635;
    private ArrayList vect_635;
    private Object temp_639;
    private Object temp_643;

    private Object temp_645;

    private Object temp_652;
    private ArrayList vect_662;
    private Object temp_660;
    private ArrayList vect_664;
    private Object temp_663;
    private ArrayList vect_666;
    private Object temp_665;
    private Object temp_667;
    private Object temp_672;
    private ArrayList vect_678;
    private Object temp_677;
    private Object temp_679;
    private Object temp_681;


    private LinkedList show_658_output;
    private Object temp_692;
    private Object temp_697;
    private ArrayList vect_697;
    private Object temp_701;
    private Object temp_705;

    private Object temp_707;

    private Object temp_714;
    private ArrayList vect_724;
    private Object temp_722;
    private ArrayList vect_726;
    private Object temp_725;
    private Object temp_727;
    private Object temp_729;
    private ArrayList vect_735;
    private Object temp_734;
    private Object temp_736;
    private Object temp_738;


    private LinkedList show_720_output;
    private Object temp_749;
    private Object temp_754;
    private ArrayList vect_754;
    private Object temp_758;
    private Object temp_762;

    private Object temp_764;

    private Object temp_771;
    private ArrayList vect_813;
    private Object temp_811;
    private Object temp_814;

protected void initializeOutputMatches()
{
    AtomMatcher work;
    ArrayList args;
    LinkedList args1;
    LinkedList args2;

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "task",
                                 new Variable("T",new BoundConstraint()));
    work.addOutputTokens("Do you want to ");
    work.addOutputPredicate("task", "T");   // must generate _nestedMatch
    work.addOutputString("?");
    addOutputMatch(work);


    work = newOutputMatcher();
    work.addInputPredicate("ask", new Variable("Tasklist",
                                               new ListConstraint()));
    work.addOutputTokens("Do you want to ");
    work.addOutputAlternatives("Tasklist", ",", "or ");  // _nestedMatch
    work.addOutputString("?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "price", new Variable("Price"));
    work.addOutputString("It will cost ");
    work.addOutputVariable("Price");
    work.addOutputString(" dollars.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("inform", "booked", new Variable("Booked"));
    work.addOutputString("We have successfully booked your flight.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("answer", "duration", new Variable("Dur"));
    work.addOutputString("You can stay for ");
    work.addOutputVariable("Dur");
    work.addOutputString(" months.");
    addOutputMatch(work);

    vect_538 = new ArrayList();
    vect_540 = new ArrayList();
    vect_542 = new ArrayList();
    temp_543 = new Variable("_");
    vect_542.add(temp_543);
    temp_541 = new Predicate("to", vect_542);
    vect_540.add(temp_541);
    temp_539 = new Predicate("alts", vect_540);
    vect_538.add(temp_539);
    temp_548 = new Variable("AltList");
    vect_538.add(temp_548);
/*
    vect_554 = new ArrayList();
    temp_555 = new Variable("_");
    vect_554.add(temp_555);
    temp_557 = new Variable("AltList");
    vect_554.add(temp_557);
    temp_553 = new Predicate("alts", vect_554);
    vect_538.add(temp_553);
*/
    temp_536 = new Predicate("answer", vect_538);
    show_534_output = new LinkedList();
    temp_568 = ("We serve the following cities: ");
    show_534_output.add(temp_568);
    vect_573 = new ArrayList();
    vect_573.add(new Variable("AltList"));
    vect_573.add(",");
    vect_573.add("and");
    temp_573 = new Predicate("_conjoinedText", vect_573);
    show_534_output.add(temp_573);
    temp_590 = (".");
    show_534_output.add(temp_590);
    vect_600 = new ArrayList();
    vect_602 = new ArrayList();
    vect_604 = new ArrayList();
    temp_605 = new Variable("_");
    vect_604.add(temp_605);
    temp_603 = new Predicate("from", vect_604);
    vect_602.add(temp_603);
    temp_610 = new Variable("_");
    vect_602.add(temp_610);
    temp_601 = new Predicate("alts", vect_602);
    vect_600.add(temp_601);
    vect_616 = new ArrayList();
    temp_617 = new Variable("_");
    vect_616.add(temp_617);
    temp_619 = new Variable("AltList");
    vect_616.add(temp_619);
    temp_615 = new Predicate("alts", vect_616);
    vect_600.add(temp_615);
    temp_598 = new Predicate("answer", vect_600);
    show_596_output = new LinkedList();
    temp_630 = ("We serve the following cities: ");
    show_596_output.add(temp_630);
   vect_635 = new ArrayList();
    vect_635.add(new Variable("AltList"));
    vect_635.add(",");
    vect_635.add("and");
   temp_635 = new Predicate("_conjoinedText", vect_635);
    show_596_output.add(temp_635);
    temp_652 = (".");
    show_596_output.add(temp_652);
    vect_662 = new ArrayList();
    vect_664 = new ArrayList();
    vect_666 = new ArrayList();
    temp_667 = new Variable("_");
    vect_666.add(temp_667);
    temp_665 = new Predicate("how", vect_666);
    vect_664.add(temp_665);
    temp_672 = new Variable("_");
    vect_664.add(temp_672);
    temp_663 = new Predicate("alts", vect_664);
    vect_662.add(temp_663);
    vect_678 = new ArrayList();
    temp_679 = new Variable("_");
    vect_678.add(temp_679);
    temp_681 = new Variable("AltList");
    vect_678.add(temp_681);
    temp_677 = new Predicate("alts", vect_678);
    vect_662.add(temp_677);
    temp_660 = new Predicate("answer", vect_662);
    show_658_output = new LinkedList();
    temp_692 = ("The following means of transport are available: ");
    show_658_output.add(temp_692);
   vect_697 = new ArrayList();
    vect_697.add(new Variable("AltList"));
    vect_697.add(",");
    vect_697.add("and");
   temp_697 = new Predicate("_conjoinedText", vect_697);
    show_658_output.add(temp_697);
    temp_714 = (".");
    show_658_output.add(temp_714);
    vect_724 = new ArrayList();
    vect_726 = new ArrayList();
    temp_727 = new Variable("_");
    vect_726.add(temp_727);
    temp_729 = new Variable("_");
    vect_726.add(temp_729);
    temp_725 = new Predicate("alts", vect_726);
    vect_724.add(temp_725);
    vect_735 = new ArrayList();
    temp_736 = new Variable("_");
    vect_735.add(temp_736);
    temp_738 = new Variable("AltList");
    vect_735.add(temp_738);
    temp_734 = new Predicate("alts", vect_735);
    vect_724.add(temp_734);
    temp_722 = new Predicate("answer", vect_724);
    show_720_output = new LinkedList();
    temp_749 = ("The available alternatives are: ");
    show_720_output.add(temp_749);
   vect_754 = new ArrayList();
    vect_754.add(new Variable("AltList"));
    vect_754.add(",");
    vect_754.add("and");
   temp_754 = new Predicate("_conjoinedText", vect_754);
    show_720_output.add(temp_754);
    temp_771 = (".");
    show_720_output.add(temp_771);

    addOutputMatch(temp_536, show_534_output);  // list of cities serviced
    addOutputMatch(temp_598, show_596_output);
    addOutputMatch(temp_660, show_658_output);
    addOutputMatch(temp_722, show_720_output);

    work = newOutputMatcher();
    work.addInputPredicate("ack", new Variable("P"));
    work.addOutputString("OK, ");
    work.addOutputVariable("P");
    work.addOutputString(".");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputString("greet");
    work.addOutputTokens("Welcome to the travel agency.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("quit", new Variable("Cause"));
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
    work.addInputNestedPredicate("ask", "return", new Variable("X"));
    work.addOutputTokens("Do you want a return ticket?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "how", new Variable("X"));
    work.addOutputTokens("How do you want to travel?");
    addOutputMatch(work);

    work = newOutputMatcher();
    args1 = new LinkedList();
    args1.add(new Variable("How"));
    args2 = new LinkedList();
    args2.add(new Predicate("how", args1));
    work.addInputNestedPredicate("inform", "invalid", new Predicate("answer", args2));
    work.addOutputTokens("I don't recognize the mode of transport named");
    work.addOutputVariable("How");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "from", new Variable("X"));
    work.addOutputTokens("What city do you want to go from?");
    addOutputMatch(work);

    work = newOutputMatcher();
    args1 = new LinkedList();
    args1.add(new Variable("City"));
    args2 = new LinkedList();
    args2.add(new Predicate("from", args1));
    work.addInputNestedPredicate("inform", "invalid", new Predicate("answer", args2));
    work.addOutputTokens("I don't recognize the city named");
    work.addOutputVariable("City");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "to", new Variable("X"));
    work.addOutputTokens("What city do you want to go to?");
    addOutputMatch(work);

    work = newOutputMatcher();
    args1 = new LinkedList();
    args1.add(new Variable("City"));
    args2 = new LinkedList();
    args2.add(new Predicate("to", args1));
    work.addInputNestedPredicate("inform", "invalid", new Predicate("answer", args2));
    work.addOutputTokens("I don't recognize the city named");
    work.addOutputVariable("City");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "month", new Variable("X"));
    work.addOutputTokens("What month do you want to leave?");
    addOutputMatch(work);

    work = newOutputMatcher();
    args1 = new LinkedList();
    args1.add(new Variable("Month"));
    args2 = new LinkedList();
    args2.add(new Predicate("month", args1));
    work.addInputNestedPredicate("inform", "invalid", new Predicate("answer", args2));
    work.addOutputTokens("I don't recognize the month named");
    work.addOutputVariable("Month");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "ret_month", new Variable("X"));
    work.addOutputTokens("What month do you want to go back?");
    addOutputMatch(work);

    work = newOutputMatcher();
    args1 = new LinkedList();
    args1.add(new Variable("Month"));
    args2 = new LinkedList();
    args2.add(new Predicate("ret_month", args1));
    work.addInputNestedPredicate("inform", "invalid", new Predicate("answer", args2));
    work.addOutputTokens("I don't recognize the month named");
    work.addOutputVariable("Month");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "class", new Variable("X"));
    work.addOutputTokens("What class did you have in mind?");
    addOutputMatch(work);

    work = newOutputMatcher();
    args1 = new LinkedList();
    args1.add(new Variable("Class"));
    args2 = new LinkedList();
    args2.add(new Predicate("to", args1));
    work.addInputNestedPredicate("inform", "invalid", new Predicate("answer", args2));
    work.addOutputTokens("I don't recognize the class named");
    work.addOutputVariable("Class");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "country", new Variable("X"));
    work.addOutputTokens("What country do you want to go to?");
    addOutputMatch(work);

    work = newOutputMatcher();
    args1 = new LinkedList();
    args1.add(new Variable("Country"));
    args2 = new LinkedList();
    args2.add(new Predicate("to", args1));
    work.addInputNestedPredicate("inform", "invalid", new Predicate("answer", args2));
    work.addOutputTokens("I don't recognize the country named");
    work.addOutputVariable("Country");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "account", new Variable("X"));
    work.addOutputTokens("What is your credit card number?");
    addOutputMatch(work);

    work = newOutputMatcher();
    args1 = new LinkedList();
    args1.add(new Variable("Account"));
    args2 = new LinkedList();
    args2.add(new Predicate("to", args1));
    work.addInputNestedPredicate("inform", "invalid", new Predicate("answer", args2));
    work.addOutputTokens("I don't recognize the account number");
    work.addOutputVariable("Account");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "task",
                                 new Variable("T",new UnboundConstraint()));
    work.addOutputTokens("What can I do for you?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("ask", "order_trip", new Variable("X"));
    work.addOutputTokens("Would you like to make that reservation now?");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("task", "price_info");
    work.addOutputTokens("get price information about a trip");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("task", "order_trip");
    work.addOutputTokens("make a reservation");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("respond", new Variable("X",
                                                   new UnboundConstraint()));
    work.addOutputTokens("Sorry, I could not find a trip matching your specification.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputNestedPredicate("answer", "duration", "0");
    work.addOutputTokens("You don't need any visa there.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("inform", "booked");
    work.addOutputTokens("Your trip has been booked.");
    addOutputMatch(work);

    work = newOutputMatcher();
    work.addInputPredicate("repeat", new Variable("Move"));
    work.addOutputVariable("Move");
    addOutputMatch(work);
}

protected void initializeSynonyms()
{
    List syn_1523_input = Arrays.asList(new String[] {"flight","flights","plane","fly","airplane"});
        addSynonymMap(syn_1523_input,"plane");

        addSynonymMap(Arrays.asList(new String[] {"cheap"}),"economy");
}

protected void initializeInputMatches()
{
    ListMatcher work;
    ArrayList args;

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
    work.addInputTokens("to");
    work.addInputVariable("C");
    work.addOutputNestedPredicate("answer","to",new Variable("C"));
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("from");
    work.addInputVariable("C");
    work.addOutputNestedPredicate("answer","from",new Variable("C"));
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("by");
    work.addInputVariable("C");
    work.addOutputNestedPredicate("answer","how",new Variable("C"));
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("in");
    work.addInputVariable("C");
    work.addOutputNestedPredicate("answer","month",new Variable("C"));
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("of");
    work.addInputVariable("C");
    work.addOutputNestedPredicate("answer","month",new Variable("C"));
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("on");
    work.addInputTokens("the");
    work.addInputVariable("C");
    work.addOutputNestedPredicate("answer","day",new Variable("C"));
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("on");
    work.addInputVariable("C");
    work.addOutputNestedPredicate("answer","day",new Variable("C"));
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("one");
    work.addInputTokens("way");
    work.addOutputNestedPredicate("answer","return","no");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("round");
    work.addInputTokens("trip");
    work.addOutputNestedPredicate("answer","return","yes");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("price");
    work.addOutputNestedPredicate("answer","task","price_info");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("trip");
    work.addOutputNestedPredicate("answer","task","price_info");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("reservation");
    work.addOutputNestedPredicate("answer","task","order_trip");
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("choices");
    work.addOutputNestedPredicate("ask","alts",new Variable("Choices"));
    addInputMatch(work);

    work = newInputMatcher();
    work.addInputTokens("alternatives");
    work.addOutputNestedPredicate("ask","alts",new Variable("Choices"));
    addInputMatch(work);
}

    public travel_english_lexicon()
    {
        super();
    }
    static public void main(String[] args)
    {
        travel_english_lexicon lex =
            new travel_english_lexicon();
        List al = new LinkedList();
        for (int i=0; i<args.length; i++) {
            al.add(args[i]);
        }
        lex.doInput(al);
    }
}
