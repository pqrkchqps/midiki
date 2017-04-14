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
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;

/**
 * Stores instances of cells. Each instance is identified by an
 * instance identifier, and contains data for each slot in the
 * defining feature structure. (Slot data may be null.)
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class CellStore
{
    /**
     * Describe variable <code>cellContract</code> here.
     */
    protected Contract cellContract;
    /**
     * Return the cell's <code>Contract</code>.
     *
     * @return a <code>Contract</code> value
     */
    public Contract contract()
    {
        return cellContract;
    }
    /* {transient=false, volatile=false}*/
    /**
     * Describe variable <code>mediatedBy</code> here.
     */
    protected Mediator mediatedBy;
    /* {transient=false, volatile=false}*/
    /**
     * Describe variable <code>cellInstances</code> here. The element values
     * are of type <code>CellInstance</code>.
     */
    protected Map cellInstances;
    /* {transient=false, volatile=false}*/

    /**
     * Describe <code>createCompatibleInstance</code> method here.
     *
     * @param key an <code>Object</code> value
     * @return a <code>CellInstance</code> value
     */
    public CellInstance createCompatibleInstance(Object key)
    {
        LinkedList data = new LinkedList();
        Iterator attr = cellContract.attributes();
        while (attr.hasNext()) {
            Contract.Attribute an =
                (Contract.Attribute)attr.next();
            data.add(null);
        }
        return new CellInstance(cellContract, key, data);
    }

    /**
     * Describe <code>registerInstance</code> method here.
     *
     * @param inst a <code>CellInstance</code> value
     */
    public void registerInstance(CellInstance inst)
    {
        cellInstances.put(inst.instanceId, inst);
    }

    /**
     * Creates a new <code>CellStore</code> instance.
     *
     * @param contract a <code>Contract</code> value
     * @param mediator a <code>Mediator</code> value
     */
    public CellStore(Contract contract, Mediator mediator) {
        cellContract = contract;
        mediatedBy = mediator;
        cellInstances = new HashMap();
    }

    public CellStore(CellStore model)
    {
        cellContract = model.cellContract;
        mediatedBy = model.mediatedBy;
        cellInstances = new HashMap();
    }
}
