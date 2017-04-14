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

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import org.mitre.midiki.logic.Bindings;
import org.mitre.midiki.logic.Predicate;
import org.mitre.midiki.logic.Unify;
import org.mitre.midiki.state.QueryHandler;


/**
 * CheckPriceHandler 
 *
 * @author Carl Burke
 * @version $Id: CheckPriceHandler.java,v 1.1 2005/07/23 19:17:09 cburke Exp $
 */
public class CheckPriceHandler implements QueryHandler
{

    /* (non-Javadoc)
     * @see org.mitre.midiki.state.QueryHandler#query(java.util.Collection, org.mitre.midiki.logic.Bindings)
     */
    public boolean query(Collection arguments, Bindings bindings)
    {
        /* for now, just make up a price and set it.
         * we aren't accessing any real database anyway.
         */
        ArrayList al = new ArrayList();
        al.add(new Integer(423));
        Predicate price = new Predicate("price", al);
        /* find the right argument to bind it to.
         */
        Iterator it = arguments.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (!(next instanceof Predicate)) continue;
            Predicate nextPred = (Predicate)next;
            if (nextPred.functor().equals(price.functor())) {
                //System.out.println("Unify "+nextPred+" and "+price+" is "+
                Unify.getInstance().matchTerms(nextPred, price, bindings);
                //System.out.println("bindings after unify "+bindings);
                //bindings.reset();
                //System.out.println("bindings after reset "+bindings);
                return true;
            }
        }
        return true;
    }

}
