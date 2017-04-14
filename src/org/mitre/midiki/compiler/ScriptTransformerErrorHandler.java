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
 * ScriptTransformerErrorHandler.java
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

public class ScriptTransformerErrorHandler implements org.xml.sax.ErrorHandler
{
    public void warning(SAXParseException exception)
        throws SAXException
    {
        System.out.println("*** caught WARNING at "+
                           exception.getLineNumber()+","+
                           exception.getColumnNumber()+" in "+
                           exception.getPublicId()+"; "+
                           exception.getSystemId());
        exception.printStackTrace();
        System.out.println("*** continuing...");
    }
    public void error(SAXParseException exception)
        throws SAXException
    {
        System.out.println("*** caught ERROR at "+
                           exception.getLineNumber()+","+
                           exception.getColumnNumber()+" in "+
                           exception.getPublicId()+"; "+
                           exception.getSystemId());
        exception.printStackTrace();
        System.out.println("*** continuing...");
    }
    public void fatalError(SAXParseException exception)
        throws SAXException
    {
        System.out.println("*** caught FATAL ERROR at "+
                           exception.getLineNumber()+","+
                           exception.getColumnNumber()+" in "+
                           exception.getPublicId()+"; "+
                           exception.getSystemId());
        throw exception;
    }
}
