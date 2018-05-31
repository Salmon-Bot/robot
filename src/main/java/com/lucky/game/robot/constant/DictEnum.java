package com.lucky.game.robot.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * conan
 * 2017/10/10 16:33
 **/
public enum DictEnum {


    MARKET_PERIOD_1MIN("1min", "一分钟"),
    MARKET_PERIOD_5MIN("5min", "五分钟"),
    MARKET_PERIOD_15MIN("15min", "十五分钟"),
    MARKET_PERIOD_30MIN("30min", "三十分钟"),
    MARKET_PERIOD_60MIN("60min", "六十分钟"),
    MARKET_PERIOD_1DAY("1day", "一天"),
    MARKET_PERIOD_1WEEK("1week", "一周"),
    MARKET_PERIOD_1MON("1mon", "一月"),
    MARKET_PERIOD_1YEAR("1year", "一年"),


    //火币基础对
    HB_MARKET_BASE_BTC("btc", "btc"),
    HB_MARKET_BASE_ETH("eth", "eth"),
    HB_MARKET_BASE_USDT("usdt", "usdt"),


    //主流
    MARKET_HUOBI_SYMBOL_BTC_USDT("btcusdt", "btc/usdt行情"),
    MARKET_HUOBI_SYMBOL_BTC_ETH("ethbtc", "btc/eth行情"),
    MARKET_HUOBI_SYMBOL_BTC_LTC("ltcbtc", "btc/ltc行情"),
    MARKET_HUOBI_SYMBOL_BTC_ETC("etcbtc", "btc/etc市场"),
    MARKET_HUOBI_SYMBOL_BTC_EOS("eosbtc", "btc/eos市场"),
    MARKET_HUOBI_SYMBOL_BTC_OMG("omgbtc", "btc/omg市场"),
    MARKET_HUOBI_SYMBOL_BTC_DASH("dashbtc", "btc/dash市场"),
    MARKET_HUOBI_SYMBOL_BTC_XRP("xrpbtc", "btc/xrp市场"),
    MARKET_HUOBI_SYMBOL_BTC_ZEC("zecbtc", "btc/zec市场"),

