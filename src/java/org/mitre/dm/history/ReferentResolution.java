/****************************************************************************
 *
 * Copyright (C) 2005. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 * Consult the LICENSE file in the root of the distribution for terms and restrictions.
 *
 *       Release: 1.0
 *       Date: 1-June-2005
 *       Author: Carl Burke
 *
 *****************************************************************************/
package org.mitre.dm.history;

import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;

import java.util.*;

/**
 * Defines a first attempt at a generic interface for referent resolution.
 * Also includes access to dialogue history structures, because
 * referent resolution is intimately connected to dialogue history.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class ReferentResolution
{
    static protected HashMap db;
    static
    {
        db = new HashMap();
        db.put("referent_resolution",RefResContract());
    }
    static public Contract find(String name)
    {
        return (Contract)db.get(name);
    }
    protected static Contract RefResContract()
    {
        ContractImpl refres = new ContractImpl("referent_resolution");

        refres.addMethod(new MethodImpl("createScope",
            new ParameterImpl[]{
                new ParameterImpl("scopeName", null, null)}));
        refres.addMethod(new MethodImpl("deleteScope",
            new ParameterImpl[]{
                new ParameterImpl("scopeName", null, null)}));

        // if considering scopes as frames, frame=toScopeName,
        // slot=byRelation, value=scopeName
        refres.addMethod(new MethodImpl("attachScope",
            new ParameterImpl[]{
                new ParameterImpl("scopeName", null, null),      // value
                new ParameterImpl("toScopeName", null, null),    // frame
                new ParameterImpl("byRelation", null, null)}));  // slot
        refres.addMethod(new MethodImpl("removeScope",
            new ParameterImpl[]{
                new ParameterImpl("scopeName", null, null),
                new ParameterImpl("fromScopeName", null, null),
                new ParameterImpl("byRelation", null, null)}));
        refres.addQuery(new QueryImpl("findScope",
            new ParameterImpl[]{
                new ParameterImpl("scopeName", null, null),
                new ParameterImpl("inScopeName", null, null),
                new ParameterImpl("withRelation", null, null)}));

        // set/get theory-specific variables/registers
        refres.addMethod(new MethodImpl("setPointer",
            new ParameterImpl[]{
                new ParameterImpl("scopeName", null, null),
                new ParameterImpl("pointerName", null, null),
                new ParameterImpl("pointerValue", null, null)}));
        refres.addQuery(new QueryImpl("getPointer",
            new ParameterImpl[]{
                new ParameterImpl("scopeName", null, null),
                new ParameterImpl("pointerName", null, null),
                new ParameterImpl("pointerValue", null, null)}));

        // activate/deactivate specified scope (if bound) or default scope (if unbound).
        // notion of default inter-scope linkages is theory-specific.
        refres.addMethod(new MethodImpl("enterScope",
            new ParameterImpl[]{
                new ParameterImpl("scopeName", null, null)}));
        refres.addMethod(new MethodImpl("exitScope",
            new ParameterImpl[]{
                new ParameterImpl("scopeName", null, null)}));

        // equivalent routines to create referents.
        // some theories may cconsider scopes to be referents.
        // some theories may map referents to e.g. lambda variables.
        refres.addMethod(new MethodImpl("createReferent",
            new ParameterImpl[]{
                new ParameterImpl("referentName", null, null)}));
        refres.addMethod(new MethodImpl("deleteReferent",
            new ParameterImpl[]{
                new ParameterImpl("referentName", null, null)}));

        // if considering Referents as frames, frame=toReferentName,
        // slot=byRelation, value=ReferentName
        refres.addMethod(new MethodImpl("attachReferent",
            new ParameterImpl[]{
                new ParameterImpl("referentName", null, null),      // value
                new ParameterImpl("toScopeName", null, null),    // frame
                new ParameterImpl("byRelation", null, null)}));  // slot
        refres.addMethod(new MethodImpl("removeReferent",
            new ParameterImpl[]{
                new ParameterImpl("referentName", null, null),
                new ParameterImpl("fromScopeName", null, null),
                new ParameterImpl("byRelation", null, null)}));
        // assumption is that there are cognitive weights that affect
        // referent resolution. not sure if there needs to be a query to find
        // referents and a method to commit to a specific binding, but am assuming so.
        refres.addQuery(new QueryImpl("findReferent",
            new ParameterImpl[]{
                new ParameterImpl("referentName", null, null),
                new ParameterImpl("withRelation", null, null),
                new ParameterImpl("withScore", null, null),
                new ParameterImpl("inScopeName", null, null)}));
        refres.addMethod(new MethodImpl("resolveReferent",
            new ParameterImpl[]{
                new ParameterImpl("referentName", null, null),
                new ParameterImpl("asReferentName", null, null),
                new ParameterImpl("inScopeName", null, null)}));

        // structure the scopes with some form of information.
        // typically that would be a predicate, and it is called that here.
        refres.addMethod(new MethodImpl("assertStructure",
            new ParameterImpl[]{
                new ParameterImpl("predicate", null, null),
                new ParameterImpl("inScopeName", null, null)}));
        refres.addMethod(new MethodImpl("retractStructure",
            new ParameterImpl[]{
                new ParameterImpl("predicate", null, null),
                new ParameterImpl("inScopeName", null, null)}));

        return refres;
    }
}
