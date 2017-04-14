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
package org.mitre.dm;

import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;

import java.util.*;

/**
 * Provides a source for common contract definitions
 * within the dialogue system examples.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class ContractDatabase
{
    static protected HashMap db;
    static
    {
        db = new HashMap();
        db.put("lu",FS_LU());
        db.put("shared",FS_Shared());
        db.put("private",FS_Private());
        db.put("is",FS_IS());
        db.put("domain",FS_Domain());
        db.put("lexicon",FS_Lexicon());
        db.put("interpreter",FS_Interpreter());
        db.put("database",FS_Database());
        db.put("rec",FS_Rec());
        db.put("output",FS_Output());
    }
    static public Contract find(String name)
    {
        return (Contract)db.get(name);
    }
    protected static Contract FS_LU()
    {
        ContractImpl lu = new ContractImpl("lu");
        lu.addAttribute(new AttributeImpl("speaker", null, null));
        lu.addAttribute(new AttributeImpl("moves", null, null));   // List
        return lu;
    }
    protected static Contract FS_Shared()
    {
        ContractImpl shared = new ContractImpl("shared");
        shared.addAttribute(new AttributeImpl("bel", null, null));  // List
        shared.addAttribute(new AttributeImpl("qud", null, null));  // List/Stack
        shared.addAttribute(new AttributeImpl("com", null, null));  // List; accessed by update
        shared.addAttribute(new AttributeImpl("nim", null, null));  // accessed by update
        shared.addAttribute(new AttributeImpl("lu", (Contract)db.get("lu"), null));
        return shared;
    }
    protected static Contract FS_Private()
    {
        ContractImpl priv = new ContractImpl("private");
        priv.addAttribute(new AttributeImpl("plan", null, null));
        priv.addAttribute(new AttributeImpl("agenda", null, null));  // Stack
        priv.addAttribute(new AttributeImpl("topic_shift", null, null));
        priv.addAttribute(new AttributeImpl("bel", null, null));
        priv.addAttribute(new AttributeImpl("tmp", (Contract)db.get("shared"), null));
        return priv;
    }
    protected static Contract FS_IS()
    {
        ContractImpl is = new ContractImpl("is");
        is.addAttribute(new AttributeImpl("turn_number", "Integer", null));
        is.addAttribute(new AttributeImpl("turn_taker", "String", null));
        is.addAttribute(new AttributeImpl("user_input", null, null));
        is.addAttribute(new AttributeImpl("latest_speaker", null, null));
        is.addAttribute(new AttributeImpl("latest_moves", null, null));  // List
        is.addAttribute(new AttributeImpl("next_moves", null, null));    // List
        is.addAttribute(new AttributeImpl("turn_for_moves", "Integer", null));
        is.addAttribute(new AttributeImpl("moves_for_turn", List.class, null));
        is.addAttribute(new AttributeImpl("moves_for_last_turn", List.class, null));
        is.addAttribute(new AttributeImpl("input", null, null));
        is.addAttribute(new AttributeImpl("output", null, null));
        is.addAttribute(new AttributeImpl("user_output", null, null));
        is.addAttribute(new AttributeImpl("program_state", null, null));
        is.addAttribute(new AttributeImpl("private", (Contract)db.get("private"), null));
        is.addAttribute(new AttributeImpl("shared", (Contract)db.get("shared"), null));
        return is;
    }
    protected static Contract FS_Domain()
    {
        ContractImpl domain = new ContractImpl("domain");
        domain.addQuery(new QueryImpl("reduce",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null), 
                new ParameterImpl("q", null, null),
                new ParameterImpl("r", null, null),
                new ParameterImpl("p", null, null)}));
        domain.addQuery(new QueryImpl("relevant_answer",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null),
                new ParameterImpl("q", null, null),
                new ParameterImpl("r", null, null)}));    // matches questions to specific answers
        domain.addQuery(new QueryImpl("relevant_category",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null),
                new ParameterImpl("q", null, null),
                new ParameterImpl("r", null, null)}));  // matches questions to attributes
        domain.addQuery(new QueryImpl("abstract",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null),
                new ParameterImpl("a", null, null),
                new ParameterImpl("oldp", null, null),
                new ParameterImpl("q", null, null)}));
        domain.addQuery(new QueryImpl("relevant_to_task",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null),
                new ParameterImpl("move", null, null),
                new ParameterImpl("task", null, null),
                new ParameterImpl("plan", null, null)}));
        domain.addQuery(new QueryImpl("relevant_to_tasks",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null),
                new ParameterImpl("move", null, null),
                new ParameterImpl("tasks", null, null)}));
        domain.addQuery(new QueryImpl("default",
            new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("task", null, null)}));
        domain.addQuery(new QueryImpl("dominates",
            new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("oldTask", null, null),
               new ParameterImpl("task", null, null)}));
        domain.addQuery(new QueryImpl("plan",
            new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("t", null, null),
               new ParameterImpl("plan", null, null)}));
        domain.addQuery(new QueryImpl("plan_task",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null),
                new ParameterImpl("plan", null, null),
                new ParameterImpl("task", null, null)}));
        domain.addQuery(new QueryImpl("sysaction",
            new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("a", null, null)}));
        domain.addQuery(new QueryImpl("all_answers",
            new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("question", null, null),
               new ParameterImpl("answers", null, null)}));
        //
        // Plan execution
        //
        domain.addMethod(new MethodImpl("instantiate_plan",
            new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("code", null, null),
               new ParameterImpl("objid", null, null)}));
        domain.addQuery(new QueryImpl("next_step",
           new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("plan", null, null),
               new ParameterImpl("step", null, null)}));
        domain.addMethod(new MethodImpl("advance",
            new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("plan", null, null),
               new ParameterImpl("annotations", null, null)}));                // optional logging of plan execution
        domain.addMethod(new MethodImpl("reset",
            new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("plan", null, null)}));
        domain.addMethod(new MethodImpl("set_answer",
            new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("plan", null, null),
               new ParameterImpl("answer", null, null)}));      // answers posed as question(answer)
        domain.addQuery(new QueryImpl("get_answer",
           new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("plan", null, null),
               new ParameterImpl("question", null, null)}));
        domain.addMethod(new MethodImpl("retract_answer",
            new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("plan", null, null),
               new ParameterImpl("question", null, null)}));
        domain.addQuery(new QueryImpl("wants_answer",
           new ParameterImpl[]{
               new ParameterImpl("iptr", null, null),
               new ParameterImpl("plan", null, null),
               new ParameterImpl("question", null, null)})); // check all findout actions in plan
        return domain;
    }
    protected static Contract FS_Lexicon()
    {
        ContractImpl lexicon = new ContractImpl("lexicon");
        lexicon.addMethod(new MethodImpl("wordlist2moves",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null), 
                new ParameterImpl("wordlist", null, null),
                new ParameterImpl("moves", null, null)}));
        lexicon.addMethod(new MethodImpl("movelist2words",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null), 
                new ParameterImpl("movelist", null, null),
                new ParameterImpl("words", null, null)}));
        lexicon.addMethod(new MethodImpl("remove_duplicates",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null), 
                new ParameterImpl("movesin", null, null),
                new ParameterImpl("movesout", null, null)}));
        return lexicon;
    }
    protected static Contract FS_Interpreter()
    {
        ContractImpl interpreter = new ContractImpl("interpreter");
        interpreter.addMethod(new MethodImpl("string2wordlist",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null),
                new ParameterImpl("string", null, null),
                new ParameterImpl("wordlist", null, null)}));
        return interpreter;
    }
    protected static Contract FS_Database()
    {
        ContractImpl database = new ContractImpl("database");
        database.addMethod(new MethodImpl("consultDB",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null),
                new ParameterImpl("q", null, null), 
                new ParameterImpl("shared_bel", null, null), 
                new ParameterImpl("r", null, null)}));
        return database;
    }
    // the contract for rec is a kludge, so if it goes away, great.
    protected static Contract FS_Rec()
    {
        ContractImpl rec = new ContractImpl("rec");
        rec.addMethod(new MethodImpl("set",             // resolved to put()
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null),
                new ParameterImpl("src", null, null)}));
        rec.addMethod(new MethodImpl("copy",            // resolved to put(get(...))
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("src", null, null)}));
        rec.addQuery(new QueryImpl("val",               // resolved to get()+unify
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("src", null, null)}));
        rec.addMethod(new MethodImpl("push",            // resolved to Stack.push
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("src", null, null)}));
        rec.addQuery(new QueryImpl("fst",     // defined on List, can't backtrack
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("src", null, null)}));
        rec.addMethod(new MethodImpl("clear", // defined on Cell and individual types
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null)}));
        rec.addQuery(new QueryImpl("empty",   // defined on Collection, can't backtrack
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null)}));
        rec.addMethod(new MethodImpl("del", 
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("item", null, null)}));
        rec.addMethod(new MethodImpl("add",    // resolved to List.add
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("newv", null, null)}));
        rec.addMethod(new MethodImpl("extend", 
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("item", null, null)}));
        rec.addQuery(new QueryImpl("assoc", 
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("newv", null, null),
                 new ParameterImpl("flag", null, null)}));
        rec.addMethod(new MethodImpl("set_assoc", 
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("newv", null, null),
                 new ParameterImpl("flag", null, null),
                 new ParameterImpl("result", null, null)}));
        rec.addMethod(new MethodImpl("addAll", 
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("newv", null, null),
                 new ParameterImpl("flag", null, null)}));
        rec.addMethod(new MethodImpl("pop",     // resolved to Stack.pop
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null)}));
        rec.addQuery(new QueryImpl("in",        // resolves to binding iteration
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("item", null, null)}));
        rec.addQuery(new QueryImpl("unifies", 
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("term", null, null)}));
        rec.addQuery(new QueryImpl("length", 
             new ParameterImpl[]{
                 new ParameterImpl("iptr", null, null),
                 new ParameterImpl("len", null, null)}));
        return rec;
    }
    protected static Contract FS_Output()
    {
        ContractImpl output = new ContractImpl("output");
        output.addAttribute(new AttributeImpl("output", null, null));
        output.addAttribute(new AttributeImpl("output_index", "Integer", null));
        output.addAttribute(new AttributeImpl("output_for_turn", "Integer", null));
        output.addMethod(new MethodImpl("put",
            new ParameterImpl[]{
                new ParameterImpl("iptr", null, null),
                new ParameterImpl("utterance", null, null)}));
        return output;
    }
    protected static Contract FS_Builtin()
    {
        // attr true; -- use Java keyword
        // attr false;-- use Java keyword
        // exec unify(result,arg1,arg2); -- method on ImmutableInfoState
        // operator "$==" is unify; -- scripting language only
        // exec set(var,value); -- method on InfoState
        // exec clear(var); -- method on InfoState
        // exec log_module_use(source,module,message); -- logger interface
        // exec log_rule_use(source,rule,message); -- logger interface
        // exec log_rule_status(source,rule,testindex,message); -- logger
        return null;
    }
}
