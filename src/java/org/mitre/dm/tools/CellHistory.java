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
package org.mitre.dm.tools;

import org.mitre.dm.Executive;
import org.mitre.midiki.state.*;
import org.mitre.midiki.tools.*;

import org.mitre.midiki.impl.mitre.*;

import java.util.*;

public class CellHistory extends CellStore
{
    protected Object rootId;
    protected HashMap cellAttributes;
    protected CellInstance currentInstance;

    public CellHistory(CellStore cs)
    {
        super(cs);
        cellAttributes = new HashMap();
        Iterator atIt = cellContract.attributes();
        for (int index = 0; atIt.hasNext(); index++) {
            Contract.Attribute ca = (Contract.Attribute)atIt.next();
            cellAttributes.put(ca.name(), new Integer(index));
        }
    }

    /**
     * Get the value of the specified attribute.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>Object</code> value
     */
    public Object get(String fqName)
    {
        Object iobj = cellAttributes.get(fqName);
        if (iobj == null) {
            return null;
        }
        Integer index = (Integer)iobj;
        if (currentInstance == null) return null;
        if (currentInstance.instanceData == null) return null;
        if (index == null) return "<unknown attribute>";
        return currentInstance.instanceData.get(index.intValue());
    }

    public CellInstance createCompatibleInstance(Object key)
    {
        throw new UnsupportedOperationException();
    }

    public CellInstance getInstanceValue(Object key)
    {
        return (CellInstance)cellInstances.get(key);
    }

    public void setInstanceValue(Object key, CellInstance inst)
    {
        cellInstances.put(key, inst);
    }

    public void loadInstanceValue(Object key)
    {
        currentInstance = getInstanceValue(key);
    }

    public Object getRoot()
    {
        return rootId;
    }

    public void setRoot(Object key)
    {
        rootId = key;
    }
}
