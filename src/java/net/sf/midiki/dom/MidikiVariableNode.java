/**
 * Implements the Element interface for Midiki Variables.
 */
package net.sf.midiki.dom;

import org.w3c.dom.*;
import org.mitre.midiki.logic.*;

public class MidikiVariableNode extends MidikiElement
{
    
    public MidikiVariableNode(Object obj) {
        this((Variable)obj);
    }
    /**
     * Instantiates a VariableNode backed by the original data.
     * These should be free variables only.
     *
     * May need to review and revise this after studying XML variables
     * in more depth.
     */
    public MidikiVariableNode(Variable data) {
        super(data);
        // our namespace prefix is midiki (? should this be true?)
        // our element local name is the Variable functor
    }
    
    /**
     * Extract the object that this DOM Node reflects.
     */
    public Object extractShadow() {
        _shadow = new Variable((String)getShadow());
        return _shadow;
    }
    
    /**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName() {
        return _shadow.toString();
    }

}
