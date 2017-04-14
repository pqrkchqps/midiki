/*
 * Provides an interface for receiving dialogue system events.
 * These events occur outside the processing of an information
 * state and provide insight into information state control.
 *
 * Created on July 30, 2005, 2:11 PM
 */

package org.mitre.midiki.agent;

import org.mitre.midiki.state.*;

/**
 *
 * @author  carl
 */
public interface DialogueSystemListener {
    /**
     * Called when the information state becomes quiescent;
     * i.e., there are no further changes to be propagated.
     */    
    public void quiescence(ImmutableInfoState is);
    /**
     * Called just before the information state begins propagating
     * changes. May be called as each change propagates.
     */    
    public void activation(ImmutableInfoState is);
}
