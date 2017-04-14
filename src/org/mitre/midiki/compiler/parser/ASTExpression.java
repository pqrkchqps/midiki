/* Generated By:JJTree: Do not edit this line. ASTExpression.java */

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

public class ASTExpression extends SimpleNode {
  public ASTExpression(int id) {
    super(id);
  }

  public ASTExpression(MKParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(MKParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

    public void generateCode(LinkedList buffer)
    {
        MKInstruction mkin = null;
        if (children == null) return;
        if (children.length > 1) {
            MKInstruction arity =
                new MKInstruction(MKInstruction.MKIN_ARITY, new Integer(4));
            arity.setLexicalHook(this);
            buffer.addLast(arity);
            ASTLogicalExpression pe1 = (ASTLogicalExpression)children[0];
            ASTAssignmentOperator mo =
                (ASTAssignmentOperator)children[1];
            ASTLogicalExpression pe2 = (ASTLogicalExpression)children[2];
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
            pe1.generateCode(buffer);
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
}
