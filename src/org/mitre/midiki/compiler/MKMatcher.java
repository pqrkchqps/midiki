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
/** Implements a visitor that parses MKParser input based on
 * a language specification provided at creation time.
 *
 * Initially, the language spec consists of statement
 * pattern definitions. Matching those patterns is a matter
 * of traversing a state graph based on the input, where a
 * new graph is entered at each new statement. This can be
 * done by building a DFA; less formally, it could be done
 * by traversing the graph on the fly, which is less efficient.
 */

package org.mitre.midiki.compiler;

import java.util.*;

import org.mitre.midiki.compiler.parser.*;

public class MKMatcher implements MKParserVisitor
{
    private MKDFA dfa; // base DFA
    private boolean[] avail;
    private int availCnt;
    private void setAvailable()
    {
        Vector succ = dfa.current.successors;
        try {
            availCnt = succ.size();
        } catch (Exception e) {
            availCnt = 0;
        }
        for (int i=0; i<availCnt; i++) {
            avail[i] = true;
        }
    }
    private boolean canEnter(SimpleNode node, Object data)
    {
        boolean enter = false;
        for (int i=0; i<availCnt; i++) {
            if (avail[i]) {
                MKDFANode dfanode =
                    (MKDFANode)(dfa.current.successors.elementAt(i));
                avail[i] = dfa.canEnter(dfanode.transition, node);
            }
            enter = enter || avail[i];
        }
        return enter;
    }
    private int canMatch(SimpleNode node, Object data)
    {
        for (int i=0; i<availCnt; i++) {
            if (avail[i]) {
                MKDFANode dfanode =
                    (MKDFANode)(dfa.current.successors.elementAt(i));
                if (dfa.canMatch(dfanode.transition, node)) return i;
            }
        }
        return -1;
    }
    private void expandSubnode(int i, ASTPrimaryExpression node, Object data)
    {
        MKDFANode dfanode =
            (MKDFANode)(dfa.current.successors.elementAt(i));
        if (dfanode.subGrammar != null) {
            MKMatcher submatch = new MKMatcher(new MKDFA(dfanode.subGrammar));
            submatch.setAvailable();
            data = node.childrenAccept(submatch, data);
        }
    }
    public MKMatcher()
    {
        avail = new boolean[128];
    }
    public MKMatcher(MKLanguageSpec ls)
    {
        this();
        dfa = new MKDFA(ls);
    }
    public MKMatcher(MKDFA d)
    {
        this();
        dfa = d;
    }
    public Object visit(SimpleNode node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTCompilationUnit node, Object data) {
        setAvailable();
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTName node, Object data) {
        if (!canEnter(node, data)) {
            System.out.println("name not accessible here");
            return data;
        }
        int match = canMatch(node, data);
        if (match != -1) {
            System.out.println("name matched");
            dfa.advance(match);
            setAvailable();
            return data;
        }
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTDesignator node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTIdentifier node, Object data) {
        if (!canEnter(node, data)) {
            System.out.println("identifier not accessible here");
            return data;
        }
        int match = canMatch(node, data);
        if (match != -1) {
            System.out.println("identifier matched: "+node.getName());
            dfa.advance(match);
            setAvailable();
            return data;
        }
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTVariable node, Object data) {
        if (!canEnter(node, data)) {
            System.out.println("variable not accessible here");
            return data;
        }
        int match = canMatch(node, data);
        if (match != -1) {
            System.out.println("variable matched: "+node.getName());
            dfa.advance(match);
            setAvailable();
            return data;
        }
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTQualifierList node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTDotQualifier node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTHashQualifier node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTAssignmentOperator node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTLogicalExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTLogicalOperator node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTRelationalExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTRelationalOperator node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTAdditiveExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTAdditiveOperator node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTMultiplicativeExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTMultiplicativeOperator node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTUnaryExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTNegationOperator node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTPrimaryExpression node, Object data) {
        if (!canEnter(node, data)) {
            System.out.println("primary not accessible here");
            return data;
        }
        int match = canMatch(node, data);
        if (match != -1) {
            System.out.println("primary matched");
            expandSubnode(match, node, data);
            dfa.advance(match);
            setAvailable();
            return data;
        }
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTLiteral node, Object data) {
        if (!canEnter(node, data)) {
            System.out.println("literal not accessible here");
            return data;
        }
        int match = canMatch(node, data);
        if (match != -1) {
            System.out.println("literal matched");
            dfa.advance(match);
            setAvailable();
            return data;
        }
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTIntegerLiteral node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTFloatingPointLiteral node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTCharacterLiteral node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTStringLiteral node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTArguments node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTArgumentList node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTExpressions node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTExpressionList node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTStatements node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTStatementList node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTStatement node, Object data) {
        //if (!canEnter(node, data)) {
        //    System.out.println("statement not accessible here");
        //    return data;
        //}
        int match = canMatch(node, data);
        if (match != -1) {
            System.out.println("unconditional statement matched");
            dfa.advance(match);
            setAvailable();
            return data;
        }
        data = node.childrenAccept(this, data);
        int st = dfa.accept();
        if (st==-1) {
            System.out.println("unrecognized statement");
        } else {
            System.out.println("accepted statement "+st);
        }
        dfa.reset();
        return data;
    }
}
