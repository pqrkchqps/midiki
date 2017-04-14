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
    public class AttributeImpl implements Contract.Attribute
    {
        /* {src_lang=Java}*/

        private String myName;
        private Object myType;
        private Object myElemType;

        public AttributeImpl(String name, Object type, Object elemType)
        {
            myName = name;
            myType = type;
            myElemType = elemType;
        }

        protected String className(Object o)
        {
            if (o==null) return "";
            if (o instanceof java.lang.Class) {
                return ((java.lang.Class)o).getName();
            } else {
                return o.toString();
            }
        }

        public String toString()
        {
            String cls = className(myType);
            String ccls = className(myElemType);
            if (ccls.length() > 0) ccls = "("+ccls+")";
            return myName+":"+cls+ccls;
        }

        /**
         * The name of the attribute.
         *
         * @return a <code>String</code> value
         */
        public String name()
        {
            return myName;
        }

        /**
         * Returns <code>true</code> if the specified object has a type
         * which is compatible with the specification.
         *
         * @param o an <code>Object</code> value
         * @return a <code>boolean</code> value
         */
        public boolean hasCompatibleType(Object o)
        {
            if (myType instanceof Class) {
                return (o.getClass().equals(myType));
            }
            // element types not checked yet
            if (myType instanceof Contract) {
                // object being passed in must be a CellImpl
            }
            return false;
        }

        public Object type()
        {
            return myType;
        }
    }
