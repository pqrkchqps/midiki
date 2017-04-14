/*
 * MidikiTextNode.java
 *
 * Created on January 6, 2006, 9:59 AM
 */

package net.sf.midiki.dom;

import org.w3c.dom.*;
import org.mitre.midiki.logic.*;

/**
 *
 * @author  carl
 */
public class MidikiTextNode extends MidikiNode implements org.w3c.dom.Text {
    
    /** Creates a new instance of MidikiTextNode */
    public MidikiTextNode(Object obj) {
        super(obj);
        _nodeType = TEXT_NODE;
    }
    
    /**
     * Extract the object that this DOM Node reflects. Use this method to
     * extract predicates from MidikiNodes created from transforms or from
     * parsing a text-based XML file. Default just calls getShadow().
     */
    public Object extractShadow() {
        if (_shadow == null) return null;
        // input from text or via transform will not generate variables;
        // they will appear only as text. Workaround is to have an automatic
        // conversion based on text format. This could vary depending upon
        // the syntactic conventions preferred by the author, but for now
        // just use a '?' prefix as a cue for Variables.
        String text = (""+getShadow()).trim();
        if (text.startsWith("?") && text.length()>1) {
            _shadow = new Variable(text);
        }
        return _shadow;
    }
    
    /**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName() {
        return "#text";
    }

    /**
     * The value of this node, depending on its type; see the table above. 
     * When it is defined to be <code>null</code>, setting it has no effect, 
     * including if the node is read-only.
     * @exception DOMException
     *   DOMSTRING_SIZE_ERR: Raised when it would return more characters than 
     *   fit in a <code>DOMString</code> variable on the implementation 
     *   platform.
     */
    public String getNodeValue()
                              throws DOMException {
        // None of our node types has a value other than null
        return _shadow.toString();
    }
    
    /**
     * The value of this node, depending on its type; see the table above. 
     * When it is defined to be <code>null</code>, setting it has no effect, 
     * including if the node is read-only.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly and if 
     *   it is not defined to be <code>null</code>.
     */
    public void setNodeValue(String nodeValue)
                              throws DOMException
    {
        _shadow = nodeValue;
    }

    // these are the methods required to implement the CharacterData interface.
    // currently not implemented; software depending on these will fail.
    public String getData()
        throws DOMException {
        return getNodeValue();
    }
    public void setData(String data)
        throws DOMException {
        setNodeValue(data);
    }
    public int getLength() {
        return getData().length();
    }
    public String substringData(int offset,
            int count)
            throws DOMException {
        return getData().substring(offset, offset+count);
    }
    public void appendData(String arg)
        throws DOMException {
        setData(getData()+arg);
    }
    public void insertData(int offset,
            String arg)
            throws DOMException {
        replaceData(offset, 0, arg);
    }
    public void deleteData(int offset,
            int count)
            throws DOMException {
        replaceData(offset, count, "");
    }
    public void replaceData(int offset,
            int count,
            String arg)
            throws DOMException {
        if (offset == 0) {
            setData(arg+getData().substring(count));
        } else {
            String temp = getData();
            setData(temp.substring(0,offset)+arg+temp.substring(offset+count));
        }
    }
            
    
    // these are the methods required to implement the Text interface.
    // currently not implemented; software depending on these will fail.
    
    public String getWholeText() {
        throw new RuntimeException("Unsupported Operation");
    }

    public Text replaceWholeText(String content)
                          throws DOMException {
        throw new RuntimeException("Unsupported Operation");
    }
    
    public Text splitText(int offset)
                          throws DOMException {
        throw new RuntimeException("Unsupported Operation");
    }
            
    public boolean isElementContentWhitespace() {
        throw new RuntimeException("Unsupported Operation");
    }
}
