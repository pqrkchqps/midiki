/****************************************************************************
 *
 * Copyright (C) 2004. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 * Consult the LICENSE file in the root of the distribution for terms and restrictions.
 *
 *       Release: 1.0
 *       Date: 24-August-2004
 *       Author: Carl Burke
 *
 *****************************************************************************/

/**
 * ScriptTransformer.java
 *
 *
 * Created: Tue Feb 26 15:04:43 2002
 *
 * @author <a href="mailto: "Carl Burke</a>
 * @version
 */

package org.mitre.midiki.compiler;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class ScriptTransformer 
{
    static public String fetchName(Document doc)
    {
        // extract code/statement/name/@value
        NodeList top = doc.getElementsByTagName("code");
        org.w3c.dom.Node codeNode = top.item(0);
        NodeList code = codeNode.getChildNodes();
        for (int i=0; i<code.getLength(); i++) {
            org.w3c.dom.Node n = code.item(i);
            if (n.getNodeName().equals("statement")) {
                NodeList st = n.getChildNodes();
                for (int j=0; j<st.getLength(); j++) {
                    org.w3c.dom.Node m = st.item(j);
                    if (m.getNodeName().equals("name")) {
                        NamedNodeMap nm = m.getAttributes();
                        return nm.getNamedItem("value").getNodeValue();
                    }
                }
            }
        }
        return "*";
    }

    static public void dumpElement(Element e)
    {
        System.out.println("tagname:"+e.getTagName());
        dumpNodes(e.getElementsByTagName("*"));
    }

    static public void dumpNodes(NodeList nodes)
    {
        System.out.println(nodes.getLength()+" child nodes");
        for (int i=0; i<nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            System.out.print(node.getNodeName()+" "+
                             node.getNodeValue()+" ");
            NamedNodeMap attrs = node.getAttributes();
            if (attrs != null) {
                for (int j=0; j<attrs.getLength(); j++) {
                    org.w3c.dom.Node attr = attrs.item(j);
                    System.out.print("("+attr.getNodeName()+" "+
                                     attr.getNodeValue()+")");
                }
            }
            System.out.println();
            if (node.hasChildNodes())
                dumpNodes(node.getChildNodes());
        }
    }

    static public void main(String[] args)
    {
        try {
            // in order to do validation, must enable validation
            // and set a SAXErrorHandler.
            DocumentBuilderFactory dbFac =
                DocumentBuilderFactory.newInstance();
            dbFac.setValidating(true);
            dbFac.setNamespaceAware(true);
            // May need next line for compatibility with future
            // versions of Xerces
            dbFac.setAttribute("http://xml.org/sax/features/validation", new Boolean(true));
            dbFac.setAttribute("http://apache.org/xml/features/validation/schema", new Boolean(true));
            DocumentBuilder db = dbFac.newDocumentBuilder();
            db.setErrorHandler(new ScriptTransformerErrorHandler());
            System.out.println("Loading test file");
            File xmlFile = new File("iftest01.xml");
            //System.out.println(xmlFile+":"+xmlFile.exists());
            Document doc = db.parse(xmlFile);
            System.out.println("Loading transformation templates");
            //dumpElement(doc.getDocumentElement());
            TransformerFactory trFac = TransformerFactory.newInstance();
            // set error listener
            //trFac.setErrorResolver(mkErrorListener);
            // set URI resolver
            //trFac.setURIResolver(mkURIResolver);
            // make a source for the stub template
            StreamSource trStubSource = new StreamSource("java_stubs.xsl");
            // get the Transformer
            Transformer trStub = trFac.newTransformer(trStubSource);
            // make a source for the skeleton template
            StreamSource trSkelSource = new StreamSource("java_skeletons.xsl");
            // get the Transformer
            Transformer trSkel = trFac.newTransformer(trSkelSource);
            System.out.println("Transforming test template");
            // extract code/statement/name/@value
            String ifname = fetchName(doc);
            System.out.println("interface name = "+ifname);
            // transform the base template
            DOMSource ds = new DOMSource(doc.getDocumentElement());
            try {
                File f = new File(ifname+"_stub.java");
                trStub.transform(ds, new StreamResult(f));
            } catch (Exception e) {
                System.out.println("Exception encountered while writing stub");
                e.printStackTrace();
            }
            try {
                File f = new File(ifname+"_skeleton.java");
                trSkel.transform(ds, new StreamResult(f));
            } catch (Exception e) {
                System.out.println("Exception encountered while writing skeleton");
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
