/*
 * AttributeEvaluator.java
 *
 * Provides a standard interface between the DomainImplementation
 * and attribute types for evaluation of relevance, informativeness,
 * possibility, etc. Leaves mechanisms unspecified.
 *
 * Created on April 4, 2005, 4:14 PM
 */

package org.mitre.dm.qud.domain;

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;

/**
 *
 * @author CBURKE
 */
public interface AttributeEvaluator {
    /**
     * Simple evaluation of the value for this type specification.
     * Does not consider current context. Does not consider faceted
     * type specs, varying across types and/or acros individuals.
     *
     * @returns true if the value is compatible with this type.
     */
    public boolean evaluate(Object value);
    /**
     * Simple evaluation of the value for this type specification.
     * Does not consider current context. Does not consider faceted
     * type specs, varying across types and/or acros individuals.
     *
     * @returns true if the value is compatible with this type.
     */
    public boolean evaluate(Object value, String slotName, String className,
            Object instanceId, int modality, InfoState infoState, Bindings bindings);
}
