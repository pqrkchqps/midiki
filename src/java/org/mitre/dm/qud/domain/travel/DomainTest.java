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
 * Performs some simple tests of Midiki unification and major
 * domain operations. Not intended as a formal test suite;
 * may be removed and/or replaced in a future release.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class DomainTest
{
    public DomainTest()
    {
    }

    public void runTests()
    {
        /*
         * some basic unification tests
         */
        Variable x = new Variable("X");
        Variable y = new Variable("Y");
        BindingsImpl bu = new BindingsImpl();
        System.out.println("*** unification of atoms.");
        bu.enterScope();
        System.out.println("matchTerms(malome,malome) == "+
                           Unify.getInstance().matchTerms("malome","malome",bu));
        System.out.println("bu == "+bu);
        bu.exitScope();
        bu.enterScope();
        System.out.println("matchTerms(malome,salome) == "+
                           Unify.getInstance().matchTerms("malome","salome",bu));
        System.out.println("bu == "+bu);
        bu.exitScope();
        System.out.println("*** unification of atoms with variables: success.");
        bu.enterScope();
        System.out.println("matchTerms(Y,malome) == "+
                           Unify.getInstance().matchTerms(y,"malome",bu));
        System.out.println("bu == "+bu);
        //bu.exitScope();
        //bu.enterScope();
        System.out.println("matchTerms(X,salome) == "+
                           Unify.getInstance().matchTerms(x,"malome",bu));
        System.out.println("bu == "+bu);
        System.out.println("matchTerms(X,Y) == "+
                           Unify.getInstance().matchTerms(x,y,bu));
        System.out.println("bu == "+bu);
        bu.exitScope();
        System.out.println("*** unification of atoms with variables: failure.");
        bu.enterScope();
        System.out.println("matchTerms(Y,malome) == "+
                           Unify.getInstance().matchTerms(y,"malome",bu));
        System.out.println("bu == "+bu);
        //bu.exitScope();
        //bu.enterScope();
        System.out.println("matchTerms(X,salome) == "+
                           Unify.getInstance().matchTerms(x,"salome",bu));
        System.out.println("bu == "+bu);
        System.out.println("matchTerms(X,Y) == "+
                           Unify.getInstance().matchTerms(x,y,bu));
        System.out.println("bu == "+bu);
        bu.exitScope();
        /*
         * domain tests
         */
        travel_domain td = new travel_domain();
        BindingsImpl bi = new BindingsImpl();
        Variable q = new Variable("Q");
        Variable r = new Variable("R");
        System.out.println ("**** relevant_answer(Q,R)");
        bi.enterScope();
        td.doQuery_domain_relevant_answer("travel", q, r, bi);
        for (boolean ok = bi.initializeBacktracking();
             ok;
             ok = bi.backtrack()) {
            System.out.println(bi);
        }
        System.out.println ("****");
        bi.exitScope();
        System.out.println(bi);
        System.out.println ("**** relevant_answer(to(Q),to(R))");
        ArrayList al = new ArrayList();
        al.add(q);
        Predicate to_q = new Predicate("to", al);
        al = new ArrayList();
        al.add(r);
        Predicate to_r = new Predicate("to", al);
        bi.enterScope();
        td.doQuery_domain_relevant_answer("travel", to_q, to_r, bi);
        for (boolean ok = bi.initializeBacktracking();
             ok;
             ok = bi.backtrack()) {
            System.out.println(bi);
        }
        System.out.println ("****");
        bi.exitScope();
        System.out.println(bi);
        System.out.println ("**** relevant_answer(to(Q),to(malmoe))");
        al = new ArrayList();
        al.add(q);
        to_q = new Predicate("to", al);
        al = new ArrayList();
        al.add("malmoe");
        to_r = new Predicate("to", al);
        bi.enterScope();
        td.doQuery_domain_relevant_answer("travel", to_q, to_r, bi);
        for (boolean ok = bi.initializeBacktracking();
             ok;
             ok = bi.backtrack()) {
            System.out.println(bi);
        }
        System.out.println ("****");
        bi.exitScope();
        System.out.println ("**** relevant_category(Q,R)");
        bi.enterScope();
        td.doQuery_domain_relevant_category("travel", q, r, bi);
        for (boolean ok = bi.initializeBacktracking();
             ok;
             ok = bi.backtrack()) {
            System.out.println(bi);
        }
        System.out.println ("****");
        bi.exitScope();
        System.out.println(bi);
        System.out.println ("**** relevant_category(to(Q),R)");
        al = new ArrayList();
        al.add(q);
        to_q = new Predicate("to", al);
        bi.enterScope();
        td.doQuery_domain_relevant_category("travel", to_q, r, bi);
        for (boolean ok = bi.initializeBacktracking();
             ok;
             ok = bi.backtrack()) {
            System.out.println(bi);
        }
        System.out.println ("****");
        bi.exitScope();
        System.out.println(bi);
        System.out.println ("**** relevant_category(Q,location)");
        bi.enterScope();
        td.doQuery_domain_relevant_category("travel", q, "location", bi);
        for (boolean ok = bi.initializeBacktracking();
             ok;
             ok = bi.backtrack()) {
            System.out.println(bi);
        }
        System.out.println ("****");
        bi.exitScope();
    }

    static public final void main(String[] args)
    {
        DomainTest dt = new DomainTest();
        dt.runTests();
    }
}
