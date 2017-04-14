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
package org.mitre.midiki.state;

import java.util.*;

/**
 * Implements a structure similar to a <code>class</code>,
 * but with an as-yet-unspecified type system. Many systems
 * for computational linguistics use some form of feature
 * structure, based on a list of named attributes with types
 * and values. Our implementation extends feature structures
 * with method signatures, providing declarative cell definitions
 * compatible with Trindikit, but does not yet specify a type
 * inheritance system and stores attribute values elsewhere.
 * This is likely to be enhanced in future releases of Midiki.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface Contract
{
    /**
     * Specification of an attribute of a <code>Contract</code>.
     * Assumes the specification of a type and (for collections)
     * an element type, in line with Trindikit. Implementations
     * of <code>Contract</code> may implement whatever type
     * scheme they require (or none).
     *
     * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
     * @version 1.0
     * @since 1.0
     */
    public interface Attribute
    {
        /* {src_lang=Java}*/

        /**
         * The name of the attribute.
         *
         * @return a <code>String</code> value
         */
        public String name();

        /**
         * Returns <code>true</code> if the specified object has a type
         * which is compatible with the specification.
         *
         * @param o an <code>Object</code> value
         * @return a <code>boolean</code> value
         */
        public boolean hasCompatibleType(Object o);

    }
    /**
     * Specification of a parameter of a <code>Method</code> or
     * <code>Query</code>.
     * Assumes the specification of a type and (for collections)
     * an element type, in line with Trindikit. Implementations
     * of <code>Contract</code> may implement whatever type
     * scheme they require (or none).
     *
     * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
     * @version 1.0
     * @since 1.0
     */
    public interface Parameter
    {
        /* {src_lang=Java}*/
            
        /**
         * The name of the parameter.
         *
         * @return a <code>String</code> value
         */
        public String name();

        /**
         * Returns <code>true</code> if the specified object has a type
         * which is compatible with the specification.
         *
         * @param o an <code>Object</code> value
         * @return a <code>boolean</code> value
         */
        public boolean hasCompatibleType(Object o);
        
    }

    /**
     * Specification of a method on a <code>Contract</code>.
     *
     * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
     * @version 1.0
     * @since 1.0
     */
    public interface Method
    {
        /* {src_lang=Java}*/

        /**
         * Returns the name of the method.
         *
         * @return a <code>String</code> value
         */
        public String name();
        /**
         * Returns the number of parameters this method requires.
         *
         * @return an <code>int</code> value
         */
        public int arity();
        /**
         * Returns an immutable <code>Iterator</code> over the method's
         * formal <code>Parameter</code>s.
         *
         * @return an <code>Iterator</code> over <code>Parameter</code>s
         * representing the formal parameters of the method.
         */
        public Iterator parameters();
        /**
         * Returns <code>true</code> if the specified objects have types
         * which are compatible with the formal parameters.
         *
         * @param o an <code>Object</code> value
         * @return a <code>boolean</code> value
         */
        public boolean hasCompatibleParameters(Object[] o);
    }
    /**
     * Specification of a query on a <code>Contract</code>.
     *
     * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
     * @version 1.0
     * @since 1.0
     */
    public interface Query
    {
        /* {src_lang=Java}*/

        /**
         * Returns the name of the query.
         *
         * @return a <code>String</code> value
         */
        public String name();
        /**
         * Returns the number of parameters this query requires.
         *
         * @return an <code>int</code> value
         */
        public int arity();
        /**
         * Returns an immutable <code>Iterator</code> over the query's
         * formal <code>Parameter</code>s.
         *
         * @return an <code>Iterator</code> over <code>Parameter</code>s
         * representing the formal parameters of the query.
         */
        public Iterator parameters();
        /**
         * Returns <code>true</code> if the specified objects have types
         * which are compatible with the formal parameters.
         *
         * @param o an <code>Object</code> value
         * @return a <code>boolean</code> value
         */
        public boolean hasCompatibleParameters(Object[] o);
    }
    /**
     * Returns the name this <code>Contract</code> prefers to be called.
     *
     * @return a <code>String</code> value
     */
    public String name();
    /**
     * Returns an <code>Iterator</code> over the attributes.
     *
     * @return an <code>Iterator</code> of <code>Attribute</code>
     */
    public Iterator attributes();
    /**
     * Return the <code>Attribute</code> with the specified name,
     * or null if it doesn't exist.
     *
     * @param name a <code>String</code> value
     * @return an <code>Attribute</code> value
     */
    public Attribute attribute(String name);
    /**
     * Returns an <code>Iterator</code> over the queries.
     *
     * @return a <code>Iterator</code> of <code>Query</code>
     */
    public Iterator queries();
    /**
     * Return the <code>Query</code> with the specified name,
     * or null if it doesn't exist.
     *
     * @param name a <code>String</code> value
     * @return an <code>Query</code> value
     */
    public Query query(String name);
    /**
     * Returns an <code>Iterator</code> over the queries.
     *
     * @return a <code>Iterator</code> of <code>Method</code>
     */
    public Iterator methods();
    /**
     * Return the <code>Method</code> with the specified name,
     * or null if it doesn't exist.
     *
     * @param name a <code>String</code> value
     * @return an <code>Method</code> value
     */
    public Method method(String name);
}
