/**
 * Implements the Element interface for Midiki Objects.
 */
package net.sf.midiki.dom;

import org.w3c.dom.*;

public class MidikiObjectNode extends MidikiElement
{
    /**
     * Instantiates a ObjectNode backed by the original data.
     * At present this is just treated as an atom.
     * Future implementations may use reflection and/or BeanInfo
     * to build Object nodes with children.
     */
    public MidikiObjectNode(Object data) {
        super(data);
        // our namespace prefix is midiki (? should this be true?)
        // our element local name is Object
        // there should be an attribute containing the java class name
        // the value of the node should be the toString() of the Object.
    }
    
    /**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName() {
        return _shadow.getClass().getName();
    }

}
