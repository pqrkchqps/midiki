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

import java.util.*;
import java.util.logging.*;

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;

import org.mitre.midiki.impl.mitre.*;

/**
 * Provides an implementation of the Information State Control contract.
 * The purpose of that contract is to lock the information state during
 * an update and to notify all agents once the update is complete.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class ISControlCell
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.ISControlCell");
    private class MessageGate
    {
        public boolean sendMessage;
        public boolean stop;
        
        public MessageGate()
        {
        }
    }

    static private ContractImpl is_control;

    static {
        is_control = new ContractImpl("is_control");
        is_control.addMethod(new MethodImpl("begin_transaction",
            new ParameterImpl[]{
                new ParameterImpl("agent", null, null),
                new ParameterImpl("flag", null, null)}));
        is_control.addMethod(new MethodImpl("end_transaction",
            new ParameterImpl[]{
                new ParameterImpl("agent", null, null),
                new ParameterImpl("flag", null, null)}));
        is_control.addMethod(new MethodImpl("shutdown",
            new ParameterImpl[]{}));
        is_control.addAttribute(new AttributeImpl("tick", null, null));
    }

    static public Contract getContract()
    {
        return is_control;
    }

    private LinkedList ISControlListeners;
    
    /**
     * Add the specified listener to the notification queue
     * for dialogue system events.
     *
     * @param dsl ISControlListener to be added
     */    
    public void addISControlListener(ISControlListener dsl)
    {
        if (ISControlListeners == null) {
            ISControlListeners = new LinkedList();
        }
        ISControlListeners.add(dsl);
    }
    /**
     * Fetch the list of currently registered dialogue system listeners.
     *
     * @return
     */    
    public ISControlListener[] getISControlListeners()
    {
        if (ISControlListeners == null) {
            return null;
        }
        return (ISControlListener[])ISControlListeners.toArray();
    }
    /**
     * Remove the specified listener from the notification queue
     * for dialogue system events.
     *
     * @param dsl ISControlListener to be removed
     */    
    public void removeISControlListener(ISControlListener dsl)
    {
        if (ISControlListeners != null) {
            ISControlListeners.remove(dsl);
        }
    }

    private void activate()
    {
        if (ISControlListeners != null) {
            Iterator it = ISControlListeners.iterator();
            while (it.hasNext()) {
                ISControlListener icl = (ISControlListener)it.next();
                icl.activation();
            }
        }
    }

    private void propagate()
    {
        if (ISControlListeners != null) {
            Iterator it = ISControlListeners.iterator();
            while (it.hasNext()) {
                ISControlListener icl = (ISControlListener)it.next();
                icl.propagation();
            }
        }
    }

    private void quiesce()
    {
        if (ISControlListeners != null) {
            Iterator it = ISControlListeners.iterator();
            while (it.hasNext()) {
                ISControlListener icl = (ISControlListener)it.next();
                icl.quiescence();
            }
        }
    }
    
    private boolean shuttingDown = false;
    public void requestShutdown()
    {
        shuttingDown = true;
        if (lockDepth <= 0) {
            // not currently propagating changes, so exit immediately
            shutdown();
        }
    }
    public void shutdown()
    {
        System.out.println("ISControlCell quiescent, shutting down.");
        try {
            synchronized(messageGate) {
                messageGate.stop = true;
                messageGate.notify();
            }
            if (signalWhenDone != null) {
                synchronized(signalWhenDone) {
                    signalWhenDone.notifyAll();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected LinkedList unprovidedContracts;
    /**
     * Builds a list of cells for required contracts that have not
     * been provided by other agents.
     */
    public Set getProvidedCells()
    {
        Set cellSet = new HashSet();
        if (unprovidedContracts != null) {
            Iterator it = unprovidedContracts.iterator();
            while (it.hasNext()) {
                Contract ctr = (Contract)it.next();
                if (ctr.name().equals(getContract().name())) continue;
                cellSet.add(new CellHandlers(ctr));
                //System.out.println("WARNING: ISControlCell providing "+ctr.name());
            }
        }
        return cellSet;
    }
    public CellHandlers initializeHandlers()
    {
        CellHandlers controlCell = new CellHandlers(getContract());
        MethodHandler beginTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Agent
                    // 2: Flag
                    Object agent = null;
                    Object flag = null;
                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) argIt.next();  // ignore instance ID
                    if (argIt.hasNext()) agent = argIt.next();
                    if (argIt.hasNext()) flag = argIt.next();
                    begin_transaction((String)agent, flag, bindings);
                    return true;
                }
            };
        controlCell.addMethodHandler("begin_transaction", beginTransducer);
        MethodHandler endTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Agent
                    // 2: Flag
                    Object agent = null;
                    Object flag = null;
                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) argIt.next();  // ignore instance ID
                    if (argIt.hasNext()) agent = argIt.next();
                    if (argIt.hasNext()) flag = argIt.next();
                    end_transaction((String)agent, flag, bindings);
                    return true;
                }
            };
        controlCell.addMethodHandler("end_transaction", endTransducer);
        MethodHandler shutdownTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments: none
                    requestShutdown();
                    return true;
                }
            };
        controlCell.addMethodHandler("shutdown", shutdownTransducer);
        return controlCell;
    }

    public void begin_transaction(String agent, Object flag, Bindings bindings)
    {
        // insert code to block subsequent transactions
        // however, mustn't block this thread, because it's the one
        // that has to wait for the end_transaction to appear.

        // To accomplish this, use oaaDelaySolution() and maintain
        // a list of delayed requests with associated delay ids.
        // Each begin increments the lock, each end decrements it.
        // If the lock is not zero when begin is entered, generate a delay.

        // That solution only works for remote requests. Local requests
        // need to be processed in another way.

        // OAA version operates a bit differently, regarding responses.
        // Still haven't completely worked out how those will work.

        // activation, propagation, quiescence should be ok for in-core
        // mediation, but not sure how it will play with other versions.
        // not using those at the moment, so will address those later.
        
        logger.logp(Level.FINER,"org.mitre.dm.ISControlCell","begin_transaction","agent",agent);
        if (lockDepth != 0) {
            String key = "is_control_delay_"+lockCount;
            delayedLocks.addLast(new Object[]{key,null});
            // following API still in flux; arg 2 unused with OAA
            mediator.pauseInteraction(key, null);
            logger.logp(Level.FINER,"org.mitre.dm.ISControlCell","begin_transaction","delayed solution",key);
        } else {
            activate();
        }
        lockDepth++;
        lockCount++;
    }

    public void end_transaction(String agent, Object flag, Bindings bindings) {
        lockDepth--;
        logger.logp(Level.FINER,"org.mitre.dm.ISControlCell","end_transaction","agent",agent);
        logger.logp(Level.FINER,"org.mitre.dm.ISControlCell","end_transaction","lockDepth",new Integer(lockDepth));
        if (delayedLocks.size() > 0) {
            Object[] delay = (Object[])delayedLocks.removeFirst();
            LinkedList answers = new LinkedList();
            answers.add(delay[1]);
            logger.logp(Level.FINER,"org.mitre.dm.ISControlCell","end_transaction","Unlocking is_control",delay[0]);
            mediator.resumeInteraction((String)delay[0], answers);
        }
        // transaction complete
        if (((Boolean)flag).booleanValue()) {
            propagate();
            // tell agents to evaluate triggers
            try {
                synchronized(messageGate) {
                    messageGate.sendMessage = true;
                    messageGate.notify();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            quiesce();
            if (shuttingDown) {
                shutdown();
            }
        }
    }

    /*
                public boolean handleEvent(Object event, Object results) {
                    //System.out.println("evaluate(now)");
                    if (_agent.evaluating==0) {
                        //System.out.println("** calling");
                        _agent.evaluating++;
		        _agent.evaluate_triggers();
                    } else {
                        //System.out.println("** queueing");
                        _agent.evaluating++;
                    }
                    return true;
                }
     */
    /*    private int evaluating = 0;
    private void evaluate_triggers() // notional routine on receiving side
    {
        while (evaluating>0) {

        lockInterfaces();
        // for each client, fireAllTriggers()
        unlockInterfaces();
        evaluating--;
        }
    }
    */
    private Mediator mediator;

    private int lockCount;  // total number of lock requests received to date
    private LinkedList delayedLocks;  // list of delayed lock responses
    private int lockDepth;  // number of currently pending locks

    private MessageGate messageGate;
    private Thread evaluateNow;

    private Object signalWhenDone;

    public void setObjectToSignalWhenDone(Object obj)
    {
        signalWhenDone = obj;
    }

    public Object getObjectToSignalWhenDone()
    {
        return signalWhenDone;
    }

    public ISControlCell(Mediator m, Set requiredContracts, Set providedCells)
    {
        this(m);
        Iterator rit = requiredContracts.iterator();
        while (rit.hasNext()) {
            Contract ctr = (Contract)rit.next();
            boolean found = false;
            Iterator pit = providedCells.iterator();
            while (pit.hasNext()) {
                CellHandlers ch = (CellHandlers)pit.next();
                if (ctr.name().equals(ch.getContract().name())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (unprovidedContracts == null)
                    unprovidedContracts = new LinkedList();
                unprovidedContracts.add(ctr);
            }
        }
    }
    public ISControlCell(Mediator m)
    {
        mediator = m;
        lockCount = 0;
        lockDepth = 0;
        delayedLocks = new LinkedList();
        messageGate = new MessageGate();
        evaluateNow = new Thread() {
                public void run()
                {
                    while (true) {
                        try {
                            synchronized(messageGate) {
                                while (!messageGate.sendMessage &&
                                       !messageGate.stop)
                                    messageGate.wait();
                                messageGate.sendMessage = false;
                                if (messageGate.stop) break;
                                // tell agents to evaluate triggers
                                ArrayList al = new ArrayList();
                                al.add("now");
                                LinkedList answers = new LinkedList();
                                boolean found = mediator.broadcastServiceRequest("is_control$evaluate", al, answers);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        evaluateNow.start();
    }

}
