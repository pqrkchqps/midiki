/* Generated By:JJTree: Do not edit this line. ASTStringLiteral.java */

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

public class ASTStringLiteral extends SimpleNode {
    private String value;

    public void setValue(String v) {value=v;}
    public String getValue() {return value;}

  public ASTStringLiteral(int id) {
    super(id);
  }

  public ASTStringLiteral(MKParser p, int id) {
    super(p, id);
  }

  public String toString() {
    return "StringLiteral: \"" + value + "\"";
  }

  /** Accept the visitor. **/
  public Object jjtAccept(MKParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

    /* Each node has a representation in terms of instructions for
     * a hypothetical stack machine. This routine appends that
     * representation onto a list, for each of its children.
     * Instruction representation is as an op code and an argument.
     * Some instructions which want two arguments have been split
     * into separate instructions.
     * The default implementation has no effect.
     */
    public void generateCode(LinkedList buffer)
    {
        MKInstruction mkin = null;
        mkin = new MKInstruction(MKInstruction.MKIN_PUSH,
                                 new String(value));
        mkin.setLexicalHook(this);
        buffer.addLast(mkin);
    }
}
