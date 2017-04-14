/*
 * A minimal convenience class implementing the DialogueSystemListener
 * interface. The methods in this class are empty. Extend this class
 * to create a DialogueSystemListener and override the methods of interest.
 *
 * Created on July 30, 2005, 2:11 PM
 */

package org.mitre.midiki.agent;

import org.mitre.midiki.state.*;

/**
 *
 * @author  carl
 */
public class DialogueSystemAdapter implements DialogueSystemListener {
    
    /** Creates a new instance of DialogueSystemAdapter */
    public DialogueSystemAdapter() {
    }
    
    /**
     * Called when the information state becomes quiescent;
     * i.e., there are no further changes to be propagated.
     */    
    public void quiescence(ImmutableInfoState is)
    {
        // no-op
    }
    /**
     * Called just before the information state begins propagating
     * changes. May be called as each change propagates.
     */    
    public void activation(ImmutableInfoState is)
    {
        // no-op
    }
}
