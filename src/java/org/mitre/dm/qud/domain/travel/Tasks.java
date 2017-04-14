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
package org.mitre.dm.qud.domain.travel;

import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;

import java.util.*;

/**
 * Provides a simple source of contract information for the travel domain.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class Tasks
{
    static protected HashMap db;
    static
    {
        db = new HashMap();
        db.put("top",taskTop());
        db.put("price_info",PriceInfoCell.getContract());
        db.put("order_trip",OrderTripCell.getContract());
    }
    static public Contract find(String name)
    {
        return (Contract)db.get(name);
    }
    protected static Contract taskTop()
    {
        ContractImpl top = new ContractImpl("top");
        top.addAttribute(new AttributeImpl("task", "task", null));
        return top;
    }
}
