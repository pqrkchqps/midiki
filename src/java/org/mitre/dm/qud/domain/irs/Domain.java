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
import java.util.logging.*;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;
import org.mitre.dm.qud.domain.*;

/**
 * Concrete implementation of the IRS toy domain.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see DomainCell
 */
public class Domain extends DomainCell
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.domain.irs.Domain");
    public Domain()
    {
        super();
    }

    public void init(Object config)
    {
        super.init(config);
    }

    public void connect(InfoState infoState)
    {
        super.connect(infoState);
    }

    protected void initializeAttributes()
    {
        ArrayList val = new ArrayList();
        val.add("child_tax_credits");
        val.add("household_employer_taxes");
        val.add("individual_tax_return");
        val.add("wage_and_tax_statement");
        val.add("withholding_allowance_certificate");
        val.add("refund_status");
        val.add("filing_date");
        val.add("none");
        val.add("abandon_call");
        attributeMap.put("topic", val);
        val = new ArrayList();
        val.add("general_info");
        val.add("specific_info");
        val.add("send_publication");
        val.add("transfer_call");
        val.add("none");
        val.add("abandon_call");
        attributeMap.put("action", val);
        val = new ArrayList();
        val.add("single");
        val.add("married_filing_jointly");
        val.add("married_filing_separately");
        val.add("head_of_household");
        val.add("qualifying_widower");
        attributeMap.put("filing_status", val);
        val = new ArrayList();
        val.add("972");
        val.add("926");
        val.add("1040");
        val.add("W-2");
        val.add("W-4");
        attributeMap.put("publication", val);
        val = new ArrayList();
        val.add("not_received");
        val.add("received");
        val.add("processed");
        val.add("deposited");
        val.add("sent");
        val.add("no_matching_refund");
        val.add("no_such_ssn");
        attributeMap.put("refund_status", val);
        val = new ArrayList();
        val.add("yes");
        val.add("no");
        attributeMap.put("yesno", val);

        initializedAttributes = true;
    }
    protected static Contract structSystem()
    {
        ContractImpl top = new ContractImpl("System");
        top.addMethod(new MethodImpl("general_info",
            new ParameterImpl[]{
               new ParameterImpl("topic", null, null)}));
        top.addMethod(new MethodImpl("specific_info",
            new ParameterImpl[]{
               new ParameterImpl("topic", null, null),
               new ParameterImpl("parameters", null, null)}));
        top.addMethod(new MethodImpl("send_publication",
            new ParameterImpl[]{
               new ParameterImpl("topic", null, null),
               new ParameterImpl("address", null, null)}));
        top.addMethod(new MethodImpl("transfer_call",
            new ParameterImpl[]{
               new ParameterImpl("topic", null, null),
               new ParameterImpl("action", null, null)}));
        top.addMethod(new MethodImpl("lookup_address",
            new ParameterImpl[]{
               new ParameterImpl("ssn", null, null),
               new ParameterImpl("address", null, null)}));
        top.addMethod(new MethodImpl("lookup_ctc",
            new ParameterImpl[]{
               new ParameterImpl("ssn", null, null),
               new ParameterImpl("filing_status", null, null),
               new ParameterImpl("exemptions", null, null),
               new ParameterImpl("ctc", null, null)}));
        top.addMethod(new MethodImpl("lookup_refund_status",
            new ParameterImpl[]{
               new ParameterImpl("ssn", null, null),
               new ParameterImpl("filing_status", null, null),
               new ParameterImpl("refund_amt", null, null),
               new ParameterImpl("refund_status", null, null)}));
        return top;
    }
    protected static Contract structConversation()
    {
        ContractImpl top = new ContractImpl("Conversation");
        top.addAttribute(new AttributeImpl("topic", "yesno", null));
        top.addAttribute(new AttributeImpl("action", "yesno", null));
        top.addMethod(new MethodImpl("do_action",
            new ParameterImpl[]{
               new ParameterImpl("topic", null, null),
               new ParameterImpl("action", null, null)}));
        return top;
    }
    protected static Contract taskTop()
    {
        ContractImpl top = new ContractImpl("top");
        top.addAttribute(new AttributeImpl("domore", "yesno", null));
        return top;
    }
    protected static Contract taskConversation()
    {
        ContractImpl top = new ContractImpl("conversation");
        top.addAttribute(new AttributeImpl("topic", "topic", null));
        top.addAttribute(new AttributeImpl("action", "action", null));
        return top;
    }
    protected static Contract taskGeneralInfo()
    {
        ContractImpl top = new ContractImpl("general_info");
        top.addAttribute(new AttributeImpl("topic", "topic", null));
        return top;
    }
    protected static Contract taskSpecificInfo()
    {
        ContractImpl top = new ContractImpl("specific_info");
        top.addAttribute(new AttributeImpl("topic", "topic", null));
        return top;
    }
    protected static Contract taskSendPublication()
    {
        ContractImpl top = new ContractImpl("send_publication");
        top.addAttribute(new AttributeImpl("topic", "topic", null));
        top.addAttribute(new AttributeImpl("address_valid", "yesno", null));
        top.addAttribute(new AttributeImpl("ssn", null, null));
        top.addAttribute(new AttributeImpl("publication", null, null));
        top.addAttribute(new AttributeImpl("address", null, null));
        return top;
    }
    protected static Contract taskGetStatusOfRefund()
    {
        ContractImpl top = new ContractImpl("get_status_of_refund");
        top.addAttribute(new AttributeImpl("ssn", null, null));
        top.addAttribute(new AttributeImpl("filing_status", "filing_status", null));
        top.addAttribute(new AttributeImpl("refund_amt", null, null));
        top.addAttribute(new AttributeImpl("refund_status", "refund_status", null));
        return top;
    }
    protected static Contract taskGetChildTaxCredits()
    {
        ContractImpl top = new ContractImpl("get_child_tax_credits");
        top.addAttribute(new AttributeImpl("ssn", null, null));
        top.addAttribute(new AttributeImpl("filing_status", "filing_status", null));
        top.addAttribute(new AttributeImpl("exemptions", null, null));
        top.addAttribute(new AttributeImpl("ctc", null, null));
        return top;
    }
    protected void initializeTopTask()
    {
        defaultTask = ("top");
        Contract top = taskTop();
        Plan topPlan = new Plan(top);
        Contract conversation = initializeConversationTask();

        /*
         * greet the caller
         * have a conversation
         * ask the caller if we can help with anything else.
         * if yes, have another conversation
         * if no, close conversation and end the call
         */
        topPlan.addAssert("domore","yes");
        Plan haveConversation = new Plan(top);
        haveConversation.addCall(conversation);
        haveConversation.addMove("forget",true);
        haveConversation.addFindout("domore");
        topPlan.addDoWhile("domore","yes",haveConversation);

        topPlan.addMove("greet",true);
        topPlan.addExec(top);

        taskMap.put("top", topPlan);
        keyMap.put("top", ("top"));
    }
    protected Contract initializeConversationTask()
    {
        Contract top = taskConversation();
        Plan topPlan = new Plan(top);
        Contract taskGeneralInfo = initializeGeneralInfoTask();
        Contract taskSpecificInfo = initializeSpecificInfoTask();
        Contract taskSendPublication = initializeSendPublicationTask();

        /*
         * find out the topic
         * find out the action, constrained by selected topic
         * if action is general_info, execute general_info(topic(X))
         * if action is specific_info, execute specific_info(topic(X),Y)
         * if action is send_publication, execute send_publication(topic(X),Z)
         * if action is transfer_call, execute transfer_call()
         * otherwise, we couldn't understand or process the caller's request.
         */
        topPlan.addFindout("topic");
        topPlan.addFindout("action");

        Plan generalInfo = new Plan(top);
        generalInfo.addCall(taskGeneralInfo);
        topPlan.addIfThen("action","general_info",generalInfo);

        Plan specificInfo = new Plan(top);
        specificInfo.addCall(taskSpecificInfo);
        topPlan.addIfThen("action","specific_info",specificInfo);

        Plan sendPublication = new Plan(top);
        sendPublication.addCall(taskSendPublication);
        topPlan.addIfThen("action","send_publication",sendPublication);

        Plan transferCall = new Plan(top);
        transferCall.addMethodCall("system","transfer_call");
        transferCall.addMove("forget", true);
        transferCall.addMove("reset", true);
        topPlan.addIfThen("action","transfer_call",transferCall);

        System.out.println("initializeConversationTask: "+topPlan.plan());

        taskMap.put("conversation", topPlan);
        keyMap.put("conversation", ("conversation"));
        return top;
    }
    protected Contract initializeGeneralInfoTask()
    {
        Contract top = taskGeneralInfo();
        Plan topPlan = new Plan(top);

        /*
         * find out the topic
         * inform the caller of information(topic(X))
         */
        topPlan.addFindout("topic");
        topPlan.addInformPredicate("describe","topic");

        taskMap.put("general_info", topPlan);
        keyMap.put("general_info", ("general_info"));
        return top;
    }
    protected Contract initializeSpecificInfoTask()
    {
        Contract top = taskSpecificInfo();
        Plan topPlan = new Plan(top);
        Contract taskGetStatusOfRefund = initializeGetStatusOfRefundTask();
        Contract taskGetChildTaxCredits = initializeGetChildTaxCreditsTask();

        /*
         * find out the topic
         * if topic(status_of_refund), execute get_status_of_refund
         * if topic(child_tax_credits), execute get_child_tax_credits
         */
        topPlan.addFindout("topic");

        Plan statusYes = new Plan(top);
        Plan statusNo = new Plan(top);
        statusYes.addCall(taskGetStatusOfRefund);

        Plan ctcYes = new Plan(top);
        Plan ctcNo = new Plan(top);
        ctcYes.addCall(taskGetChildTaxCredits);
        ctcNo.addInform(new Predicate("failure", new LinkedList()));
        statusNo.addIfThenElse("topic","child_tax_credits",ctcYes,ctcNo);
        topPlan.addIfThenElse("topic","refund_status",statusYes,statusNo);

        taskMap.put("specific_info", topPlan);
        keyMap.put("specific_info", ("specific_info"));
        return top;
    }
    protected Contract initializeGetStatusOfRefundTask()
    {
        Contract top = taskGetStatusOfRefund();
        Plan topPlan = new Plan(top);

        /*
         * find out ssn(X)
         * find out filing_status(Y)
         * find out refund_amt(Z)
         * lookup_refund_status(ssn(X),filing_status(Y),refund_amt(Z),refund_status(S))
         * inform refund_status(S)
         */
        topPlan.addFindout("ssn");
        topPlan.addFindout("filing_status");
        topPlan.addFindout("refund_amt");
        topPlan.addQueryCall("system","lookup_refund_status");
        topPlan.addInform("refund_status");

        taskMap.put("get_status_of_refund", topPlan);
        keyMap.put("get_status_of_refund", ("get_status_of_refund"));
        return top;
    }
    protected Contract initializeGetChildTaxCreditsTask()
    {
        Contract top = taskGetChildTaxCredits();
        Plan topPlan = new Plan(top);

        /*
         * find out the topic
         * if topic(status_of_refund), execute get_status_of_refund
         * if topic(child_tax_credits), execute get_child_tax_credits
         */
        topPlan.addFindout("ssn");
        topPlan.addFindout("filing_status");
        topPlan.addFindout("exemptions");
        topPlan.addQueryCall("system","lookup_ctc");
        topPlan.addInform("ctc");

        taskMap.put("get_child_tax_credits", topPlan);
        keyMap.put("get_child_tax_credits", ("get_child_tax_credits"));
        return top;
    }
    protected Contract initializeSendPublicationTask()
    {
        Contract top = taskSendPublication();
        Plan topPlan = new Plan(top);

        /*
         * find out the topic
         * find out if caller's last address is valid
         * find out caller's SSN
         * get_publication(topic(X), pub(Y))
         * send_publication(pub(Y), address(Z))
         */
        topPlan.addFindout("topic");
        topPlan.addFindout("address_valid");

        Plan addressYes = new Plan(top);
        Plan addressNo = new Plan(top);
        addressYes.addFindout("ssn");
        addressYes.addQueryCall("system","lookup_address");
        addressYes.addQueryCall("system","lookup_publication");
        addressYes.addMethodCall("system","send_publication");
        addressYes.addInform(new Predicate("sending", new LinkedList()));

        // address changed or no prior return; transfer call
        addressNo.addMethodCall("system","transfer_call");
        addressNo.addMove("forget", true);
        addressNo.addMove("reset", true);

        topPlan.addIfThenElse("address_valid","yes",addressYes,addressNo);

        taskMap.put("send_publication", topPlan);
        keyMap.put("send_publication", ("send_publication"));
        return top;
    }
    protected void initializeTasks()
    {
        initializeTopTask();

        if (!initializedQuestions) initializeQuestions();
        calculateTaskDominationAndRelevance();
        initializedTasks = true;
    }
    protected void initializeQuestions()
    {
        Iterator qiter = taskMap.entrySet().iterator();
        while (qiter.hasNext()) {
            Map.Entry me = (Map.Entry)qiter.next();
            Plan p = (Plan)me.getValue();
            Iterator atit = p.getTask().attributes();
            while (atit.hasNext()) {
                AttributeImpl at = (AttributeImpl)atit.next();
                String name = at.name();
                String type = (String)at.type();
                questionTypes.put(name, type);
            }
        }

        initializedQuestions = true;
    }
}
