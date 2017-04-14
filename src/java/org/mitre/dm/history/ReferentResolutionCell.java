/****************************************************************************
 *
 * Copyright (C) 2005. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 * Consult the LICENSE file in the root of the distribution for terms and restrictions.
 *
 *       Release: 1.0
 *       Date: 1-June-2004
 *       Author: Carl Burke
 *
 *****************************************************************************/
package org.mitre.dm.history;

import java.util.*;

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;

import org.mitre.dm.*;

/**
 * Provides CellHandlers for the simple ReferentResolution contract.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see ReferentResolutionImplementation
 */
abstract public class ReferentResolutionCell
{
    public ReferentResolutionCell()
    {
        super();
    }

    /*
     * ReferentResolution methods and queries
     */
    protected MethodHandler createScopeTransducer;
    protected MethodHandler deleteScopeTransducer;
    protected MethodHandler attachScopeTransducer;
    protected MethodHandler removeScopeTransducer;
    protected QueryHandler findScopeTransducer;
    protected MethodHandler setPointerTransducer;
    protected QueryHandler getPointerTransducer;
    protected MethodHandler enterScopeTransducer;
    protected MethodHandler exitScopeTransducer;
    protected MethodHandler createReferentTransducer;
    protected MethodHandler deleteReferentTransducer;
    protected MethodHandler attachReferentTransducer;
    protected MethodHandler removeReferentTransducer;
    protected QueryHandler findReferentTransducer;
    protected MethodHandler resolveReferentTransducer;
    protected MethodHandler assertStructureTransducer;
    protected MethodHandler retractStructureTransducer;

        abstract boolean createScope(
                Object intfid,
                Object scopeName,
                Bindings bindings);
        abstract boolean deleteScope(
                Object intfid,
                Object scopeName,
                Bindings bindings);

        // if considering scopes as frames, frame=toScopeName,
        // slot=byRelation, value=scopeName
        abstract boolean attachScope(
                Object intfid,
                Object scopeName,      // value
                Object toScopeName,    // frame
                Object byRelation,
                Bindings bindings);  // slot
        abstract boolean removeScope(
                Object intfid,
                Object scopeName,
                Object fromScopeName,
                Object byRelation,
                Bindings bindings);
        abstract boolean findScope(
                Object intfid,
                Object scopeName,
                Object inScopeName,
                Object withRelation,
                Bindings bindings);

        // set/get theory-specific variables/registers
        abstract boolean setPointer(
                Object intfid,
                Object scopeName,
                Object pointerName,
                Object pointerValue,
                Bindings bindings);
        abstract boolean getPointer(
                Object intfid,
                Object scopeName,
                Object pointerName,
                Object pointerValue,
                Bindings bindings);

        // activate/deactivate specified scope (if bound) or default scope (if unbound).
        // notion of default inter-scope linkages is theory-specific.
        abstract boolean enterScope(
                Object intfid,
                Object scopeName,
                Bindings bindings);
        abstract boolean exitScope(
                Object intfid,
                Object scopeName,
                Bindings bindings);

        // equivalent routines to create referents.
        // some theories may cconsider scopes to be referents.
        // some theories may map referents to e.g. lambda variables.
        abstract boolean createReferent(
                Object intfid,
                Object referentName,
                Bindings bindings);
        abstract boolean deleteReferent(
                Object intfid,
                Object referentName,
                Bindings bindings);

        // if considering Referents as frames, frame=toReferentName,
        // slot=byRelation, value=ReferentName
        abstract boolean attachReferent(
                Object intfid,
                Object referentName,      // value
                Object toScopeName,    // frame
                Object byRelation,
                Bindings bindings);  // slot
        abstract boolean removeReferent(
                Object intfid,
                Object referentName,
                Object fromScopeName,
                Object byRelation,
                Bindings bindings);
        // assumption is that there are cognitive weights that affect
        // referent resolution. not sure if there needs to be a query to find
        // referents and a method to commit to a specific binding, but am assuming so.
        abstract boolean findReferent(
                Object intfid,
                Object referentName,
                Object withRelation,
                Object withScore,
                Object inScopeName,
                Bindings bindings);
        abstract boolean resolveReferent(
                Object intfid,
                Object referentName,
                Object asReferentName,
                Object inScopeName,
                Bindings bindings);

        // structure the scopes with some form of information.
        // typically that would be a predicate, and it is called that here.
        abstract boolean assertStructure(
                Object intfid,
                Object predicate,
                Object inScopeName,
                Bindings bindings);
        abstract boolean retractStructure(
                Object intfid,
                Object predicate,
                Object inScopeName,
                Bindings bindings);

