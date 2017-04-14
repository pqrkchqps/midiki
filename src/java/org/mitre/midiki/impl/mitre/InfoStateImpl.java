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

import java.util.*;
import java.util.logging.*;

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.Bindings;
import org.mitre.midiki.logic.BindingsImpl;
import org.mitre.midiki.logic.Unifier;
import org.mitre.midiki.logic.Unify;

/**
 * Provides the facilities for modifying the information state,
 * as an extension of the read-only <code>ImmutableInfoStateImpl</code>.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see ImmutableInfoStateImpl
 */
public class InfoStateImpl implements InfoState
{
    private static Logger logger =
        Logger.getLogger("org.mitre.midiki.impl.mitre.InfoStateImpl");
    protected ImmutableInfoState infoView;
    protected HashMap cellMap;
    protected LinkedList clients;
    protected LinkedList servers;
    protected LinkedList viewers;  // "closures" of this info state
    protected Mediator myMediator;
    /**
     * Describe variable <code>notifyingInfoListeners</code> here.
     */
    public Collection notifyingInfoListeners;
    /* {transient=false, volatile=false}*/
    /**
     * Describe variable <code>notifiedInfoListeners</code> here.
     */
    public Collection notifiedInfoListeners;
    /* {transient=false, volatile=false}*/
    public void fire(InfoListener il)
    {
        if (!notifiedInfoListeners.contains(il)) {
            notifiedInfoListeners.add(il);
        }
    }
    public void lockViewers()
    {
        // for each viewing InfoState, make a copy of each client store
        Iterator vit = viewers.iterator();
        while (vit.hasNext()) {
            InfoStateImpl isi = (InfoStateImpl)vit.next();
            isi.lock();
        }
    }
    public void notifyViewers()
    {
        // for each viewing InfoState, notifyAllInfoListeners
        Iterator vit = viewers.iterator();
        while (vit.hasNext()) {
            InfoStateImpl isi = (InfoStateImpl)vit.next();
            isi.notifyAllInfoListeners();
        }
    }
    public void unlockViewers()
    {
        Iterator vit = viewers.iterator();
        while (vit.hasNext()) {
            InfoStateImpl isi = (InfoStateImpl)vit.next();
            isi.unlock();
        }
    }
    /**
     * Notify all of the listeners that need to be called
     * in response to changes in the information state.
     *
     */
    public void notifyAllInfoListeners() {
        // for every attribute, if it is marked as changed,
        // call fireTriggers for that attribute and value
        logger.entering("org.mitre.midiki.impl.mitre.InfoStateImpl","notifyAllInfoListeners",this);
        // since there are cases where we want to respond to changes
        // within this agent, need to do evaluations during firing.
        // to support that, make a copy to hold the triggers we are
        // currently firing.
        LinkedList notifyingInfoListeners;
	    notifyingInfoListeners = new LinkedList(notifiedInfoListeners);
	    notifiedInfoListeners.clear();
	    Iterator it = notifyingInfoListeners.iterator();
	    while (it.hasNext()) {
	        InfoListener inst = (InfoListener)it.next();
	        inst.infoChanged(new InfoStateEventImpl(null, this));
	    }
	    notifyingInfoListeners.clear();
    }

    public InfoStateImpl(ImmutableInfoStateImpl view)
    {
        infoView = view;
        cellMap = new HashMap();
        clients = new LinkedList();
        servers = new LinkedList();
        viewers = new LinkedList();
        notifyingInfoListeners = new LinkedList();
        notifiedInfoListeners = new LinkedList();
    }

    public void instantiateRootDataSet()
    {
        lock();
        Iterator cit = servers.iterator();
        while (cit.hasNext()) {
            CellServer cs = (CellServer)cit.next();
            Contract c = cs.contract;
            CellImpl cell = (CellImpl)cell(c.name());
            if (cell == null) {
                System.out.println("CellImpl null");
            }
            CellClient cl = cell.getClient();
            fillOutInstance(cl.rootInstance(),cell);
        }
        unlock();
    }

    protected void fillOutInstance(CellInstance ci, CellImpl inCell)
    {
        //System.out.println("Filling out "+ci.instanceId);
        Contract c = ci.instanceType;
        Iterator it = c.attributes();
        while (it.hasNext()) {
            AttributeImpl ca = (AttributeImpl)it.next();
            if (ca.type() instanceof Contract) {
                Contract cac = (Contract)ca.type();
                CellClient cl = ((CellImpl)cell(cac.name())).getClient();
                CellInstance ciNew = cl.newInstance();
                if (ciNew!=null) {
                    // this is fine if not a Midiki-protocol cell.
                    // should probably check the mediator to verify.
                    //System.out.println("adding subcell "+ciNew.instanceId);
                    inCell.put(ca.name(), ciNew.instanceId);
                    fillOutInstance(ciNew,(CellImpl)inCell.cell(ca.name()));
                }
            }
        }
    }

    public void addCell(String name, Cell cell)
    {
        cellMap.put(name, cell);
    }

