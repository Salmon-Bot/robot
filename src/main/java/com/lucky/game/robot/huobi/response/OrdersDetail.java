package com.lucky.game.robot.huobi.response;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 18:22
 */
@Data
public class OrdersDetail implements Serializable{

    /**
     * id : 59378
     * symbol : ethusdt
     * account-id : 100009
     * amount : 10.1000000000
     * price : 100.1000000000
     * created-at : 1494901162595
     * type : buy-limit
     * field-amount : 10.1000000000
     * field-cash-amount : 1011.0100000000
     * field-fees : 0.0202000000
     * finished-at : 1494901400468
     * user-id : 1000
     * source : api
     * state : filled
     * canceled-at : 0
     * exchange : huobi
     * batch :
     */

    private String id;

    private String symbol;

    @com.google.gson.annotations.SerializedName("account-id")
    private String accountId;

    private BigDecimal amount;

    private BigDecimal price;

    @com.google.gson.annotations.SerializedName("created-at")
    private long createdAt;

    private String type;

    @com.google.gson.annotations.SerializedName("fieldamount")
    private BigDecimal fieldAmount;

    @com.google.gson.annotations.SerializedName("fieldcashamount")
    private BigDecimal fieldCashAmount;

    @com.google.gson.annotations.SerializedName("fieldfees")
    private BigDecimal fieldFees;

    @com.google.gson.annotations.SerializedName("finishedat")
    private long finishedAt;

    private String source;
    private String state;

    @com.google.gson.annotations.SerializedName("canceled_at")
    private long canceledAt;
}
