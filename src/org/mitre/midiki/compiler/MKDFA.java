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
package org.mitre.midiki.compiler;

import java.util.*;
import java.io.Serializable;

import org.mitre.midiki.compiler.parser.*;

public class MKDFA implements Serializable
{
    protected MKDFANode root;
    protected MKDFANode current;

    public MKDFA()
    {
    }

    /**
     * Shallow cloning constructor.
     *
     * @param dfa a <code>MKDFA</code> value
     */
    public MKDFA(MKDFA dfa)
    {
        this();
        root = dfa.root;
        reset();
    }

    /**
     * Creates a new <code>MKDFA</code> instance from the supplied
     * language specification. Will create nested DFAs as required,
     * so long as the language specification is well-formed.
     *
     * @param ls a <code>MKLanguageSpec</code> value
     */
    public MKDFA(MKLanguageSpec ls)
    {
        this();
        Object[] work = ls.validStatements.toArray();
        MKLanguageSpecPatternElement[][] patterns =
            new MKLanguageSpecPatternElement[work.length][];
        int[] patidx = new int[work.length];
        for (int i=0; i<work.length; i++)
        {
            MKLanguageSpecPatternElement[] pat =
                new MKLanguageSpecPatternElement[((Vector)work[i]).size()];
            patterns[i] =
                (MKLanguageSpecPatternElement[])((Vector)work[i]).toArray(pat);
            patidx[i] = i;
        }
        /* sort the patterns lexicographically */
        for (int i=0; i<work.length-1; i++)
        {
            for (int j=i+1; j<work.length; j++)
            {
                if (comparePatterns(patterns[i], patterns[j])>0)
                {
                    // patterns out of sorted order
                    MKLanguageSpecPatternElement[] mke = patterns[i];
                    int mki = patidx[i];
                    patterns[i] = patterns[j];
                    patidx[i] = patidx[j];
                    patterns[j] = mke;
                    patidx[j] = mki;
                }
            }
        }
        /* initialize node merger references */
        MKLanguageSpecPatternElement[][] mergeWith =
            new MKLanguageSpecPatternElement[work.length][];
        for (int i=0; i<work.length; i++)
        {
            mergeWith[i] =
                new MKLanguageSpecPatternElement[patterns[i].length];
        }
        /* mark nodes for combination */
        root = new MKDFANode(); // start node
        Vector succ = markMergingNodes(0, patterns.length-1, 0, patterns, patidx, root);
        root.successors = succ;

        reset();
    }

    /**
     * Compare two pattern specifications lexicographically.
     *
     * @param one a pattern as a <code>MKLanguageSpecPatternElement[]</code>
     * @param two a pattern as a <code>MKLanguageSpecPatternElement[]</code>
     * @return the value 0 if the patterns are identical, a value less than
     * 0 if pattern one is less than pattern two, and a value greater
     * than 0 if pattern one is greater than pattern two.
     */
    private int comparePatterns(MKLanguageSpecPatternElement[] one,
                                MKLanguageSpecPatternElement[] two)
    {
        int idx=0;
        while ((idx<one.length) && (idx<two.length))
        {
            int cmp = one[idx].element.compareTo(two[idx].element);
            if (cmp != 0) return cmp;
            idx++;
        }
        return (one.length - two.length);
    }

