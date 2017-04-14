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
package org.mitre.midiki.impl.mitre.galaxy;

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;
import org.mitre.midiki.impl.mitre.*;

import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.io.*;

// Communicator imports
import galaxy.lang.*;
import galaxy.util.*;
import galaxy.server.*;
import galaxy.server.ui.ServerUI;

/**
 * Provides a base class for the custom server classes required
 * by Communicator. Also includes a simple code generator that
 * will generate appropriate method names for Communicator servers
 * given a package name, a class name, and a collection of cell
 * handlers. The generated methods will forward the calls to
 * cell handlers that are passed to the server statically.
 * (Static assignment is required due to some class loader issues
 * which require modification to Galaxy code to resolve, as is
 * the creation of server class source code rather than a dynamic proxy.)
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see galaxy.server.Server
 */
public class MidikiServer extends galaxy.server.Server
{
    public MidikiServer(MainServer ms, Socket s) throws IOException
    {
        super(ms,s);
    }
    public void serverOpReinitialize(GFrame gfr)
    {
        GalaxyMediator.currentServer = this;
    }
    // one server for all provided cells
    // code generator takes collection of CellHandlers,
    // package name, desired server name, & PrintWriter
    static public void generateServer(String packageName,
                                      String className,
                                      Collection cellHandlers,
                                      PrintWriter pw)
    {
        pw.println("package "+packageName+";");
        pw.println();
        pw.println("import org.mitre.midiki.logic.*;");
        pw.println("import org.mitre.midiki.state.*;");
        pw.println("import org.mitre.midiki.impl.mitre.galaxy.*;");
        pw.println("import java.io.*;");
        pw.println("import java.net.*;");
        pw.println("import java.util.*;");
        pw.println("import galaxy.server.*;");
        pw.println("import galaxy.lang.*;");
        pw.println();
        pw.println("public class "+className+" extends MidikiServer {");
        pw.println("    public "+className+"(MainServer ms, Socket s) throws IOException");
        pw.println("    {");
        pw.println("        super(ms,s);");
        pw.println("    }");
        pw.println("    static protected Map cellHandlers;");
        pw.println("    static public void setHandlers(Collection coll)");
        pw.println("    {");
        pw.println("        cellHandlers = new HashMap();");
        pw.println("        Iterator it = coll.iterator();");
        pw.println("        while (it.hasNext()) {");
        pw.println("            CellHandlers ch = (CellHandlers)it.next();");
        pw.println("            cellHandlers.put(ch.getContract().name(), ch);");
        pw.println("        }");
        pw.println("    }");
        Iterator it = cellHandlers.iterator();
        while (it.hasNext()) {
            CellHandlers ch = (CellHandlers)it.next();
            // iterate over all queries and methods in the contract.
            // assume we must make serverOps for all of them.
            Iterator qit = ch.getContract().queries();
            while (qit.hasNext()) {
                Contract.Query query = (Contract.Query)qit.next();
                String opName = "serverOp"+Normalize.normalizeWithInitialCap(query.name());
                pw.println("    public GFrame "+opName+"(GFrame frame) {");
                pw.println("        CellHandlers ch = (CellHandlers)cellHandlers.get(\""+ch.getContract().name()+"\");");
                pw.println("        Contract c = ch.getContract();");
                pw.println("        Collection params = extractProperties(frame, c.query(\""+query.name()+"\").parameters());");
                pw.println("        Bindings b = new BindingsImpl();");
                pw.println("        ch.query(\""+query.name()+"\").query(params, b);");
                pw.println("        GFrame returnFrame = composeFrame(params, b, c.name(), c.query(\""+query.name()+"\").parameters());");
                pw.println("        return returnFrame;");
                pw.println("    }");
            }
            Iterator mit = ch.getContract().methods();
            while (mit.hasNext()) {
                Contract.Method method = (Contract.Method)mit.next();
                String opName = "serverOp"+Normalize.normalizeWithInitialCap(method.name());
                pw.println("    public GFrame "+opName+"(GFrame frame) {");
                pw.println("        CellHandlers ch = (CellHandlers)cellHandlers.get(\""+ch.getContract().name()+"\");");
                pw.println("        Contract c = ch.getContract();");
                pw.println("        Collection params = extractProperties(frame, c.method(\""+method.name()+"\").parameters());");
                pw.println("        Bindings b = new BindingsImpl();");
                pw.println("        ch.method(\""+method.name()+"\").invoke(params, b);");
                pw.println("        GFrame returnFrame = composeFrame(params, b, c.name(), c.method(\""+method.name()+"\").parameters());");
                pw.println("        return returnFrame;");
                pw.println("    }");
            }
        }
        pw.println("}");
    }
    static public List extractProperties(GFrame frame, Iterator params)
    {
        LinkedList extractedParams = new LinkedList();
        // extract properties in order of appearance
        while (params.hasNext()) {
            Contract.Parameter param = (Contract.Parameter)params.next();
            Object property = frame.getProperty(":"+param.name());
            if (property == null) {
                extractedParams.add(Variable.newVariable());
            } else {
                extractedParams.add(property);
            }
        }
        System.out.println("extractedParams == "+extractedParams);
        return extractedParams;
    }
    static public GFrame composeFrame(Collection args, Bindings b, String name, Iterator params)
    {
        GFrame newFrame = new Clause(name);
        Iterator argIt = args.iterator();
        // append properties in order of appearance
        while (params.hasNext()) {
            Contract.Parameter param = (Contract.Parameter)params.next();
            if (!argIt.hasNext()) {
                System.out.println("Too few parameters!");
                break;
            }
            Object arg = argIt.next();
            arg = Unify.getInstance().deref(arg, b);
            if (!(arg instanceof Variable)) {
                newFrame.setProperty(":"+param.name(), arg);
            }
        }
        System.out.println("newFrame == "+newFrame);
        return newFrame;
    }
    static public Collection initializeTestHandlers()
    {
        CellHandlers ioCell = new CellHandlers(GalaxyContractDatabase.find("io_podium_output"));
        MethodHandler newIO = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    return false;
                }
            };
        ioCell.addMethodHandler("FromIOPodium", newIO);
        LinkedList list = new LinkedList();
        list.add(ioCell);
        return list;
    }
    static public final void main(String[] args)
    {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("MidikiServerTest.java"));
            generateServer("org.mitre.midiki.impl.mitre.galaxy","MidikiServerTest",initializeTestHandlers(),pw);
            pw.flush();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
