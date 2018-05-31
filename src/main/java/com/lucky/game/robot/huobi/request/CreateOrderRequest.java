package com.lucky.game.robot.huobi.request;

import lombok.Data;

@Data
public class CreateOrderRequest {
    public  interface OrderType {
        /**
         * 限价买入
         */
        String BUY_LIMIT = "buy-limit";
        /**
         * 限价卖出
         */
        String SELL_LIMIT = "sell-limit";
        /**
         * 市价买入
         */
        String BUY_MARKET = "buy-market";
        /**
         * 市价卖出
         */
        String SELL_MARKET = "sell-market";
    }

    /**
     * 交易对，必填，例如："ethcny"，
     */
    private String symbol;

    /**
     * 账户ID，必填，例如："12345"
     */
    private String accountId;

    /**
     * 当订单类型为buy-limit,sell-limit时，表示订单数量， 当订单类型为buy-market时，表示订单总金额， 当订单类型为sell-market时，表示订单总数量
     */
    private String amount;

    /**
     * 订单价格，仅针对限价单有效，例如："1234.56"
     */
    private String price = "0.0";

    /**
     * 订单类型，取值范围"buy-market,sell-market,buy-limit,sell-limit"
     */
    private String type;

    /**
     * 订单来源，例如："api"
     */
    private String source = "api";
}