    private Vector markMergingNodes(int first, int last, int ptr,
                                    MKLanguageSpecPatternElement[][] patterns,
                                    int[] patidx, MKDFANode parent)
    {
        if (first > last)
        {
            return null;
        }
        Vector curr;
        Vector succ = null;
        if (first == last)
        {
            if (patterns[first].length < ptr)
            {
                return null;
            }
            else if (patterns[first].length == ptr)
            {
                if (parent.productionIndex != -1)
                {
                    System.out.println("Error: duplicate productions");
                }
                parent.productionIndex = patidx[first];
                return null;
            }
            else {
                MKDFANode newNode = new MKDFANode();
                newNode.transition = patterns[first][ptr].element;
                if (patterns[first][ptr].sublanguage != null)
                {
                    newNode.subGrammar =
                        new MKDFA(patterns[first][ptr].sublanguage);
                }
                curr = new Vector();
                curr.addElement(newNode);
                newNode.successors =
                    markMergingNodes(first, last, ptr+1,
                                     patterns, patidx, newNode);
                return curr;
            }
        }
        curr = new Vector();
        int start=first;
        while (start <= last) {
            int i;
            for (i=start+1; i<=last; i++)
            {
                if ((patterns[start].length > ptr) &&
                    (patterns[i].length > ptr) &&
                    (patterns[start][ptr].element.compareTo(patterns[i][ptr].element)==0))
                {
                    if (patterns[start][ptr].sublanguage !=
                        patterns[i][ptr].sublanguage)
                    {
                        System.out.println("Error: Conflicting sublanguages");
                    }
                }
                else break;
            }
            // 'i' is now index of first failed match at position ptr
            // running out of options counts as a failed match.
            if (patterns[start].length == ptr)
            {
                if (parent.productionIndex != -1)
                {
                    System.out.println("Error: multiple null transitions!");
                }
                parent.productionIndex = patidx[start];
            }
            else
            {
                MKDFANode newNode = new MKDFANode();
                succ = markMergingNodes(start, i-1, ptr+1, patterns, patidx, newNode);
                newNode.successors = succ;
                newNode.transition = patterns[start][ptr].element;
                if (patterns[start][ptr].sublanguage != null)
                {
                    newNode.subGrammar =
                        new MKDFA(patterns[start][ptr].sublanguage);
                }
                curr.addElement(newNode);
            }
            start = i;
        }
        return curr;
    }

    /**
     * Check a statement for compliance, returning index of match or -1.
     */
    public int matches(ASTStatement st)
    {
        return -1;
    }

    /**
     * Reinitialize DFA to start node.
     *
     */
    public void reset()
    {
        current = root;
    }

    /**
     * If the DFA can accept an input which ends here, return the index
     * of the statement spec accepted.
     *
     * @return an <code>int</code> value
     */
    public int accept()
    {
        return current.productionIndex;
    }

