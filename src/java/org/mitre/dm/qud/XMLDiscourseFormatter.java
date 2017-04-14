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


package org.mitre.dm.qud;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.*;

/**
 * Formats a LogRecord for output as XML. Outputs only those parameters
 * felt useful for logging of human-computer discourse.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see Formatter
 */
public class XMLDiscourseFormatter extends java.util.logging.Formatter {
    // Append a two digit number.
    private void appendTwoDigits(StringBuffer sb, int x) {
        if (x < 10) {
            sb.append('0');
        }
        sb.append(x);
    }

    // Append the time and date in ISO 8601 format
    private void appendISO8601(StringBuffer sb, long millis) {
        Date date = new Date(millis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        sb.append(calendar.get(Calendar.YEAR));
        sb.append('-');
        appendTwoDigits(sb, calendar.get(Calendar.MONTH) + 1);
        sb.append('-');
        appendTwoDigits(sb, calendar.get(Calendar.DAY_OF_MONTH));
        sb.append('T');
        appendTwoDigits(sb, calendar.get(Calendar.HOUR_OF_DAY));
        sb.append(':');
        appendTwoDigits(sb, calendar.get(Calendar.MINUTE));
        sb.append(':');
        appendTwoDigits(sb, calendar.get(Calendar.SECOND));
    }

    // Append to the given StringBuffer an escaped version of the
    // given text string where XML special characters have been escaped.
    // For a null string we append "<null>"
    private void escape(StringBuffer sb, String text) {
        if (text == null) {
            text = "<null>";
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '<') {
                sb.append("&lt;");
            } else if (ch == '>') {
                sb.append("&gt;");
            } else if (ch == '&') {
                sb.append("&amp;");
            } else {
                sb.append(ch);
            }
        }
    }

    /**
     * Format the given message to XML.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public String format(LogRecord record) {
        StringBuffer sb = new StringBuffer(500);
        sb.append("<record>\n");

        sb.append("  <logger>");
        sb.append(record.getLoggerName());
        sb.append("</logger>\n");

        sb.append("  <date>");
        appendISO8601(sb, record.getMillis());
        sb.append("</date>\n");

        sb.append("  <millis>");
        sb.append(record.getMillis());
        sb.append("</millis>\n");

        if (record.getMessage() != null) {
            // Format the message string.
            String message = formatMessage(record);
            sb.append("  <message>");
            escape(sb, message);
            sb.append("</message>");
            sb.append("\n");
        }

        if (record.getParameters() != null) {
            // Format the accompanying parameters. The base version
            // provided by the logging API does not handle this.
            sb.append("  <parameters>");
            Object[] paramArray = record.getParameters();
            for (int i=0; i<paramArray.length; i++) {
                Object paramObj = paramArray[i];
                if (paramObj == null) continue;
                String param = paramObj.toString();
                escape(sb, param);
                if (i < (paramArray.length-1)) sb.append(",");
            }
            sb.append("</parameters>");
            sb.append("\n");
        }

        sb.append("</record>\n");
        return sb.toString();
    }

    /**
     * Return the header string for a set of XML formatted records.
     * 
     * @param   h  The target handler.
     * @return  header string
     */
    public String getHead(Handler h) {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\"");
        String encoding = h.getEncoding();
        if (encoding == null) {
            // Figure out the default encoding.
            encoding = sun.io.Converters.getDefaultEncodingName();
        }
        // Try to map the encoding name to a canonical name.
        try {
            Charset cs = Charset.forName(encoding);
            encoding = cs.name();
        } catch (Exception ex) {
            // We hit problems finding a canonical name.
            // Just use the raw encoding name.
        }	
        
        sb.append(" encoding=\"");
        sb.append(encoding);
        sb.append("\"");
        sb.append(" standalone=\"no\"?>\n");
        sb.append("<!DOCTYPE log SYSTEM \"logger.dtd\">\n");
        sb.append("<log>\n");
        return sb.toString();
    }

    /**
     * Return the tail string for a set of XML formatted records.
     * 
     * @param   h  The target handler.
     * @return  tail string
     */
    public String getTail(Handler h) {
        return "</log>\n";
    }
}
