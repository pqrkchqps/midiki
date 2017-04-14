/**
 * Implements the Element interface for Midiki Predicates.
 */
package net.sf.midiki.dom;

import org.w3c.dom.*;
import java.util.*;
import org.mitre.midiki.logic.*;

public class MidikiPredicateNode extends MidikiElement
{
    public MidikiPredicateNode(Object obj) {
        this((Predicate)obj);
    }
    /**
     * Instantiates a PredicateNode backed by the original data.
     * The Collection itself only serves to contain the children;
     * none of the type information is retained.
     */
    public MidikiPredicateNode(Predicate data) {
        super(data);
        // our namespace prefix is midiki (? should this be true?)
        // our element local name is the predicate functor
    }

    /**
     * Extract the object that this DOM Node reflects.
     */
    public Object extractShadow() {
        ArrayList al = new ArrayList();
        if (_nodeList != null) {
            int len = _nodeList.getLength();
            for (int i=0; i<len; i++) {
                al.add(((MidikiNode)_nodeList.item(i)).extractShadow());
            }
        }
        _shadow = new Predicate(getNodeName(), al);
        return _shadow;
    }
    
    /**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName() {
        return ((Predicate)_shadow).functor();
    }

}