    public boolean canEnter(String succ, SimpleNode sn)
    {
        if (succ.equals("_statement")) {
            return (sn instanceof ASTStatement);
        } else if (sn instanceof ASTPrimaryExpression) {
            ASTPrimaryExpression pe = (ASTPrimaryExpression)sn;
            Object o = predefined.get(succ);
            System.out.println("succ "+succ+" yields "+o);
            if (o==null) {
                return (pe.getBranch() == ASTPrimaryExpression.br_nam);
            } else {
                Integer i = (Integer)o;
                if (i.intValue()==0) {
                    return ((pe.getBranch() != ASTPrimaryExpression.br_blk) &&
                            (pe.getBranch() != ASTPrimaryExpression.br_lst));
                } else {
                    return (i.intValue() == pe.getBranch());
                }
            }
        } else {
            System.out.println("canEnter called for "+succ+" at "+sn);
        }
        return true;
    }
    // returns true if this particular node succeeds as a match
    // for the specified result type.
    public boolean canMatch(String succ, SimpleNode sn)
    {
        if (succ.equals("_statement")) {
            return (sn instanceof ASTStatement);
        } else if (succ.equals("_expression")) {
            // handle typed arbitrary expressions differently
            return false;
        } else {
            Object o = predefined.get(succ);
            if (o==null) {
                // the result is a constant, so must be matched exactly
                if (sn instanceof ASTIdentifier) {
                    ASTIdentifier idnode = (ASTIdentifier)sn;
                    return succ.equals(idnode.getName());
                } else if (sn instanceof ASTVariable) {
                    ASTVariable varnode = (ASTVariable)sn;
                    return succ.equals(varnode.getName());
                } else return false;
            } else {
                Integer i = (Integer)o;
                switch (i.intValue()) {
                    case 0:
                    case ASTPrimaryExpression.br_par:
                        // expression; must match a specified type
                        break;
                    case ASTPrimaryExpression.br_lit:
                        if (sn instanceof ASTLiteral) {
                            ASTLiteral litrl = (ASTLiteral)sn;
                            switch (litrl.getBranch()) {
                                case ASTLiteral.br_int:
                                    return (succ.equals("_integer") ||
                                            succ.equals("_number"));
                                case ASTLiteral.br_flt:
                                    return (succ.equals("_real") ||
                                            succ.equals("_number"));
                                case ASTLiteral.br_chr:
                                    return (succ.equals("_char"));
                                case ASTLiteral.br_str:
                                    return (succ.equals("_string"));
                                default:
                                    return false;
                            }
                        } else {
                            return false;
                        }
                    case ASTPrimaryExpression.br_lst:
                        return (succ.equals("_list"));
                    case ASTPrimaryExpression.br_blk:
                        return (succ.equals("_block"));
                    case ASTPrimaryExpression.br_nam:
                        if (succ.equals("_name")) {
                            if (sn instanceof ASTName) {
                                return !((ASTName)sn).hasArgs();
                            } else {
                                return false;
                            }
                        } else if (succ.equals("_designator")) {
                            if (sn instanceof ASTName) {
                                ASTName des = (ASTName)sn;
                                return !des.isQualified();
                            } else {
                                return false;
                            }
                        } else if (succ.equals("_identifier")) {
                            return (sn instanceof ASTIdentifier);
                        } else if (succ.equals("_variable")) {
                            return (sn instanceof ASTVariable);
                        } else {
                            return false;
                        }
                    case ASTPrimaryExpression.br_fnc:
                        if (succ.equals("_function")) {
                            if (sn instanceof ASTName) {
                                return ((ASTName)sn).hasArgs();
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    default: break; // unexpected case
                }
            }
        }
        return false;
        }
    public boolean canExit(String succ, SimpleNode sn)
    {
        if (sn instanceof ASTStatement) {
            return (accept() != -1);
        }
        return false;
    }
    /**
     * Advance the DFA to the specified successor node.
     *
     * @param sn the <code>SimpleNode</code> or derivative for the
     *           current position in the syntax tree.
     * @return true if the supplied node is legal at this point.
     */
    public boolean advance(int i)
    {
        // if we can't go anywhere from here, report the error.
        if (current.successors.size()==0) {
            current = errorNode;
            return false;
        }
        if ((current.successors.size()<i) || (i<0)) {
            current = errorNode;
            return false;
        }
        current = (MKDFANode)(current.successors.elementAt(i));
        return false;
    }
    /**
     * If the engine enters an erroneous state, set the current node to
     * errorNode. Error information should also be stored in there.
     */
    static protected MKDFANode errorNode;
    static protected HashMap predefined;
    static protected final String[] predefined_list = {
        "_name",
        "_designator",
        "_identifier",
        "_variable",
        "_expression", // use hash qualifier for typing
        "_string",
        "_integer",
        "_real",
        "_number",
        "_char",
        "_list",
        "_block",
        "_function",
        "_statement"
    };
    static protected final int[] predef_prim = {
        ASTPrimaryExpression.br_nam,
        ASTPrimaryExpression.br_nam,
        ASTPrimaryExpression.br_nam,
        ASTPrimaryExpression.br_nam,
        0, // _expression requires special handling
        ASTPrimaryExpression.br_lit,
        ASTPrimaryExpression.br_lit,
        ASTPrimaryExpression.br_lit,
        ASTPrimaryExpression.br_lit,
        ASTPrimaryExpression.br_lit,
        ASTPrimaryExpression.br_lst,
        ASTPrimaryExpression.br_blk,
        ASTPrimaryExpression.br_fnc,
        -1 // statement not valid here
    };
    static
    {
        errorNode = new MKDFANode();
        predefined = new HashMap();
        for (int i=0; i<predefined_list.length; i++)
            predefined.put(predefined_list[i], new Integer(predef_prim[i]));
    }
}
