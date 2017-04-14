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
package org.mitre.midiki.state;

import java.util.Collection;
import org.mitre.midiki.logic.Unifier;

/**
 * Provides read-only access to the information state.
 * With an <code>ImmutableInfoState</code>, the user can
 * access cell attributes and call query methods, but cannot
 * change the contents of the information state. (Provided,
 * of course, that all query methods obey the Midiki contract
 * for queries. It is outside the scope of this API to enforce
 * that contract.)
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface ImmutableInfoState
{
  /* {src_lang=Java}*/

    /**
     * Get a read-only reference to the specified <code>Cell</code>
     * attribute.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>ImmutableCell</code> value
     */
    public ImmutableCell cell(String fqName);
    /**
     * Return <code>true</code> if the specified <code>Cell</code>
     * is present.
     *
     * @param fqName a <code>String</code> value
     * @return an <code>boolean</code> value
     */
    public boolean hasCell(String fqName);
    /**
     * Get a unification engine that works with this information state.
     *
     * @return an <code>Unifier</code> value
     */
    public Unifier getUnifier();
}
