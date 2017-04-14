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
/** Implements a visitor that generates a language specification
 * for MK from parser input.
 *
 * A language specification consists of the following elements:
 * - a name
 * - a list of patterns for valid sentences, possibly nested
 * - a structure containing name and type information?
 *
 * A statement pattern is a statement consisting of designators,
 * blocks, and string constants.
 * - A constant designator denotes itself.
 * - A variable designator denotes a structure of that type:
 *   [ list of valid variables ]
 * - A block denotes a sublanguage
 * - the assignment operator '=' attaches a sublanguage to an element;
 *   format must be 'variable = block'
 * - A string constant denotes an assignment operator
 *
 * This processor expects a definition of the form:
 * language _identifier _block
 */

package org.mitre.midiki.compiler;

import java.util.*;

import org.mitre.midiki.compiler.parser.*;

public class MKLangspecVisitor implements MKParserVisitor
{
  public static void main(String args[]) {
    MKParser parser;
    if (args.length == 0) {
      System.out.println("MK Language Specifier:  Reading from standard input . . .");
      parser = new MKParser(System.in);
    } else if (args.length == 1) {
      System.out.println("MK Language Specifier:  Reading from file " + args[0] + " . . .");
      try {
        parser = new MKParser(new java.io.FileInputStream(args[0]));
      } catch (java.io.FileNotFoundException e) {
        System.out.println("MK Language Specifier:  File " + args[0] + " not found.");
        return;
      }
    } else {
      System.out.println("Langspec Parser:  Usage is one of:");
      System.out.println("         java MKLangspecVisitor < inputfile");
      System.out.println("OR");
      System.out.println("         java MKLangspecVisitor inputfile");
      return;
    }
    try {
      ASTCompilationUnit n = parser.CompilationUnit();
      parser.dumpErrors();
      n.dump("*");
      if (parser.parse_errors.size() == 0) {
          MKLangspecVisitor lang = new MKLangspecVisitor();
          Object result = lang.visit(n, parser.parse_errors);
          if (lang.errorCount > 0)
          {
              System.out.println("Language interpretation errors");
          }
          else
          {
              MKDFA dfa = new MKDFA(lang.languageSpec);
              System.out.println("DFA constructed from this spec:");
              System.out.println("===============================");
              System.out.println(dfa.root);
              System.out.println("===============================");
              MKMatcher mk = new MKMatcher(dfa);
              mk.visit(n, null);
          }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

    public MKLanguageSpec topSpec()
    {
        if (open_specs.empty()) return null;
        return (MKLanguageSpec)open_specs.peek();
    }

    private void resetSpec()
    {
        state = 0;
        languageSpec = new MKLanguageSpec();
        open_specs.push(languageSpec);
    }

    private void defineLanguage(MKLanguageSpec spec)
    {
        if (spec==null) {
            return;
        }
        if (spec.languageName!=null) {
            // named spec
            symbolTable.bindSymbol(spec.languageName, MKLanguageSpec.langspecType, spec);
        }
    }

    public MKSymbolTable symbolTable;
    private int anonymousBlockCount;
    public MKLanguageSpec languageSpec;
    public Stack open_specs;
    public int state;
    public int errorCount;
    public MKLangspecVisitor()
    {
        errorCount = 0;
        anonymousBlockCount = 0;
        open_specs = new Stack();
        resetSpec();
        symbolTable = new MKSymbolTable();
    }
    public Object visit(SimpleNode node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTCompilationUnit node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTName node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTDesignator node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTIdentifier node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        if (state == 0) {
            // match against 'language'
            if (!node.getName().equals("language")) {
                ParseException pe =
                    new ParseException("Keyword 'language' expected.",node);
                errvec.addElement(pe);
                errorCount++;
                return errvec;
            }
            state = 1;
        } else if (state == 1) {
            languageSpec.languageName = node.getName();
            state = 2;
            // store as language name
        } else if (state >= 3) {
            // store in current pattern as constant
            MKLanguageSpecPatternElement patternElement =
                new MKLanguageSpecPatternElement(node.getName(), true, null);
            if (topSpec()==null) {
                System.out.println("*** spec stack empty in identifier()!");
                languageSpec = new MKLanguageSpec();
                open_specs.push(languageSpec);
            }
            topSpec().appendPatternElement(patternElement);
        } else {
            // syntax error -- failed to match pattern
            ParseException pe =
                new ParseException("Unexpected identifier.",node);
            errvec.addElement(pe);
            errorCount++;
            return errvec;
        }
        data = node.childrenAccept(this, errvec);
        return data;
    }
    public Object visit(ASTVariable node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        if (state >= 3) {
            // check to ensure that the variable is a known value
            String var = node.getName();
            // store in current pattern as constant
            MKLanguageSpecPatternElement patternElement =
                new MKLanguageSpecPatternElement(var, false, null);
            if (topSpec()==null) {
                System.out.println("*** spec stack empty in variable()!");
                languageSpec = new MKLanguageSpec();
                open_specs.push(languageSpec);
            }
            topSpec().appendPatternElement(patternElement);
        } else if (state == 1) {
            languageSpec.languageName = node.getName();
            state = 2;
            // store as language name
        } else {
            // syntax error -- failed to match pattern
            ParseException pe =
                new ParseException("Unexpected variable.",node);
            errvec.addElement(pe);
            errorCount++;
            return errvec;
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
        // this is where we test for type information
        return data;
    }
    public Object visit(ASTHashQualifier node, Object data) {
        data = node.childrenAccept(this, data);
        // this is where we test for type information
        return data;
    }
    public Object visit(ASTExpression node, Object data) {
        data = node.childrenAccept(this, data);
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        if (node.jjtGetNumChildren() > 1) {
            // first child must resolve to a variable
            // second child must be '='
            // third child must be a block or the name of a block
            // Condition 1 means last element but one must be a variable
            // Condition 2 is handled by ASTAssignmentOperator
            // Condition 3 means last element is a block or a name
            if (topSpec()==null) {
                ParseException pe =
                    new ParseException("Expression not legal here",node);
                errvec.addElement(pe);
                errorCount++;
                return errvec;
            }
            MKLanguageSpecPatternElement elm = topSpec().popLastPatternElement();
            if (elm == null) {
                ParseException pe =
                    new ParseException("Expression error",node);
                errvec.addElement(pe);
                errorCount++;
            } else if (elm.element != null) {
                // expect this to resolve to a langspec name.
                MKSymbol ls = symbolTable.getSymbol(elm.element);
                if (ls==null) {
                    ParseException pe =
                        new ParseException(elm.element+" not defined",node);
                    errvec.addElement(pe);
                    errorCount++;
                } else if (ls.getType() != MKLanguageSpec.langspecType) {
                    ParseException pe =
                        new ParseException(elm.element+" not a language spec",node);
                    errvec.addElement(pe);
                    errorCount++;
                } else {
                    elm.sublanguage = (MKLanguageSpec)(ls.getValue());
                }
            } else if (elm.sublanguage == null) {
                ParseException pe =
                    new ParseException("Block expected",node);
                errvec.addElement(pe);
                errorCount++;
            } else {
                MKLanguageSpecPatternElement pelm =
                    topSpec().lastPatternElement();
                if ((pelm.element == null) ||
                    pelm.isConstant ||
                    (pelm.sublanguage != null)) {
                    ParseException pe =
                        new ParseException("Variable expected",node);
                    errvec.addElement(pe);
                    errorCount++;
                } else {
                    pelm.sublanguage = elm.sublanguage;
                    elm.sublanguage = null;
                }
            }
        }
        return errvec;
    }
    public Object visit(ASTAssignmentOperator node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        if (!node.getOp().equals("=")) {
            ParseException pe =
                new ParseException("Unknown assignment operator '"+
                                   node.getOp()+"'",node);
            errvec.addElement(pe);
            errorCount++;
        }
        data = node.childrenAccept(this, errvec);
        return data;
    }
    public Object visit(ASTLogicalExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTLogicalOperator node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTRelationalExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTRelationalOperator node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTAdditiveExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTAdditiveOperator node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTMultiplicativeExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTMultiplicativeOperator node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTUnaryExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTNegationOperator node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTPrimaryExpression node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTLiteral node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTIntegerLiteral node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTFloatingPointLiteral node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTCharacterLiteral node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTStringLiteral node, Object data) {
        System.out.println("String constant");
        // verify that this only contains symbol characters
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTArguments node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTArgumentList node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTExpressions node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTExpressionList node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        ParseException pe =
            new ParseException("Semantic error: "+
                           node.getClass().getName()+
                           " not valid here",node);
        errvec.addElement(pe);
        errorCount++;
        //data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTStatements node, Object data) {
        Vector errvec = null;
        if (data != null) errvec = (Vector)data;
        if (state == 2) {
            state = 3;
            // open spec for language
            //System.out.println("Defining language '"+languageSpec.languageName+"'");
            defineLanguage(languageSpec);
            symbolTable.openContext("@"+languageSpec.languageName);
            node.context = symbolTable.topContext();
        } else if (state >= 3) {
            // build a local sub-language
            //System.out.println("Defining a sub-language");
            MKLanguageSpec spec = new MKLanguageSpec();
            defineLanguage(spec);
            MKLanguageSpecPatternElement localBlock =
                new MKLanguageSpecPatternElement(null, true, spec);
            if (topSpec()==null) {
                System.out.println("*** spec stack empty in block()!");
                languageSpec = new MKLanguageSpec();
                open_specs.push(languageSpec);
            }
            topSpec().appendPatternElement(localBlock);
            open_specs.push(spec);
            symbolTable.openContext("@"+(anonymousBlockCount++));
            node.context = symbolTable.topContext();
        } else {
            // syntax error -- failed to match pattern
            ParseException pe =
                new ParseException("Unexpected block.",node);
            errvec.addElement(pe);
            errorCount++;
            return errvec;
        }
        state++;
        data = node.childrenAccept(this, data);
        state--;
        open_specs.pop();
        if (topSpec()==null) {
            resetSpec();
        }
        symbolTable.closeContext();
        return data;
    }
    public Object visit(ASTStatementList node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }
    public Object visit(ASTStatement node, Object data) {
        if (state >= 3) {
            //System.out.println("Defining a statement pattern");
            if (topSpec()==null) {
                System.out.println("*** spec stack empty in statement()!");
                languageSpec = new MKLanguageSpec();
                open_specs.push(languageSpec);
            }
            topSpec().appendPattern();
        }
        data = node.childrenAccept(this, data);
        return data;
    }
}
