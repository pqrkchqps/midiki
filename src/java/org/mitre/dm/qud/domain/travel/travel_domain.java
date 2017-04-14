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
package org.mitre.dm.qud.domain.travel;

import java.util.*;
import java.util.logging.*;

import org.mitre.midiki.logic.*;
import org.mitre.midiki.state.*;
import org.mitre.dm.qud.domain.*;

/**
 * Concrete implementation of the travel domain.
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 * @see DomainCell
 */
public class travel_domain extends DomainCell
{
    private static Logger logger =
        Logger.getLogger("org.mitre.dm.qud.domain.travel.travel_domain");
    public travel_domain()
    {
        super();
    }

    protected void initializeAttributes()
    {
        ArrayList val_361 = new ArrayList();
        ArrayList val_363 = new ArrayList();
        val_363.add(("malmoe"));
        val_363.add(("sweden"));
        val_361.add(new Predicate("in", val_363));
        ArrayList val_378 = new ArrayList();
        val_378.add(("gothenburg"));
        val_378.add(("sweden"));
        val_361.add(new Predicate("in", val_378));
        ArrayList val_393 = new ArrayList();
        val_393.add(("washington"));
        val_393.add(("usa"));
        val_361.add(new Predicate("in", val_393));
        ArrayList val_408 = new ArrayList();
        val_408.add(("paris"));
        val_408.add(("france"));
        val_361.add(new Predicate("in", val_408));
        ArrayList val_423 = new ArrayList();
        val_423.add(("london"));
        val_423.add(("great_britain"));
        val_361.add(new Predicate("in", val_423));
        ArrayList val_438 = new ArrayList();
        val_438.add(("seattle"));
        val_438.add(("usa"));
        val_361.add(new Predicate("in", val_438));
        attributeMap.put("in", val_361);
        ArrayList val_458 = new ArrayList();
        val_458.add(("malmoe"));
        val_458.add(("paris"));
        val_458.add(("london"));
        val_458.add(("gothenburg"));
        val_458.add(("washington"));
        val_458.add(("seattle"));
        attributeMap.put("location", val_458);
        ArrayList val_489 = new ArrayList();
        val_489.add(("thailand"));
        val_489.add(("usa"));
        val_489.add(("sweden"));
        val_489.add(("france"));
        val_489.add(("great_britain"));
        attributeMap.put("country", val_489);
        ArrayList val_516 = new ArrayList();
        val_516.add(("plane"));
        val_516.add(("boat"));
        val_516.add(("train"));
        attributeMap.put("means_of_transport", val_516);
        ArrayList val_535 = new ArrayList();
        val_535.add(("january"));
        val_535.add(("february"));
        val_535.add(("march"));
        val_535.add(("april"));
        val_535.add(("may"));
        val_535.add(("june"));
        val_535.add(("july"));
        val_535.add(("august"));
        val_535.add(("september"));
        val_535.add(("october"));
        val_535.add(("november"));
        val_535.add(("december"));
        attributeMap.put("month", val_535);
        ArrayList val_590 = new ArrayList();
        val_590.add(("1"));
        val_590.add(("2"));
        val_590.add(("3"));
        val_590.add(("4"));
        val_590.add(("5"));
        val_590.add(("6"));
        val_590.add(("7"));
        val_590.add(("8"));
        val_590.add(("9"));
        val_590.add(("10"));
        val_590.add(("11"));
        val_590.add(("12"));
        val_590.add(("13"));
        val_590.add(("14"));
        val_590.add(("15"));
        val_590.add(("16"));
        val_590.add(("17"));
        val_590.add(("18"));
        val_590.add(("19"));
        val_590.add(("20"));
        val_590.add(("21"));
        val_590.add(("22"));
        val_590.add(("23"));
        val_590.add(("24"));
        val_590.add(("25"));
        val_590.add(("26"));
        val_590.add(("27"));
        val_590.add(("28"));
        val_590.add(("29"));
        val_590.add(("30"));
        val_590.add(("31"));
        attributeMap.put("day", val_590);
        ArrayList val_721 = new ArrayList();
        val_721.add(("economy"));
        val_721.add(("business"));
        attributeMap.put("class", val_721);
        ArrayList val_736 = new ArrayList();
        val_736.add(("yes"));
        val_736.add(("no"));
        attributeMap.put("yesno", val_736);
        ArrayList val_751 = new ArrayList();
        val_751.add(("1234567890"));
        attributeMap.put("account", val_751);

        initializedAttributes = true;
    }
    protected void initializeTasks()
    {
        defaultTask = ("price_info");
        Contract priceInfo = PriceInfoCell.getContract();
        Contract orderTrip = OrderTripCell.getContract();
        Contract top = Tasks.taskTop();
        Plan priceInfoPlan = new Plan(priceInfo);
        Plan orderTripPlan = new Plan(orderTrip);
        Plan topPlan = new Plan(top);

        priceInfoPlan.addFindout("how");
        priceInfoPlan.addFindout("to");
        priceInfoPlan.addFindout("from");
        priceInfoPlan.addFindout("month");
        priceInfoPlan.addFindout("class");
        priceInfoPlan.addFindout("return");
        Plan returnYes = new Plan(priceInfo);
        returnYes.addFindout("ret_month");
        priceInfoPlan.addIfThen("return","yes",returnYes);
        priceInfoPlan.addQueryCall("price_info","check_price");
        priceInfoPlan.addInform("price");
        priceInfoPlan.addFindout("order_trip");
        Plan orderTripYes = new Plan(priceInfo);
        orderTripYes.addExec(orderTrip);
        priceInfoPlan.addIfThen("order_trip","yes",orderTripYes);
        priceInfoPlan.addMove("thank",true);
        priceInfoPlan.addMove("forget",true);
        priceInfoPlan.addExec(top);

        orderTripPlan.addFindout("how");
        orderTripPlan.addFindout("to");
        orderTripPlan.addFindout("from");
        orderTripPlan.addFindout("month");
        orderTripPlan.addFindout("class");
        orderTripPlan.addFindout("return");
        Plan otReturnYes = new Plan(orderTrip);
        otReturnYes.addFindout("ret_month");
        orderTripPlan.addIfThen("return","yes",otReturnYes);
        orderTripPlan.addQueryCall("order_trip","check_price");
        orderTripPlan.addInform("price");
        orderTripPlan.addFindout("account");
        orderTripPlan.addMethodCall("order_trip","book_trip");
        orderTripPlan.addInform("booked");
        orderTripPlan.addMove("thank",true);
        orderTripPlan.addMove("forget",true);
        orderTripPlan.addExec(top);

        topPlan.addMove("greet",true);
        //topPlan.addRaise("task");
        topPlan.addFindout("task");
        LinkedList whichTasks = new LinkedList();
        whichTasks.add(priceInfo);
        whichTasks.add(orderTrip);
        topPlan.addFindoutWhichTask(whichTasks);

        taskMap.put("order_trip", orderTripPlan);
        keyMap.put("order_trip", ("order_trip"));
        taskMap.put("price_info", priceInfoPlan);
        keyMap.put("price_info", ("price_info"));
        taskMap.put("top", topPlan);
        keyMap.put("top", ("top"));

        if (!initializedQuestions) initializeQuestions();
        calculateTaskDominationAndRelevance();
        initializedTasks = true;
    }
    protected void initializeQuestions()
    {
        defaultQuestion = ("to");
        questionTypes.put("to","location");
        questionTypes.put("from","location");
        questionTypes.put("how","means_of_transport");
        questionTypes.put("day","day");
        questionTypes.put("month","month");
        questionTypes.put("ret_month","month");
        questionTypes.put("return","yesno");
        questionTypes.put("class","class");
        questionTypes.put("country","country");
        questionTypes.put("account","account");
        questionTypes.put("order_trip","yesno");

        initializedQuestions = true;
    }
    /*
     * Local variables
     */
        protected ArrayList vect_22;
        protected Object move_20;
        protected Object move_23;
        protected Object taskop_18;
        protected ArrayList vect_36;
        protected Object move_34;
        protected Object move_37;
        protected ArrayList vect_46;
        protected Object move_44;
        protected Object move_47;
        protected Object taskop_30;
        protected ArrayList list_30;        protected ArrayList vect_66;
        protected Object move_64;
        protected Object move_67;
        protected Object taskop_62;
        protected ArrayList vect_78;
        protected Object move_76;
        protected Object move_79;
        protected Object taskop_74;
        protected ArrayList vect_90;
        protected Object move_88;
        protected Object move_91;
        protected Object taskop_86;
        protected ArrayList vect_102;
        protected Object move_100;
        protected Object move_103;
        protected Object taskop_98;
        protected ArrayList vect_114;
        protected Object move_112;
        protected Object move_115;
        protected Object taskop_110;
        protected ArrayList vect_126;
        protected Object move_124;
        protected Object move_127;
        protected Object taskop_122;
        protected Object move_136;
        protected ArrayList vect_144;
        protected Object move_142;
        protected Object move_145;
        protected Object taskop_140;
        protected Object taskop_134;
        protected ArrayList vect_155;
        protected Object move_153;
        protected Object move_156;
        protected Object taskop_153;
        protected ArrayList vect_164;
        protected Object move_162;
        protected Object move_165;
        protected Object taskop_162;
        protected ArrayList vect_175;
        protected Object move_173;
        protected Object move_176;
        protected Object taskop_171;
        protected ArrayList vect_187;
        protected Object move_185;
        protected Object move_188;
        protected Object move_194;
        protected Object taskop_194;
        protected Object taskop_183;
        protected Object taskop_199;
        protected Object move_202;
        protected Object taskop_202;
        protected ArrayList vect_217;
        protected Object move_215;
        protected Object move_218;
        protected Object taskop_213;
        protected ArrayList vect_229;
        protected Object move_227;
        protected Object move_230;
        protected Object taskop_225;
        protected ArrayList vect_241;
        protected Object move_239;
        protected Object move_242;
        protected Object taskop_237;
        protected ArrayList vect_253;
        protected Object move_251;
        protected Object move_254;
        protected Object taskop_249;
        protected ArrayList vect_265;
        protected Object move_263;
        protected Object move_266;
        protected Object taskop_261;
        protected ArrayList vect_277;
        protected Object move_275;
        protected Object move_278;
        protected Object taskop_273;
        protected Object move_287;
        protected ArrayList vect_295;
        protected Object move_293;
        protected Object move_296;
        protected Object taskop_291;
        protected Object taskop_285;
        protected ArrayList vect_306;
        protected Object move_304;
        protected Object move_307;
        protected Object taskop_304;
        protected ArrayList vect_315;
        protected Object move_313;
        protected Object move_316;
        protected Object taskop_313;
        protected ArrayList vect_326;
        protected Object move_324;
        protected Object move_327;
        protected Object taskop_322;
        protected Object move_336;
        protected Object taskop_334;
        protected Object taskop_341;

}
