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
package org.mitre.dm.qud.domain.diagnosis;

import java.util.*;
import java.util.logging.*;

import org.mitre.dm.*;
import org.mitre.dm.qud.*;

/**
 * Main program for the diagnosis domain. Uses the earlier model
 * of Midiki DM execution, where DMs are specified in source code
 * rather than as properties.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class diagnosis_dm 
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.domain.diagnosis.diagnosis_dm");

    public diagnosis_dm()
    {
    }

    static public void main(String[] args)
    {
        Executive exec = new Executive();
        String[] agentClassNames = {
            "org.mitre.dm.qud.IOAgent",
            "org.mitre.dm.qud.InterpretAgent",
            "org.mitre.dm.qud.domain.diagnosis.DomainAgent",
            "org.mitre.dm.qud.DmeAgent",
            "org.mitre.dm.qud.GenerateAgent",
            "org.mitre.dm.qud.DomainSpy",
            //"org.mitre.dm.qud.MonitorAgent",
            "org.mitre.dm.qud.ISControlAgent"
        };
        try {
            exec.launchAgents(agentClassNames);
        } catch (AgentConfigurationException ace) {
            ace.printStackTrace();
        }
    }

}