    MARKET_HUOBI_SYMBOL_ETH_USDT("ethusdt", "eth/usdt市场"),
    //创新
    MARKET_HUOBI_SYMBOL_BTC_BLZ("blzbtc", "btc/blz市场"),
    MARKET_HUOBI_SYMBOL_BTC_EDU("edubtc", "btc/edu市场"),
    MARKET_HUOBI_SYMBOL_BTC_ENG("engbtc", "btc/eng市场"),
    MARKET_HUOBI_SYMBOL_BTC_WPR("wprbtc", "btc/wpr市场"),
    MARKET_HUOBI_SYMBOL_BTC_MTX("mtxbtc", "btc/mtx市场"),
    MARKET_HUOBI_SYMBOL_BTC_MTN("mtnbtc", "btc/mtn市场"),
    MARKET_HUOBI_SYMBOL_BTC_SNC("sncbtc", "btc/snc市场"),
    MARKET_HUOBI_SYMBOL_BTC_LSK("lskbtc", "btc/lsk市场"),
    MARKET_HUOBI_SYMBOL_BTC_STK("stkbtc", "btc/stk市场"),
    MARKET_HUOBI_SYMBOL_BTC_HT("htbtc", "btc/ht市场"),
    MARKET_HUOBI_SYMBOL_BTC_ELA("elabtc", "btc/ela市场"),
    MARKET_HUOBI_SYMBOL_BTC_SRN("srnbtc", "btc/srn市场"),
    MARKET_HUOBI_SYMBOL_BTC_ZLA("zlabtc", "btc/zla市场"),
    MARKET_HUOBI_SYMBOL_BTC_TRX("trxbtc", "btc/trx市场"),
    MARKET_HUOBI_SYMBOL_BTC_OCN("ocnbtc", "btc/ocn市场"),
    MARKET_HUOBI_SYMBOL_BTC_LUN("lunbtc", "btc/lun市场"),
    MARKET_HUOBI_SYMBOL_BTC_IOST("iostbtc", "btc/iost市场"),
    MARKET_HUOBI_SYMBOL_BTC_HSR("hsrbtc", "btc/hsr市场"),
    MARKET_HUOBI_SYMBOL_BTC_SMT("smtbtc", "btc/smt市场"),
    MARKET_HUOBI_SYMBOL_BTC_LET("letbtc", "btc/let市场"),
    MARKET_HUOBI_SYMBOL_BTC_SWFTC("swftcbtc", "btc/swftc市场"),
    MARKET_HUOBI_SYMBOL_BTC_WAX("waxbtc", "btc/wax市场"),
    MARKET_HUOBI_SYMBOL_BTC_ELF("elfbtc", "btc/elf市场"),
    MARKET_HUOBI_SYMBOL_BTC_MDS("mdsbtc", "btc/mds市场"),
    MARKET_HUOBI_SYMBOL_BTC_TNB("tnbbtc", "btc/tnb市场"),
    MARKET_HUOBI_SYMBOL_BTC_NAS("nasbtc", "btc/nas市场"),
    MARKET_HUOBI_SYMBOL_BTC_BTM("btmbtc", "btc/btm市场"),
    MARKET_HUOBI_SYMBOL_BTC_ITC("itcbtc", "btc/itc市场"),
    MARKET_HUOBI_SYMBOL_BTC_THETA("thetabtc", "btc/theta市场"),
    MARKET_HUOBI_SYMBOL_BTC_WICC("wiccbtc", "btc/wicc市场"),
    MARKET_HUOBI_SYMBOL_BTC_GNX("gnxbtc", "btc/gnx市场"),
    MARKET_HUOBI_SYMBOL_BTC_VEN("venbtc", "btc/ven市场"),
    MARKET_HUOBI_SYMBOL_BTC_DTA("dtabtc", "btc/dta市场"),
    MARKET_HUOBI_SYMBOL_BTC_MANA("manabtc", "btc/mana市场"),
    MARKET_HUOBI_SYMBOL_BTC_QASH("qashbtc", "btc/qash市场"),
    MARKET_HUOBI_SYMBOL_BTC_PROPY("propybtc", "btc/propy市场"),
    MARKET_HUOBI_SYMBOL_BTC_SNT("sntbtc", "btc/snt市场"),
    MARKET_HUOBI_SYMBOL_BTC_QUN("qunbtc", "btc/qun市场"),
    MARKET_HUOBI_SYMBOL_BTC_QTUM("qtumbtc", "btc/qtum市场"),
    MARKET_HUOBI_SYMBOL_BTC_DAT("datbtc", "btc/snt市场"),
    MARKET_HUOBI_SYMBOL_BTC_TNT("tntbtc", "btc/tnt市场"),
    MARKET_HUOBI_SYMBOL_BTC_CMT("cmtbtc", "btc/cmt市场"),
    MARKET_HUOBI_SYMBOL_BTC_YEE("yeebtc", "btc/yee市场"),
    MARKET_HUOBI_SYMBOL_BTC_GAS("cmtbtc", "btc/gas市场"),
    MARKET_HUOBI_SYMBOL_BTC_AIDOC("aidocbtc", "btc/aidoc市场"),
    MARKET_HUOBI_SYMBOL_BTC_STORJ("storjbtc", "btc/storj市场"),
    MARKET_HUOBI_SYMBOL_BTC_XEM("xembtc", "btc/xem市场"),
    MARKET_HUOBI_SYMBOL_BTC_PAY("paybtc", "btc/pay市场"),
    MARKET_HUOBI_SYMBOL_BTC_NEO("neobtc", "btc/neo市场"),
    MARKET_HUOBI_SYMBOL_BTC_CVC("cvcbtc", "btc/cvc市场"),
    MARKET_HUOBI_SYMBOL_BTC_QSP("qspbtc", "btc/qsp市场"),
    MARKET_HUOBI_SYMBOL_BTC_TOPC("topcbtc", "btc/topc市场"),
    MARKET_HUOBI_SYMBOL_BTC_RCN("rcnbtc", "btc/rcn市场"),
    MARKET_HUOBI_SYMBOL_BTC_AST("astbtc", "btc/ast市场"),
    MARKET_HUOBI_SYMBOL_BTC_BAT("batbtc", "btc/bat市场"),
    MARKET_HUOBI_SYMBOL_BTC_DBC("dbcbtc", "btc/dbc市场"),
    MARKET_HUOBI_SYMBOL_BTC_RPX("rpxbtc", "btc/rpx市场"),
    MARKET_HUOBI_SYMBOL_BTC_ACT("actbtc", "btc/act市场"),
    MARKET_HUOBI_SYMBOL_BTC_ICX("rcnbtc", "btc/rcn市场"),
    MARKET_HUOBI_SYMBOL_BTC_KNC("kncbtc", "btc/knc市场"),
    MARKET_HUOBI_SYMBOL_BTC_MCO("mcobtc", "btc/mco市场"),
    MARKET_HUOBI_SYMBOL_BTC_ZRX("zrxbtc", "btc/zrx市场"),
    MARKET_HUOBI_SYMBOL_BTC_MTL("mtlbtc", "btc/mtl市场"),
    MARKET_HUOBI_SYMBOL_BTC_GNT("gntbtc", "btc/gnt市场"),
    MARKET_HUOBI_SYMBOL_BTC_REQ("reqbtc", "btc/req市场"),
    MARKET_HUOBI_SYMBOL_BTC_RDN("rdnbtc", "btc/rdn市场"),
    MARKET_HUOBI_SYMBOL_BTC_SALT("saltbtc", "btc/salt市场"),
    MARKET_HUOBI_SYMBOL_BTC_MEE("meebtc", "btc/mee市场"),
    MARKET_HUOBI_SYMBOL_BTC_ZIL("zilbtc", "btc/zil市场"),
    MARKET_HUOBI_SYMBOL_BTC_CHAT("chatbtc", "btc/chat市场"),
    MARKET_HUOBI_SYMBOL_BTC_POWR("powrbtc", "btc/powr市场"),
    MARKET_HUOBI_SYMBOL_BTC_DGD("dgdbtc", "btc/dgd市场"),
    MARKET_HUOBI_SYMBOL_BTC_APPC("appcbtc", "btc/appc市场"),
    MARKET_HUOBI_SYMBOL_BTC_OST("ostbtc", "btc/ost市场"),
    MARKET_HUOBI_SYMBOL_BTC_SOC("socbtc", "btc/soc市场"),
    MARKET_HUOBI_SYMBOL_BTC_EKO("ekobtc", "btc/eko市场"),
    MARKET_HUOBI_SYMBOL_BTC_LINK("linkbtc", "btc/link市场"),
    MARKET_HUOBI_SYMBOL_BTC_UTK("utkbtc", "btc/utk市场"),
    MARKET_HUOBI_SYMBOL_BTC_EVX("evxbtc", "btc/evx市场"),
    MARKET_HUOBI_SYMBOL_BTC_ADX("adxbtc", "btc/adx市场"),
    MARKET_HUOBI_SYMBOL_BTC_BCD("bcdbtc", "btc/bcd市场"),
    MARKET_HUOBI_SYMBOL_BTC_BCX("bcxbtc", "btc/bcx市场"),


