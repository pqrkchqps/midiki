/* Generated By:JJTree: Do not edit this line. ASTArguments.java */

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
package org.mitre.midiki.compiler.parser;

import java.util.*;

public class ASTArguments extends SimpleNode {
  public ASTArguments(int id) {
    super(id);
  }

  public ASTArguments(MKParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(MKParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

    public MKInstruction generateArity(LinkedList buffer)
    {
        int argcount = 0;
        MKInstruction arity =
            new MKInstruction(MKInstruction.MKIN_ARITY, new Integer(argcount));
        arity.setLexicalHook(this);
        buffer.addLast(arity);
        if (children == null) return arity;
        ASTArgumentList al = (ASTArgumentList)children[0];
        if (al.children == null) return arity;
        for (int i = 0; i < al.children.length; ++i) {
            SimpleNode n = (SimpleNode)al.children[i];
            if (n != null) {
                argcount++;
            }
        }
        arity.setArgument(new Integer(argcount));
        return arity;
    }

    public void generateArguments(LinkedList buffer, MKInstruction prev)
    {
        MKInstruction arginst = null;
        if (children == null) return;
        ASTArgumentList al = (ASTArgumentList)children[0];
        if (al.children == null) return;
        for (int i = 0; i < al.children.length; ++i) {
            SimpleNode n = (SimpleNode)al.children[i];
            if (n != null) {
                arginst =
                    new MKInstruction(MKInstruction.MKIN_ARG, null);
                arginst.setLexicalHook(this);
                if (prev != null) prev.setArgument(arginst);
                prev = arginst;
                buffer.addLast(arginst);
                n.generateCode(buffer);
            }
        }
        arginst =
            new MKInstruction(MKInstruction.MKIN_ENDARGS, null);
        arginst.setLexicalHook(this);
        if (prev != null) prev.setArgument(arginst);
        prev = arginst;
        buffer.addLast(arginst);
    }
}