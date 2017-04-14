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
package org.mitre.midiki.impl.mitre.galaxy;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;
import java.io.*;
import java.net.*;
import java.util.*;
import galaxy.server.*;
import galaxy.lang.*;

/**
 * Provides a simple instance of a MidikiServer that was used
 * for test purposes.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see MidikiServer
 */
public class MidikiServerTest extends MidikiServer {
    public MidikiServerTest(MainServer ms, Socket s) throws IOException
    {
        super(ms,s);
    }
    static protected Map cellHandlers;
    static public void setHandlers(Collection coll)
    {
        cellHandlers = new HashMap();
        Iterator it = coll.iterator();
        while (it.hasNext()) {
            CellHandlers ch = (CellHandlers)it.next();
            cellHandlers.put(ch.getContract().name(), ch);
        }
    }
    public GFrame serverOpFromIOPodium(GFrame frame) {
        System.out.println("MidikiServerTest serverOpFromIOPodium: "+GalaxyMediator.serviceHandlers);
        ServiceHandler sh = (ServiceHandler)GalaxyMediator.serviceHandlers.get("method$io_podium_output$FromIOPodium");
        LinkedList cp = (LinkedList)GalaxyMediator.serviceParameters.get("method$io_podium_output$FromIOPodium");
        //CellHandlers ch = (CellHandlers)cellHandlers.get("io_podium_output");
        //Contract c = ch.getContract();
        List params = extractProperties(frame, cp.iterator());
        Bindings b = new BindingsImpl();
        LinkedList bl = new LinkedList();
        //bl.add(b);
        //ch.method("FromIOPodium").invoke(params, b);
        sh.handleEvent("method$io_podium_output$FromIOPodium", params, bl);
        GFrame returnFrame = composeFrame(params, b, "FromIOPodium", cp.iterator());
        return returnFrame;
    }
}
