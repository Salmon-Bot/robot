package com.lucky.game.robot.zb.vo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author conan
 *         2018/3/30 10:32
 **/
public class ZbOrderDetailVo implements Serializable {
    /**
     * 交易类型
     */
    @SerializedName("currency")
    private String symbol;

    /**
     * 委托挂单号
     */
    private String id;
    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 挂单状态(1：取消,2：交易完成,0/3：待成交/待成交未交易部份)
     */
    @SerializedName("status")
    private String state;

    /**
     * 挂单总数量
     */
    @SerializedName("total_amount")
    private BigDecimal amount;

    /**
     * 已成交数量
     */
    @SerializedName("trade_amount")
    private BigDecimal fieldAmount;

    /**
     * 委托时间
     */
    @SerializedName("trade_date")
    private Long tradeDate;

    /**
     * 已成交总金额
     */
    @SerializedName("trade_money")
    private BigDecimal fieldCashAmount;

    /**
     * 挂单类型 1/0[buy/sell]
     */
    private String type;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getFieldAmount() {
        return fieldAmount;
    }

    public void setFieldAmount(BigDecimal fieldAmount) {
        this.fieldAmount = fieldAmount;
    }

    public Long getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(Long tradeDate) {
        this.tradeDate = tradeDate;
    }

    public BigDecimal getFieldCashAmount() {
        return fieldCashAmount;
    }

    public void setFieldCashAmount(BigDecimal fieldCashAmount) {
        this.fieldCashAmount = fieldCashAmount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ZbOrderDetailVo{" +
                "symbol='" + symbol + '\'' +
                ", id='" + id + '\'' +
                ", price=" + price +
                ", state='" + state + '\'' +
                ", amount=" + amount +
                ", fieldAmount=" + fieldAmount +
                ", tradeDate=" + tradeDate +
                ", fieldCashAmount=" + fieldCashAmount +
                ", type='" + type + '\'' +
                '}';
    }
}