    /**
     * hb订单状态
     */
    ORDER_DETAIL_STATE_PRE_SUBMITTED("pre-submitted", "准备提交"),
    ORDER_DETAIL_STATE_SUBMITTING("submitting", "已提交"),
    ORDER_DETAIL_STATE_SUBMITTED("submitted", "已提交"),
    ORDER_DETAIL_STATE_PARTIAL_FILLED("partial-filled", "部分成交"),
    ORDER_DETAIL_STATE_PARTIAL_CANCELED("partial-canceled", "部分成交撤销"),
    ORDER_DETAIL_STATE_FILLED("filled", "完全成交"),
    ORDER_DETAIL_STATE_CANCELED("canceled", "已撤销"),
    ORDER_DETAIL_STATE_SELL("sell", "买单已挂单售卖"),
    ORDER_DETAIL_STATE_BUY("buy", "卖单已挂单购买"),

    ZB_ORDER_DETAIL_STATE_0("0","待成交"),
    ZB_ORDER_DETAIL_STATE_1("1","取消"),
    ZB_ORDER_DETAIL_STATE_2("2","交易完成"),
    ZB_ORDER_DETAIL_STATE_3("3","待成交"),
    ZB_ORDER_DETAIL_STATE_4("4","已挂单售卖"),


    ORDER_TYPE_BUY_LIMIT("buy-limit", "限价买单"),
    ORDER_TYPE_SELL_LIMIT("sell-limit", "限价卖单"),

    ORDER_MODEL_REAL("real","实时"),
    ORDER_MODEL_LIMIT("limit","限价"),
    ORDER_MODEL_LIMIT_BETA("limitBeta","限价单beta策略"),
    ORDER_MODEL_LIMIT_GAMMA("limitGamma","限价单gamma策略"),
    ORDER_MODEL_LIMIT_DELTE("limitDelte","限价单delte策略"),
    ORDER_MODEL_SHUFFLE("shuffle","搬砖"),

