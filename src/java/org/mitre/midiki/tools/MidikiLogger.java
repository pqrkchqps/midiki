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
package org.mitre.midiki.tools;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.security.*;

/**
 * Provides the root logger for a Midiki instance.
 * Defers all actual logging to a surrogate. Assumes that
 * the names of its subordinate loggers are prefixed by
 * 'rootName.', with the eventual goal of allowing log level
 * specifications for the raw name of the descendant to be
 * applied in the same way to the prefixed names (although
 * it does not currently take any explicit action to guarantee
 * this outcome.)
 *
 * Provides direct access to specify a destination file as well.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see Handler
 */
public class MidikiLogger extends Logger
{
    private Logger surrogate;
    private String rootName;
    public MidikiLogger(String name)
    {
        super(name, null);
        rootName = name;
        //surrogate = Logger.getLogger(rootName);
    }

    private java.util.logging.Formatter discourseFormatter;
    public void setFormatter(java.util.logging.Formatter formatter)
    {
        discourseFormatter = formatter;
    }

    class MidikiFileHandler extends StreamHandler
    {
        FileOutputStream theFile;
        OutputStreamWriter osw;
        public MidikiFileHandler(String fileName) throws IOException
        {
            super();
            theFile = new FileOutputStream(fileName);
            osw = new OutputStreamWriter(theFile);
            setOutputStream(theFile);
        }
        public synchronized void publish(LogRecord record) {
            if (!isLoggable(record)) {
                return;
            }
            super.publish(record);
            flush();
        }
        public void close() {
            try {
                osw.write(getFormatter().getTail(null));
                osw.write("\n");
                theFile.flush();
                //theFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.close();
        }
   }
 
    private MidikiFileHandler logFileHandler = null;
    public void close()
    {
        logFileHandler.close();
    }
    public boolean setFileName(String fileName)
    {
        logFileHandler = null;
        try {
            logFileHandler = new MidikiFileHandler(fileName);
            logFileHandler.setLevel(Level.INFO);
            if (discourseFormatter != null)
                logFileHandler.setFormatter(discourseFormatter);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
        // remove any existing file handler and replace with this one.
        Handler[] handlers = getHandlers();
        for (int i=0; i<handlers.length; i++) {
            if (handlers[i] instanceof FileHandler) {
                removeHandler(handlers[i]);
            }
        }
        addHandler(logFileHandler);
        return true;
    }
}
