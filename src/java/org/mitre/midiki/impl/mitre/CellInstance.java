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

import org.mitre.midiki.state.Contract;

import java.util.*;

/**
 * Describe class <code>CellInstance</code> here.
 * Note: some previous code assumes that the first slot has instance ID,
 * but later work assumes that it doesn't have that. Reconcile.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class CellInstance
{
    /**
     * Describe variable <code>instanceType</code> here.
     */
    public Contract instanceType;
    /**
     * Describe variable <code>instanceId</code> here.
     */
    public Object instanceId;
    /* {transient=false, volatile=false}*/
    /**
     * Describe variable <code>instanceData</code> here.
     */
    public List instanceData;
    /* {transient=false, volatile=false}*/
    /**
     * Records the number of active references to the object
     * within the information state. Instances whose ref count
     * falls to zero can be pruned as necessary.
     */
    public int refCount;
    public void addRef()
    {
        refCount++;
        //System.out.println("addRef(): "+instanceId+" ==> "+refCount);
    }
    public void dropRef()
    {
        refCount--;
        //System.out.println("dropRef(): "+instanceId+" ==> "+refCount);
    }

    /**
     * Describe <code>CellInstance</code> method here.
     *
     * @param id an <code>Object</code> value
     * @param data a <code>Vector</code> value
     */
    public CellInstance(Contract type,
                        Object id,
                        List data) {
        instanceType = type;
        instanceId = id;
        instanceData = data;
    }
    /**
     * Returns a shallow copy of the instance which can be
     * modified independently of the original.
     *
     * @return a <code>CellInstance</code> value
     */
    public CellInstance copy()
    {
        LinkedList l = new LinkedList(instanceData);
        return new CellInstance(instanceType, instanceId, l);
    }
}
