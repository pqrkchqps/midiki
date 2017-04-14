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

import java.util.*;
import java.util.logging.*;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;
import org.mitre.dm.qud.domain.*;

/**
 * PriceInfo operations for the travel domain.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class PriceInfoCell
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.domain.travel.PriceInfoCell");
    public PriceInfoCell()
    {
    }

    public void init(Object configurationData)
    {
    }

    public void connect(InfoState is)
    {
    }

    protected static Contract theContract = null;
    public static Contract getContract()
    {
        if (theContract != null) return theContract;
        ContractImpl priceInfo = new ContractImpl("price_info");
        priceInfo.addAttribute(new AttributeImpl("how", "means_of_transport", null));
        priceInfo.addAttribute(new AttributeImpl("to", "location", null));
        priceInfo.addAttribute(new AttributeImpl("from", "location", null));
        priceInfo.addAttribute(new AttributeImpl("month", "month", null));
        priceInfo.addAttribute(new AttributeImpl("day", "day", null));
        priceInfo.addAttribute(new AttributeImpl("class", "class", null));
        priceInfo.addAttribute(new AttributeImpl("return", "yesno", null));
        priceInfo.addAttribute(new AttributeImpl("ret_month", "month", null));
        priceInfo.addAttribute(new AttributeImpl("ret_day", "day", null));
        priceInfo.addAttribute(new AttributeImpl("price", "price", null));
        priceInfo.addAttribute(new AttributeImpl("order_trip", "yesno", null));
        priceInfo.addAttribute(new AttributeImpl("successor", "task", null));
        priceInfo.addQuery(new QueryImpl("check_price",
            new ParameterImpl[]{
                new ParameterImpl("how", "means_of_transport", null),
                new ParameterImpl("to", "location", null),
                new ParameterImpl("from", "location", null),
                new ParameterImpl("month", "month", null),
                new ParameterImpl("day", "day", null),
                new ParameterImpl("class", "class", null),
                new ParameterImpl("return", "yesno", null),
                new ParameterImpl("ret_month", "month", null),
                new ParameterImpl("ret_day", "day", null),
                new ParameterImpl("price", "price", null)}));
        theContract = priceInfo;
        return theContract;
    }

    CellHandlers topCell;

    /* (non-Javadoc)
     * @see org.mitre.dm.qud.domain.DomainCell#initializeHandlers()
     */
    public CellHandlers initializeHandlers()
    {
        CellHandlers handlers = new CellHandlers(getContract());
        handlers.addQueryHandler("check_price", new CheckPriceHandler());
        
        return handlers;
    }
}
