/* Generated By:JJTree: Do not edit this line. ASTMultiplicativeExpression.java */

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

public class ASTMultiplicativeExpression extends SimpleNode {
  public ASTMultiplicativeExpression(int id) {
    super(id);
  }

  public ASTMultiplicativeExpression(MKParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(MKParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

    public void generateCode(LinkedList buffer)
    {
        if (children == null) return;
        if (children.length > 1) {
            generateNestedCode(buffer, children.length-2);
        } else {
            // not an operator, so just do a passthrough
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode)children[i];
                if (n != null) {
                    n.generateCode(buffer);
                }
            }
        }
    }

    public void generateNestedCode(LinkedList buffer, int start)
    {
        MKInstruction mkin = null;
        MKInstruction arity =
            new MKInstruction(MKInstruction.MKIN_ARITY, new Integer(4));
        arity.setLexicalHook(this);
        buffer.addLast(arity);
        MKInstruction argthis =
            new MKInstruction(MKInstruction.MKIN_ARG, null);
        argthis.setLexicalHook(this);
        buffer.addLast(argthis);
        MKInstruction thisPtr =
            new MKInstruction(MKInstruction.MKIN_FCNAME, "_thatptr");
        thisPtr.setLexicalHook(this);
        buffer.addLast(thisPtr);
        MKInstruction argrslt =
            new MKInstruction(MKInstruction.MKIN_ARG, null);
        argrslt.setLexicalHook(this);
        buffer.addLast(argrslt);
        MKInstruction rsltPtr =
            new MKInstruction(MKInstruction.MKIN_FCNAME, "_result");
        rsltPtr.setLexicalHook(this);
        argthis.setArgument(argrslt);
        buffer.addLast(rsltPtr);
        MKInstruction arg1 =
            new MKInstruction(MKInstruction.MKIN_ARG, null);
        arg1.setLexicalHook(this);
        argrslt.setArgument(arg1);
        buffer.addLast(arg1);
        if (start > 1) {
            generateNestedCode(buffer, start-2);
        } else {
            ASTPrimaryExpression pe1 = (ASTPrimaryExpression)children[0];
            pe1.generateCode(buffer);
        }
        ASTMultiplicativeOperator mo =
            (ASTMultiplicativeOperator)children[start];
        ASTPrimaryExpression pe2 = (ASTPrimaryExpression)children[start+1];
        MKInstruction arg2 =
            new MKInstruction(MKInstruction.MKIN_ARG, null);
        arg2.setLexicalHook(this);
        arg1.setArgument(arg2);
        buffer.addLast(arg2);
        pe2.generateCode(buffer);
        MKInstruction arge =
            new MKInstruction(MKInstruction.MKIN_ENDARGS, null);
        arge.setLexicalHook(this);
        arg2.setArgument(arge);
        buffer.addLast(arge);
        // locate candidate operators...
        mkin = new MKInstruction(MKInstruction.MKIN_FONAME,
                                         mo.getOp());
        mkin.setLexicalHook(this);
        buffer.addLast(mkin);
        // and generate the operator call
        mkin = new MKInstruction(MKInstruction.MKIN_OP, arity);
        mkin.setLexicalHook(this);
        buffer.addLast(mkin);
    }
}
