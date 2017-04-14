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
package org.mitre.midiki.compiler;

import java.io.Serializable;

public class MKTemplate implements Serializable
{
    private String _name;
    private boolean _lock;
    private boolean _builtin;
    private String _source;
    private MKLangspecMachine _machine;

    public MKTemplate()
    {
    }

    public MKTemplate(String name,
                      boolean lock,
                      boolean builtin,
                      String source,
                      MKLangspecMachine machine)
    {
        this();
        _name=name;
        _lock=lock;
        _builtin=builtin;
        _source=source;
        _machine=machine;
    }

    public void setName(String name) {_name=name;}
    public void setLock(boolean lock) {_lock=lock;}
    public void setBuiltin(boolean builtin) {_builtin=builtin;}
    public void setSource(String source) {_source=source;}
    public void setMachine(MKLangspecMachine machine) {_machine=machine;}

    public String getName() {return _name;}
    public boolean getLock() {return _lock;}
    public boolean getBuiltin() {return _builtin;}
    public String getSource() {return _source;}
    public MKLangspecMachine getMachine() {return _machine;}

}
