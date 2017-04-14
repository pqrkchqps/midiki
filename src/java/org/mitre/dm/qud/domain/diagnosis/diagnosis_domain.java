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
package org.mitre.dm.qud.domain.diagnosis;

import java.util.*;
import java.util.logging.*;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.state.*;
import org.mitre.midiki.impl.mitre.*;
import org.mitre.dm.qud.domain.*;

/**
 * Concrete implementation of the diagnosis domain.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see DomainCell
 */
public class diagnosis_domain extends DomainCell
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.domain.diagnosis.diagnosis_domain");
    public diagnosis_domain()
    {
        super();
    }

    protected void initializeAttributes()
    {
        ArrayList val = new ArrayList();
        val.add("yes");
        val.add("no");
        attributeMap.put("yesno", val);

        initializedAttributes = true;
    }
    protected static Contract taskTop()
    {
        ContractImpl top = new ContractImpl("top");
        top.addAttribute(new AttributeImpl("query", java.util.Collection.class, null));
        top.addAttribute(new AttributeImpl("headache", "yesno", null));
        top.addAttribute(new AttributeImpl("fever", "yesno", null));
        top.addAttribute(new AttributeImpl("oconus", "yesno", null));
        top.addAttribute(new AttributeImpl("malarial", "yesno", null));
        top.addAttribute(new AttributeImpl("swimfresh", "yesno", null));
        top.addAttribute(new AttributeImpl("hematouria", "yesno", null));
        top.addAttribute(new AttributeImpl("dust", "yesno", null));
        top.addAttribute(new AttributeImpl("disease", "disease", null));
        return top;
    }
    protected void initializeTasks()
    {
        defaultTask = ("top");
        Contract top = taskTop();
        Plan topPlan = new Plan(top);

        topPlan.addFindout("query");
        topPlan.addInform("query");
        topPlan.addFindout("headache");
        Plan headacheYes = new Plan(top);
        Plan headacheNo = new Plan(top);
        topPlan.addIfThenElse("headache","yes",headacheYes,headacheNo);

        headacheYes.addFindout("fever");
        Plan feverYes = new Plan(top);
        Plan feverNo = new Plan(top);
        headacheYes.addIfThenElse("fever","yes",feverYes,feverNo);

        feverYes.addFindout("oconus");
        Plan oconusYes = new Plan(top);
        Plan oconusNo = new Plan(top);
        feverYes.addIfThenElse("oconus","yes",oconusYes,oconusNo);

        oconusYes.addFindout("malarial");
        Plan malarialYes = new Plan(top);
        Plan malarialNo = new Plan(top);
        oconusYes.addIfThenElse("malarial","yes",malarialYes,malarialNo);

        malarialYes.addAssert("disease","malaria");

        malarialNo.addFindout("swimfresh");
        Plan swimfreshYes = new Plan(top);
        Plan swimfreshNo = new Plan(top);
        malarialNo.addIfThenElse("swimfresh","yes",swimfreshYes,swimfreshNo);

        swimfreshYes.addFindout("hematouria");
        Plan hematouriaYes = new Plan(top);
        Plan hematouriaNo = new Plan(top);
        swimfreshYes.addIfThenElse("hematouria","yes",hematouriaYes,hematouriaNo);

        hematouriaYes.addAssert("disease","schistos");

        hematouriaNo.addAssert("disease","malaria");

        swimfreshNo.addFindout("dust");
        Plan dustYes = new Plan(top);
        Plan dustNo = new Plan(top);
        swimfreshNo.addIfThenElse("dust","yes",dustYes,dustNo);

        dustYes.addAssert("disease","coccid");

        dustNo.addAssert("disease","malaria");

        oconusNo.addFindout("dust");
        Plan dustYes2 = new Plan(top);
        Plan dustNo2 = new Plan(top);
        oconusNo.addIfThenElse("dust","yes",dustYes2,dustNo2);

        dustYes2.addAssert("disease","coccid");

        dustNo2.addAssert("disease","flu");

        feverNo.addAssert("disease","allergies");

        headacheNo.addAssert("disease","none");

        topPlan.addInform("disease");
        topPlan.addMove("forget",true);
        topPlan.addMove("greet",true);
        topPlan.addExec(top);

        taskMap.put("top", topPlan);
        keyMap.put("top", ("top"));

        if (!initializedQuestions) initializeQuestions();
        calculateTaskDominationAndRelevance();
        initializedTasks = true;
    }
    protected void initializeQuestions()
    {
        super.initializeQuestions();
        defaultQuestion = ("symptom");
        questionTypes.put("headache","yesno");
        questionTypes.put("fever","yesno");
        questionTypes.put("oconus","yesno");
        questionTypes.put("malarial","yesno");
        questionTypes.put("swimfresh","yesno");
        questionTypes.put("hematouria","yesno");
        questionTypes.put("dust","yesno");

        initializedQuestions = true;
    }
}
