/**
 * Implements the Element interface for Midiki Collections.
 */
package net.sf.midiki.dom;

import org.w3c.dom.*;
import java.util.*;

public class MidikiCollectionNode extends MidikiElement
{
    public MidikiCollectionNode(Object obj) {
        this((Collection)obj);
    }
    /**
     * Instantiates a CollectionNode backed by the original data.
     * The Collection itself only serves to contain the children;
     * none of the type information is retained.
     */
    public MidikiCollectionNode(Collection data) {
        super(data);
        // our namespace prefix is midiki (? should this be true?)
        // our element local name is Collection
        // either an attribute value or the element value must hold
        // the java class name of the collection
    }

    /**
     * Extract the object that this DOM Node reflects.
     */
    public Object extractShadow() {
        _shadow = new LinkedList();
        if (_nodeList != null) {
            int len = _nodeList.getLength();
            for (int i=0; i<len; i++) {
                ((LinkedList)_shadow).add(((MidikiNode)_nodeList.item(i)).extractShadow());
            }
        }
        return _shadow;
    }
    
    /**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName() {
        return "#collection";
    }

}