    TRADE_CONFIG_THRESHOLD_TYPE_ONE_MIN("oneMin","1分钟"),
    TRADE_CONFIG_THRESHOLD_TYPE_FIVE_MIN("fiveMin","5分钟"),

    USER_STATUS_NORMAL("normal","正常"),
    USER_STATUS_FREEZE("freeze","冻结"),


    //中币基础对
    ZB_MARKET_BASE_BTC("btc", "btc"),
    ZB_MARKET_BASE_QC("qc", "qc"),
    ZB_MARKET_BASE_USDT("usdt", "usdt"),

    MARKET_TYPE_HB("hb","火币"),
    MARKET_TYPE_ZB("zb","中币"),

    ZB_ORDER_TRADE_TYPE_SELL("0","卖"),
    ZB_ORDER_TRADE_TYPE_BUY("1","买"),

    ZB_CURRENCY_KEY_PRICE("currency_price_","价格小数位key值"),
    ZB_CURRENCY__KEY_AMOUNT("currency_amount_","数量小数位key值"),

    IS_DELETE_NO("no","未删除"),
    IS_DELETE_YES("yes","已删除"),

    STATUS_OPEN("open","启用"),
    STATUS_STOP("stop","停用"),

    IS_USER_YES("yes","使用"),
    IS_USER_NO("no","不使用"),


    IS_FINISH_YES("yes","已完成"),
    IS_FINISH_NO("no","未完成"),


    OKEX_MARKET_PERIOD_1MIN("1min", "一分钟"),
    OKEX_MARKET_PERIOD_3MIN("3min", "三分钟"),
    OKEX_MARKET_PERIOD_5MIN("5min", "五分钟"),
    OKEX_MARKET_PERIOD_15MIN("15min", "十五分钟"),
    OKEX_MARKET_PERIOD_30MIN("30min", "三十分钟"),
    OKEX_MARKET_PERIOD_1H("1hour", "一小时"),
    OKEX_MARKET_PERIOD_2H("2hour", "两小时"),
    OKEX_MARKET_PERIOD_4H("4hour", "四小时"),
    OKEX_MARKET_PERIOD_6H("6hour", "六小时"),
    OKEX_MARKET_PERIOD_12H("12hour", "十二小时"),
    OKEX_MARKET_PERIOD_1D("1day", "一天"),
    OKEX_MARKET_PERIOD_3D("3day", "三天"),
    OKEX_MARKET_PERIOD_1W("1week", "一周"),
    ;

    public static List<String> filledOrderStates = new ArrayList<>();

    static {
        filledOrderStates.add(ORDER_DETAIL_STATE_PARTIAL_CANCELED.getCode());
        filledOrderStates.add(ORDER_DETAIL_STATE_FILLED.getCode());
        filledOrderStates.add(ORDER_DETAIL_STATE_CANCELED.getCode());

    }

    public static List<DictEnum> huobiSymbol = new ArrayList<>();

    static {
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_USDT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ETH);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_LTC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ETC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_EOS);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_OMG);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_DASH);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_XRP);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ZEC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_BLZ);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ENG);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_EDU);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_WPR);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_MTX);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_MTN);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_SNC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_LSK);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_STK);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_HT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ELA);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_SRN);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ZLA);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_TRX);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_OCN);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_LUN);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_IOST);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_HSR);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_SMT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_LET);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_SWFTC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_WAX);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ELF);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_MDS);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_TNB);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_NAS);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_BTM);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ITC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_THETA);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_WICC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_GNX);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_VEN);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_DTA);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_MANA);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_QASH);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_PROPY);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_SNT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_QUN);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_QTUM);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_DAT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_TNT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_CMT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_YEE);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_GAS);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_AIDOC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_STORJ);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_XEM);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_PAY);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_NEO);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_CVC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_QSP);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_TOPC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_RCN);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_AST);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_BAT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_DBC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_RPX);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ACT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ICX);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_KNC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_MCO);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ZRX);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_MTL);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_GNT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_REQ);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_RDN);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_SALT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_MEE);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ZIL);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_CHAT);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_POWR);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_DGD);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_APPC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_OST);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_SOC);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_EKO);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_LINK);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_UTK);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_EVX);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_ADX);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_BCD);
        huobiSymbol.add(MARKET_HUOBI_SYMBOL_BTC_BCX);
    }

    private String code;

    private String value;

    DictEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }


    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
