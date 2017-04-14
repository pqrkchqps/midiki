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
package org.mitre.dm.qud.domain;

import java.util.*;

import org.mitre.midiki.state.*;
import org.mitre.midiki.logic.*;

import org.mitre.dm.*;

/**
 * Provides CellHandlers for the simple LexiconImplementation.
 * The specifics of the input and output patterns for the lexicon
 * must be set up by concrete subclasses.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see LexiconImplementation
 */
abstract public class LexiconCell
    extends LexiconImplementation
{
    public LexiconCell()
    {
        super();
    }

    /*
     * Lexicon queries
     */
    protected MethodHandler inputTransducer;
    protected MethodHandler outputTransducer;
    protected MethodHandler uniqueTransducer;

    public CellHandlers initializeHandlers()
    {
        CellHandlers lexiconCell =
            new CellHandlers(ContractDatabase.find("lexicon"));
        inputTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: String to tokenize
                    // 3: wordlist result to unify with
                    Object intfid = null;
                    Object wordlist = null;
                    Object moves = null;

                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) wordlist = argIt.next();
                    if (argIt.hasNext()) moves = argIt.next();
                    return doExec_lexicon_wordlist2moves(intfid, wordlist, moves, bindings);
                }
            };
        lexiconCell.addMethodHandler("wordlist2moves", inputTransducer);
        outputTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Moves to generate
                    // 3: String to output [may be better to have list?]
                    Object intfid = null;
                    Object movelist = null;
                    Object words = null;
                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) movelist = argIt.next();
                    if (argIt.hasNext()) words = argIt.next();
                    return doExec_lexicon_movelist2words(intfid, movelist, words, bindings);
                }
            };
        lexiconCell.addMethodHandler("movelist2words", outputTransducer);
        uniqueTransducer = new MethodHandler()
            {
                public boolean invoke(Collection arguments, Bindings bindings)
                {
                    // Arguments:
                    // 1: Instance ID (may change in future...)
                    // 2: Bag of moves (as List)
                    // 3: Set of moves (as List)
                    Object intfid = null;
                    Object movesin = null;
                    Object movesout = null;
                    Iterator argIt = arguments.iterator();
                    if (argIt.hasNext()) intfid = argIt.next();
                    if (argIt.hasNext()) movesin = argIt.next();
                    if (argIt.hasNext()) movesout = argIt.next();
                    return doExec_lexicon_remove_duplicates(intfid, movesin, movesout, bindings);
                }
            };
        lexiconCell.addMethodHandler("remove_duplicates", uniqueTransducer);
        return lexiconCell;
    }
}
