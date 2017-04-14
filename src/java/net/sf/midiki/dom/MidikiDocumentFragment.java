/**
 * Implements the DocumentFragment interface for Midiki datatypes.
 */
package net.sf.midiki.dom;

import org.w3c.dom.*;
import java.util.*;
import org.mitre.midiki.logic.*;

public class MidikiDocumentFragment extends MidikiNode implements org.w3c.dom.DocumentFragment
{
    private MidikiDocument originatingDocument;
    /**
     * Instantiates a DocumentFragment backed by the original data.
     * The Collection itself only serves to contain the children;
     * none of the type information is retained.
     */
    public MidikiDocumentFragment(Collection data) {
        super(data);
        _nodeType = DOCUMENT_FRAGMENT_NODE;
        originatingDocument = new MidikiDocument(new Predicate("root", data));
    }
    
    /**
     * Extract the object that this DOM Node reflects. Should not be called on
     * a DocumentFragment, but in case it is we will handle it as Collection.
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
     * The <code>Document</code> object associated with this node. This is 
     * also the <code>Document</code> object used to create new nodes. When 
     * this node is a <code>Document</code> or a <code>DocumentType</code> 
     * which is not used with any <code>Document</code> yet, this is 
     * <code>null</code>.
     * @version DOM Level 2
     */
    public Document getOwnerDocument() {
        return originatingDocument;
    }

    /**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName() {
        return "#document-fragment";
    }

}
/*
 * Copyright (c) 2004 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

/**
 * <code>DocumentFragment</code> is a "lightweight" or "minimal" 
 * <code>Document</code> object. It is very common to want to be able to 
 * extract a portion of a document's tree or to create a new fragment of a 
 * document. Imagine implementing a user command like cut or rearranging a 
 * document by moving fragments around. It is desirable to have an object 
 * which can hold such fragments and it is quite natural to use a Node for 
 * this purpose. While it is true that a <code>Document</code> object could 
 * fulfill this role, a <code>Document</code> object can potentially be a 
 * heavyweight object, depending on the underlying implementation. What is 
 * really needed for this is a very lightweight object. 
 * <code>DocumentFragment</code> is such an object.
 * <p>Furthermore, various operations -- such as inserting nodes as children 
 * of another <code>Node</code> -- may take <code>DocumentFragment</code> 
 * objects as arguments; this results in all the child nodes of the 
 * <code>DocumentFragment</code> being moved to the child list of this node.
 * <p>The children of a <code>DocumentFragment</code> node are zero or more 
 * nodes representing the tops of any sub-trees defining the structure of 
 * the document. <code>DocumentFragment</code> nodes do not need to be 
 * well-formed XML documents (although they do need to follow the rules 
 * imposed upon well-formed XML parsed entities, which can have multiple top 
 * nodes). For example, a <code>DocumentFragment</code> might have only one 
 * child and that child node could be a <code>Text</code> node. Such a 
 * structure model represents neither an HTML document nor a well-formed XML 
 * document.
 * <p>When a <code>DocumentFragment</code> is inserted into a 
 * <code>Document</code> (or indeed any other <code>Node</code> that may 
 * take children) the children of the <code>DocumentFragment</code> and not 
 * the <code>DocumentFragment</code> itself are inserted into the 
 * <code>Node</code>. This makes the <code>DocumentFragment</code> very 
 * useful when the user wishes to create nodes that are siblings; the 
 * <code>DocumentFragment</code> acts as the parent of these nodes so that 
 * the user can use the standard methods from the <code>Node</code> 
 * interface, such as <code>Node.insertBefore</code> and 
 * <code>Node.appendChild</code>.
 * <p>See also the <a href='http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */

