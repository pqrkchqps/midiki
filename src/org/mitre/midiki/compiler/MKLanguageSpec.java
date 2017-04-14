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
/** A language spec defines the valid statements
 * in a language processed by MKParser.
 */

package org.mitre.midiki.compiler;

import java.util.*;
import java.io.Serializable;

import org.mitre.midiki.compiler.parser.*;

public class MKLanguageSpec implements Serializable
{
    public String languageName;
    public Vector validStatements; // Vector of Vectors
    public void appendPattern()
    {
        validStatements.addElement(new Vector());
    }
    public MKLanguageSpecPatternElement lastPatternElement()
    {
        if (validStatements.isEmpty()) {
            validStatements.add(new Vector());
        }
        Vector lastPattern = (Vector)validStatements.lastElement();
        if (lastPattern.isEmpty()) {
            return null;
        }
        return (MKLanguageSpecPatternElement)lastPattern.lastElement();
    }
    public void appendPatternElement(MKLanguageSpecPatternElement elm)
    {
        if (validStatements.isEmpty()) {
            validStatements.add(new Vector());
        }
        Vector lastPattern = (Vector)validStatements.lastElement();
        lastPattern.addElement(elm);
    }
    public MKLanguageSpecPatternElement popLastPatternElement()
    {
        if (validStatements.isEmpty()) {
            validStatements.add(new Vector());
        }
        Vector lastPattern = (Vector)validStatements.lastElement();
        if (lastPattern.isEmpty()) {
            return null;
        }
        return (MKLanguageSpecPatternElement)lastPattern.remove(lastPattern.lastIndexOf(lastPattern.lastElement()));
    }
    public MKLanguageSpec()
    {
        validStatements = new Vector();
    }
    public MKLanguageSpec(String n)
    {
        this();
        languageName = n;
    }
    public MKLanguageSpec(String n, Vector vs)
    {
        languageName = n;
        validStatements = vs;
    }

    static public MKType langspecType;
    static {
        langspecType = new MKType("$langspec");
    }
}

