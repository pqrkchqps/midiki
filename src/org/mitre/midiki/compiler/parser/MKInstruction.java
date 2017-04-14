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

import java.io.*;

public class MKInstruction implements Serializable
{
    /* list of known opcodes */
    /* list subject to revision and expansion as necessary */
    static public final int MKIN_BKTYPE  = 0;
    static public final int MKIN_BKSTART = 1;
    static public final int MKIN_BKEND   = 2;
    static public final int MKIN_STMT    = 3;
    static public final int MKIN_STBOT   = 4;
    static public final int MKIN_STTOP   = 5;
    static public final int MKIN_PUSH    = 6;
    static public final int MKIN_ARITY   = 7;
    static public final int MKIN_ARG     = 8;
    static public final int MKIN_ENDARGS = 9;
    static public final int MKIN_METHOD  = 10;
    static public final int MKIN_FCNAME  = 11; // find class of value
    static public final int MKIN_FCINST  = 12;
    static public final int MKIN_FVNAME  = 13; // find value
    static public final int MKIN_FVINST  = 14;
    static public final int MKIN_FMNAME  = 15; // find member
    static public final int MKIN_FMINST  = 16;
    static public final int MKIN_FONAME  = 17; // find op
    static public final int MKIN_OP      = 18;
    static public final int MKIN_LIST    = 19;
    static public final int MKIN_LISTELM = 20;
    static public final int MKIN_LISTCDR = 21;
    static public final int MKIN_ENDLIST = 22;
    static public final int MKIN_HALT    = 23;
    static public final int MKIN_ESTMT   = 24; // embedded statement
    static public final int MKIN_ESTBOT  = 25;
    static public final int MKIN_ESTTOP  = 26;

    static public final String[] ops = {
        "BKTYPE",
        "BKSTART",
        "BKEND",
        "STMT",
        "STBOT",
        "STTOP",
        "PUSH",
        "ARITY",
        "ARG",
        "ENDARGS",
        "METHOD",
        "FCNAME",
        "FCINST",
        "FVNAME",
        "FVINST",
        "FMNAME",
        "FMINST",
        "FONAME",
        "OP",
        "LIST",
        "LISTELM",
        "LISTCDR",
        "ENDLIST",
        "HALT",
        "ESTMT",
        "ESTBOT",
        "ESTTOP"
    };

    /* implementation */
    private int _opcode;
    private Object _argument;
    private int _address;
    private SimpleNode _lexicalHook;

    public int getOpcode() {return _opcode;}
    public Object getArgument() {return _argument;}
    public int getAddress() {return _address;}
    public SimpleNode getLexicalHook() {return _lexicalHook;}
    public void setOpcode(int o) {_opcode = o;}
    public void setArgument(Object a) {_argument = a;}
    public void setAddress(int a) {_address = a;}
    public void setLexicalHook(SimpleNode n) {_lexicalHook = n;}

    public MKInstruction() {}
    public MKInstruction(int o, Object a) {_opcode=o;_argument=a;}

    public String toString()
    {
        String val = _address+": "+ops[_opcode]+" ";
        if (_argument instanceof MKInstruction) {
            MKInstruction ref = (MKInstruction)_argument;
            val = val + "["+ops[ref.getOpcode()]+"@"+ref.getAddress()+"]";
        } else {
            val = val + _argument;
        }
        return val;
    }
}
