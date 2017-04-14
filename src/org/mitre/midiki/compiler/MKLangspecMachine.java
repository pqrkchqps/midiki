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

import java.util.*;
import java.io.Serializable;

import org.mitre.midiki.compiler.parser.*;

/**
 * An <code>MKLangspecMachine</code> manages the construction
 * and execution of the set of virtual machines required to
 * execute MK bytecode for a language specification.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public class MKLangspecMachine implements Serializable
{
    MKInstruction[] scode;
    MKInstruction[] icode;
    MKSymbolTable symbols;

    public MKLangspecMachine(MKSymbolTable sym, LinkedList clist)
    {
        // each MKContext in the symbol table represents
        // a machine that must be instantiated. It should
        // (must) correspond to an instruction in _code_.
        // The address of the block header for the context
        // is accessible from the context.
        symbols = sym;
        scode = new MKInstruction[clist.size()];
        Iterator it = clist.iterator();
        int codePtr = 0;
        while (it.hasNext()) {
            MKInstruction inst = (MKInstruction)(it.next());
            scode[codePtr++] = inst;
        }
        // create choice point stack
        choice_sPC = new int[CHOICE_STACK_MAX];
        choice_iPC = new int[CHOICE_STACK_MAX];
        choice_ids = new int[CHOICE_STACK_MAX];
        choice_opm = new int[CHOICE_STACK_MAX];
        args = new int[ARG_MAX];
        opcodesForStatement = new Vector();
    }

    static public final int RESULT_NONE    = 0;
    static public final int RESULT_SUCCESS = 1;
    static public final int RESULT_FAILURE = 2;
    static public final int RESULT_BACKTRACK = 3;

    static public boolean DEBUG_LANGSPEC_MACHINE = false;

    /**
     * The <code>sPC</code> is the specification program counter.
     */
    private int sPC;
    /**
     * The <code>sPC</code> is the input program counter.
     */
    private int iPC;

    private int[] choice_sPC;
    private int[] choice_iPC;
    private int[] choice_ids;
    private int[] choice_opm;
    private int choicePtr;
    private int choiceId;
    static private final int CHOICE_STACK_MAX = 100;

    private boolean in_op;
    private String opName;
    private int[] args;
    private int argPtr;
    static private final int ARG_MAX = 20;

    private MKSymbol typeToCheck;
    private Vector opcodesForStatement;
    private int opcodeMark;
    private int prefixDepth;

    // the push and pop operations prepare the choice stack to
    // consider multiple program patterns for a single input set.
    // the reject operation is the same as a pop followed by a push;
    // this assumes that a pattern of STMTs is present.

    private boolean pushChoice(MKInstruction sinst)
    {
        if (choicePtr >= CHOICE_STACK_MAX) return false;
        if (choicePtr < 0) return false;
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("push: choicePtr "+choicePtr+", sPC: "+
                               sinst.getAddress()+", iPC: "+iPC+
                               ", opcodeMark: "+opcodeMark);
        }
        choice_sPC[choicePtr] = sinst.getAddress();//sPC;
        choice_iPC[choicePtr] = iPC;
        choice_ids[choicePtr] = choiceId;
        //choice_opm[choicePtr] = opcodeMark;
        choicePtr++;
        return (choicePtr < CHOICE_STACK_MAX);
    }

    private boolean popChoice()
    {
        choicePtr--;
        if (choicePtr >= CHOICE_STACK_MAX) return false;
        if (choicePtr < 0) return false;
        sPC = choice_sPC[choicePtr];
        iPC = choice_iPC[choicePtr];
        choiceId = choice_ids[choicePtr];
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("pop: choicePtr "+choicePtr+", sPC: "+
                               sPC+", iPC: "+iPC+", choiceId: "+choiceId+
                               ", opcodeMark: "+opcodeMark);
        }
        return (choicePtr >= 0);
    }

    private boolean rejectChoice()
    {
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("rejecting...");
        }
        if (!popChoice()) {
            //System.out.println("rejectChoice(): popChoice() false");
            return false;
        }
        // eliminate any rejected output
        //opcodeMark = choice_opm[choicePtr];
        //clearPendingOutputToMark();
        //System.out.println("rejectChoice(): "+scode[sPC]);
        // pointer is presumed to be at a STMT, but may be at BKSTART
        if (scode[sPC].getOpcode() == MKInstruction.MKIN_BKSTART) {
            sPC++;
        }
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("rejectChoice(): "+scode[sPC]);
        }
        if (scode[sPC].getOpcode() != MKInstruction.MKIN_STMT) {
            // throw PatternMachineError
            //System.out.println("rejectChoice() failure 1: "+scode[sPC]);
            return false;
        }
        sPC++;
        //System.out.println("rejectChoice(): "+scode[sPC]);
        // next op should be a STTOP
        if (scode[sPC].getOpcode() != MKInstruction.MKIN_STTOP) {
            // throw PatternMachineError
            //System.out.println("rejectChoice() failure 2: "+scode[sPC]);
            return false;
        }
        sPC = ((MKInstruction)(scode[sPC].getArgument())).getAddress();
        // that should point to an STBOT
        //System.out.println("rejectChoice(): "+scode[sPC]);
        if (scode[sPC].getOpcode() != MKInstruction.MKIN_STBOT) {
            // throw PatternMachineError
            //System.out.println("rejectChoice() failure 3: "+scode[sPC]);
            return false;
        }
        sPC++;
        choiceId++;
        //System.out.println("rejectChoice(): "+scode[sPC]);
        // the op following that must be either STMT or BKEND
        // if STMT, push it and return true
        // (don't execute the push -- STMT processing will handle that)
        if (scode[sPC].getOpcode() == MKInstruction.MKIN_STMT) {
            //pushChoice(scode[sPC]);
            return true;
        }
        // if BKEND, return false
        if (scode[sPC].getOpcode() != MKInstruction.MKIN_BKEND) {
            //System.out.println("rejectChoice(): block end (out of options)");
            return false;
        }
        // otherwise throw PatternMachineError
        //System.out.println("rejectChoice() failure 4: "+scode[sPC]);
        return false;
    }

    // the set and commit operations prepare the choice stack to
    // reconsider a block of choices.

    private boolean setChoice(MKInstruction sinst)
    {
        if (choicePtr < 0) return false;
        if (choicePtr >= CHOICE_STACK_MAX) return false;
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("set: choicePtr "+choicePtr+", sPC: "+
                               sinst.getAddress()+", iPC: "+iPC+
                               ", opcodeMark: "+opcodeMark+
                               ", choiceId: "+choiceId);
        }
        choice_sPC[choicePtr] = sinst.getAddress();//sPC;
        choice_iPC[choicePtr] = iPC;
        choice_ids[choicePtr] = choiceId;
        //choice_opm[choicePtr] = opcodeMark;
        choicePtr++;
        return (choicePtr < CHOICE_STACK_MAX);
    }

    private boolean commitChoice()
    {
        choicePtr--;
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("commit: choicePtr "+choicePtr+" sPC="+sPC+", iPC="+iPC+
                               ", opcodeMark: "+opcodeMark);
        }
        return (choicePtr >= 0);
    }

    // the returnFromChoice operation acknowledges that a subpattern
    // has executed. the machine needs to leave the iPC where it is
    // while returning the sPC to its previous value.

    private boolean returnFromChoice()
    {
        commitChoice();  // commit to this call
        sPC = choice_sPC[choicePtr];
        choiceId = choice_ids[choicePtr];
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("return: choicePtr "+choicePtr+", sPC: "+
                               sPC+", iPC: "+iPC+", opcodeMark: "+opcodeMark);
        }
        return true;
    }

    // the acceptChoice operation acknowledges that a pattern has
    // been matched. the machine needs to leave the iPC where it is
    // while stepping the sPC to the end of the block.

    private boolean acceptChoice()
    {
        commitChoice();  // commit to this pattern in block
        commitChoice();  // commit to this block
        sPC = choice_sPC[choicePtr];
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("accept: choicePtr "+choicePtr+", sPC: "+
                               sPC+", iPC: "+iPC);
        }
        return true;
    }

    private void markPendingOutput()
    {
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("setting output mark at "+opcodeMark);
        }
        opcodeMark = opcodesForStatement.size();
    }

    private void clearPendingOutputToMark()
    {
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("clearing pending output to "+opcodeMark);
        }
        opcodesForStatement.setSize(opcodeMark);
    }

    private void emitStatement(LinkedList output)
    {
        Enumeration st = opcodesForStatement.elements();
        while (st.hasMoreElements()) {
            Object elementOut = st.nextElement();
            if (elementOut == null) {
                System.out.println("null instruction input to emitStatement at "+output.size());
            }
            output.add(elementOut);
            if (DEBUG_LANGSPEC_MACHINE) {
                System.out.println("OUTPUT: "+elementOut);
            }
        }
        opcodesForStatement.clear();
        markPendingOutput();
    }

    // consumeArgument() checks the validity of the next pattern elements,
    // assuming that they are as defined in our standard.
    // only three argument types are valid: variable (FCINST), block
    // (BKTYPE), and block name (FCNAME).
    // Assumes that [sPC-1] is the MKIN_ARG.
    // Note that the first two arguments of any operator are the 'thisptr'
    // of the class providing the operator and the location of the result.
    private boolean consumeArgument()
    {
        if (argPtr==1) {
            //System.out.println("consumeArgument(): skipping thisptr "+scode[sPC]);
            sPC++;
        } else if (argPtr==2) {
            //System.out.println("consumeArgument(): skipping result "+scode[sPC]);
            sPC++;
        } else if (argPtr==3) {
            if (scode[sPC].getOpcode() != MKInstruction.MKIN_FCINST) {
                System.out.println("consumeArgument(): expected FCINST "+scode[sPC]);
                return false;
            }
            sPC++;
        } else if (argPtr==4) {
            if ((scode[sPC].getOpcode() == MKInstruction.MKIN_FCNAME) ||
                (scode[sPC].getOpcode() == MKInstruction.MKIN_FCINST)) {
                // named block; make sure it really is!
                sPC++;
            } else if (scode[sPC].getOpcode() == MKInstruction.MKIN_BKTYPE) {
                // anonymous block
                sPC++;
                // must be bkstart
                if (scode[sPC].getOpcode() != MKInstruction.MKIN_BKSTART) {
                    System.out.println("consumeArgument(): expected BKSTART "+scode[sPC]);
                    return false;
                }
                // jump to bkend
                MKInstruction bkend = (MKInstruction)(scode[sPC].getArgument());
                sPC = bkend.getAddress();
                if (scode[sPC].getOpcode() != MKInstruction.MKIN_BKEND) {
                    System.out.println("consumeArgument(): expected BKEND "+scode[sPC]);
                    return false;
                }
                sPC++;
            } else {
                System.out.println("consumeArgument(): neither named nor anonymous! "+scode[sPC]);
                System.out.println();
                return false;
            }
        } else {
            System.out.println("consumeArgument(): argPtr <1 or >4 "+scode[sPC]);
            return false;
        }
        if ((scode[sPC].getOpcode() != MKInstruction.MKIN_ARG) &&
            (scode[sPC].getOpcode() != MKInstruction.MKIN_ENDARGS)) {
            System.out.println("consumeArgument(): neither ARG nor ENDARGS "+scode[sPC]);
            return false;
        }
        return true;
    }

    // keywords do not get output, unlike all other elements

    private boolean matchKeyword(String kwd, Vector errors)
    {
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("matchKeyword(), keyword="+kwd+", iPC="+iPC);
        }
        if (kwd == null) {
            return false;
        }
        if (icode == null) return true;
        MKInstruction iinst = icode[iPC];
        iPC++;
        if (iinst == null) {
            throwMachineError(iinst,
                              "null instruction read in matchKeyword()",
                              errors);
            return false;
        }
        if (iinst.getOpcode() != MKInstruction.MKIN_FCNAME) {
            return false;
        }
        String name = (String)(iinst.getArgument());
        if (name==null) {
            throwMachineError(iinst,
                              "FCNAME without name in matchKeyword()",
                              errors);
            return false;
        }
        return (name.equals(kwd));
    }

    private boolean isMatchablePrefix(String kwd)
    {
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("isMatchablePrefix(), prefix="+kwd+", iPC="+iPC);
        }
        if (kwd == null) return false;
        MKContext prefixContext = fetchNamedContext(kwd);
        return (prefixContext != null);
    }

    private boolean matchPrefix(String kwd, MKStack stack, int iend,
                                LinkedList output, Vector errors)
    {
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("matchPrefix(), prefix="+kwd+", iPC="+iPC);
        }
        if (icode == null) return false;
        if (kwd == null) return false;
        MKContext prefixContext = fetchNamedContext(kwd);
        if (prefixContext == null) {
            return false;
        }
        MKInstruction estmt =
            new MKInstruction(MKInstruction.MKIN_ESTMT, null);
        MKInstruction esttop =
            new MKInstruction(MKInstruction.MKIN_ESTTOP, null);
        MKInstruction estbot =
            new MKInstruction(MKInstruction.MKIN_ESTBOT, esttop);
        esttop.setArgument(estbot);
        int outputBeforePrefix = opcodeMark;
        opcodesForStatement.addElement(estmt);
        opcodesForStatement.addElement(esttop);
        //markPendingOutput();
        pushChoice(scode[sPC]);  // from executeOperator
        String inputType =
            filterInput(iPC, iend, kwd, stack, output, true, 0, errors);
        returnFromChoice();      // from executeOperator
        estmt.setArgument(inputType);
        opcodesForStatement.addElement(estbot);
        if (inputType == null) {
            opcodeMark = outputBeforePrefix;
            clearPendingOutputToMark();
            if (DEBUG_LANGSPEC_MACHINE) {
                System.out.println("matchPrefix() failure!");
            }
            return false;
        } else {
            //markPendingOutput();
        }
        return true;
    }

    private boolean acceptUntypedInput(String cat, MKStack stack, int iend,
                                       String mach, LinkedList output,
                                       Vector errors)
    {
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("acceptUntypedInput(), cat="+cat+", mach="+mach+
                               ", iPC="+iPC);
        }
        if (cat == null) {
            throwMachineError(icode[iend],
                              "acceptUntypedInput(): null syntactic category",
                              errors);
            return false;
        }
        if (icode == null) return true;
        /*
         * Language specification elements must be correlated to instructions
         * in the virtual machine.
         */
        if (cat.equals("_name")) {
            //     Matched by FCNAME, FCINST, FVNAME, FVINST and following
            //       FMNAME, FMINST
            MKInstruction desig = icode[iPC];
            stack.push(icode[iPC], MKStack._nameType);
            opcodesForStatement.addElement(desig);
            iPC++;
            if ((desig.getOpcode() == MKInstruction.MKIN_FCNAME) ||
                (desig.getOpcode() == MKInstruction.MKIN_FCINST) ||
                (desig.getOpcode() == MKInstruction.MKIN_FVNAME) ||
                (desig.getOpcode() == MKInstruction.MKIN_FVINST)) {
                while ((icode[iPC].getOpcode() == MKInstruction.MKIN_FMNAME) ||
                       (icode[iPC].getOpcode() == MKInstruction.MKIN_FMINST)) {
                    opcodesForStatement.addElement(icode[iPC]);
                    iPC++;
                }
                return true;
            }
        } else if (cat.equals("_designator")) {
            //     Matched by FCNAME, FCINST not in keyword table
            MKInstruction desig = icode[iPC];
            stack.push(desig.getArgument(), MKStack._designatorType);
            opcodesForStatement.addElement(icode[iPC]);
            iPC++;
            if (desig.getOpcode() == MKInstruction.MKIN_FCNAME) {
                if (DEBUG_LANGSPEC_MACHINE) {
                    System.out.println("_designator matched FCNAME");
                }
                return true;
            }
            if (desig.getOpcode() == MKInstruction.MKIN_FCINST) {
                if (DEBUG_LANGSPEC_MACHINE) {
                    System.out.println("_designator matched FCINST");
                }
                return true;
            }
        } else if (cat.equals("_identifier")) {
            //     Matched by FCNAME not in keyword table
            if (icode[iPC].getOpcode() == MKInstruction.MKIN_FCNAME) {
                stack.push(icode[iPC].getArgument(), MKStack._identifierType);
                opcodesForStatement.addElement(icode[iPC]);
                iPC++;
                return true;
            }
        } else if (cat.equals("_variable")) {
            //     Matched by FCINST not in keyword table
            if (icode[iPC].getOpcode() == MKInstruction.MKIN_FCINST) {
                stack.push(icode[iPC].getArgument(), MKStack._variableType);
                opcodesForStatement.addElement(icode[iPC]);
                iPC++;
                return true;
            }
        } else if (cat.equals("_expression")) {
            //     Matched by any valid expression matching the type qualifier.
            //     The difficult part of this is to fetch enough of the stack
            //     without overrunning subsequent instructions.
            boolean exprPass = false;
            // - LIST
            if (icode[iPC].getOpcode() == MKInstruction.MKIN_LIST) {
                exprPass = acceptUntypedInput("_list", stack, iend,
                                       mach, output, errors);
            }
            // - BLOCK
            else if (icode[iPC].getOpcode() == MKInstruction.MKIN_BKTYPE) {
                exprPass = acceptUntypedInput("_block", stack, iend,
                                       mach, output, errors);
            }
            // - ARITY + FONAME + (OP/METHOD)
            else if (icode[iPC].getOpcode() == MKInstruction.MKIN_ARITY) {
                exprPass = acceptUntypedInput("_function", stack, iend,
                                       mach, output, errors);
            }
            // - PUSH
            else if (icode[iPC].getOpcode() == MKInstruction.MKIN_PUSH) {
                Object obj = icode[iPC].getArgument();
                //stack.push(obj, MKStack._integerType);
                opcodesForStatement.addElement(icode[iPC]);
                iPC++;
                //if (obj instanceof Integer) return true;
            }
            // - NAME
            else {
                exprPass = acceptUntypedInput("_name", stack, iend,
                                       mach, output, errors);
            }
            return exprPass;
        } else if (cat.equals("_string")) {
            //     Matched by PUSH of type string
            if (icode[iPC].getOpcode() == MKInstruction.MKIN_PUSH) {
                Object obj = icode[iPC].getArgument();
                stack.push(obj, MKStack._stringType);
                opcodesForStatement.addElement(icode[iPC]);
                iPC++;
                if (obj instanceof String) return true;
            }
        } else if (cat.equals("_integer")) {
            //     Matched by PUSH of type int
            if (icode[iPC].getOpcode() == MKInstruction.MKIN_PUSH) {
                Object obj = icode[iPC].getArgument();
                stack.push(obj, MKStack._integerType);
                opcodesForStatement.addElement(icode[iPC]);
                iPC++;
                if (obj instanceof Integer) return true;
            }
        } else if (cat.equals("_real")) {
            //     Matched by PUSH of type real
            if (icode[iPC].getOpcode() == MKInstruction.MKIN_PUSH) {
                Object obj = icode[iPC].getArgument();
                stack.push(obj, MKStack._realType);
                opcodesForStatement.addElement(icode[iPC]);
                iPC++;
                if (obj instanceof Double) return true;
            }
        } else if (cat.equals("_number")) {
            //     Matched by PUSH of type int or real
            if (icode[iPC].getOpcode() == MKInstruction.MKIN_PUSH) {
                Object obj = icode[iPC].getArgument();
                opcodesForStatement.addElement(icode[iPC]);
                iPC++;
                if (obj instanceof Integer) {
                    stack.push(obj, MKStack._integerType);
                    return true;
                }
                if (obj instanceof Double) {
                    stack.push(obj, MKStack._realType);
                    return true;
                }
            }
        } else if (cat.equals("_char")) {
            //     Matched by PUSH of type character
            if (icode[iPC].getOpcode() == MKInstruction.MKIN_PUSH) {
                Object obj = icode[iPC].getArgument();
                stack.push(obj, MKStack._charType);
                opcodesForStatement.addElement(icode[iPC]);
                iPC++;
                if (obj instanceof Character) return true;
            }
        } else if (cat.equals("_list")) {
            //     Matched by LIST, consumes LISTELM-ENDLIST
            if (icode[iPC].getOpcode() != MKInstruction.MKIN_LIST) {
                if (DEBUG_LANGSPEC_MACHINE) {
                    System.out.println("acceptUntypedInput(_list): opcode mismatch ");
                }
                return false;
            }
            stack.push(icode[iPC], MKStack._listType);
            opcodesForStatement.addElement(icode[iPC]);
            iPC++;
            // must be LISTELM
            if (icode[iPC].getOpcode() != MKInstruction.MKIN_LISTELM) {
                if (DEBUG_LANGSPEC_MACHINE) {
                    System.out.println("acceptUntypedInput(_list): opcode mismatch 2");
                }
                return false;
            }
            opcodesForStatement.addElement(icode[iPC]);
            MKInstruction nextelm = (MKInstruction)(icode[iPC].getArgument());
            while (nextelm != null) {
                if (mach != null) {
                    MKInstruction estmt =
                        new MKInstruction(MKInstruction.MKIN_ESTMT, null);
                    MKInstruction esttop =
                        new MKInstruction(MKInstruction.MKIN_ESTTOP, null);
                    MKInstruction estbot =
                        new MKInstruction(MKInstruction.MKIN_ESTBOT, esttop);
                    esttop.setArgument(estbot);
                    opcodesForStatement.addElement(estmt);
                    opcodesForStatement.addElement(esttop);
                    //markPendingOutput(); // lock buffer to this point
                    String inputType =
                        filterInput(iPC+1, nextelm.getAddress(),
                                    mach, stack, output, true, 0, errors);
                    if (inputType == null) {
                        return false;
                    }
                    estmt.setArgument(inputType);
                    opcodesForStatement.addElement(estbot);
                } else {
                    // warn user that this list element is unmatched
                    // should perhaps try to match this against _expression...
                    // output list data up to nextelm
                    for (int skip=iPC+1; icode[skip] != nextelm; skip++) {
                        opcodesForStatement.addElement(icode[skip]);
                    }
                }
                opcodesForStatement.addElement(nextelm);
                iPC = nextelm.getAddress();
                nextelm = (MKInstruction)(nextelm.getArgument());
            }
            //if (nextelm.getOpcode() != MKInstruction.MKIN_ENDLIST)
            //    return false;
            //opcodesForStatement.addElement(nextelm);
            //iPC = nextelm.getAddress()+1;
            iPC++;
            return true;
        } else if (cat.equals("_block")) {
            //     Matched by BKTYPE, consumes BKSTART-BKEND
            if (icode[iPC].getOpcode() != MKInstruction.MKIN_BKTYPE) {
                return false;
            }
            stack.push(stack.openContext(icode[iPC]), MKStack._blockType);
            opcodesForStatement.addElement(icode[iPC]);
            iPC++;
            // must be bkstart
            if (icode[iPC].getOpcode() != MKInstruction.MKIN_BKSTART) {
                return false;
            }
            opcodesForStatement.addElement(icode[iPC]);
            MKInstruction bkend = (MKInstruction)(icode[iPC].getArgument());
            prefixDepth++;
            if (mach != null) {
                if (filterStatements(iPC+1, bkend.getAddress(),
                                     mach, stack, output, errors) == null) {
                    prefixDepth--;
                    return false;
                }
            } else {
                // warn user that this block is unmatched
                // output list data up to bkend
                for (int skip=iPC+1; icode[skip] != bkend; skip++) {
                    opcodesForStatement.addElement(icode[skip]);
                }
            }
            prefixDepth--;
            // jump to bkend
            iPC = bkend.getAddress();
            if (bkend.getOpcode() != MKInstruction.MKIN_BKEND) {
                throwMachineError(bkend,
                                  "Machine error: Expected BKEND",
                                  errors);
                return false;
            }
            opcodesForStatement.addElement(icode[iPC]);
            stack.closeContext();
            iPC++;
            return true;
        } else if (cat.equals("_function")) {
            //     Matched by ARITY, consumes ARG-ENDARGS through
            //     matching METHOD
            if (icode[iPC].getOpcode() != MKInstruction.MKIN_ARITY) {
                // Why did I consider this a MachineError??
                // It's just a failure to match a _function template.
                //throwMachineError(icode[iPC],
                //                  "Machine error: Expected ARITY",
                //                  errors);
                return false;
            }
            Integer arity = (Integer)(icode[iPC].getArgument());
            stack.push(icode[iPC], MKStack._functionType);
            opcodesForStatement.addElement(icode[iPC]);
            iPC++;
            // must be ARG if arity other than 0
            if ((icode[iPC].getOpcode() != MKInstruction.MKIN_ARG) &&
                (arity.intValue() != 0))
                return false;
            if (icode[iPC].getOpcode() == MKInstruction.MKIN_ARG) {
            opcodesForStatement.addElement(icode[iPC]);
            //MKInstruction lastarg = icode[iPC];
            MKInstruction nextarg = (MKInstruction)(icode[iPC].getArgument());
            while (nextarg != null) {
                if (mach != null) {
                    MKInstruction estmt =
                        new MKInstruction(MKInstruction.MKIN_ESTMT, null);
                    MKInstruction esttop =
                        new MKInstruction(MKInstruction.MKIN_ESTTOP, null);
                    MKInstruction estbot =
                        new MKInstruction(MKInstruction.MKIN_ESTBOT, esttop);
                    esttop.setArgument(estbot);
                    opcodesForStatement.addElement(estmt);
                    opcodesForStatement.addElement(esttop);
                    //markPendingOutput(); // lock buffer to this point
                    String inputType =
                        filterInput(iPC+1, nextarg.getAddress(),
                                    mach, stack, output, true, 0, errors);
                    if (inputType == null) {
                        return false;
                    }
                    estmt.setArgument(inputType);
                    opcodesForStatement.addElement(estbot);
                } else {
                    // warn user that this arg is unmatched
                    // output list data up to nextarg
                    for (int skip=iPC+1; icode[skip] != nextarg; skip++) {
                        opcodesForStatement.addElement(icode[skip]);
                    }
                }
                opcodesForStatement.addElement(nextarg);
                //lastarg = nextarg;
                iPC = nextarg.getAddress();
                nextarg = (MKInstruction)(nextarg.getArgument());
            }
            //if (lastarg.getOpcode() != MKInstruction.MKIN_ENDARGS) {
            //    throwMachineError(lastarg,
            //                      "Machine error: ENDARGS expected",
            //                      errors);
            //    return false;
            //}
            //opcodesForStatement.addElement(lastarg);
            //iPC = lastarg.getAddress()+1;
            iPC++;
            }
            // now we extract the function name itself
            //     Matched by FCNAME, FCINST, FVNAME, FVINST and following
            //       FMNAME, FMINST
            MKInstruction desig = icode[iPC];
            stack.push(icode[iPC], MKStack._nameType);
            opcodesForStatement.addElement(desig);
            iPC++;
            if ((desig.getOpcode() == MKInstruction.MKIN_FCNAME) ||
                (desig.getOpcode() == MKInstruction.MKIN_FCINST) ||
                (desig.getOpcode() == MKInstruction.MKIN_FVNAME) ||
                (desig.getOpcode() == MKInstruction.MKIN_FVINST)) {
                while ((icode[iPC].getOpcode() == MKInstruction.MKIN_FMNAME) ||
                       (icode[iPC].getOpcode() == MKInstruction.MKIN_FMINST)) {
                    opcodesForStatement.addElement(icode[iPC]);
                    iPC++;
                }
            }
            // and then we make the method call
            if ((icode[iPC].getOpcode() != MKInstruction.MKIN_METHOD) &&
                (icode[iPC].getOpcode() != MKInstruction.MKIN_OP)) {
                throwMachineError(icode[iPC],
                                  "Machine error: Expected METHOD or OP",
                                  errors);
                return false;
            }
            opcodesForStatement.addElement(icode[iPC]);
            iPC++;
            return true;
        } else if (cat.equals("_statement")) {
            // in general, this option should only get called when matching
            // against an entire sentence, but by the time it is called
            // the sentence header has already been read. The headers need
            // to be checked, but this routine can't guarantee that they
            // are available. Still, make the assumption that they are.

            //     Matched by STMT, consumes STBOT-STTOP
            if (icode[iPC-2].getOpcode() != MKInstruction.MKIN_STMT) {
                System.out.println("code not STMT:"+icode[iPC-2]);
                return false;
            }
            stack.push(icode[iPC-2], MKStack._statementType);
            //opcodesForStatement.addElement(icode[iPC-2]); // already added?
            //iPC++;
            // must be sttop
            if (icode[iPC-1].getOpcode() != MKInstruction.MKIN_STTOP) {
                return false;
            }
            //opcodesForStatement.addElement(icode[iPC-1]); // already added?
            MKInstruction stbot = (MKInstruction)(icode[iPC-1].getArgument());
            if (mach != null) {
                MKInstruction mklast = 
                    (MKInstruction)opcodesForStatement.lastElement();
                System.out.println("mklast.getAddress()=="+mklast.getAddress());
                System.out.println("iPC == "+(iPC));
                if (mklast.getAddress()==(iPC-1)) {
                    // statement header has already been generated.
                    // remove it, since filterStatements will generate
                    // a new copy.
                    opcodesForStatement.remove(mklast);
                    mklast = (MKInstruction)opcodesForStatement.lastElement();
                    opcodesForStatement.remove(mklast);
                }
                if (filterStatements(iPC-2, stbot.getAddress(),
                                     mach, stack, output, errors) == null) {
                    //System.out.println("filterStatements() failure");
                    return false;
                }
            } else {
                // warn user that this statement is unmatched
                // output list data up to bkend
                for (int skip=iPC; icode[skip] != stbot; skip++) {
                    opcodesForStatement.addElement(icode[skip]);
                }
            }
            iPC = stbot.getAddress();
            if (icode[iPC].getOpcode() != MKInstruction.MKIN_STBOT) {
                System.out.println("code not STBOT:"+icode[iPC]);
                return false;
            }
            //opcodesForStatement.addElement(icode[iPC]);
            //iPC++;
            return true;
        } else {
            if (DEBUG_LANGSPEC_MACHINE) {
                System.out.println("acceptUntypedInput() fall through!");
            }
        }
        return false;
    }

    private boolean acceptTypedInput(String cat, MKStack stack, int iend,
                                     LinkedList output, Vector errors)
    {
        if (cat == null) {
            throwMachineError(icode[iend],
                              "acceptTypedInput(): null syntactic category",
                              errors);
            return false;
        }
        if (!acceptUntypedInput(cat, stack, iend, null, output, errors))
            return false;
        // compare type required against type calculated above
        return true;
    }

    // executeOperator() first checks that the input matches the outer
    // form requirements of the pattern, then executes a new machine
    // on the inner parts of the form.
    // The first argument is the thisptr, the second argument is the result,
    // the third argument is the syntactic element, and the fourth argument
    // is the named machine.
    private boolean executeOperator(MKStack stack,
                                    String contextName,
                                    int iend,
                                    LinkedList output,
                                    Vector errors)
    {
        String machName = "";
        if (opName == null) return false;
        if (opName.equals("=")) {
            // the fourth argument is the machine name; should be FCNAME
            if (scode[args[3]].getOpcode() == MKInstruction.MKIN_BKTYPE) {
                // anonymous machine; check name for context, define if needed
                machName = contextName + ".anon";
                if (DEBUG_LANGSPEC_MACHINE) {
                    System.out.println("checking for anonymous block "+machName);
                }
                MKSymbol machSymbol = symbols.getSymbol("@"+machName);
                if (machSymbol == null) {
                    if (DEBUG_LANGSPEC_MACHINE) {
                        System.out.println("machSymbol == null");
                    }
                    MKContext ctx = symbols.openContext(machName);
                    symbols.topContext().setRef(scode[args[3]]);
                    symbols.closeContext();
                    symbols.bindSymbol("@"+machName,
                                       MKContext.ctxtType,
                                       ctx);
                } else {
                    if (DEBUG_LANGSPEC_MACHINE) {
                        System.out.println("Found machine in symbol table");
                    }
                }
            } else if ((scode[args[3]].getOpcode() == MKInstruction.MKIN_FCNAME) ||
                       (scode[args[3]].getOpcode() == MKInstruction.MKIN_FCINST)) {
                machName = (String)scode[args[3]].getArgument();
            } else {
                throwMachineError(scode[args[2]],
                                  "Machine error: Expected submachine name",
                                  errors);
                return false;
            }
            // the third argument is the pattern to check; should be FCINST
            if (scode[args[2]].getOpcode() != MKInstruction.MKIN_FCINST) {
                throwMachineError(scode[args[2]],
                                  "Machine error: Expected recursive pattern",
                                  errors);
                return false;
            }
            String patCat = (String)scode[args[2]].getArgument();
            pushChoice(scode[sPC]);
            boolean retval = 
                acceptUntypedInput(patCat, stack, iend, machName, output, errors);
            returnFromChoice();
            return retval;
        }
        return false;
    }

    // the filter routine will execute the program for
    // the input code starting at startSymbol.
    // it must also "execute" the code in some way.
    // perhaps this means passing in another object that
    // exposes an interface for a statement name string
    // and a value/type stack.

    public LinkedList filter(LinkedList input,
                             String startSymbol,
                             MKStack stack,
                             Vector errors)
    {
        Object[] obj = input.toArray();
        MKInstruction[] mkinput = new MKInstruction[obj.length];
        for (int i=0; i<obj.length; i++)
            mkinput[i] = (MKInstruction)(obj[i]);
        return filter(mkinput, startSymbol, stack, errors);
    }

    private MKContext fetchNamedContext(String startSymbol)
    {
        // first, locate the language specification named in 'startSymbol'.
        // this is the unqualified name of a context. (Other way to do it
        // is to fetch the langspec and then access the derived context.)
        MKSymbol machSymbol = symbols.getSymbol("@"+startSymbol);
        if (machSymbol == null) {
            // throw SymbolNotFound exception
            //System.out.println("SymbolNotFound: @"+startSymbol);
            machSymbol = symbols.getSymbol(startSymbol);
            if (machSymbol == null) {
                // throw SymbolNotFound exception
                //System.out.println("SymbolNotFound: "+startSymbol);
                return null;
            }
        }
        Object machCtxt = machSymbol.getValue();
        if (!machSymbol.getType().equals(MKContext.contextType)) {
            // throw SymbolNotLanguageSpec exception
            //System.out.println("Symbol not language spec: "+machSymbol.getType());
            if (!machSymbol.getType().equals(MKContext.ctxtType)) {
                // throw SymbolNotLanguageSpec exception
                //System.out.println("Symbol not language ref: "+machSymbol.getType());
                return null;
            }
        }
        return (MKContext)(machCtxt);
    }

    // output a warning for elements that aren't processed, so user knows
    // when semantic forms aren't checked.

    public LinkedList filter(MKInstruction[] input,
                             String startSymbol,
                             MKStack stack,
                             Vector errors)
    {
        // this routine filters the input code and generates a
        // new version of the input code. must trap errors, permit
        // symbol generation.
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("filter(): input.length="+input.length+", startSymbol="+startSymbol);
        }
        icode = input;
        LinkedList output = new LinkedList();
        choicePtr = 0;
        return filterStatements(0, icode.length, startSymbol, stack, output, errors);
    }

    public LinkedList filterStatements(int istart,
                                       int iend,
                                       String startSymbol,
                                       MKStack stack,
                                       LinkedList output,
                                       Vector errors)
    {
        // this routine filters the input code and generates a
        // new version of the input code. must trap errors, permit
        // symbol generation.

        MKContext machContext = fetchNamedContext(startSymbol);
        if (machContext == null) {
            if (DEBUG_LANGSPEC_MACHINE) {
                System.out.println("filterStatements(): couldn't find named context "+startSymbol);
            }
            return null;
        }
        sPC = machContext.getRef().getAddress();
        iPC = istart;
        int istop = iend;
        in_op = false;
        MKInstruction istmt = null;  // fill this when we know what it matches
        MKInstruction iinst = null;
        MKInstruction ilast = null;
        while ((icode == null) || (iPC < iend)) {
            // try recognizing statements until we succeed or fail.
            // try advancing past STMT header
            if (icode != null) {
                try {
                    iinst = icode[iPC];
                    if (iinst.getOpcode() != MKInstruction.MKIN_STMT) {
                        throwMachineError(iinst,
                                          "Statement expected",
                                          errors);
                        return null;
                    }
                    istmt = iinst;
                    opcodesForStatement.addElement(iinst);
                    iPC++;
                    iinst = icode[iPC];
                    if (iinst.getOpcode() != MKInstruction.MKIN_STTOP) {
                        throwMachineError(iinst,
                                          "Statement expected",
                                          errors);
                        return null;
                    }
                    opcodesForStatement.addElement(iinst);
                    //markPendingOutput();
                    iPC++;
                    ilast = (MKInstruction)(iinst.getArgument());
                    if (ilast.getOpcode() != MKInstruction.MKIN_STBOT) {
                        throwMachineError(ilast,
                                          "End of statement expected",
                                          errors);
                        return null;
                    }
                    istop = ilast.getAddress();
                } catch (Exception e) {
                    throwMachineError(icode[icode.length],
                                      "Unexpected end of input",
                                      errors);
                    return null;
                }
            }
            //
            // here's the magic bit where the statement body is scanned
            //
            // sPC points to the start of the matching block.
            //
            //markPendingOutput();
            String inputType =
                filterInput(iPC, istop, startSymbol, stack, output, false, 1, errors);
            istmt.setArgument(inputType);
            if (inputType == null) {
                throwMachineError(istmt,
                                  "Statement in error; expected "+startSymbol,
                                  errors);
                if ((icode != null) && (iPC != istop)) {
                    iinst = icode[iPC];
                    while ((iinst.getOpcode() != MKInstruction.MKIN_STBOT) &&
                           (iPC < istop)) {
                        iPC++; iinst = icode[iPC];
                    }
                }
                iPC = istop+1;
                stack.reset();
            } else {
                if ((icode != null) && (iPC < iend)) {
                try {
                    if (DEBUG_LANGSPEC_MACHINE) {
                        System.out.println("Read statement. icode.length = "+icode.length+
                                           ", iPC = "+iPC);
                    }
                    opcodesForStatement.addElement(ilast);
                    iPC++;
                } catch (Exception e) {
                    throwMachineError(ilast,
                                      "Error at end of statement; expected "+
                                      startSymbol,
                                      errors);
                    e.printStackTrace();
                    return null;
                }
                if (prefixDepth==0) emitStatement(output);
                }
            }
        }
        return output;
    }

    public String filterInput(int istart,
                              int iend,
                              String startSymbol,
                              MKStack stack,
                              LinkedList output,
                              boolean isPrefix,
                              int forgiveMissingSeparators,
                              Vector errors)
    {
        String inputType = null;
        // this routine filters the input code and generates a
        // new version of the input code. must trap errors, permit
        // symbol generation.

        MKContext machContext = fetchNamedContext(startSymbol);
        if (machContext == null) {
            return inputType;
        }
        sPC = machContext.getRef().getAddress();
        iPC = istart;
        choiceId = 0;
        in_op = false;
        if (prefixDepth < 0) {
            throwMachineError(scode[sPC],
                              "prefixDepth underflow!",
                              errors);
            prefixDepth = 0;
        }
        if (isPrefix) prefixDepth++;
        MKInstruction istmt = null;  // fill this when we know what it matches
        while ((icode == null) || (iPC < iend)) {
            // try recognizing statements until we succeed or fail.
            markPendingOutput();
            int localOpcodeMark = opcodeMark;
            int result = RESULT_NONE;
            MKInstruction iinst = null;
            while (result == RESULT_NONE) {
                MKInstruction sinst = scode[sPC];
                MKInstruction linst = iinst;
                int opcode = sinst.getOpcode();
                sPC++;
                if (icode==null) {
                    if (DEBUG_LANGSPEC_MACHINE) {
                        System.out.println("==> "+sinst);
                    }
                } else {
                    iinst = icode[iPC];
                    if (DEBUG_LANGSPEC_MACHINE) {
                        System.out.println("==> "+sinst+"\t"+iinst);
                    }
                }
                switch(opcode) {
                    case MKInstruction.MKIN_BKTYPE:
                        if (!setChoice(sinst)) {
                            throwMachineError(sinst,
                                              "setChoice failure: "+choicePtr,
                                              errors);
                            if (isPrefix) prefixDepth--;
                            return inputType;
                        }
                        break;
                    case MKInstruction.MKIN_BKSTART:
                        // no-op
                        break;
                    case MKInstruction.MKIN_BKEND:
                        // if we execute one of these, we haven't found a match
                        // or we've reached the end of the code to validate.
                        if (icode==null) {
                            if (DEBUG_LANGSPEC_MACHINE) {
                                System.out.println("Code verifies OK.");
                            }
                            if (isPrefix) prefixDepth--;
                            return inputType;
                        }
                        throwMachineError(iinst,
                                          "block end encountered",
                                          errors);
                        if (isPrefix) prefixDepth--;
                        return inputType;
                    case MKInstruction.MKIN_STMT:
                        if (!pushChoice(sinst)) {
                            throwMachineError(iinst,
                                              "pushChoice failure",
                                              errors);
                            if (isPrefix) prefixDepth--;
                            return inputType;
                        }
                        // normally I expect a matching STMT in the input
                        // that may be different if using embedded machines
                        break;
                    case MKInstruction.MKIN_STBOT:
                        // normally I expect a matching STBOT in the input
                        // if the statement is a match.
                        // that may be different if using embedded machines.
                        // if input is an STBOT, accept.
                        inputType = startSymbol+"."+choiceId;
                        if (icode == null) {
                            commitChoice(); // accept this statement
                            result = RESULT_SUCCESS;
                            stack.recognize(inputType);
                            //} else if (icode[iPC].getOpcode() == MKInstruction.MKIN_STBOT) {
                        } else if ((isPrefix) || (iPC == iend)) {
                            acceptChoice(); // accept this statement
                            result = RESULT_SUCCESS;
                            if (istmt != null) {
                                istmt.setArgument(inputType);
                            }
                            stack.recognize(inputType);
                        } else if (forgiveMissingSeparators != 0) {
                            // we recognized a statement, but we aren't at
                            // the end of one. claim we're missing the
                            // appropriate separator and skip the rest
                            // of the input.
                            if (linst==null) {
                                throwMachineError(iinst,
                                                  "Missing separator",
                                                  errors);
                            } else {
                                throwMachineError(linst,
                                                  "Missing separator",
                                                  errors);
                            }
                            iPC = iend;
                            acceptChoice(); // accept this statement
                            result = RESULT_SUCCESS;
                            if (istmt != null) {
                                istmt.setArgument(inputType);
                            }
                            stack.recognize(inputType);
                        } else if (rejectChoice()) { // backtrack or fail
                            result = RESULT_BACKTRACK;
                        } else {
                            result = RESULT_FAILURE;
                        }
                        stack.reset();
                        break;
                    case MKInstruction.MKIN_STTOP:
                        // non-op
                        break;
                    case MKInstruction.MKIN_ARITY:
                        // set aside method argument space
                        // the language specification currently only allows
                        // one type of operator: '=', denoting a subgrammar.
                        if (in_op) {
                            throwMachineError(iinst,
                                              "Bad pattern instruction",
                                              errors);
                            result = RESULT_FAILURE;
                        } else {
                            in_op = true;
                            argPtr = 0;
                            opName = null;
                        }
                        break;
                    case MKInstruction.MKIN_ARG:
                        // advance to next argument
                        args[argPtr] = sPC;
                        argPtr++;
                        if (!consumeArgument()) {
                            throwMachineError(iinst,
                                              "Bad ARG in pattern machine",
                                              errors);
                            result = RESULT_FAILURE;
                        }
                        break;
                    case MKInstruction.MKIN_ENDARGS:
                        // no-op: done with arguments
                        break;
                    case MKInstruction.MKIN_FCNAME: {
                        // specification is a constant, denoting a keyword
                        String kwd = (String)(sinst.getArgument());
                        if (!matchKeyword(kwd, errors)) {
                            if (rejectChoice()) { // backtrack or fail
                                //System.out.println("rejectChoice() true");
                                result = RESULT_BACKTRACK;
                            } else {
                                //System.out.println("rejectChoice() false");
                                result = RESULT_FAILURE;
                            }
                        }
                        break;
                    }
                    case MKInstruction.MKIN_FCINST: {
                        // specification is a variable, denoting a type
                        // or a named pattern. different from other forms
                        // in that there are no expectations about the length
                        // of the pattern to be matched.
                        String type = (String)(sinst.getArgument());
                        //if (isMatchablePrefix(type)) {
                        if (matchPrefix(type, stack, iend, output, errors)) {
                                // matched a submachine, so continue
                            if (DEBUG_LANGSPEC_MACHINE) {
                                System.out.println("matched machine "+type+", sPC="+sPC+", iPC="+iPC);
                            }
                            /*                            } else {
                                // match failed. want to reject choice and either backtrack or fail.
                                // when done on its own, failure is screwing up the machine.
                                if (DEBUG_LANGSPEC_MACHINE) {
                                    System.out.println("failed to match machine "+type+", sPC="+sPC+", iPC="+iPC);
                                }
                                }*/
                        } else if (!acceptUntypedInput(type, stack, iend, null, output, errors)) {
                            if (rejectChoice()) { // backtrack or fail
                                result = RESULT_BACKTRACK;
                            } else {
                                result = RESULT_FAILURE;
                            }
                        }
                        break;
                    }
                    case MKInstruction.MKIN_FVNAME: {
                        // specification is a type to be subsequently matched
                        String specType = (String)(sinst.getArgument());
                        typeToCheck = symbols.getSymbol(specType);
                        if (typeToCheck == null) {
                            throwMachineError(iinst,
                                              "Pattern machine type '"+
                                              specType+"' not found",
                                              errors);
                            result = RESULT_FAILURE;
                        } else {
                            MKType type = (MKType)(typeToCheck.getType());
                            if (DEBUG_LANGSPEC_MACHINE) {
                                System.out.println("type is "+type);
                            }
                            if (!type.equals(MKType.typeType)) {
                                throwMachineError(iinst,
                                                  "Bad pattern machine typecheck",
                                                  errors);
                                result = RESULT_FAILURE;
                            }
                        }
                        break;
                    }
                    case MKInstruction.MKIN_FMINST: {
                        // specification is a variable, yielding a type.
                        // this type must be matched against passed type.
                        String var = (String)(sinst.getArgument());
                        if (!acceptTypedInput(var, stack, iend, output, errors)) {
                            if (rejectChoice()) { // backtrack or fail
                                result = RESULT_BACKTRACK;
                            } else {
                                result = RESULT_FAILURE;
                            }
                        }
                        break;
                    }
                    case MKInstruction.MKIN_FONAME:
                        // load operator name
                        opName = (String)(sinst.getArgument());
                        break;
                    case MKInstruction.MKIN_OP:
                        // execute operator: run a subordinate engine
                        // after first verifying that the essential
                        // shell is there for the type.
                        in_op = false;
                        if (!executeOperator(stack, 
                                             (startSymbol+"."+choiceId),
                                             iend,
                                             output,
                                             errors)) {
                            if (rejectChoice()) { // backtrack or fail
                                result = RESULT_BACKTRACK;
                            } else {
                                result = RESULT_FAILURE;
                            }
                        }
                        break;
                    case MKInstruction.MKIN_FMNAME:
                    case MKInstruction.MKIN_FVINST:
                    case MKInstruction.MKIN_PUSH:
                    case MKInstruction.MKIN_METHOD:
                    case MKInstruction.MKIN_LIST:
                    case MKInstruction.MKIN_LISTELM:
                    case MKInstruction.MKIN_ENDLIST:
                    case MKInstruction.MKIN_HALT:
                    case MKInstruction.MKIN_ESTMT:
                    case MKInstruction.MKIN_ESTBOT:
                    case MKInstruction.MKIN_ESTTOP:
                        // none of these instruction types are expected
                        // given the current language specification syntax.
                    default:
                        throwMachineError(iinst,
                                          "Bad pattern machine instruction",
                                          errors);
                        result = RESULT_FAILURE;
                }
            }
            if (result == RESULT_FAILURE) {
                // report failure
                if (DEBUG_LANGSPEC_MACHINE) {
                    System.out.println("RESULT_FAILURE");
                }
                // if we failed, don't we need to return?
                returnFromChoice();
                stack.reset(); // throw away the contents of the stack
                if (isPrefix) prefixDepth--;
                return inputType;
            } else if (result == RESULT_BACKTRACK) {
                if (DEBUG_LANGSPEC_MACHINE) {
                    System.out.println("RESULT_BACKTRACK");
                }
                stack.reset();
                opcodeMark = localOpcodeMark;
                clearPendingOutputToMark();
            } else if ((result == RESULT_SUCCESS) && (isPrefix)) {
                if (DEBUG_LANGSPEC_MACHINE) {
                    System.out.println("RESULT_SUCCESS WHEN PREFIX");
                }
                // Don't actually output anything, because we've found
                // a prefix rather than an entire statement. The machine
                // could still backtrack at a higher level.
                if (isPrefix) prefixDepth--;
                return inputType;
                //break;
            }
        }
        if (DEBUG_LANGSPEC_MACHINE) {
            System.out.println("RESULT_SUCCESS");
        }
        if (prefixDepth==0) {
            emitStatement(output);
        }
        if (isPrefix) prefixDepth--;
        return inputType;
    }

    private void throwMachineError(MKInstruction inst,
                                   String message,
                                   Vector errors)
    {
        if (inst.getLexicalHook() != null) {
            ParseException pe =
                new ParseException(message, inst.getLexicalHook());
            errors.add(pe);
        } else {
            System.out.println("lexicalHook null in throwMachineError("+message+":"+inst);
        }
    }
}
