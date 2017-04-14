/*
 * Implements a DOM node reflecting an (immutable) information state.
 *
 * Created on September 3, 2006, 2:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.midiki.dom;

import org.w3c.dom.*;
import java.util.*;
import org.mitre.midiki.logic.*;
import org.mitre.midiki.state.*;

/**
 *
 * @author carl
 */
public class MidikiInfoStateNode extends MidikiElement {
    
    static public String INFO_STATE_ROOT = "ISROOT";
    
    public MidikiInfoStateNode(Object obj) {
        this((InfoState)obj);
    }
    /**
     * Instantiates a InfoStateNode backed by the original data.
     * The Collection itself only serves to contain the children;
     * none of the type information is retained.
     */
    public MidikiInfoStateNode(InfoState data) {
        super(data);
        // our namespace prefix is midiki (? should this be true?)
        // our element local name is the InfoState functor
    }
    /**
     * Instantiates a InfoStateNode backed by the original data.
     * The Collection itself only serves to contain the children;
     * none of the type information is retained.
     */
    public MidikiInfoStateNode(ImmutableInfoState data) {
        super(data);
        // our namespace prefix is midiki (? should this be true?)
        // our element local name is the InfoState functor
    }

    /**
     * Extract the object that this DOM Node reflects.
     * Problematic for InfoStates; the 'extract' routine implicitly constructs
     * a copy of the underlying object.
     */
    public Object extractShadow() {
        return _shadow;
    }
    
    /**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName() {
        return INFO_STATE_ROOT;
    }

}
