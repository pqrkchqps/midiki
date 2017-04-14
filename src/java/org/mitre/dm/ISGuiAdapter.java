/*
 * ISGuiAdapter.java
 *
 * Created on July 30, 2005, 2:35 PM
 */

package org.mitre.dm;

import org.mitre.midiki.agent.*;
import org.mitre.midiki.state.*;
import org.mitre.dm.tools.*;

/**
 *
 * @author  carl
 */
public class ISGuiAdapter extends DialogueSystemAdapter {
    private InfoStateGui infoStateGui;

    /** Creates a new instance of ISControlAdapter */
    public ISGuiAdapter() {
    }
    
    public void addToplevelContract(String name)
    {
        if (infoStateGui == null) {
            infoStateGui = new InfoStateGui();
        }
        infoStateGui.addToplevelContract(name);
    }

    /**
     * Called just before the information state begins propagating
     * changes. May be called as each change propagates.
     */    
    public void activation(ImmutableInfoState is)
    {
        if (infoStateGui == null) {
            infoStateGui = new InfoStateGui();
        }
        infoStateGui.showInfoState(is);
    }
}
