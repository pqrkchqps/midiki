/*
 * ISControlListener.java
 *
 * Created on July 30, 2005, 2:33 PM
 */

package org.mitre.dm;

/**
 *
 * @author  carl
 */
public interface ISControlListener {
    
    /**
     * Called when the information state becomes quiescent;
     * i.e., there are no further changes to be propagated.
     */    
    public void quiescence();
    /**
     * Called just before the information state begins propagating
     * changes. May be called as each change propagates.
     */    
    public void activation();
    /**
     * Called just before the information state begins propagating
     * a change. Will be called as each change propagates.
     */    
    public void propagation();
}
