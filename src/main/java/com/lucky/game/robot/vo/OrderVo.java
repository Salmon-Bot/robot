package com.lucky.game.robot.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author conan
 *         2018/3/28 16:51
 **/
@Data
public class OrderVo {

    private String oid;
    /**
     * 订单id
     */
    private String orderId;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 模块
     */
    private String model;

    /**
     * 市场
     */
    private String marketType;
    /**
     * 订单数量
     */
    private BigDecimal amount;

    /**
     * 订单价格
     */
    private BigDecimal price;

    /**
     * 订单类型
     */
    private String type;

    /**
     * 订单状态 pre-submitted 准备提交, submitting , submitted 已提交, partial-filled 部分成交, partial-canceled 部分成交撤销, filled 完全成交,canceled 已撤销,selling 挂单售卖中,saled 售卖已成交
     */
    private String state;

    /**
     * 已成交数量
     */
    private BigDecimal fieldAmount;

    /**
     * 已成交总额
     */
    private BigDecimal fieldCashAmount;

    /**
     * 已成交手续费（买入为币，卖出为钱）
     */
    private BigDecimal fieldFees;

    /**
     * 买单订单id(卖单对应的买单id)
     */
    private String buyOrderId;
    /**
     * 转换成usdt实时的总额
     */
    private BigDecimal totalToUsdt;

    private long createdAt;

    private long canceledAt;

    private long finishedAt;

    private Timestamp createTime;

    private Timestamp updateTime;
}
