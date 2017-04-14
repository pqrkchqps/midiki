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
package org.mitre.dm;

import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;
import org.mitre.midiki.tools.*;
import org.mitre.dm.tools.*;

import java.util.*;
import java.util.logging.*;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Loads a set of agents, identifies the contracts
 * they require and cells they provide, builds an
 * information state compatible with those requirements,
 * connects all of the agents and starts the DM.
 * 
 * There are two modes for using the Executive class:
 * as an object created by another Java class, and as
 * a standalone executable. When invoked by another class,
 * as in e.g. org.mitre.dm.qud.domain.travel.travel_dm,
 * the invoking class creates an Executive object,
 * creates a list of agent classes, optionally sets
 * configuration data destined for selected agents
 * (indexed by the agent's name), and then calls the
 * launchAgents method to start Midiki. When invoked
 * as a standalone executable, the Executive expects
 * a properties file name as an argument. These properties
 * will be passed to each agent as a PropertyTree
 * when configuration data is required.
 *
 * Future versions of the Executive may accept a list of
 * properties files to be merged into a single tree.
 */
 
public class Executive implements DialogueSystem
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.Executive");
    
    private HashMap configObjects;
    private Object signalWhenDone;
    private LinkedList dialogueSystemListeners;
    private boolean showInfoStateGui;

    public Object getObjectToBeSignaledUponTermination()
    {
        return signalWhenDone;
    }

    /**
     * Return the central point for logging dialogue system events.
     * This version builds a logger that includes the Executive hash code
     * as a component of the logger name. It does not do anything to the
     * log record destinations or formatting.
     *
     * @return a <code>Logger</code> value
     */
    public Logger getLogger()
    {
        //return Logger.getLogger(hashCode()+".midiki.dialogue");
        return midikiLogger;
    }
    /**
     * Return the central point for logging dialogue system events.
     * Allows logging with class-based logger names while still
     * maintaining cohesion of logs for a single system.
     *
     * @param name a <code>String</code> value
     * @return a <code>Logger</code> value
     */
    public Logger getLogger(String name)
    {
        //return Logger.getLogger(hashCode()+"."+name);
        return midikiLogger.getLogger(name);
    }

    /**
     * Add the specified listener to the notification queue
     * for dialogue system events.
     *
     * @param dsl DialogueSystemListener to be added
     */    
    public void addDialogueSystemListener(DialogueSystemListener dsl)
    {
        if (dialogueSystemListeners == null) {
            dialogueSystemListeners = new LinkedList();
        }
        dialogueSystemListeners.add(dsl);
    }
    /**
     * Fetch the list of currently registered dialogue system listeners.
     *
     * @return
     */    
    public DialogueSystemListener[] getDialogueSystemListeners()
    {
        if (dialogueSystemListeners == null) {
            return null;
        }
        return (DialogueSystemListener[])dialogueSystemListeners.toArray();
    }
    /**
     * Remove the specified listener from the notification queue
     * for dialogue system events.
     *
     * @param dsl DialogueSystemListener to be removed
     */    
    public void removeDialogueSystemListener(DialogueSystemListener dsl)
    {
        if (dialogueSystemListeners != null) {
            dialogueSystemListeners.remove(dsl);
        }
    }

    /**
     * Figure out which ClassLoader to use.  For JDK 1.2 and later use
     * the context ClassLoader.
     */           
    private static ClassLoader findClassLoader()
        throws AgentConfigurationException
    {
        Method m = null;
      
        try {
            m = Thread.class.getMethod("getContextClassLoader", (Class[])null);
        } catch (NoSuchMethodException e) {
            // Assume that we are running JDK 1.1, use the current ClassLoader
            logger.logp(Level.FINE,"org.mitre.dm.Executive","findClassLoader","assuming JDK 1.1");
            return Executive.class.getClassLoader();
        }
      
        try {
            return (ClassLoader) m.invoke(Thread.currentThread(), (Object[])null);
        } catch (IllegalAccessException e) {
            // assert(false)
            throw new AgentConfigurationException("Unexpected IllegalAccessException",
                                         e);
        } catch (InvocationTargetException e) {
            // assert(e.getTargetException() instanceof SecurityException)
            throw new AgentConfigurationException("Unexpected InvocationTargetException",
                                         e);
        }
    }
      
    /**
     * Create an instance of a class using the specified ClassLoader
     */
    private static Object newInstance(String className,
                                      ClassLoader classLoader)
        throws AgentConfigurationException
    {
        try {
            Class spiClass;
            if (classLoader == null) {
                spiClass = Class.forName(className);
            } else {
                spiClass = classLoader.loadClass(className);
            }
            return spiClass.newInstance();
        } catch (ClassNotFoundException x) {
            throw new AgentConfigurationException(
                                         "Provider " + className + 
                                         " not found", x);
        } catch (Exception x) {
            throw new AgentConfigurationException(
                                         "Provider " + className + 
                                         " could not be instantiated: " + x,
                                         x);
        }
    }
      
    static public Object find(String agentClassId)
        throws AgentConfigurationException
    {
        ClassLoader classLoader = findClassLoader();
      
        return newInstance(agentClassId, classLoader);
    }
      
    public Agent[] assembleFederates(String[] agentClassNames)
        throws AgentConfigurationException
    {
        Agent[] agents = new Agent[agentClassNames.length];
        for (int i=0; i<agentClassNames.length; i++) {
            agents[i] = (Agent)find(agentClassNames[i]);
            agents[i].attachTo(this);
        }
        return agents;
    }

    static public void extendInfoState(InfoStateImpl isi,
                                       Contract c,
                                       CellClient client)
    {
        ImmutableCellImpl icell = new ImmutableCellImpl(c, isi.getView());
        CellImpl cell = new CellImpl(c, isi);
        cell.setView(icell);
        cell.setClient(client);
        icell.setClient(client);
        isi.addCell(c.name(), cell);
        ((ImmutableInfoStateImpl)isi.getView()).addImmutableCell(c.name(), icell);
        // build query and method proxies accessing the client
        Iterator qit = c.queries();
        while (qit.hasNext()) {
            Contract.Query queryMethod =
                (Contract.Query)qit.next();
            cell.addQueryHandler(queryMethod.name(), new QueryHandlerProxy(queryMethod.name(), icell, client));
        }
        Iterator mit = c.methods();
        while (mit.hasNext()) {
            Contract.Method actionMethod =
                (Contract.Method)mit.next();
            cell.addMethodHandler(actionMethod.name(), new MethodHandlerProxy(actionMethod.name(), icell, client));
        }
    }

    private int evaluating;
    public Agent[] agents;
    private HashSet requiredContracts;
    private HashSet providedCells;
    private ImmutableInfoStateImpl iis;
    private InfoStateImpl is;

    /**
     * Puts an object in the executive that will be passed to an agent when its
     * init method is called based on its name.
     * 
     * @param agentName
     * @param configObject
     */
    public void addAgentConfigurationObject(String agentName, Object configObject)
    {
        configObjects.put(agentName, configObject);
    }
    
    private void activate()
    {
        if (dialogueSystemListeners != null) {
            Iterator it = dialogueSystemListeners.iterator();
            while (it.hasNext()) {
                DialogueSystemListener dsl = (DialogueSystemListener)it.next();
                dsl.activation(is.getView());
            }
        }
    }

    private void propagate()
    {
        // no associated dialogue system event
    }

    private void quiesce()
    {
        if (dialogueSystemListeners != null) {
            Iterator it = dialogueSystemListeners.iterator();
            while (it.hasNext()) {
                DialogueSystemListener dsl = (DialogueSystemListener)it.next();
                dsl.quiescence(iis);
            }
        }
    }
    
    public void launchAgents(String[] agentClassNames)
        throws AgentConfigurationException
    {
        HashSet contracts = new HashSet();
        HashSet cells = new HashSet();
        HashMap clientMap = new HashMap();
        ensureLoggerSupport();
        activateLogFile();
        try {
            agents = assembleFederates(agentClassNames);
        } catch (AgentConfigurationException ex) {
            System.out.println("Unrecoverable agent configuration problem.");
            logger.throwing("org.mitre.dm.Executive","launchAgents",ex);
            throw ex;
        }
        for (int i=0; i<agents.length; i++) {
            logger.logp(Level.CONFIG,"org.mitre.dm.Executive","launchAgents","Agent",agents[i]);
            agents[i].init(configObjects.get(agents[i].getName()));
            Set ctrs = agents[i].getRequiredContracts();
            if (ctrs != null) contracts.addAll(ctrs);
            Set cls = agents[i].getProvidedCells();
            if (cls != null) cells.addAll(cls);
        }
        // create the Mediator (backplane, agent environment, ...)
        Mediator mediator = new DummyMediator();
        // This single-process Executive always creates an ISControlCell
        // in required contracts and provided cells.
        ISControlCell controlCell = new ISControlCell(mediator, contracts, cells);
        controlCell.setObjectToSignalWhenDone(signalWhenDone);
        controlCell.addISControlListener(new ISControlAdapter() {
            /**
             * Called when the information state becomes quiescent;
             * i.e., there are no further changes to be propagated.
             */
            public void quiescence() {
                //System.out.println("@@@@@ quiescence @@@@@");
                quiesce();
            }
            /**
             * Called just before the information state begins propagating
             * changes. May be called as each change propagates.
             */
            public void activation() {
                //System.out.println("@@@@@ activation @@@@@");
                //activate();
            }
            /**
             * Called just before the information state begins propagating
             * changes. May be called as each change propagates.
             */
            public void propagation() {
                //System.out.println("@@@@@ propagation @@@@@");
                propagate();
            }
        });
        contracts.add(controlCell.getContract());
        cells.add(controlCell.initializeHandlers());
        Collection defaultingCells = controlCell.getProvidedCells();
        if (defaultingCells != null) getLogger().info("Providing default servers for "+defaultingCells);
        cells.addAll(defaultingCells);
        logger.logp(Level.CONFIG,"org.mitre.dm.Executive","launchAgents","Contracts",contracts);
        logger.logp(Level.CONFIG,"org.mitre.dm.Executive","launchAgents","Cells",cells);
        // create the master info state components.
        iis = new ImmutableInfoStateImpl();
        is = new InfoStateImpl(iis) {
                public void declare_external_services(Collection services, Collection requests, Mediator mediatedBy)
                {
                    // is_control has additional requirements.
                    // add service handler for $evaluate.
                    ServiceHandler svcHdlr;
                    // Register as a provider for this interface
                    Object evaluateTag =
                        mediatedBy.appendServiceDeclaration("$evaluate",
                                                            ISControlCell.getContract(),
                                                            services);
                    //System.out.println("ISControl.evaluate tag == "+evaluateTag);
                    svcHdlr = new ServiceHandler() {
                            public boolean handleEvent(String event,
                                                       List parameters,
                                                       Collection results) {
                                if (evaluating==0) {
                                    logger.logp(Level.FINE,"org.mitre.dm.Executive","$evaluate.handleEvent","evaluating agents");
                                    evaluating++;
                                    while (evaluating>0) {
                                        lock();
                                        lockViewers();
                                        notifyViewers();
                                        activate();
                                        notifyAllInfoListeners();
                                        unlockViewers();
                                        unlock();
                                        evaluating--;
                                    }
                                } else {
                                    logger.logp(Level.FINE,"org.mitre.dm.Executive","$evaluate.handleEvent","queueing evaluation");
                                    evaluating++;
                                }
                                return true;
                            }
                        };
                    mediatedBy.registerServiceHandler(evaluateTag, svcHdlr);
                }
            };
        Iterator it = contracts.iterator();
        while (it.hasNext()) {
            Contract c = (Contract)it.next();
            // for each contract, build a client, an icell and a cell.
            // cell client only needs infostate for 2 things:
            // (1) getting a Unifier, and
            // (2) notifying an infolistener.
            CellClient client = new CellClient(c, mediator);
            clientMap.put(c, client);
            extendInfoState(is, c, client);
            is.addClient(client);
            // if handlers have been provided, create a server.
            Iterator cit = cells.iterator();
            while (cit.hasNext()) {
                CellHandlers ch = (CellHandlers)cit.next();
                if (ch.getContract().name().equals(c.name())) {
                    CellServer server = new CellServer(ch, mediator);
                    is.addServer(server);
                    client.setLocalProvider(server);
                    break;
                }
            }
        }
        logger.logp(Level.CONFIG,"org.mitre.dm.Executive","launchAgents","ClientMap",clientMap);
        InfoStateImpl[] infoStates = new InfoStateImpl[agents.length];
        infoStates[0] = is; // first declared agent gets master IS
        for (int i=1; i<agents.length; i++) {
            ImmutableInfoStateImpl iisi = new ImmutableInfoStateImpl();
            infoStates[i] = new InfoStateImpl(iisi);
            Set requiredContracts = agents[i].getRequiredContracts();
            if (requiredContracts != null) {
                //Iterator cit = requiredContracts.iterator();
                Iterator cit = contracts.iterator();
                while (cit.hasNext()) {
                    Contract c = (Contract)cit.next();
                    extendInfoState(infoStates[i],
                                    c,
                                    (CellClient)clientMap.get(c));
                }
            }
            logger.logp(Level.CONFIG,"org.mitre.dm.Executive","main","ISControl client",(CellClient)clientMap.get(controlCell.getContract()));
            extendInfoState(infoStates[i],
                            controlCell.getContract(),
                            (CellClient)clientMap.get(controlCell.getContract()));
            is.addViewer(infoStates[i]);
        }
        // now the info state should be initialized. Connect it.
        // note this may be pulled out of InfoStateImpl with the
        // advent of InfoState closures. this stuff is for setting up
        // cell clients and servers.
        if (!is.connect(mediator)) {
            RuntimeException ex =
                new RuntimeException("Cannot connect IS to Mediator");
            logger.throwing("org.mitre.dm.Executive","main",ex);
            throw ex;
        }
        is.instantiateRootDataSet();
        // IS is connected. connect all of the agents to it.
        // this will become connection of each agent to its IS.
        for (int i=0; i<agents.length; i++) {
            if (!agents[i].connect(infoStates[i])) {
                RuntimeException ex =
                    new RuntimeException("Agent connect failed: "+
                                         agents[i].getName());
                logger.throwing("org.mitre.dm.Executive","main",ex);
                throw ex;
            }
        }
        // start the ball rolling!
        // pick one info state, probably is_control, to set the attribute
        is.lock();
        is.cell("is").put("program_state","starting");
        is.unlock();
        
        try {
            synchronized(signalWhenDone) {
                signalWhenDone.wait();
            }
            //System.out.println("*** executive resumed");
            ((MidikiLogger)getLogger()).close();
            terminate();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        
    }

    private void getContractsAndCells()
    {
        requiredContracts = new HashSet();
        providedCells = new HashSet();
        for (int i=0; i<agents.length; i++) {
            logger.logp(Level.CONFIG,"org.mitre.dm.Executive","getContractsAndCells","Agent",agents[i]);
            agents[i].init(null);
            Set ctrs = agents[i].getRequiredContracts();
            if (ctrs != null) requiredContracts.addAll(ctrs);
            Set cls = agents[i].getProvidedCells();
            if (cls != null) providedCells.addAll(cls);
        }
        logger.logp(Level.CONFIG,"org.mitre.dm.Executive","getContractsAndCells","Contracts",requiredContracts);
        logger.logp(Level.CONFIG,"org.mitre.dm.Executive","getContractsAndCells","Cells",providedCells);
    }

    /**
     * Returns true if the specified contract is to be included
     * in the current information state. Assumption is to include it
     * unless we specify a mediator and it doesn't match the tag.
     * We maintain this assumption to keep the property file smaller.
     *
     * @param contractName a <code>String</code> value
     * @param agentSpecs a <code>PropertyTree</code> value
     * @param mediatorTag a <code>String</code> value
     * @return a <code>boolean</code> value
     */
    private boolean inThisInfoState(String contractName,
                                    PropertyTree agentSpecs,
                                    String mediatorTag)
    {
        if (agentSpecs == null) {
            System.out.println("No properties: everything goes in default");
            System.out.println(agentSpecs);
            return mediatorTag.equals("default");
        }
        PropertyTree contract = agentSpecs.child("contract");
        if (contract == null) {
            // no contract spec; true iff default infoState
            return mediatorTag.equals("default");
        }
        PropertyTree thisContract = contract.child(contractName);
        if (thisContract == null) {
            // no contract spec; true iff default infoState
            return mediatorTag.equals("default");
        }
        PropertyTree reqdMediator = thisContract.child("mediator");
        if (reqdMediator == null) {
            // no contract mediator spec; true iff default infoState
            return mediatorTag.equals("default");
        }
        return ((String)(reqdMediator.value())).equals(mediatorTag);
    }

    static public String DEFAULT_FORMATTER = "org.mitre.dm.qud.XMLDiscourseFormatter";

    private MidikiLogger midikiLogger;
    private String logFileName;

    public void setLogFileFormatter(java.util.logging.Formatter f)
    {
        midikiLogger.setFormatter(f);
    }

    public String getLogFileName()
    {
        return logFileName;
    }

    public void setLogFileName(String logfile)
    {
        logFileName = logfile;
    }

    public void activateLogFile()
    {
        midikiLogger.setFileName(logFileName);
    }

    public Executive()
    {
        configObjects = new HashMap();
        signalWhenDone = new Integer(0);
        midikiLogger = new MidikiLogger(hashCode()+".midiki.discourse");
        try {
            setLogFileFormatter((java.util.logging.Formatter)find(DEFAULT_FORMATTER));
        } catch (AgentConfigurationException ace) {
            System.out.println("Could not find log formatter class "+DEFAULT_FORMATTER);
        }
        Calendar calendar = Calendar.getInstance();
        String dateString = calendar.get(Calendar.YEAR)+"_"+(calendar.get(Calendar.MONTH)+1)+"_"+
                calendar.get(Calendar.DATE)+"_"+calendar.get(Calendar.HOUR_OF_DAY)+"_"+
                calendar.get(Calendar.MINUTE);
        logFileName = "midiki-"+dateString+"-"+hashCode()+".log";
    }

    public Executive(PropertyTree agentSpecs, Mediator mediator, String mTag)
    {
        this();
        //TODO: implementation needed here!
    }

    public class MediatorInstance
    {
        public String tag;
        public Mediator mediator;
        public HashMap clientMap;
        public ISControlCell controlCell;
        public MediatorInstance(String t, Mediator i)
        {
            tag=t; mediator=i; controlCell = null; clientMap = new HashMap();
        }
    }

    public class AgentInstance
    {
        String tag;
        Agent agent;
        boolean isMaster;
        public AgentInstance(String t, Agent i, boolean m)
        {
            tag=t; agent=i; isMaster=m;
        }
    }

    public InfoStateImpl makeInfoState(MediatorInstance medInst,
                                       AgentInstance[] agents,
                                       PropertyTree configurationData)
    {
        logger.logp(Level.CONFIG,"org.mitre.dm.Executive","makeInfoState","MediatorInstance",medInst);
        ImmutableInfoStateImpl iis = null;
        InfoStateImpl is = null;
        PropertyTree agentSpecs = configurationData.child("agent");

        // create the Agents and gather contracts, cells by mediator
        requiredContracts = new HashSet();
        providedCells = new HashSet();
        for (int i=0; i<agents.length; i++) {
            logger.logp(Level.CONFIG,"org.mitre.dm.Executive","makeInfoState","Agent",agents[i].agent);
            agents[i].agent.init(configurationData);
            Set ctrs = agents[i].agent.getRequiredContracts();
            if (ctrs != null) {
                Iterator cit = ctrs.iterator();
                while (cit.hasNext()) {
                    Contract contract = (Contract)cit.next();
                    try {
                        if (inThisInfoState(contract.name(),
                                            agentSpecs.child(agents[i].tag),
                                            medInst.tag))
                            requiredContracts.add(contract);
                    } catch (NullPointerException e) {
                        throw new RuntimeException("agent "+agents[i].tag+" contract error: null contract pointer");
                    }
                }
            }
            Set cls = agents[i].agent.getProvidedCells();
            if (cls != null) {
                Iterator cit = cls.iterator();
                while (cit.hasNext()) {
                    CellHandlers cell = (CellHandlers)cit.next();
                    if (inThisInfoState(cell.getContract().name(),
                                        agentSpecs.child(agents[i].tag),
                                        medInst.tag))
                        providedCells.add(cell);
                }
            }
        }
        logger.logp(Level.CONFIG,"org.mitre.dm.Executive","makeInfoState","Contracts",requiredContracts);
        logger.logp(Level.CONFIG,"org.mitre.dm.Executive","makeInfoState","Cells",providedCells);

        // if mediator mode not native, create ISControlCell
        if (medInst.mediator.usesMidikiProtocol()) {
            medInst.controlCell = new ISControlCell(medInst.mediator, requiredContracts, providedCells);
            medInst.controlCell.addISControlListener(new ISControlAdapter() {
                /**
                 * Called when the information state becomes quiescent;
                 * i.e., there are no further changes to be propagated.
                 */
                public void quiescence() {
                    //System.out.println("@@@@@ quiescence @@@@@");
                    quiesce();
                }
                /**
                 * Called just before the information state begins propagating
                 * changes. May be called as each change propagates.
                 */
                public void activation() {
                    //System.out.println("@@@@@ activation @@@@@");
                    //activate();
                }
                /**
                 * Called just before the information state begins propagating
                 * changes. May be called as each change propagates.
                 */
                public void propagation() {
                    //System.out.println("@@@@@ propagation @@@@@");
                    propagate();
                }
            });
            medInst.controlCell.setObjectToSignalWhenDone(signalWhenDone);
            requiredContracts.add(medInst.controlCell.getContract());
            providedCells.add(medInst.controlCell.initializeHandlers());
            providedCells.add(medInst.controlCell.getProvidedCells());
            // create the master info state components.
            iis = new ImmutableInfoStateImpl();
            is = new InfoStateImpl(iis) {
                    public void declare_external_services(Collection services, Collection requests, Mediator mediatedBy)
                    {
                        // is_control has additional requirements.
                        // add service handler for $evaluate.
                        ServiceHandler svcHdlr;
                        // Register as a provider for this interface
                        Object evaluateTag =
                            mediatedBy.appendServiceDeclaration("$evaluate",
                                                                ISControlCell.getContract(),
                                                                services);
                        svcHdlr = new ServiceHandler() {
                                public boolean handleEvent(String event,
                                                           List parameters,
                                                           Collection results) {
                                    if (evaluating==0) {
                                        logger.logp(Level.FINE,"org.mitre.dm.Executive","$evaluate.handleEvent","evaluating agents");
                                        evaluating++;
                                        while (evaluating>0) {
                                            lock();
                                            lockViewers();
                                            notifyViewers();
                                            activate();
                                            notifyAllInfoListeners();
                                            unlockViewers();
                                            unlock();
                                            evaluating--;
                                        }
                                    } else {
                                        logger.logp(Level.FINE,"org.mitre.dm.Executive","$evaluate.handleEvent","queueing evaluation");
                                        evaluating++;
                                    }
                                    return true;
                                }
                            };
                        mediatedBy.registerServiceHandler(evaluateTag, svcHdlr);
                    }
                };
        } else {
            iis = new ImmutableInfoStateImpl();
            is = new InfoStateImpl(iis);
        }
        // build the infoStates
        //System.out.println("Contracts for "+medInst.tag+" = "+requiredContracts);
        Iterator it = requiredContracts.iterator();
        while (it.hasNext()) {
            Contract c = (Contract)it.next();
            // for each contract, build a client, an icell and a cell.
            CellClient client = new CellClient(c, medInst.mediator);
            medInst.clientMap.put(c, client);
            extendInfoState(is, c, client);
            is.addClient(client);
            // if handlers have been provided, create a server.
            Iterator cit = providedCells.iterator();
            while (cit.hasNext()) {
                CellHandlers ch = (CellHandlers)cit.next();
                if (ch.getContract().name().equals(c.name())) {
                    CellServer server = new CellServer(ch, medInst.mediator);
                    is.addServer(server);
                    client.setLocalProvider(server);
                    break;
                }
            }
        }
        logger.logp(Level.CONFIG,"org.mitre.dm.Executive","makeInfoState","ClientMap",medInst.clientMap);
        return is;
    }

    protected int masterAgentIndex;
    public AgentInstance[] makeAgents(PropertyTree agentSpecs)
    {
        AgentInstance[] agents = null;
        int agentCount = 0;
        masterAgentIndex = 0;
        Iterator it = agentSpecs.children();
        while (it.hasNext()) {
            PropertyTree aNext = (PropertyTree)it.next();
            PropertyTree agentMaster = aNext.child("master");
            if (agentMaster != null) {
                masterAgentIndex = agentCount;
            }
            agentCount++;
        }
        agents = new AgentInstance[agentCount];
        agentCount = 0;
        it = agentSpecs.children();
        while (it.hasNext()) {
            PropertyTree aNext = (PropertyTree)it.next();
            String agentTag = (String)(aNext.name());
            String agentClassName = (String)(aNext.child("class").value());
            Agent agentTemp = null;
            try {
                agentTemp = (Agent)find(agentClassName);
                agentTemp.attachTo(this);
                agents[agentCount] = new AgentInstance(agentTag, agentTemp, (agentCount==masterAgentIndex));
                agentCount++;
            } catch (AgentConfigurationException ex) {
                logger.logp(Level.SEVERE,"org.mitre.dm.Executive","makeAgents","configuration error: "+agentClassName,ex);
            }
        }
        return agents;
    }

    public MediatorInstance[] makeMediators(PropertyTree mediatorSpecs)
    {
        MediatorInstance[] mediators = null;
        int mediatorCount = 0;
        Iterator it = mediatorSpecs.children();
        while (it.hasNext()) {
            PropertyTree mNext = (PropertyTree)it.next();
            mediatorCount++;
        }
        mediators = new MediatorInstance[mediatorCount];
        mediatorCount = 0;
        it = mediatorSpecs.children();
        while (it.hasNext()) {
            PropertyTree mNext = (PropertyTree)it.next();
            PropertyTree className = mNext.child("class");
            String medName = (String)mNext.name();
            Mediator medInst = null;
            try {
                if (className != null) {
                    medInst = (Mediator)find((String)className.value());
                    medInst.configure(mNext);
                }
                mediators[mediatorCount] =
                    new MediatorInstance(medName, medInst);
                // increment the mediator count, proceed to next one.
                mediatorCount++;
            } catch (AgentConfigurationException ex) {
                logger.logp(Level.SEVERE,"org.mitre.dm.Executive","makeMediators","configuration error: "+className,ex);
            }
        }
        return mediators;
    }

    static public Executive[] spawn(PropertyTree props)
    {
        logger.logp(Level.CONFIG,"org.mitre.dm.Executive","spawn","Configuration data",props);
        Executive temp = new Executive();
        temp.activateLogFile();
        return temp.spawnExecs(props);
    }

    public Executive[] spawnExecs(PropertyTree props)
    {
        // just an interim hack during multi-mediator code development...
        Executive[] execs = new Executive[1];
        execs[0] = this;

        // create Mediators
        MediatorInstance[] mediators = makeMediators(props.child("mediator"));
        // create Agents
        AgentInstance[] agents = makeAgents(props.child("agent"));
        // create Mediator-dependent infoStates
        InfoStateImpl[] infoStates = new InfoStateImpl[mediators.length];
        for (int i=0; i<mediators.length; i++) {
            infoStates[i] = makeInfoState(mediators[i], agents, props);
        }
        // now the info states should be initialized. Connect them.
        // note this may be pulled out of InfoStateImpl with the
        // advent of InfoState closures. this stuff is for setting up
        // cell clients and servers.
        for (int i=0; i<infoStates.length; i++) {
            infoStates[i].setAgentName(mediators[i].tag);
            if (!infoStates[i].connect(mediators[i].mediator)) {
                RuntimeException ex =
                    new RuntimeException("Cannot connect IS to Mediator");
                logger.throwing("org.mitre.dm.Executive","main",ex);
                throw ex;
            }
        }
        // create composite info states
        is = infoStates[0];
        if (infoStates.length > 1) {
            ImmutableCompositeInfoState icis = new ImmutableCompositeInfoState();
            CompositeInfoState cis = new CompositeInfoState(icis);
            for (int i=0; i<infoStates.length; i++) {
                if (infoStates[i]==null) {
                    System.out.println("Composing infoState, but component "+i+" is null!");
                    continue;
                }
                cis.addInfoState(infoStates[i]);
                icis.addImmutableInfoState(infoStates[i].getView());
            }
            is = cis;
        }
        // assign infostates to agents
        InfoStateImpl[][] agentInfoStates = new InfoStateImpl[agents.length][mediators.length];
        for (int i=0; i<agents.length; i++) {
            if (i == masterAgentIndex) {
                // agent declared as master gets master IS
                // it doesn't matter what the other IS' are set to.
                agentInfoStates[i][0] = is;
                continue;
            }
            for (int j=0; j<mediators.length; j++) {
                ImmutableInfoStateImpl iisi = new ImmutableInfoStateImpl();
                agentInfoStates[i][j] = new InfoStateImpl(iisi);
                // changed following pair of lines so that all agents that
                // share a mediator see the same cells. this corrects a
                // problem introduced by allowing queries and methods to be
                // called from within task plans. the contracts were visible
                // from the domain agent, but not visible from the DmeAgent
                // which needed to execute the queries and methods.
                //Set agentContracts = agents[i].agent.getRequiredContracts();
                Set agentContracts = is.getContracts();
                if (agentContracts != null) {
                    Iterator cit = agentContracts.iterator();
                    while (cit.hasNext()) {
                        Contract c = (Contract)cit.next();
                        // only add a Client for a Mediator
                        // if it is defined there!
                        if (infoStates[j].hasCell(c.name())) {
                            extendInfoState(agentInfoStates[i][j],
                                            c,
                                            (CellClient)mediators[j].clientMap.get(c));
                        }
                    }
                    /*
                      if (mediator.usesMidikiProtocol()) {
                      logger.logp(Level.FINE,"org.mitre.dm.Executive","main","ISControl client",(CellClient)clientMap.get(controlCell.getContract()));
                      extendInfoState(agentInfoStates[i],
                      controlCell.getContract(),
                      (CellClient)clientMap.get(controlCell.getContract()));
                      }
                    */
                }
                infoStates[j].addViewer(agentInfoStates[i][j]);
            }
            // NOTE: this test assumes every agent has a composite IS.
            // that isn't usually the case, so a more sophisticated test
            // is called for. in the meantime, drop any null infoStates.
            if ((mediators.length > 1) && (i != masterAgentIndex)) {
                ImmutableCompositeInfoState icis = new ImmutableCompositeInfoState();
                CompositeInfoState cis = new CompositeInfoState(icis);
                for (int j=0; j<agentInfoStates[i].length; j++) {
                    if (agentInfoStates[i][j]==null) continue;
                    cis.addInfoState(agentInfoStates[i][j]);
                    icis.addImmutableInfoState(agentInfoStates[i][j].getView());
                }
                agentInfoStates[i][0] = cis;
            }
        }

        // if we're going to make a composite info state, it must be NLT here.
        // might need to be earlier, but then must remember extra details.

        //System.out.println("\n*** Instantiating root instances\n");
        is.instantiateRootDataSet();
        // IS is connected. connect all of the agents to it.
        // this will become connection of each agent to its IS.
        for (int i=0; i<agents.length; i++) {
            if (!agents[i].agent.connect(agentInfoStates[i][0])) {
                RuntimeException ex =
                    new RuntimeException("Agent connect failed: "+
                                         agents[i].agent.getName());
                logger.throwing("org.mitre.dm.Executive","main",ex);
                throw ex;
            }
        }

        // in order to correctly terminate the federation, we need to keep
        // some reference to the agents in a readily accessible place.
        // we will put that on the first executive instance.
        execs[0].agents = new Agent[agents.length];
        for (int i=0; i<agents.length; i++) {
            execs[0].agents[i] = agents[i].agent;
        }

        // set the executive program state attribute, if defined
        //System.out.println("\n*** Setting startup attribute\n");
        PropertyTree psProp = props.child("executive");
        if (psProp != null) {
            PropertyTree psStartup = psProp.child("startup");
            if (psStartup != null) {
                PropertyTree psCell = psStartup.child("cell");
                PropertyTree psAttr = psStartup.child("attribute");
                PropertyTree psValue = psStartup.child("value");
                String psCellValue = (String)psCell.value();
                String psAttrValue = (String)psAttr.value();
                String psValueValue = (String)psValue.value();
                try {
                    if (is.hasCell(psCellValue)) {
                        is.lock();
                        is.cell(psCellValue).put(psAttrValue,psValueValue);
                        is.unlock();
                    }
                } catch (Exception e) {
                    System.out.println("*** exception during startup invocation");
                    e.printStackTrace();
                    System.out.println("*** continuing");
                }
            }
            PropertyTree psPrintState = psProp.child("printstate");
            if (psPrintState != null) {
                if (psPrintState.value().equals("true"))
                    addDialogueSystemListener(new ISGuiAdapter());
            }
        }
        return execs;
    }

    public void terminate()
    {
        // notify all agents of disconnection and destruction
        if (agents == null) {
            System.out.println("agents have already been destroyed!");
        }
        for (int i=0; i<agents.length; i++) {
            agents[i].disconnect();
        }
        for (int i=0; i<agents.length; i++) {
            agents[i].destroy();
            agents[i] = null;
        }
        agents = null;
        is = null;
        iis = null;
    }

    static protected void ensureLoggerSupport()
    {
        boolean haveParent = true;
        Logger myLogger = logger;
        LogEventHandler logEventHandler = null;
        while (haveParent) {
            Handler[] handlers = myLogger.getHandlers();
            if (handlers != null) {
                for (int i=0; i<handlers.length; i++) {
                    if (handlers[i] instanceof LogEventHandler) {
                        if (logEventHandler != null) {
                            logger.warning("Multiple LogEventHandlers!");
                        } else {
                            logEventHandler = (LogEventHandler)handlers[i];
                        }
                    }
                }
            }
            Level myLevel = myLogger.getLevel();
            /* In case the user has forgotten to update the logging.properties
             * file, add the handler and level that we need for this to work.
             */
            if (((myLogger.getParent() == null) && 
                 (logEventHandler == null)) ||
                (handlers == null))
            {
                logEventHandler = new LogEventHandler();
                logEventHandler.setLevel(Level.FINER);
                myLogger.addHandler(logEventHandler);
            }
            myLogger = myLogger.getParent();
            if (myLogger == null) haveParent = false;
        }
    }

    static public void main(String[] args)
    {
        /*
        Executive exec = new Executive();
        String[] agentClassNames = {
            "org.mitre.dm.qud.IOAgent",
            "org.mitre.dm.qud.InterpretAgent",
            "org.mitre.dm.qud.domain.diagnosis.DomainAgent",
            "org.mitre.dm.qud.DmeAgent",
            "org.mitre.dm.qud.GenerateAgent",
            "org.mitre.dm.qud.ISControlAgent"
        };
        exec.launchAgents(agentClassNames);
        */
        if (args.length != 1) {
            System.out.println("Specify a .properties file to load.");
            System.exit(1);
        }
        ensureLoggerSupport();
        PropertyTree props = PropertyTree.load(args[0]);
        Executive[] execs = spawn(props);
        
        try {
            synchronized(execs[0].signalWhenDone) {
                execs[0].signalWhenDone.wait();
            }
            // System.out.println("*** executive resumed");
            for (int i=0; i<execs.length; i++) {
                ((MidikiLogger)execs[i].getLogger()).close();
                execs[i].terminate();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        
    }

}
