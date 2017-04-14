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

public class CellClient extends CellStore
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.CellClient");
    /**
     * Creates a new <code>LocalCellImpl</code> instance.
     * NOTE: need a way to get the mediator and infostate
     * down into the CellStore
     *
     * @param c a <code>Contract</code> value
     */
    public CellClient(Contract c, Mediator m)
    {
        super(c,m);
        changedInstances = new LinkedList();
        incomingInstances = new LinkedList();
    }
    public void recordChange(Object oldValue,
                             Object newValue,
                             CellInstance instance)
    {
        logger.logp(Level.CONFIG,"org.mitre.midiki.impl.mitre.CellClient","recordChange",mediatedBy.publicName(),instance);
        logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellClient","recordChange","instance",instance.instanceId);
        logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.CellClient","recordChange","old-value",oldValue);
        logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.CellClient","recordChange","new-value",newValue);
        if ((oldValue==null) && (newValue==null)) return;
        if ((oldValue!=null) && (newValue!=null) &&
            termsMatch(oldValue, newValue)) return;
        if ((cellLocked || overrideChangeLock) &&
            !changedInstances.contains(instance)) {
            logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellClient","recordChange","changed-instance",instance);
            changedInstances.add(instance);
        }
    }

    /**
     * Describe variable <code>triggers</code> here.
     */
    public HashMap triggers;
    /* {transient=false, volatile=false}*/
    /**
     * Describe variable <code>cellLocked</code> here.
     */
    protected boolean cellLocked;
    /**
     * Describe variable <code>overrideChangeLock</code> here.
     */
    protected boolean overrideChangeLock;

    /* {transient=false, volatile=false}*/
    /**
     * Describe variable <code>localProvider</code> here.
     */
    public CellServer localProvider;
    /* {transient=false, volatile=false}*/
    /**
     * Describe variable <code>changedInstances</code> here.
     */
    public Collection changedInstances;
    /* {transient=false, volatile=false}*/
    /**
     * Describe variable <code>incomingInstances</code> here.
     */
    public Collection incomingInstances;
    /* {transient=false, volatile=false}*/
    /**
     * Describe variable <code>servicesDeclared</code> here.
     */
    protected boolean servicesDeclared;

    /**
     * Describe <code>addInfoListener</code> method here.
     *
     */
    public void addInfoListener(String attribute,
                                InfoListener cl,
                                InfoStateImpl isi) {
        if (triggers == null) {
            triggers = new HashMap();
        }
        Object o = triggers.get(attribute);
        if (o == null) {
            o = new LinkedList();
            triggers.put(attribute, o);
        }
        LinkedList triggersForAttribute = (LinkedList)o;
        Object[] ilRec = new Object[3];
        ilRec[0] = attribute;
        ilRec[1] = cl;
        ilRec[2] = isi;
        triggersForAttribute.add(ilRec);
    }
    /**
     * Describe <code>evaluateInfoListeners</code> method here.
     * Note: script-driven version assumed two-phase triggers
     * (evaluation and execution). New version has single-phase
     * triggers. API also requires InfoState parameter, and the
     * utility of passing a value to the change handler is unclear.
     * Note: this routine should no longer require a value argument,
     * since the single-phase execution takes place during the _second_
     * of the original two phases.
     *
     */
    public void evaluateInfoListeners(String attribute, Object value) {
        logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellClient","evaluateInfoListeners",attribute,value);
        if (triggers == null) {
            return;
        }
        Object o = triggers.get(attribute);
        if (o == null) {
            logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellClient","evaluateInfoListeners","no triggers registered for attribute");
            return;
        }
        LinkedList triggersForAttribute = (LinkedList)o;
        Iterator iter = triggersForAttribute.iterator();
        while (iter.hasNext())
        {
            Object[] ilRec = (Object[])iter.next();
            InfoListener il = (InfoListener)ilRec[1];
            InfoStateImpl isi = (InfoStateImpl)ilRec[2];
            isi.fire(il);
        }
    }
    /**
     * Describe <code>pendingInfoListenerCount</code> method here.
     *
     */
    /*
    public int pendingInfoListenerCount() {
        return notifiedInfoListeners.size();
    }
    */
    /**
     * Describe <code>lock</code> method here.
     *
     */
    public void lock(boolean override) {
        cellLocked = true;
        overrideChangeLock = override;
    }
    /**
     * Describe <code>unlock</code> method here.
     *
     */
    public boolean unlock() {
        logger.entering("org.mitre.midiki.impl.mitre.CellClient","unlock",cellContract.name());
        boolean sentChanges = false;
        Iterator it = changedInstances.iterator();
        while (it.hasNext()) {
            CellInstance inst = (CellInstance)it.next();
            reportChanges(inst);
            sentChanges = true;
        }
        changedInstances.clear();
        cellLocked = false;
        overrideChangeLock = false;
        it = incomingInstances.iterator();
        while (it.hasNext()) {
            CellInstance record = (CellInstance)it.next();
            changedCellData(record);
        }
        incomingInstances.clear();
        return sentChanges;
    }
    /**
     * Describe <code>setLocalProvider</code> method here.
     *
     */
    public void setLocalProvider(CellServer lp) {
        localProvider = lp;
    }
    /**
     * Describe <code>findInstance</code> method here.
     *
     * @param instanceKey an <code>Object</code> value
     * @return a <code>CellInstance</code> value
     */
    public CellInstance findInstance(Object instanceKey)
    {
        CellInstance tempV = (CellInstance)cellInstances.get(instanceKey);
        if (tempV==null) {
            // skeleton hasn't given us the data for this entry yet.
            // make a local instance and force an update, assuming that
            // the instance is valid. must log this fact for later review.
            tempV = createCompatibleInstance(instanceKey);
            registerInstance(tempV);
        }
        return tempV;
    }
    /**
     * Describe <code>newInstance<code> method here.
     *
     */
    public CellInstance newInstance() {
        CellInstance tempV = null;
        if (mediatedBy.usesMidikiProtocol()) {
            if (localProvider == null) {
                LinkedList parameters = new LinkedList();
                LinkedList results = new LinkedList();
                mediatedBy.useService(cellContract.name()+"$new",parameters,results);
                tempV = (CellInstance)results.get(0);
            } else {
                tempV = localProvider.createInstance();
            }
            registerInstance(tempV);
        }
        logger.logp(Level.CONFIG, "org.mitre.midiki.impl.mitre.CellClient","newInstance",mediatedBy.publicName(),tempV);
        return tempV;
    }
    /**
     * Describe <code>initializeDataStore</code> method here.
     *
     */
    public void initializeDataStore(CellInstance inst) {
        if (localProvider == null) {
            requestFullUpdate(inst);
        } else {
            localProvider.requestUpdate(inst);
        }
    }
    /**
     * Describe <code>hasProvider</code> method here.
     *
     */
    public boolean hasProvider() {
        // if we're using a local provider, we're done
        if (localProvider!=null) return true;
        if (!mediatedBy.usesMidikiProtocol()) {
            return true;
        }
        LinkedList args = new LinkedList();
        args.add(cellContract.name());
        args.add(new Variable());
        LinkedList answers = new LinkedList();
        boolean found = mediatedBy.useService("$provides", args, answers);
        return found;
    }
    /**
     * Describe <code>waitForProvider</code> method here.
     *
     */
    public void waitForProvider() {
        // if we're using a local provider, don't poll the mediator
        if (localProvider!=null) return;
        if (!mediatedBy.usesMidikiProtocol()) {
            return;
        }
        boolean found=false;
        LinkedList args = new LinkedList();
        args.add(cellContract.name());
        args.add(new Variable());
        LinkedList answers = new LinkedList();
        while (!found) {
            // if waiting, want to be able to shutdown cleanly...
            found = mediatedBy.useService("$provides", args, answers);
            try {
                if (!found) Thread.sleep(1000);
            } catch (InterruptedException _interruptedException) {};
        };
    }
    public CellInstance rootInstanceData;
    public Object rootInstanceId()
    {
        if (!mediatedBy.usesMidikiProtocol()) {
            return null;
        }
        if (rootInstanceData == null) {
            rootInstanceData = rootInstance();
        }
        return rootInstanceData.instanceId;
    }
    /**
     * Describe <code>rootInstance</code> method here.
     *
     */
    public CellInstance rootInstance() {
        CellInstance root = null;
        CellInstance tempV = null;
        waitForProvider();
        // check if waiting ended normally or not...
        if (localProvider!=null) {
            root = localProvider.rootInstance();
        } else {
            if (!mediatedBy.usesMidikiProtocol()) {
                return null;
            }
            LinkedList args = new LinkedList();
            args.add(cellContract.name());
            args.add(new Variable());
            LinkedList answers = new LinkedList();
            boolean found =
                mediatedBy.useService("$rootinstance", args, answers);
            Object rslt = null;
            if (answers.size() > 0) {
                List parms = (List)answers.get(0);
                root = (CellInstance)parms.get(1);
            } else {
                // the information state isn't stable; we requested the
                // root instance for a cell, the mediator request completed,
                // but there is no value. that means that the cell isn't
                // hosted anywhere, or the host has not started up; both
                // conditions are supposed to be blocked by the API.
                return null;
            }
        }
        tempV = findInstance(root.instanceId);
        if (tempV == null) {
            tempV = createCompatibleInstance(root.instanceId);
            registerInstance(tempV);
        }
        // this event should probably be filtered from the table
        logger.logp(Level.CONFIG, "org.mitre.midiki.impl.mitre.CellClient","rootInstance",mediatedBy.publicName(),tempV);
        return tempV;
    }
    /**
     * Describe <code>reportChanges</code> method here.
     *
     * @param data a <code>CellInstance</code> value
     */
    public void reportChanges(CellInstance data)
    {
        logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellClient","repostChanges",""+data.instanceId,data);
        if (localProvider!=null) {
            localProvider.requestInstanceDataChange(data);
        } else if (mediatedBy.usesMidikiProtocol()) {
            mediatedBy.useService(data.instanceType.name()+"$change",data,null);
        }
    }
    /**
     * Routine for detecting changes in incoming terms,
     * which must be robust in the face of changes to
     * variable names. Specifically ignore the case where
     * a free variable is bound to a free variable.
     */ 
    private Bindings bindings=null;
    private boolean termsMatch(Object oldValue, Object newValue)
    {
        //if (bindings==null) bindings = infoState.createBindings();
        if (bindings==null) bindings = new BindingsImpl();
        bindings.clear();
        boolean match =
            Unify.getInstance().matchTerms(oldValue, newValue, bindings);
        if (!match) {
            return false;
        }
        if (bindings.size() > 0) {
            Iterator it1 = bindings.entrySet().iterator();
            while (it1.hasNext()) {
                Map.Entry entry = (Map.Entry)it1.next();
                if (!(entry.getValue() instanceof Object)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Describe <code>changedCellData</code> method here.
     *
     */
    public boolean changedCellData(CellInstance data) {
        CellInstance inst = findInstance(data.instanceId);
        if (inst==null) {
            logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellClient","changedCellData","unknown-instance",data);
            registerInstance(data);
            inst = findInstance(data.instanceId);
        }
        logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellClient","changedCellData","client checking for change.");

        Iterator base = inst.instanceData.iterator();
        Iterator chgd = data.instanceData.iterator();
        Iterator attr = cellContract.attributes();

        while (base.hasNext() && chgd.hasNext() && attr.hasNext())
        {
            Object bn = base.next();
            Object cn = chgd.next();
            Contract.Attribute an =
                (Contract.Attribute)attr.next();
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.CellClient","changedCellData","attribute",an.name());
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.CellClient","changedCellData","old-value",bn);
            logger.logp(Level.FINEST,"org.mitre.midiki.impl.mitre.CellClient","changedCellData","new-value",cn);
            if ((cn==null) && (bn==null)) continue;
            if ((cn!=null) && (bn!=null) &&
                termsMatch(cn, bn)) continue;
            evaluateInfoListeners(an.name(), cn);
        }
        if (base.hasNext() || chgd.hasNext() || attr.hasNext()) {
            // there's a mismatch in number of slots!
        }
        // now store the changed data, but don't interfere with
        // any indexing schemes that might be in play...
        inst.instanceData = data.instanceData;
        logger.logp(Level.CONFIG,"org.mitre.midiki.impl.mitre.CellClient","changedCellData",mediatedBy.publicName(),inst);
        // should we lock the cell now? do we need to lock at the level
        // of instance?
        return true;
    }
    /**
     * Describe <code>requestFullUpdate</code> method here.
     * Note: change detection requires access to term matching,
     * which is a property of the information state. Must give
     * the cell a reference to the info state it's a part of.
     *
     */
    public void requestFullUpdate(CellInstance data) {
        LinkedList _answers = new LinkedList();
        if (!mediatedBy.usesMidikiProtocol()) {
            return;
        }
        boolean success = mediatedBy.getData(cellContract.name(),
                                             data.instanceId,
                                             _answers);
        if (_answers.size() < 1) {
            return;
        }
        List goal = (List)_answers.get(0);  // take first tuple

        Iterator base = data.instanceData.iterator();
        Iterator chgd = goal.iterator();
        Iterator attr = cellContract.attributes();

        while (base.hasNext() && chgd.hasNext() && attr.hasNext())
        {
            Object bn = base.next();
            Object cn = chgd.next();
            Contract.Attribute an =
                (Contract.Attribute)attr.next();
            if ((cn==null) && (bn==null)) continue;
            if ((cn!=null) && (bn!=null) &&
                termsMatch(cn, bn)) continue;
            evaluateInfoListeners(an.name(), cn);
        }
        if (base.hasNext() || chgd.hasNext() || attr.hasNext()) {
            // there's a mismatch in number of slots!
        }
        data.instanceData = goal;
    }
    /**
     * Describe <code>getLocalProvider</code> method here.
     *
     */
    public CellServer getLocalProvider() {
        return localProvider;
    }
    /**
     * Describe <code>declare_services</code> method here.
     *
     */
    public boolean declare_services(Collection services, Collection requests) {
        if (servicesDeclared) {
            return true;
        }
        servicesDeclared = true;
        if (mediatedBy.usesMidikiProtocol()) {
            // Note: as the client, it should append a declaration for
            // every element of the protocol. it is up to the mediator
            // to ensure that server specifications override rather than
            // duplicate client declarations.
            // Register as a provider for this interface
            Object provideTag =
                mediatedBy.appendServiceSpecification("$provides",
                                                      cellContract,
                                                      requests);
            Object instantiateTag =
                mediatedBy.appendServiceSpecification("$instantiate",
                                                      cellContract,
                                                      requests);
            Object rootofTag =
                mediatedBy.appendServiceSpecification("$rootinstance",
                                                      cellContract,
                                                      requests);
            Object changeTag =
                mediatedBy.appendServiceSpecification("$change",
                                                      cellContract,
                                                      requests);
            Object createTag =
                mediatedBy.appendServiceSpecification("$new",
                                                      cellContract,
                                                      requests);
            // Declare the data solvable(s).
            // the format is interface_name(id,slot1,...,slotn)
            //
            Object dataTag = 
                mediatedBy.appendServiceDeclaration("$data",
                                                    cellContract,
                                                    services);
        
            // Declare the interface record change solvable
            Object changedTag = 
                mediatedBy.appendServiceDeclaration("$changed",
                                                    cellContract,
                                                    services);
            ServiceHandler changedHandler =
                new ServiceHandler() {
                        public boolean handleEvent(String event,
                                                   List parameters,
                                                   Collection result) {
                            LinkedList data = new LinkedList(parameters);
                            Object instanceId = data.removeFirst();
                            CellInstance cell =
                                new CellInstance(cellContract, instanceId, data);
                            logger.logp(Level.FINER,"org.mitre.midiki.impl.mitre.CellClient","handleEvent","changed-data",cell.instanceData);
                            if (cellLocked && !overrideChangeLock) {
                                incomingInstances.add(cell);
                                return true;
                            } else {
                                return changedCellData(cell);
                            }
                        }
                    };
            mediatedBy.registerServiceHandler(changedTag, changedHandler);

        }

        /*
         * Define any possible query requests
         */
        Iterator queries = cellContract.queries();
        while (queries.hasNext()) {
            Contract.Query queryMethod =
                (Contract.Query)queries.next();
            Object querySpecTag =
                mediatedBy.appendQuerySpecification(cellContract.name(),
                                                    queryMethod.name(),
                                                    false,
                                                    queryMethod.parameters(),
                                                    requests);
        }
        /*
         * Define any possible action requests
         */
        Iterator actions = cellContract.methods();
        while (actions.hasNext()) {
            Contract.Method actionMethod =
                (Contract.Method)actions.next();
            Object actionSpecTag =
                mediatedBy.appendActionSpecification(cellContract.name(),
                                                     actionMethod.name(),
                                                     false,
                                                     actionMethod.parameters(),
                                                     requests);
        }
        return true;
    }
}
