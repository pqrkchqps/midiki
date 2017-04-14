/*
 * ISControlAdapter.java
 *
 * Created on July 30, 2005, 2:35 PM
 */

package org.mitre.dm;

/**
 *
 * @author  carl
 */
public class ISControlAdapter implements ISControlListener {
    
    /** Creates a new instance of ISControlAdapter */
    public ISControlAdapter() {
    }
    
    /**
     * Called when the information state becomes quiescent;
     * i.e., there are no further changes to be propagated.
     */    
    public void quiescence()
    {
        // no-op
    }
    /**
     * Called just before the information state begins propagating
     * changes. May be called as each change propagates.
     */    
    public void activation()
    {
        // no-op
    }
    /**
     * Called just before the information state begins propagating
     * changes. May be called as each change propagates.
     */    
    public void propagation()
    {
        // no-op
    }
}
