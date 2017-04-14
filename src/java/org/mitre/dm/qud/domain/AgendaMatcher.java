/*
 * AgendaMatcher.java
 *
 * Created on March 24, 2005, 5:43 PM
 */

package org.mitre.dm.qud.domain;

import java.util.*;
import org.mitre.midiki.logic.*;
import org.mitre.midiki.state.*;
import java.util.logging.*;

/**
 *
 * @author CBURKE
 */
public class AgendaMatcher extends ListMatcher {
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.domain.AgendaMatcher");
    Object move;
    String mv;
    InfoState infoState;
    
    /** Creates a new instance of AgendaMatcher */
    public AgendaMatcher(MatchSet set, InfoState i, Object m) {
        super(set);
        move = m;
        if (move instanceof Predicate) {
            Iterator it = ((Predicate)move).arguments();
            Object innerMove = it.next();
            if (innerMove instanceof Predicate) {
                mv = ((Predicate)innerMove).functor();
            } else {
                mv = "inner_move";
            }
        } else {
            mv = "from_agenda";
        }
        infoState = i;
    }
    public String toString()
    {
        return "AgendaMatcher(move="+move+")";
    }
    /**
     * Attempts to match the input item, adding any resulting
     * interpretations to the existing results.
     *
     * @param input an <code>Object</code> to be matched.
     * Implementing classes may expect specific subclasses as input.
     * @param interpretations a <code>Collection</code> of values
     * transduced from the input.
     * @return <code>true</code> if this match succeeded.
     */
    public boolean match(Object input, Collection interpretations)
    {
        Bindings bindings = new BindingsImpl();
        Object agenda = infoState.cell("is").cell("private").get("agenda");
        if (agenda == null) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.domain.AgendaMatcher","test","agenda null");
            return false;
        }
        if (!(agenda instanceof Stack)) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.domain.AgendaMatcher","test","agenda not list");
            return false;
        }
        if (((Stack)agenda).isEmpty()) {
            logger.logp(Level.FINER,"org.mitre.dm.qud.domain.AgendaMatcher","test","agenda empty");
            return false;
        }
        Object fst = ((Stack)agenda).peek();
        boolean matched = infoState.getUnifier().matchTerms(fst,move,bindings);
        System.out.println("Agendamatcher for "+move+" "+matched);
        System.out.println("Agenda is currently "+agenda);
        bindings.reset();
        if (matched) {
            LinkedList ans = new LinkedList((Collection)input);
            ArrayList args = new ArrayList(1);
            args.add(ans);
            Predicate inner = new Predicate(mv, args);
            args = new ArrayList(1);
            args.add(inner);
            interpretations.add(new Predicate("answer", args));
            ((Collection)input).clear();
        }
        return matched;
    }
    
}
