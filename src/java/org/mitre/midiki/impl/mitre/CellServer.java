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

import org.mitre.midiki.logic.*;
import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;

import java.util.*;
import java.util.logging.*;

/**
 * Implements a <code>Cell</code> which is controlled locally.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see CellStore
 */
public class CellServer extends CellStore
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.CellServer");
    /**
     * Creates a new <code>LocalCellImpl</code> instance.
     * NOTE: the InfoState is still under construction when
     * we get it, so beware any use of it until later.
     *
     * @param ch a <code>Contract</code> value
     * @param m a <code>Mediator</code> value
     */
    public CellServer(CellHandlers ch, Mediator m)
    {
        super(ch.getContract(),m);
        contract = ch.getContract();
        cellHandlers = ch;
    }
    protected Contract contract;
    protected CellHandlers cellHandlers;
    /**
     * Describe variable <code>nextInstanceId</code> here.
     */
    public int nextInstanceId;
    /* {transient=false, volatile=false}*/
    /**
     * Describe variable <code>rootInstanceRef</code> here.
     */
    public CellInstance rootInstanceRef;
    /* {transient=false, volatile=false}*/

    /**
     * Describe <code>rootInstanceId</code> method here.
     *
     */
    public Object rootInstanceId() {
        return null;
    }
    /**
     * Describe <code>rootInstance</code> method here.
     *
     */
    public CellInstance rootInstance() {
        if (rootInstanceRef==null) {
            rootInstanceRef = createInstance();
        }
        return rootInstanceRef;
    }
    /**
     * Describe <code>createInstanceId</code> method here.
     *
     */
    public Object createInstanceId() {
        Object tempID = contract.name()+"_"+nextInstanceId;
        nextInstanceId++;
        return tempID;
    }
    /**
     * Describe <code>createInstance</code> method here.
     *
     */
    public CellInstance createInstance() {
        Object tempId = createInstanceId();
        try {
            CellInstance tempV = createCompatibleInstance(tempId);
            fillOutInstance(tempV);
            registerInstance(tempV);
            return tempV;
        } catch (NullPointerException npe) {
            logger.logp(Level.WARNING,"org.mitre.midiki.impl.mitre.CellServer","createInstance","cell instantiation failure",contract.name());
        }
        return null;
    }
    /**
     * Describe <code>findInstance</code> method here.
     *
     */
    public CellInstance findInstance(Object key) {
        Object tempV = cellInstances.get(key);
        if (tempV==null) return null;
        return (CellInstance)tempV;
    }
    /**
     * Describe <code>registerInstance</code> method here.
     *
     */
    public void registerNewInstance(Object tempId) {
        try {
            CellInstance tempV = createCompatibleInstance(tempId);
            fillOutInstance(tempV);
            registerInstance(tempV);
        } catch (NullPointerException npe) {
            logger.logp(Level.WARNING,"org.mitre.midiki.impl.mitre.CellServer","registerNewInstance","cell instantiation failure",contract.name());
        }
    }
    /**
     * Describe <code>fillOutInstance</code> method here.
     *
     */
    public void fillOutInstance(CellInstance tempV) {
        // NOTE: original version was abstract!! must extend functionality.
    }
    /**
     * Describe <code>changeInstanceData</code> method here.
     *
     */
    public boolean changeInstanceData(CellInstance inst) {
        CellInstance instancePtr = findInstance(inst.instanceId);
        if (instancePtr==null) {
            logger.logp(Level.WARNING,"org.mitre.midiki.impl.mitre.CellServer","changeInstanceData","change message received for unknown instance implies multiple servers",contract.name());
            registerInstance(inst);
            reportChangedData(inst);
        } else {
            instancePtr.instanceData = inst.instanceData;
            reportChangedData(instancePtr);
        }
        return true;
    }
    /**
     * Describe <code>requestInstanceDataChange</code> method here.
     *
     */
    public void requestInstanceDataChange(CellInstance data) {
        CellInstance instancePtr = findInstance(data.instanceId);
        if (instancePtr==null) {
            logger.logp(Level.WARNING,"org.mitre.midiki.impl.mitre.CellServer","requestInstanceDataChange","change message received for unknown instance implies multiple servers",contract.name());
            registerInstance(data);
            instancePtr = findInstance(data.instanceId);
        }
        // one way to change data is to just copy the aggregate.
        instancePtr.instanceData = data.instanceData;
        reportChangedData(instancePtr);
    }
    /**
     * Describe <code>requestUpdate</code> method here.
     *
     * @param data a <code>CellInstance</code> value
     */
    public void requestUpdate(CellInstance data) {
        CellInstance instancePtr = findInstance(data.instanceId);
        if (instancePtr!=null) {
            data.instanceData.clear();
            Iterator dit = instancePtr.instanceData.iterator();
            while (dit.hasNext()) {
                data.instanceData.add(dit.next());
            }
        }
    }
    /**
     * Describe <code>reportChangedData</code> method here.
     *
     */
    public void reportChangedData(CellInstance data) {
        if (!mediatedBy.usesMidikiProtocol()) {
            return;
        }
        mediatedBy.replaceData(data,data);
        logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.CellServer","reportChangedData","changed-data",data.instanceData);
        mediatedBy.useService(data.instanceType.name()+"$changed", data, null);
    }
    /**
     * Describe <code>declare_services</code> method here.
     *
     */
    public void declare_services(Collection services, Collection requests) {
        ServiceHandler svcHdlr;
        if (mediatedBy.usesMidikiProtocol()) {
            // Register as a provider for this interface
            Object provideTag =
                mediatedBy.appendServiceDeclaration("$provide",
                                                    contract,
                                                    services);
            svcHdlr = new ServiceHandler() {
                    public boolean handleEvent(String event,
                                               List parameters,
                                               Collection results) {
                        ArrayList args = new ArrayList();
                        args.add(contract.name());
                        args.add(new Integer(hashCode()));
                        results.add(args);
                        return true;
                    }
                };
            mediatedBy.registerServiceHandler(provideTag, svcHdlr);

            // Permit instantiation for this class
            Object instantiateTag =
                mediatedBy.appendServiceDeclaration("$instantiate",
                                                    contract,
                                                    services);
            svcHdlr = new ServiceHandler() {
                    public boolean handleEvent(String event,
                                               List parameters,
                                               Collection results) {
                        ArrayList args = new ArrayList();
                        args.add(contract.name());
                        args.add(createInstance());
                        results.add(args);
                        return true;
                    }
                };
            mediatedBy.registerServiceHandler(instantiateTag, svcHdlr);

            // Expose the root instance for this class
            Object rootofTag =
                mediatedBy.appendServiceDeclaration("$rootinstance",
                                                    contract,
                                                    services);
            svcHdlr = new ServiceHandler() {
                    public boolean handleEvent(String event,
                                               List parameters,
                                               Collection results) {
                        ArrayList args = new ArrayList();
                        args.add(contract.name());
                        args.add(rootInstance());
                        results.add(args);
                        return true;
                    }
                };
            mediatedBy.registerServiceHandler(rootofTag, svcHdlr);

            // Declare the interface record change service
            Object changeTag =
                mediatedBy.appendServiceDeclaration("$change",
                                                    contract,
                                                    services);
            svcHdlr = new ServiceHandler() {
                    public boolean handleEvent(String event,
                                               List parameters,
                                               Collection results) {
                        LinkedList data = new LinkedList(parameters);
                        Object instanceId = data.removeFirst();
                        CellInstance inst = new CellInstance(contract, instanceId, data);
                        boolean retval = changeInstanceData(inst);
                        return retval;
                    }
                };
            mediatedBy.registerServiceHandler(changeTag, svcHdlr);

            // Declare the interface record creation service
            Object createTag =
                mediatedBy.appendServiceDeclaration("$new",
                                                    contract,
                                                    services);
            svcHdlr = new ServiceHandler() {
                    public boolean handleEvent(String event,
                                               List parameters,
                                               Collection results) {
                        results.add(createInstance());
                        return true;
                    }
                };
            mediatedBy.registerServiceHandler(createTag, svcHdlr);
        }

        /*
         * Define any query services
         */
        Iterator queries = contract.queries();
        while (queries.hasNext()) {
            Contract.Query queryMethod =
                (Contract.Query)queries.next();
            Object queryTag =
                mediatedBy.appendQueryDeclaration(contract.name(),
                                                  queryMethod.name(),
                                                  false,
                                                  queryMethod.parameters(),
                                                  services);
            final QueryHandler qHandler =
                cellHandlers.query(queryMethod.name());
            if (qHandler != null) {
                svcHdlr = new ServiceHandler() {
                    public QueryHandler queryHandler = qHandler;
                    public boolean handleEvent(String event,
                                               List parameters,
                                               Collection results) {
                        Bindings bindings = new BindingsImpl();
                        bindings.unmarshalAll(results);
                        results.clear();
                        boolean success = queryHandler.query(parameters,
                                                             bindings);
                        results.addAll(bindings.marshalLatest());
                        return success;
                    }
                };
            } else {
                // no handler provided by user, so include a dummy stub.
                svcHdlr = new ServiceHandler() {
                    public boolean handleEvent(String event,
                                               List parameters,
                                               Collection results) {
                        results.clear();
                        return false;
                    }
                };
            }
            mediatedBy.registerServiceHandler(queryTag, svcHdlr);
        }
        /*
         * Define any action services
         */
        Iterator actions = contract.methods();
        while (actions.hasNext()) {
            Contract.Method actionMethod =
                (Contract.Method)actions.next();
            Object actionTag =
                mediatedBy.appendActionDeclaration(contract.name(),
                                                   actionMethod.name(),
                                                   false,
                                                   actionMethod.parameters(),
                                                   services);
            final MethodHandler qHandler =
                cellHandlers.method(actionMethod.name());
            if (qHandler != null) {
                svcHdlr = new ServiceHandler() {
                    public MethodHandler actionHandler =
                        (MethodHandler)qHandler;
                    public boolean handleEvent(String event,
                                               List parameters,
                                               Collection results) {
                        Bindings bindings = new BindingsImpl();
                        bindings.unmarshalAll(results);
                        results.clear();
                        boolean success = actionHandler.invoke(parameters,
                                                                bindings);
                        results.addAll(bindings.marshalLatest());
                        return success;
                    }
                };
            } else {
                // no handler provided by user, so include a dummy stub.
                svcHdlr = new ServiceHandler() {
                    public boolean handleEvent(String event,
                                               List parameters,
                                               Collection results) {
                        results.clear();
                        return false;
                    }
                };
            }
            mediatedBy.registerServiceHandler(actionTag, svcHdlr);
        }

    }
    /**
     * Describe <code>addServiceHandler</code> method here.
     *
     */
    public void addServiceHandler() {
    }
    /**
     * Describe <code>handleService</code> method here.
     *
     */
    public void handleService() {
    }
}
