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
/** A language spec pattern element is a single node
 * in a statement pattern for MKParser.
 */
package org.mitre.midiki.compiler;

import java.io.Serializable;

import org.mitre.midiki.compiler.parser.*;

public class MKLanguageSpecPatternElement implements Serializable
{
    public String element;
    public boolean isConstant;
    public MKLanguageSpec sublanguage;
    public MKLanguageSpecPatternElement()
    {
    }
    public MKLanguageSpecPatternElement(String elm)
    {
        element = elm;
        isConstant = true;
    }
    public MKLanguageSpecPatternElement(String elm, boolean c, MKLanguageSpec sl)
    {
        element = elm;
        isConstant = c;
        sublanguage = sl;
    }

    public String toString()
    {
        return "MKLanguageSpecPatternElement("+element+","+isConstant+","+sublanguage+")";
    }
}

