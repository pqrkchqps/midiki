/* Generated By:JJTree: Do not edit this line. ASTRelationalOperator.java */

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

public class ASTRelationalOperator extends SimpleNode {
    private String op;

    public void setOp(String v) {op=v;}
    public String getOp() {return op;}

  public ASTRelationalOperator(int id) {
    super(id);
  }

  public ASTRelationalOperator(MKParser p, int id) {
    super(p, id);
  }

  public String toString() {
      return "RelationalOperator: " + op;
  }

  /** Accept the visitor. **/
  public Object jjtAccept(MKParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}