    /**
     * Get a read-only view of the information state.
     *
     * @return an <code>ImmutableInfoStateImpl</code> value
     */
    public ImmutableInfoState getView()
    {
        return infoView;
    }
    /**
     * Get a unification engine that works with this information state.
     *
     * @return an <code>Unifier</code> value
     */
    public Unifier getUnifier()
    {
        return Unify.getInstance();
    }
    /**
     * Get a reference to the specified <code>Cell</code>
     * attribute.
     *
     * @param fqName a <code>String</code> value
     * @return a <code>Cell</code> value
     */
    public Cell cell(String fqName)
    {
        Object icobj = cellMap.get(fqName);
        if (icobj==null) {
            RuntimeException ex =
                new RuntimeException("No cell '"+fqName+"' in infostate");
            logger.throwing("org.mitre.midiki.impl.mitre.InfoStateImpl","cell",ex);
            throw ex;
        }
        CellImpl cell = (CellImpl)icobj;
        try {
            //System.out.println("InfoStateImpl.cell("+fqName+") cell="+cell+" cellView="+cell.cellView);
            //System.out.println("InfoStateImpl.cell client="+cell.cellView.client);
            cell.cellView.loadInstance(cell.cellView.client.rootInstanceId());
        } catch (Exception e) {
            logger.logp(Level.WARNING,"org.mitre.midiki.impl.mitre.InfoStateImpl","cell","Exception while accessing cell "+fqName,e);
            //System.out.println("Exception accessing cell "+fqName+" myMediator "+myMediator);
            e.printStackTrace();
        }
        return (Cell)cell;
    }

    /**
     * Return <code>true</code> if the specified <code>Cell</code>
     * is present.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>boolean</code> value
     */
    public boolean hasCell(String fqName)
    {
        return (cellMap.get(fqName) != null);
    }

    /**
     * Return all of the <code>Contract</code>s available in this infostate.
     *
     * @return a <code>Collection</code> value
     */
    public Set getContracts()
    {
        Set contracts = new HashSet();
        Iterator it = cellMap.values().iterator();
        while (it.hasNext()) {
            CellImpl cit = (CellImpl)it.next();
            contracts.add(cit.getContract());
        }
        return contracts;
    }

    public boolean addClient(CellClient client)
    {
        clients.add(client);
        return true;
    }
    public boolean addServer(CellServer server)
    {
        servers.add(server);
        return true;
    }
    public boolean addViewer(InfoStateImpl viewer)
    {
        viewers.add(viewer);
        return true;
    }
    /**
     * Create a <code>Bindings</code> object for storage of
     * variable assignments.
     *
     * @return a <code>Bindings</code> value
     */
    public Bindings createBindings()
    {
        return new BindingsImpl();
    }
    /**
     * Releases the specified <code>Bindings</code> object.
     * Implementations of <code>InfoStateImpl</code> can take advantage
     * of this to recycle <code>Bindings</code>, if possible.
     *
     * @param bindings a <code>Bindings</code> value
     */
    public void releaseBindings(Bindings bindings)
    {
    }
    /**
     * Blocks external changes to the local copy of the
     * information state. Automatically called whenever
     * an <code>InfoListener</code> is invoked. Should be
     * followed by <code>unlock</code> as quickly as possible,
     * because the agent will not respond to any new events
     * or pass on its changes to the information state until
     * the lock is released.<p>
     * This method is primarily of interest when injecting data
     * into the information state in response to non-Midiki events.
     *
     */
    public void lock()
    {
        // first, flush the local data store to force reloading
        // of any data which may have changed. then lock all
        // of our clients.
        ((ImmutableInfoStateImpl)infoView).flushLocalState();
        Iterator cit = clients.iterator();
        while (cit.hasNext()) {
            CellClient cc = (CellClient)cit.next();
            cc.lock(false);
        }
    }
    /**
     * Propagates any recent information state changes to the
     * other agents, and processes inputs that have arrived since
     * <code>lock</code> was called. For maximum system responsiveness,
     * locks should be held as briefly as is feasible. Automatically
     * called when control returns from an <code>InfoListener</code>.<p>
     * This method is primarily of interest when injecting data
     * into the information state in response to non-Midiki events.
     *
     * Viewers, closures, ghosted info states... none of them are given
     * lists of clients. In that way, they are able to execute this code
     * without sending excessive messages.
     */
    public void unlock()
    {
        if ((myMediator != null) && myMediator.usesMidikiProtocol() && !clients.isEmpty()) {
            ArrayList al = new ArrayList();
            al.add("agentName");
            al.add("dummyTerm");
            Bindings b = new BindingsImpl();
            cell("is_control").method("begin_transaction").invoke(al, b);
            boolean requiresEvaluation = false;
            Iterator cit = clients.iterator();
            while (cit.hasNext()) {
                CellClient cc = (CellClient)cit.next();
                boolean triggered = cc.unlock();
                requiresEvaluation = requiresEvaluation || triggered;
            }
            al = new ArrayList();
            al.add("agentName");
            al.add(new Boolean(requiresEvaluation));
            cell("is_control").method("end_transaction").invoke(al, b);
        }
    }

    protected String agentName = "infoStateImpl";
    public void setAgentName(String name)
    {
        agentName = name;
    }

    public boolean connect(Mediator mediator)
    {
        myMediator = mediator;
        if (!mediator.register(agentName)) {
            return false;
        }

        LinkedList services = new LinkedList();  // incoming services
        LinkedList requests = new LinkedList();  // outgoing requests

        // accumulate services for all clients
        Iterator stubIt = clients.iterator();
        while (stubIt.hasNext()) {
            CellClient stub = (CellClient)stubIt.next();
            stub.declare_services(services, requests);
        }

        // accumulate services for all servers
        Iterator skelIt = servers.iterator();
        while (skelIt.hasNext()) {
            CellServer skel = (CellServer)skelIt.next();
            skel.declare_services(services, requests);
        }

        declare_external_services(services, requests, mediator);

        if (!mediator.declareServices(services, requests)) {
            return false;
        }

        // top-level instances will be fleshed out elsewhere

        mediator.assertReadiness();

        return true;
    }

    public void declare_external_services(Collection services,
                                          Collection requests,
                                          Mediator mediatedBy)
    {
    }
}
