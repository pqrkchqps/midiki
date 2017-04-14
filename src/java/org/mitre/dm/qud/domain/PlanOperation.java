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
package org.mitre.dm.qud.domain;

/**
 * Provides a representation of an operation as an untyped step
 * and an optional annotation. If an annotation is attached to
 * the operation, it is logged when the step is executed.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class PlanOperation
{
    protected Object theOperation;
    protected Object theAnnotation;
    public PlanOperation(Object op, Object ann)
    {
        theOperation = op;
        theAnnotation = ann;
    }
    public PlanOperation(Object op)
    {
        this(op, null);
    }
    public Object getOperation()
    {
        return theOperation;
    }
    public Object getAnnotation()
    {
        return theAnnotation;
    }
    public void setAnnotation(Object ann)
    {
        theAnnotation = ann;
    }
}