    public CellHandlers initializeHandlers()
    {
        CellHandlers referent_resolutionCell =
            new CellHandlers(ContractDatabase.find("referent_resolution"));

        createScopeTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object scopeName = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    return createScope(intfid, scopeName, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("createScope", createScopeTransducer);
        deleteScopeTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object scopeName = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    return deleteScope(intfid, scopeName, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("deleteScope", deleteScopeTransducer);
        attachScopeTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object scopeName = null;
                    Object toScopeName = null;
                    Object byRelation = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    if (argIt.hasNext()) toScopeName = argIt.next();
                    if (argIt.hasNext()) byRelation = argIt.next();
                    return attachScope(intfid, scopeName, toScopeName, byRelation, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("attachScope", attachScopeTransducer);
        removeScopeTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object scopeName = null;
                    Object fromScopeName = null;
                    Object byRelation = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    if (argIt.hasNext()) fromScopeName = argIt.next();
                    if (argIt.hasNext()) byRelation = argIt.next();
                    return removeScope(intfid, scopeName, fromScopeName, byRelation, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("removeScope", removeScopeTransducer);
        findScopeTransducer = new QueryHandler()
            {
                public boolean query(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object scopeName = null;
                    Object inScopeName = null;
                    Object withRelation = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    if (argIt.hasNext()) inScopeName = argIt.next();
                    if (argIt.hasNext()) withRelation = argIt.next();
                    return findScope(intfid, scopeName, inScopeName, withRelation, bindings);
                }
            };
        referent_resolutionCell.addQueryHandler("findScope", findScopeTransducer);
        setPointerTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object scopeName = null;
                    Object pointerName = null;
                    Object pointerValue = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    if (argIt.hasNext()) pointerName = argIt.next();
                    if (argIt.hasNext()) pointerValue = argIt.next();
                    return setPointer(intfid, scopeName, pointerName, pointerValue, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("setPointer", setPointerTransducer);
        getPointerTransducer = new QueryHandler()
            {
                public boolean query(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object scopeName = null;
                    Object pointerName = null;
                    Object pointerValue = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    if (argIt.hasNext()) pointerName = argIt.next();
                    if (argIt.hasNext()) pointerValue = argIt.next();
                    return getPointer(intfid, scopeName, pointerName, pointerValue, bindings);
                }
            };
        referent_resolutionCell.addQueryHandler("getPointer", getPointerTransducer);
        enterScopeTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object scopeName = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    return enterScope(intfid, scopeName, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("enterScope", enterScopeTransducer);
        exitScopeTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object scopeName = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    return exitScope(intfid, scopeName, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("exitScope", exitScopeTransducer);
        createReferentTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object scopeName = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    return createReferent(intfid, scopeName, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("createReferent", createReferentTransducer);
        deleteReferentTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object scopeName = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    return deleteReferent(intfid, scopeName, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("deleteReferent", deleteReferentTransducer);
        attachReferentTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object referentName = null;
                    Object toScopeName = null;
                    Object byRelation = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) referentName = argIt.next();
                    if (argIt.hasNext()) toScopeName = argIt.next();
                    if (argIt.hasNext()) byRelation = argIt.next();
                    return attachReferent(intfid, referentName, toScopeName, byRelation, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("attachReferent", attachReferentTransducer);
        removeReferentTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object referentName = null;
                    Object fromScopeName = null;
                    Object byRelation = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) referentName = argIt.next();
                    if (argIt.hasNext()) fromScopeName = argIt.next();
                    if (argIt.hasNext()) byRelation = argIt.next();
                    return removeReferent(intfid, referentName, fromScopeName, byRelation, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("removeReferent", removeReferentTransducer);
        findReferentTransducer = new QueryHandler()
            {
                public boolean query(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object referentName = null;
                    Object inScopeName = null;
                    Object withRelation = null;
                    Object withScore = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) referentName = argIt.next();
                    if (argIt.hasNext()) withRelation = argIt.next();
                    if (argIt.hasNext()) withScore = argIt.next();
                    if (argIt.hasNext()) inScopeName = argIt.next();
                    return findReferent(intfid, referentName, withRelation, withScore, inScopeName, bindings);
                }
            };
        referent_resolutionCell.addQueryHandler("findReferent", findReferentTransducer);
        resolveReferentTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object referentName = null;
                    Object inScopeName = null;
                    Object withRelation = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) referentName = argIt.next();
                    if (argIt.hasNext()) withRelation = argIt.next();
                    if (argIt.hasNext()) inScopeName = argIt.next();
                    return resolveReferent(intfid, referentName, withRelation, inScopeName, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("resolveReferent", resolveReferentTransducer);
        assertStructureTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object predicate = null;
                    Object scopeName = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) predicate = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    return assertStructure(intfid, predicate, scopeName, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("assertStructure", assertStructureTransducer);
        retractStructureTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Name of scope
                    Object intfid = null;
                    Object predicate = null;
                    Object scopeName = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) predicate = argIt.next();
                    if (argIt.hasNext()) scopeName = argIt.next();
                    return retractStructure(intfid, predicate, scopeName, bindings);
                }
            };
        referent_resolutionCell.addMethodHandler("retractStructure", retractStructureTransducer);
        return referent_resolutionCell;
    }
}
