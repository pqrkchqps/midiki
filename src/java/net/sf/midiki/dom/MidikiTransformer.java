/*
 * Script transformation applier for MiDiKi.
 * Calling sequence:
 *   source-xml template-name output-file [parameter value ...]
 */
package net.sf.midiki.dom;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.border.TitledBorder;

import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import java.awt.*;              //for layout managers
import java.awt.event.*;        //for action and window events

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.io.*;
import java.io.IOException;

import java.util.*;

public class MidikiTransformer
{
    protected Transformer transformer;
    public MidikiTransformer() {
    }

    public MidikiTransformer(String template, HashMap parameters) {
        // generate the transformer for input
        try {
            TransformerFactory trFac = TransformerFactory.newInstance();
            StreamSource trSource = new StreamSource(template);
            transformer = trFac.newTransformer(trSource);
            Iterator it = parameters.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                transformer.setParameter(key, parameters.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Result transform(DOMSource ds) {
        DOMResult dr = new DOMResult(new MidikiDocumentFragment(new LinkedList()));
        //StreamResult sr = new StreamResult(new OutputStreamWriter(System.out));
        try {
            transformer.transform(ds, dr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dr;
    }
    
    static  boolean permitted = true;

    static private void transformFile(String source,
    String template,
    String target,
    HashMap parameters) {
        /*
         * Create the source
         */
        try {
        // in order to do validation, must enable validation
        // and set a SAXErrorHandler.
        DocumentBuilderFactory dbFac =
        DocumentBuilderFactory.newInstance();
        dbFac.setValidating(false);
        dbFac.setNamespaceAware(true);
        // May need next line for compatibility with future
        // versions of Xerces
        dbFac.setAttribute("http://xml.org/sax/features/validation", new Boolean(false));
        //dbFac.setAttribute("http://apache.org/xml/features/validation/schema", new Boolean(true));
        DocumentBuilder db = dbFac.newDocumentBuilder();
        //db.setErrorHandler(new ScriptTransformerErrorHandler());
        // make an input source from the document
        InputSource ss = new InputSource(new FileReader(source));
        // parse the source
        org.w3c.dom.Document doc = db.parse(source);
        DOMSource ds = new DOMSource(doc.getDocumentElement());
        
            /*
             *create the Result
             */
        
        // make an output result file writer
        FileWriter sw = new FileWriter(target);
        StreamResult sr = new StreamResult(sw);
        
            /*
             * Perform the transform
             */
        
        transform(ds, template, sr, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static private void transform(Source source,
                                        String template,
                                        Result target,
                                        HashMap parameters)
    {
        try {
            // prepare for transformation
            TransformerFactory trFac = TransformerFactory.newInstance();
            // make a source for the template
            StreamSource trSource = new StreamSource(template);
            // get the Transformer
            Transformer tr = trFac.newTransformer(trSource);
            Iterator it = parameters.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                tr.setParameter(key, parameters.get(key));
            }
            // transform the base template
            tr.transform(source, target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: MidikiTransformer source-xml script-xsl target-file [parameter value ...]");
            System.exit(0);
        }
        HashMap params = new HashMap();
        if (args.length > 3) {
            int alen = (args.length - 3);
            if ((alen % 2) != 0) {
                System.out.println("Must have a value for every parameter");
                System.exit(0);
            }
            for (int i=0; i<alen; i+=2) {
                params.put(args[i+3],args[i+4]);
            }
        }
        MidikiTransformer.transformFile(args[0], args[1], args[2], params);
    }
}

