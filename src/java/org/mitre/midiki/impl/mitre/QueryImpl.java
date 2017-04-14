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
package org.mitre.midiki.impl.mitre;

import org.mitre.midiki.state.*;

import java.util.*;

    /**
     * Specification of a query on a <code>Contract</code>.
     *
     * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
     * @version 1.0
     * @since 1.0
     */
    public class QueryImpl implements Contract.Query
    {
        /* {src_lang=Java}*/

        private String myName;
        private Contract.Parameter[] myParams;

        public QueryImpl(String name, Contract.Parameter[] params)
        {
            myName = name;
            myParams = params;
        }

    public String toString()
    {
        String name = myName;
        String parms = null;
        Iterator it = parameters();
        while (it.hasNext()) {
            if (parms == null) {
                parms = "";
            } else {
                parms = parms + ",";
            }
            parms = parms+it.next();
        }
        return "query "+name + "("+parms+")";
    }

        /**
         * Returns the name of the query.
         *
         * @return a <code>String</code> value
         */
        public String name()
        {
            return myName;
        }
        /**
         * Returns the number of parameters this query requires.
         *
         * @return an <code>int</code> value
         */
        public int arity()
        {
            if (myParams==null) return 0;
            return myParams.length;
        }
        /**
         * Returns an immutable <code>Iterator</code> over the query's
         * formal <code>Parameter</code>s.
         *
         * @return an <code>Iterator</code> over <code>Parameter</code>s
         * representing the formal parameters of the query.
         */
        public Iterator parameters()
        {
            return Arrays.asList(myParams).iterator();
        }
        /**
         * Returns <code>true</code> if the specified objects have types
         * which are compatible with the formal parameters.
         *
         * @param o an <code>Object</code> value
         * @return a <code>boolean</code> value
         */
        public boolean hasCompatibleParameters(Object[] o)
        {
            if (o.length != arity()) return false;
            return true;
        }
    }
