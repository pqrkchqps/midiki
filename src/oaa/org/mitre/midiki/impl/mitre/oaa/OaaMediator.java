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
package org.mitre.midiki.impl.mitre.oaa;

import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;
import org.mitre.midiki.logic.*;

import java.util.*;
import java.util.logging.*;

// OAA imports
import com.sri.oaa2.com.*;
import com.sri.oaa2.lib.*;
import com.sri.oaa2.icl.*;

/**
 * Provides an implementation of <code>Mediator</code>
 * for use with the Open Agent Architecture (OAA) framework.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class OaaMediator implements Mediator
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.oaa.OaaMediator");
    /**
     * Local storage of data distribution agent name.
     */
    private String myAgentName;
	/**
     * Local instance of OAA library using TCP/IP connections.
     */
    LibOaa myOaa = new LibOaa(new LibCom(new LibComTcpProtocol(), null));
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
    static public HashMap serviceNames;
    static public HashMap serviceParameters;
    static public HashMap requestParameters;
    /**
     * Return the tag by which this mediator is known.
     */
    public String publicName()
    {
        return "oaa";
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
    private String hostName;
    private int portNumber;
    private String[] commandLineArguments;
    private boolean isNative=true;
    /**
     * Set configuration data for this mediator from properties.
     *
     * @param props a <code>PropertyTree</code> value
     * @return a <code>boolean</code> value
     */
    public boolean configure(PropertyTree props)
    {
        PropertyTree phost = props.child("host");
        if (phost != null) {
            hostName = (String)phost.value();
        }
        PropertyTree pport = props.child("port");
        if (pport != null) {
            try {
                portNumber = Integer.parseInt((String)(pport.value()));
            } catch (NumberFormatException nfe) {
                portNumber = 3378;
                logger.logp(Level.WARNING,
                            "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                            "configure",
                            "port number format error",(String)pport.value());
            }
        }
        System.out.println("hostName="+hostName+", portNumber="+portNumber);
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
     * Perform OAA-specific registration actions.
     *
     * @param asName a <code>String</code> value
     * @return a <code>boolean</code> value
     */
    protected boolean registerWithFramework(String asName)
    {
        // First, connects to the facilitator
        if (myOaa.getComLib().comConnected("parent")) {
            logger.logp(Level.WARNING,
                        "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "registerWithFramework",
                        "Already connected");
            return true;
        }
        
        if (!myOaa.getComLib().comConnect("parent", 
                                          IclTerm.fromString(true,"tcp(A,B)"), 
                                          (IclList)IclTerm.fromString(true,"[]"))) {
            logger.logp(Level.WARNING,
                        "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "registerWithFramework",
                        "Couldn't connect to the facilitator");
            return false;
        }
                    
        // Once the connection is established,
        //performs handshaking with the facilitator.
        if (!myOaa.oaaRegister("parent",
                               asName,
                               (IclList)IclTerm.fromString(true,"[]"), 
                               (IclList)IclTerm.fromString(true,"[]"))) 
        {
            logger.logp(Level.WARNING,
                        "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "registerWithFramework",
                        "Could not register");
            return false;
        }
        Thread.yield();  // give the OAA listener a chance to respond.

        return true;
    }

    /**
     * Register the agent with the mediator. There should be
     * only one InfoState connected to this mediator.
     */
    public boolean register(String asName)
    {
        logger.logp(Level.FINER,
                    "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                    "register",asName);
        myAgentName = asName;
        return registerWithFramework(asName);
    }
    /**
     * Describe <code>declareServices</code> method here.
     *
     * Store created signatures for addition to servers.
     */
    public boolean declareServices(Object services, Object requests)
    {
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "declareServices",services);
        System.out.println("declareServices:"+services);
        IclList solvables = new IclList(new ArrayList((List)services));
        Object solvRet = myOaa.oaaDeclare(solvables,
                                          IclTerm.fromString(false,"[]"),
                                          IclTerm.fromString(false,"[]"),
                                          IclTerm.fromString(false,"[]"));
        logger.logp(Level.FINER,
                    "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                    "declareServices","solvables returned",solvRet);
        return true;
    }
    /**
     * Append a declaration for a service which is neither a query
     * nor an action, but instead takes the attributes of a contract
     * as its parameters.
     *
     * The following are operations defined on the data_master service
     * for the Contract:
     *    $data, $changed, $provide, $instantiate, $rootinstance, $change, $new
     */
    public Object appendServiceDeclaration(String name,
                                           Object parameters,
                                           Object serviceList)
    {
        final String serviceTag = ((Contract)parameters).name()+name;
        serviceNames.put(serviceTag, serviceTag);
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "appendServiceDeclaration",serviceTag);
        tag_id++;
        System.out.println("appendServiceDeclaration("+name+","+((Contract)parameters).name()+")");
        // create ArrayList to hold parameters
        ArrayList parms = new ArrayList();
        parms.add(new IclVar("_instanceid"));
        // create new IclVar for each parameter ("_"+name)
        Iterator parmIt = ((Contract)parameters).attributes();
        while (parmIt.hasNext()) {
            Contract.Attribute attr = (Contract.Attribute)parmIt.next();
            IclVar attrVar = new IclVar("_"+attr.name());
            parms.add(attrVar);
        }
        // create new IclStruct for name plus parameters
        IclStruct svc = new IclStruct(serviceTag.replace('$','_'), parms);
        // create and register a callback
        myOaa.oaaRegisterCallback(serviceTag.replace('$','_'),
            new OAAEventListener() {
                public boolean doOAAEvent(IclTerm goal, IclList params, IclList _answers) {
                    // put the selection from registered handlers in here
                    ServiceHandler sh =
                        (ServiceHandler)serviceHandlers.get(serviceTag);
                    Contract cp =
                        (Contract)serviceParameters.get(serviceTag);
                    // --- a miracle occurs ---
                    // - strip all parameters from the goal and make list
                    // - make empty list for results
                    // - invoke the service handler
                    // - shift results to answers, converting as required. 
                    return true;
                }
            });
        // build the solvable struct, append it to the serviceList
        ArrayList callbackParms = new ArrayList();
        callbackParms.add(new IclStr(serviceTag.replace('$','_')));
        IclStruct cb = new IclStruct("callback", callbackParms);
        IclList solvableArgs = new IclList();
        solvableArgs.add(cb);
        ArrayList solvableParms = new ArrayList();
        solvableParms.add(svc);
        solvableParms.add(solvableArgs);
        solvableParms.add(new IclList());
        IclStruct solv = new IclStruct("solvable", solvableParms);
        ((List)serviceList).add(solv);

        serviceParameters.put(serviceTag, parameters);

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
        final String serviceTag = "query$"+cell+"$"+query;
        String solvableTag = serviceTag;
        if (isNative) solvableTag = query;
        serviceNames.put(serviceTag, solvableTag);
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "appendQueryDeclaration",serviceTag);
        tag_id++;

        // create ArrayList to hold parameters
        ArrayList parms = new ArrayList();
        LinkedList paramCopy = new LinkedList();
        // create new IclVar for each parameter ("_"+name)
        while (((Iterator)parameters).hasNext()) {
            Contract.Parameter attr = (Contract.Parameter)((Iterator)parameters).next();
            IclVar attrVar = new IclVar("_"+attr.name());
            parms.add(attrVar);
            paramCopy.add(attr);
        }
        // create new IclStruct for name plus parameters
        IclStruct svc = new IclStruct(solvableTag.replace('$','_'), parms);
        // create and register a callback
        myOaa.oaaRegisterCallback(serviceTag.replace('$','_'),
            new OAAEventListener() {
                public boolean doOAAEvent(IclTerm goal, IclList params, IclList _answers) {
                    // put the selection from registered handlers in here
                    ServiceHandler sh =
                        (ServiceHandler)serviceHandlers.get(serviceTag);
                    List cp =
                        (List)serviceParameters.get(serviceTag);
                    // --- a miracle occurs ---
                    // - strip all parameters from the goal and make list
                    // - make empty list for results
                    // - invoke the service handler
                    // - shift results to answers, converting as required. 
                    _answers.add(goal);
                    return true;
                }
            });
        // build the solvable struct, append it to the serviceList
        ArrayList callbackParms = new ArrayList();
        callbackParms.add(new IclStr(serviceTag.replace('$','_')));
        IclStruct cb = new IclStruct("callback", callbackParms);
        IclList solvableArgs = new IclList();
        solvableArgs.add(cb);
        ArrayList solvableParms = new ArrayList();
        solvableParms.add(svc);
        solvableParms.add(solvableArgs);
        solvableParms.add(new IclList());
        IclStruct solv = new IclStruct("solvable", solvableParms);
        ((List)serviceList).add(solv);

        // now store a copy of the parameters
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
        final String serviceTag = "method$"+cell+"$"+action;
        String solvableTag = serviceTag;
        if (isNative) solvableTag = action;
        serviceNames.put(serviceTag, solvableTag);
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "appendActionDeclaration",serviceTag);
        tag_id++;

        // create ArrayList to hold parameters
        ArrayList parms = new ArrayList();
        LinkedList paramCopy = new LinkedList();
        // create new IclVar for each parameter ("_"+name)
        while (((Iterator)parameters).hasNext()) {
            Contract.Parameter attr = (Contract.Parameter)((Iterator)parameters).next();
            IclVar attrVar = new IclVar("_"+attr.name());
            parms.add(attrVar);
            paramCopy.add(attr);
        }
        // create new IclStruct for name plus parameters
        IclStruct svc = new IclStruct(solvableTag.replace('$','_'), parms);
        // create and register a callback
        myOaa.oaaRegisterCallback(serviceTag.replace('$','_'),
            new OAAEventListener() {
                public boolean doOAAEvent(IclTerm goal, IclList params, IclList _answers) {
                    System.out.println("action callback: "+goal);
                    // put the selection from registered handlers in here
                    ServiceHandler sh =
                        (ServiceHandler)serviceHandlers.get(serviceTag);
                    List cp =
                        (List)serviceParameters.get(serviceTag);
                    // --- a miracle occurs ---
                    // - strip all parameters from the goal and make list
                    LinkedList parameters = new LinkedList();
                    Iterator pit = goal.iterator();
                    while (pit.hasNext()) {
                        parameters.add(fromIcl((IclTerm)pit.next()));
                    }
                    // - make empty list for results
                    LinkedList results = new LinkedList();
                    // - invoke the service handler
                    System.out.println("invoking handler with "+serviceTag+", "+parameters+", "+results);
                    boolean success =
                        sh.handleEvent(serviceTag, parameters, results);
                    // - shift results to answers, converting as required. 
                    // not extracting parameters for now, just return goal
                    _answers.add(goal);
                    return true;
                }
            });
        // build the solvable struct, append it to the serviceList
        ArrayList callbackParms = new ArrayList();
        callbackParms.add(new IclStr(serviceTag.replace('$','_')));
        IclStruct cb = new IclStruct("callback", callbackParms);
        IclList solvableArgs = new IclList();
        solvableArgs.add(cb);
        ArrayList solvableParms = new ArrayList();
        solvableParms.add(svc);
        solvableParms.add(solvableArgs);
        solvableParms.add(new IclList());
        IclStruct solv = new IclStruct("solvable", solvableParms);
        ((List)serviceList).add(solv);

        // now store a copy of the parameters
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
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "appendRequestSpecification",decl);
        String requestTag = ((Contract)parameters).name()+name;
        tag_id++;
        // now store a copy of the parameters
        LinkedList paramCopy = new LinkedList();
        paramCopy.add("_instanceid");
        Iterator parmIt = ((Contract)parameters).attributes();
        while (parmIt.hasNext()) {
            paramCopy.add(parmIt.next());
        }
        System.out.println("appendServiceSpecification("+requestTag+")");
        requestParameters.put(requestTag, paramCopy);
        String solvableTag = requestTag.replace('$','_');
        serviceNames.put(requestTag, solvableTag);
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
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "appendQueryDeclaration",decl);
        String requestTag = "query$"+cell+"$"+query;
        tag_id++;
        // now store a copy of the parameters
        LinkedList paramCopy = new LinkedList();
        while (((Iterator)parameters).hasNext()) {
            paramCopy.add(((Iterator)parameters).next());
        }
        requestParameters.put(requestTag, paramCopy);
        String solvableTag = requestTag.replace('$','_');
        if (isNative) solvableTag = query;
        serviceNames.put(requestTag, solvableTag);
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
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "appendActionSpecification",decl);
        String requestTag = "method$"+cell+"$"+action;
        tag_id++;
        // now store a copy of the parameters
        LinkedList paramCopy = new LinkedList();
        while (((Iterator)parameters).hasNext()) {
            paramCopy.add(((Iterator)parameters).next());
        }
        requestParameters.put(requestTag, paramCopy);
        String solvableTag = requestTag.replace('$','_');
        if (isNative) solvableTag = action;
        serviceNames.put(requestTag, solvableTag);
        return requestTag;
    }
    /**
     * Describe <code>registerServiceHandler</code> method here.
     *
     */
    public void registerServiceHandler(Object service, 
                                       ServiceHandler handler)
    {
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "registerServiceHandler",service);
        if (serviceHandlers.put(service, handler) != null) {
            logger.logp(Level.FINER,
                        "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "registerServiceHandler",
                        "replacing previous handler");
        }
    }
	/**
     * Defines the capabilities registered with Facilitator.
     *
     * @param goal an <code>IclTerm</code> value
     * @param params an <code>IclList</code> value
     * @param answers an <code>IclList</code> value
     * @return a <code>boolean</code> value
     */
    public boolean oaaDoEventCallback(IclTerm goal, IclList params, IclList answers) {
        System.out.println("MKAgentOaa.oaaDoEventCallback("+goal+")");
        return false;
    }
    /**
     * Declare that this federate is ready to participate.
     *
     * mainServer.start()
     */
    public void assertReadiness()
    {
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "assertReadiness");
        // Connection succeeded!
        myOaa.oaaReady(true);

        
        // Register OAA callback
        myOaa.oaaRegisterCallback("oaa_AppDoEvent",
                                  new OAAEventListener() {
                                          public boolean doOAAEvent(IclTerm goal, IclList params, IclList answers) 
                                          {
                                              return oaaDoEventCallback(goal, params, answers);
                                          }
                                      });
    }
    protected IclTerm toIcl(Object obj)
    {
        if (obj instanceof Variable) {
            return new IclVar(((Variable)obj).name());
        } else if (obj instanceof Predicate) {
            ArrayList args = new ArrayList();
            Iterator acts = ((Predicate)obj).arguments();
            while (acts.hasNext()) {
                args.add(toIcl(acts.next()));
            }
            return new IclStruct(((Predicate)obj).functor(), args);            
        } else if (obj instanceof Integer) {
            return new IclInt(((Integer)obj).intValue());
        } else if (obj instanceof Long) {
            return new IclInt(((Long)obj).longValue());
        } else if (obj instanceof Float) {
            return new IclFloat(((Float)obj).floatValue());
        } else if (obj instanceof Double) {
            return new IclFloat(((Double)obj).doubleValue());
        } else if (obj instanceof String) {
            return new IclStr((String)obj);
        } else if (obj instanceof List) {
            ArrayList args = new ArrayList();
            Iterator acts = ((List)obj).iterator();
            while (acts.hasNext()) {
                args.add(toIcl(acts.next()));
            }
            return new IclList(args);
        } else {
            // serialize and punt
            return new IclStr(obj.toString());
        }
    }
    protected Object fromIcl(IclTerm obj)
    {
        if (obj instanceof IclVar) {
            return new Variable(((IclVar)obj).toString());
        } else if (obj instanceof IclStruct) {
            ArrayList args = new ArrayList();
            Iterator acts = ((IclStruct)obj).iterator();
            while (acts.hasNext()) {
                args.add(fromIcl((IclTerm)acts.next()));
            }
            return new Predicate(((IclStruct)obj).getFunctor(), args);            
        } else if (obj instanceof IclInt) {
            return ((IclInt)obj).toLongObject();
        } else if (obj instanceof IclFloat) {
            return (((IclFloat)obj).toDoubleObject());
        } else if (obj instanceof IclStr) {
            return ((IclStr)obj).toUnquotedString();
        } else if (obj instanceof IclList) {
            ArrayList args = new ArrayList();
            Iterator acts = ((IclList)obj).iterator();
            while (acts.hasNext()) {
                args.add(fromIcl((IclTerm)acts.next()));
            }
            return args;
        } else {
            // serialize and punt
            return (obj.toString());
        }
    }
    /**
     * Construct an OAA solvable for the specified request and parameters.
     * The OAA doesn't care about the names of the formal parameters from
     * the service declaration, so they can be ignored.
     *
     * @param request an <code>Object</code> value
     * @param parameters an <code>Object</code> value
     * @return an <code>IclTerm</code> value
     */
    protected IclTerm composeRequest(Object request,
                                     Object actuals,
                                     List formals)
    {
        Object solvableName = serviceNames.get(request);
        if (solvableName == null) {
            throw new RuntimeException("No service name registered for "+solvableName);
        }
        ArrayList args = new ArrayList();
        Iterator acts = ((List)actuals).iterator();
        while (acts.hasNext()) {
            args.add(toIcl(acts.next()));
        }
        return new IclStruct((String)solvableName, args);
    }
    /**
     * Convert the set of replies into bindings.
     * NOTE: method for doing this depends on type of service,
     * which we do not always know at this low level.
     * Approach: put all of the responses into the bindings.
     * If more than one, separate each one by reset() calls.
     * Do not reset at the end; leave that up to the caller.
     *
     * @return a <code>Bindings</code> value
     */
    protected void bindSolutions(IclList solutions, Object results)
    {
        if (results == null) return;
        List rsltSet = (List)results;
        Bindings workingBindings = new BindingsImpl();
        // --- a miracle occurs ---
        rsltSet.addAll(workingBindings.marshalLatest());
    }
    static protected final int RETRY_INTERVAL = 1000;
    /**
     * Invokes the specified service and waits for a response.
     */
    public boolean useService(Object request,
                              Object parameters,
                              Object results)
    {
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
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

        System.out.println("OaaMediator.useService(): "+rqst+": "+parameters+": "+results);
        ServiceHandler svc = (ServiceHandler)serviceHandlers.get(rqst);
        if (svc==null) {
            System.out.println("Sending event to Facilitator!");
            LinkedList cp = (LinkedList)requestParameters.get(rqst);
            if (cp==null) {
                RuntimeException re = new RuntimeException("Request parameters not registered: "+rqst);
                System.out.println(requestParameters);
                logger.throwing("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                                "useService",re);
                re.printStackTrace();
                //throw re;
                return false;
            }
            // compose and dispatch the message, handling errors
            IclTerm solvableRequest = composeRequest(rqst, parameters, cp);
            IclList solutions = new IclList();
            boolean retval = false;
            while (!retval) {
                retval = myOaa.oaaSolve(solvableRequest, (IclList)IclTerm.fromString(false,"[block(true)]"), solutions);
                if (!retval) {
                    System.out.println("send failed; retrying");
                    try {
                        Thread.sleep(RETRY_INTERVAL);
                    } catch (InterruptedException ie) {
                    }
                }
            }
            bindSolutions(solutions, results);

            logger.logp(Level.FINE,
                        "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "useService",request.toString(),
                        (retval ? "succeeded" : "failed"));
            success = retval;
        } else {
            System.out.println("Handling service");
            success = svc.handleEvent(rqst, (List)parameters, (Collection)results);
        }

        logger.exiting("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                       "useService",
                       request.toString());
        logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.oaa.OaaMediator",
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
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "requestService",
                        request.toString());
        return false;
    }
    /**
     * Broadcasts a request to all listeners. The OAA handles
     * distribution, so just reuse the code for 'useService',
     * changing the 'block' parameter to false and adding 'reply(none)'.
     */
    public boolean broadcastServiceRequest(Object request,
                                           Object parameters,
                                           Object results)
    {
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "broadcastServiceRequest",
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

        System.out.println("OaaMediator.broadcastServiceRequest(): "+rqst+": "+parameters+": "+results);
        ServiceHandler svc = (ServiceHandler)serviceHandlers.get(rqst);
        if (svc==null) {
            System.out.println("Sending event to Facilitator!");
            LinkedList cp = (LinkedList)requestParameters.get(rqst);
            if (cp==null) {
                RuntimeException re = new RuntimeException("Request parameters not registered: "+rqst);
                System.out.println(requestParameters);
                logger.throwing("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                                "broadcastServiceRequest",re);
                re.printStackTrace();
                //throw re;
                return false;
            }
            // compose and dispatch the message, handling errors
            IclTerm solvableRequest = composeRequest(rqst, parameters, cp);
            IclList solutions = new IclList();   // will be discarded
            boolean retval = myOaa.oaaSolve(solvableRequest, (IclList)IclTerm.fromString(false,"[block(false),reply(none)]"), solutions);

            logger.logp(Level.FINE,
                        "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "broadcastServiceRequest",request.toString(),
                        (retval ? "succeeded" : "failed"));
            return retval;
        } else {
            System.out.println("Handling service");
            success = svc.handleEvent(rqst, (List)parameters, (Collection)results);
        }

        logger.exiting("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                       "broadcastServiceRequest",
                       request.toString());
        logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.oaa.OaaMediator",
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
                    "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
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
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
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
                    "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                    "replaceData",
                    "old-data",oldData);
        logger.logp(Level.FINER,
                    "org.mitre.midiki.impl.mitre.oaa.OaaMediator",
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
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "pauseInteraction",
                        key);
        myOaa.oaaDelaySolution(key);
        return key;
    }
    /**
     * Describe <code>resumeInteraction</code> method here.
     *
     */
    public Object resumeInteraction(String key, Object param)
    {
        logger.entering("org.mitre.midiki.impl.mitre.oaa.OaaMediator",
                        "resumeInteraction",
                        key);
        myOaa.oaaReturnDelayedSolutions(key, (IclList)param);
        return key;
    }

    public OaaMediator()
    {
        hostName = "localhost";
        portNumber = 7777;
        commandLineArguments = new String[0];
    }

    public OaaMediator(String[] arguments, int port)
    {
        commandLineArguments = arguments;
        portNumber = port;
    }

    static int tag_id = 0;
    static {
        serviceHandlers = new HashMap();
        serviceNames = new HashMap();
        serviceParameters = new HashMap();
        requestParameters = new HashMap();
    }
}
