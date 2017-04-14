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

import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Provides the standard implementation of a first-order logical predicate.
 * [Question: should this be an interface rather than a class?]
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class Predicate
{
    /**
     * Local storage for the predicate name (functor).
     */
    protected String theFunctor;
    /**
     * Local storage for the predicate arguments.
     */
    protected Collection theArguments;

    /**
     * No-arg constructor supporting serialization.
     *
     */
    public Predicate()
    {
    }

    /**
     * Creates a new <code>Predicate</code> instance.
     *
     * @param funct a <code>String</code> value
     * @param args a <code>Collection</code> value
     */
    public Predicate(String funct, Collection args)
    {
        theFunctor = funct;
        theArguments = args;
    }

    /**
     * Creates a new <code>Predicate</code> instance
     * containing a shallow copy of the source.
     *
     * @param p a <code>Predicate</code> value
     */
    public Predicate(Predicate p)
    {
        theFunctor = p.theFunctor;
        theArguments = new LinkedList(p.theArguments);
    }

    /**
     * Returns the value of the functor.
     *
     * @return a <code>String</code> value
     */
    public String functor()
    {
        return theFunctor;
    }

    /**
     * Returns an iterator over the arguments of the predicate.
     *
     * @return a <code>Collection</code> value
     */
    public Iterator arguments()
    {
        return theArguments.iterator();
    }

    public int arity()
    {
        if (theArguments == null) return 0;
        return theArguments.size();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Predicate)) return false;
        Predicate p = (Predicate)o;
        if (!theFunctor.equals(p.functor())) return false;
        if (arity() != p.arity()) return false;
        Iterator args = arguments();
        Iterator toArgs = p.arguments();
        while (args.hasNext() && toArgs.hasNext()) {
            Object arg = args.next();
            Object toArg = toArgs.next();
            if (!(arg.equals(toArg))) return false;
        }
        return true;
    }

    public String toString()
    {
        String retval = functor()+"(";
        Iterator it = arguments();
        while (it.hasNext()) {
            retval = retval + it.next();
            if (it.hasNext()) retval = retval+",";
        }
        retval = retval + ")";
        return retval;
    }
}
