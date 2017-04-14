/*
 * IsUnqualifiedAnswer.java
 *
 * Created on April 7, 2005, 5:08 PM
 */

package org.mitre.dm.qud.conditions;

import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;

import java.util.*;

/**
 *
 * @author CBURKE
 */
public class IsUnqualifiedAnswer extends Condition {
    
    Object answer;

    /** Creates a new instance of IsUnqualifiedAnswer */
    public IsUnqualifiedAnswer(Object a) {
        answer = a;
    }
    public boolean test(ImmutableInfoState infoState,
                        Bindings bindings)
    {
        Object a = Unify.getInstance().deref(answer, bindings);
        return !(a instanceof Predicate);
    }
    
}
