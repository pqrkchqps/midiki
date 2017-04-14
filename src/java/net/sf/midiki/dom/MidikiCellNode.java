/*
 * MidikiCellNode.java
 *
 * Created on September 3, 2006, 2:27 PM
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
public class MidikiCellNode extends MidikiElement {
    public MidikiCellNode(Object obj) {
        this((Cell)obj);
    }
    /**
     * Instantiates a CellNode backed by the original data.
     * The Collection itself only serves to contain the children;
     * none of the type information is retained.
     */
    public MidikiCellNode(Cell data) {
        super(data);
        // our namespace prefix is midiki (? should this be true?)
        // our element local name is the contract name
    }
    /**
     * Instantiates a CellNode backed by the original data.
     * The Collection itself only serves to contain the children;
     * none of the type information is retained.
     */
    public MidikiCellNode(ImmutableCell data) {
        super(data);
        // our namespace prefix is midiki (? should this be true?)
        // our element local name is the contract name
    }

    /**
     * Extract the object that this DOM Node reflects.
     * For a Cell, this is problematic. Ideally, should reflect the instance.
     */
    public Object extractShadow() {
        return _shadow;
    }
    
    /**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName() {
        return ((Cell)_shadow).getContract().name();
    }

}
