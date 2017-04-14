/****************************************************************************
 *
 * Copyright (C) 2005. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 * Consult the LICENSE file in the root of the distribution for terms and restrictions.
 *
 *       Release: 1.0
 *       Date: 13-January-2005
 *       Author: Carl Burke
 *
 *****************************************************************************/
package org.mitre.dm.qud.domain;

import java.util.List;
import java.util.Iterator;

/**
 * Defines an operation to assemble list elements into a string.
 * Individual list elements are converted to strings using thier
 * own toString() methods, and elements are separated by single
 * spaces. Space insertion is suppressed immediately before certain
 * punctuation marks.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class SimpleTextFormatter implements Formatter
{
    /**
     * Maximum length of a single output line, in characters.
     */
    public int MAXCOLUMNS = 80;
    /**
     * Assembles list elements into a single string.
     *
     * @param l a <code>List</code> value
     * @return a <code>String</code> value
     */
    public String format(List l)
    {
        if (l == null) return "";
        StringBuffer sb = new StringBuffer(MAXCOLUMNS);
        int lineLength = 0;
        Iterator it = l.iterator();
        while (it.hasNext()) {
            String s = it.next().toString();
            if (s == null) continue;
            if (s.length() == 0) continue;
            if (lineLength > 0)
                switch (Character.getType(s.charAt(0))) {
                    case Character.CONNECTOR_PUNCTUATION:
                    case Character.DASH_PUNCTUATION:
                    case Character.END_PUNCTUATION:
                    case Character.FINAL_QUOTE_PUNCTUATION:
                    case Character.START_PUNCTUATION:
                    case Character.OTHER_PUNCTUATION:
                        break;
                    default:
                        sb.append(" ");
                }
            lineLength += s.length();
            if (lineLength > MAXCOLUMNS) {
                sb.append("\n");
                lineLength = s.length();
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
