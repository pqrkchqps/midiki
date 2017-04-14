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
package org.mitre.midiki.logic;

/**
 * Provides the default implementation of a logical variable.
 * [Question: should this be an interface rather than a class?]
 *
 * Variables with the same name are intended to be the same variable
 * for unification purposes. In practical terms, this means that
 * the hashCode() and equals() methods for variables are based on
 * the names, and do not consider any constraints.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class Variable
{
    /**
     * Local storage for the variable name.
     */
    public String theName;

    /**
     * Local storage for the constraint on values bound to this variable.
     * In typical Midiki usage, most variables are unconstrained.
     */
    public Constraint theConstraint;

    /**
     * No-arg constructor supporting serialization.
     *
     */
    public Variable()
    {
    }

    /**
     * Creates a new <code>Variable</code> with the specified name.
     *
     * @param n a <code>String</code> value
     */
    public Variable(String n)
    {
        theName = n;
    }

    /**
     * Creates a new <code>Variable</code> with the specified name
     * and <code>Constraint</code>.
     *
     * @param n a <code>String</code> value
     * @param c a <code>Constraint</code> value
     */
    public Variable(String n,Constraint c)
    {
        theName = n;
        theConstraint = c;
    }

    /**
     * Returns the variable name.
     *
     * @return a <code>String</code> value
     */
    public String name()
    {
        return theName;
    }

    /**
     * Returns the <code>Constraint</code> on this variable, if any.
     *
     * @return a <code>Constraint</code> value
     */
    public Constraint constraint()
    {
        return theConstraint;
    }

    /**
     * Returns <code>true</code> if this <code>Variable</code>
     * is constrained.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isConstrained()
    {
        return (theConstraint != null);
    }

    public int hashCode()
    {
        return theName.hashCode();
    }

    public boolean equals(Object o)
    {
        if (o instanceof Variable) {
            return name().equals(((Variable)o).name());
        } else {
            return false;
        }
    }

    public String toString()
    {
        return name();
    }

    static private int varnum;
    static public Variable newVariable()
    {
        int v = varnum++;
        return new Variable("_"+v);
    }
    static public Variable newVariable(Constraint c)
    {
        int v = varnum++;
        return new Variable("_"+v,c);
    }
    static
    {
        varnum = 0;
    }
}
