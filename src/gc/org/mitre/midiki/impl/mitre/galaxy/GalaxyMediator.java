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

import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;
import org.mitre.midiki.logic.*;

import java.util.*;
import java.util.logging.*;

// Communicator imports
import galaxy.lang.*;
import galaxy.server.*;
import galaxy.server.ui.ServerUI;

/**
 * Provides an implementation of <code>Mediator</code>
 * for use with the Galaxy Communicator framework.
 *
 * Needs a support class to provide the Server.
 * The server _must_ have methods that follow a specific
 * naming convention in order to make a valid server.
 * That server should be generated automatically based on
 * the contracts and cells defined by the agents.
 * - could override MainServer.initServer()
 *   - this would need to add necessary signatures
 *
 * This type of Mediator differentiates between operations
 * which are client-side and server-side. Accordingly, need
 * to specify in a Midiki-ized way which pieces are client
 * and which are server. The standard way to do this is by
 * declaring contracts and cells.
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class GalaxyMediator implements Mediator
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.GalaxyMediator");
    /**
     * Local storage of data distribution agent name.
     */
    private String myAgentName;
    protected static Server currentServer = null;
    /**
     * Refers to service handlers registered at runtime. Would prefer to
     * have this local to each Mediator, but Communicator creates Server
     * objects dynamically with no way to pass objects other than via
     * command line arguments. Since the Midiki way requires that we set
     * our handlers dynamically and the Communicator way requires that we
     * bind our services statically by building them as methods on a server,
     * we need a static junction between the two. Requires that only one
     * Communicator federation be active at a time; not too restrictive.
     *
     * Another way to do this, if we can rely on the Server getting its
     * MainServer as an argument, is to cast the MainServer to a known
     * Midiki-ized subclass and get the data from there.
     */
    static public HashMap serviceHandlers;
    static public HashMap serviceParameters;
    static public HashMap requestParameters;
    /**
     * Return the tag by which this mediator is known.
     */
    public String publicName()
    {
        return "galaxy";
    }
    /**
     * Describe <code>agentName</code> method here.
     *
     */
    public String agentName()
    {
        return myAgentName;
    }
    /*
     * Storage of infrastructure-specific data items
     */
    private MainServer mainServer;
    private String serverClassName;
    private int serverPortNumber;
    private String[] commandLineArguments;
    private boolean isNative=false;
    /**
     * Set configuration data for this mediator from properties.
     *
     * @param props a <code>PropertyTree</code> value
     * @return a <code>boolean</code> value
     */
    public boolean configure(PropertyTree props)
    {
        PropertyTree pmode = props.child("mode");
        if (pmode != null) {
            isNative = ((String)(pmode.value())).equalsIgnoreCase("native");
        }
        PropertyTree serverClass = props.child("server_class");
        if (serverClass != null) {
            serverClassName = (String)(serverClass.value());
        }
        return true;
    }
    /**
     * Returns <code>true</code> if this mediator follows Midiki
     * protocols for data exchange, or <code>false</code> if only
     * the underlying framework is to be used. Enables/disables
     * additional processing in cells.
     *
     * @return a <code>boolean</code> value
     */
    public boolean usesMidikiProtocol()
    {
        return !isNative;
    }
    /**
     * Register the agent with the mediator. There should be
     * only one InfoState connected to this mediator.
     *
     * create new MainServer()
     */
    public boolean register(String asName)
    {
        logger.logp(Level.FINER,
                    "org.mitre.midiki.impl.mitre.GalaxyMediator",
                    "register",asName);
        myAgentName = asName;
        mainServer =
            new MainServer(myAgentName, commandLineArguments, serverPortNumber);
        mainServer.setServerClassName(serverClassName);
        return true;
    }
    /**
     * Describe <code>declareServices</code> method here.
     *
     * Store created signatures for addition to servers.
     */
    public boolean declareServices(Object services, Object requests)
    {
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "declareServices",services);
        return true;
    }
    /**
     * Describe <code>appendServiceDeclaration</code> method here.
     *
     * Create a Signature
     *
	SigEntry[] foo = {new SigEntry(":int",GalaxyObject.GAL_INT, Signature.GAL_KEY_ALWAYS), new SigEntry(":program",GalaxyObject.GAL_STRING, Signature.GAL_KEY_SOMETIMES)};
	addSignature(new Signature("twice", foo , Signature.GAL_OTHER_KEYS_NEVER, (SigEntry[])null, Signature.GAL_OTHER_KEYS_NEVER, Signature.GAL_REPLY_NONE));	
    *
    * The following are operations defined on the data_master service
    * for the Contract:
    *    $data, $changed, $provide, $instantiate, $rootinstance, $change, $new
     */
    public Object appendServiceDeclaration(String name,
                                           Object parameters,
                                           Object serviceList)
    {
        // name, contract_name
        ArrayList decl = new ArrayList();
        decl.add(name);
        decl.add(((Contract)parameters).name());
        ((List)serviceList).add(decl);
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "appendServiceDeclaration",decl);
        String serviceTag = ((Contract)parameters).name()+name;
        tag_id++;
        return serviceTag;
    }
    /**
     * Describe <code>appendQueryDeclaration</code> method here.
     *
     */
    public Object appendQueryDeclaration(String cell,
                                         String query,
                                         boolean isnative,
                                         Object parameters,
                                         Object serviceList)
    {
        //
        ArrayList decl = new ArrayList();
        decl.add(cell);
        decl.add(query);
        decl.add(new Boolean(isnative));
        decl.add(parameters);
        ((List)serviceList).add(decl);
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "appendQueryDeclaration",decl);
        String serviceTag = "query$"+cell+"$"+query;
        tag_id++;
        // now store a copy of the parameters
        LinkedList paramCopy = new LinkedList();
        while (((Iterator)parameters).hasNext()) {
            paramCopy.add(((Iterator)parameters).next());
        }
        serviceParameters.put(serviceTag, paramCopy);
        return serviceTag;
    }
    /**
     * Describe <code>appendActionDeclaration</code> method here.
     *
     */
    public Object appendActionDeclaration(String cell,
                                          String action,
                                          boolean isnative,
                                          Object parameters,
                                          Object serviceList)
    {
        //
        ArrayList decl = new ArrayList();
        decl.add(cell);
        decl.add(action);
        decl.add(new Boolean(isnative));
        decl.add(parameters);
        ((List)serviceList).add(decl);
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "appendActionDeclaration",decl);
        String serviceTag = "method$"+cell+"$"+action;
        tag_id++;
        // now store a copy of the parameters
        LinkedList paramCopy = new LinkedList();
        while (((Iterator)parameters).hasNext()) {
            paramCopy.add(((Iterator)parameters).next());
        }
        serviceParameters.put(serviceTag, paramCopy);
        return serviceTag;
    }
    /**
     * Describe <code>appendServiceSpecification</code> method here.
     * Declarations handle the 'subscribe' side of the interface.
     * Some interfaces also require specification of the 'publish' side.
     * That's what the specifications are for.
     *
     */
    public Object appendServiceSpecification(String name,
                                             Object parameters,
                                             Object requestList)
    {
        // name, contract_name
        ArrayList decl = new ArrayList();
        decl.add(name);
        decl.add(((Contract)parameters).name());
        ((List)requestList).add(decl);
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "appendRequestDeclaration",decl);
        String requestTag = ((Contract)parameters).name()+name;
        tag_id++;
        return requestTag;
    }
    /**
     * Describe <code>appendQuerySpecification</code> method here.
     *
     */
    public Object appendQuerySpecification(String cell,
                                           String query,
                                           boolean isnative,
                                           Object parameters,
                                           Object requestList)
    {
        //
        ArrayList decl = new ArrayList();
        decl.add(cell);
        decl.add(query);
        decl.add(new Boolean(isnative));
        decl.add(parameters);
        ((List)requestList).add(decl);
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "appendQueryDeclaration",decl);
        String requestTag = "query$"+cell+"$"+query;
        tag_id++;
        // now store a copy of the parameters
        LinkedList paramCopy = new LinkedList();
        while (((Iterator)parameters).hasNext()) {
            paramCopy.add(((Iterator)parameters).next());
        }
        requestParameters.put(requestTag, paramCopy);
        return requestTag;
    }
    /**
     * Describe <code>appendActionSpecification</code> method here.
     *
     */
    public Object appendActionSpecification(String cell,
                                            String action,
                                            boolean isnative,
                                            Object parameters,
                                            Object requestList)
    {
        System.out.println("appendActionSpecification "+cell+" "+action);
        //
        ArrayList decl = new ArrayList();
        decl.add(cell);
        decl.add(action);
        decl.add(new Boolean(isnative));
        decl.add(parameters);
        ((List)requestList).add(decl);
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "appendActionSpecification",decl);
        String requestTag = "method$"+cell+"$"+action;
        tag_id++;
        // now store a copy of the parameters
        LinkedList paramCopy = new LinkedList();
        while (((Iterator)parameters).hasNext()) {
            paramCopy.add(((Iterator)parameters).next());
        }
        requestParameters.put(requestTag, paramCopy);
        return requestTag;
    }
    /**
     * Describe <code>registerServiceHandler</code> method here.
     *
     */
    public void registerServiceHandler(Object service, 
                                       ServiceHandler handler)
    {
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "registerServiceHandler",service);
        if (serviceHandlers.put(service, handler) != null) {
            logger.logp(Level.FINER,
                        "org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "registerServiceHandler",
                        "replacing previous handler");
        }
    }
    /**
     * Describe <code>assertReadiness</code> method here.
     *
     * mainServer.start()
     */
    public void assertReadiness()
    {
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "assertReadiness");
        try {
            mainServer.start();
        } catch(Exception ex) {
            logger.throwing("org.mitre.midiki.impl.mitre.GalaxyMediator",
                            "assertReadiness", ex);
        }
    }
    /**
     * Describe <code>useService</code> method here.
     *
     * should use EnvDispatchFrame() (synchronous)
     *
     * Communicator doesn't support the notion of broadcast.
     * In order to send a broadcast, we need to know all of the
     * destinations in a way Communicator understands.
     * Or we need to make a Hub program that distributes messages.
     */
    public boolean useService(Object request,
                              Object parameters,
                              Object results)
    {
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "useService",
                        request.toString());
        boolean success = false;
        String rqst = (String)request;
        if (rqst.charAt(0) == ('$')) {
            if (parameters instanceof CellInstance) {
                rqst = ((CellInstance)parameters).instanceType.name()+rqst;
            } else {
                rqst = ((List)parameters).get(0)+rqst;
            }
        }

        ((Collection)results).clear();

        System.out.println("GalaxyMediator.useService(): "+request+": "+parameters+": "+results);
        ServiceHandler svc = (ServiceHandler)serviceHandlers.get(rqst);
        if (svc==null) {
            System.out.println("Sending event to Hub!");
            LinkedList cp = (LinkedList)requestParameters.get(request);
            if (cp==null) {
                RuntimeException re = new RuntimeException("Request parameters not registered: "+request);
                logger.throwing("org.mitre.midiki.impl.mitre.GalaxyMediator",
                                "useService",re);
                re.printStackTrace();
                //throw re;
                return false;
            }
            int lastDollarSign = ((String)request).lastIndexOf('$');
            if (lastDollarSign != -1) {
                request = ((String)request).substring(lastDollarSign+1);
            }
            Bindings bindings = new BindingsImpl();
            GFrame rqstFrame = MidikiServer.composeFrame((Collection)parameters,bindings,(String)request,cp.iterator());
            System.out.println(rqstFrame);
            try {
                //Environment env = new Environment();
                Environment env = currentServer.getCurrentEnvironment();
                GFrame res = env.dispatchFrame(rqstFrame);
                System.out.println("res == "+res);
            } catch (DispatchError dex) {
                logger.logp(Level.SEVERE,
                            "org.mitre.midiki.impl.mitre.GalaxyMediator",
                            "useService",request.toString(),
                            "Received error from Environment.dispatchFrame: " + dex.getData());
                return false;
            } catch (Exception ex) {
                logger.logp(Level.SEVERE,
                            "org.mitre.midiki.impl.mitre.GalaxyMediator",
                            "useService",request.toString(),
                            "Caught exception while writing to Hub"+ex);
                return false;
            }
            logger.logp(Level.FINE,
                        "org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "useService",request.toString(),
                        "succeeded");
            return true;
        } else {
            System.out.println("Handling service");
            success = svc.handleEvent(rqst, (List)parameters, (Collection)results);
        }

        logger.exiting("org.mitre.midiki.impl.mitre.GalaxyMediator",
                       "useService",
                       request.toString());
        logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.GalaxyMediator",
                    "useService",
                    (success ? "succeeded" : "failed"));
        return success;
    }
    /**
     * Describe <code>requestService</code> method here.
     *
     * should also use EnvDispatchFrame (synchronous)
     */
    public boolean requestService(Object request,
                                  Object parameters,
                                  Object results)
    {
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "requestService",
                        request.toString());
        return false;
    }
    /**
     * Broadcasts a request to all listeners. Since this version of the
     * Mediator only serves a single local InfoState, we don't need to
     * do anything fancy. Just use the same code used for 'use'.
     *
     * Should use EnvWriteFrame (asynchronous)
     *
     * Communicator doesn't support the notion of broadcast.
     * In order to send a broadcast, we need to know all of the
     * destinations in a way Communicator understands.
     */
    public boolean broadcastServiceRequest(Object request,
                                           Object parameters,
                                           Object results)
    {
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "broadcastServiceRequest",
                        request.toString());
        boolean success = false;
        String rqst = (String)request;
        if (rqst.charAt(0) == ('$')) {
            rqst = ((List)parameters).get(0)+rqst;
        }
        ServiceHandler svc = (ServiceHandler)serviceHandlers.get(rqst);
        if (svc==null) {
            logger.logp(Level.WARNING,
                        "org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "broadcastServiceRequest",request.toString(),
                        "failed");
        } else {
            success = svc.handleEvent(rqst, (List)parameters, (Collection)results);
        }
        logger.exiting("org.mitre.midiki.impl.mitre.GalaxyMediator",
                       "broadcastServiceRequest",
                       request.toString());
        logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.GalaxyMediator",
                    "broadcastServiceRequest",
                    (success ? "succeeded" : "failed"));
        return success;
    }
    /**
     * Describe <code>getData</code> method here.
     *
     */
    public boolean getData(Object cellName,
                           Object cellInstance,
                           Object results)
    {
        logger.logp(Level.FINER,
                    "org.mitre.midiki.impl.mitre.GalaxyMediator",
                    "getData",
                    cellName.toString(),cellInstance);
        return false;
    }
    /**
     * Describe <code>putData</code> method here.
     *
     */
    public boolean putData(Object request)
    {
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "putData",
                        request);
        return false;
    }
    /**
     * Describe <code>replaceData</code> method here.
     *
     */
    public boolean replaceData(Object oldData, Object newData)
    {
        logger.logp(Level.FINER,
                    "org.mitre.midiki.impl.mitre.GalaxyMediator",
                    "replaceData",
                    "old-data",oldData);
        logger.logp(Level.FINER,
                    "org.mitre.midiki.impl.mitre.GalaxyMediator",
                    "replaceData",
                    "new-data",newData);
        return false;
    }
    /**
     * Describe <code>pauseInteraction</code> method here.
     *
     */
    public Object pauseInteraction(String key, Object param)
    {
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "pauseInteraction",
                        key);
        return null;
    }
    /**
     * Describe <code>resumeInteraction</code> method here.
     *
     */
    public Object resumeInteraction(String key, Object param)
    {
        logger.entering("org.mitre.midiki.impl.mitre.GalaxyMediator",
                        "resumeInteraction",
                        key);
        return null;
    }

    public GalaxyMediator()
    {
        serverClassName = "galaxy.server.Server";
        serverPortNumber = 6502;
        commandLineArguments = new String[0];
    }

    public GalaxyMediator(String serverClass)
    {
        this();
        serverClassName = serverClass;
    }

    public GalaxyMediator(String serverClass, String[] arguments, int port)
    {
        this(serverClass);
        commandLineArguments = arguments;
        serverPortNumber = port;
    }

    static int tag_id = 0;
    static {
        serviceHandlers = new HashMap();
        serviceParameters = new HashMap();
        requestParameters = new HashMap();
    }
}
