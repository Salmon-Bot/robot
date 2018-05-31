package com.lucky.game.robot.entity;

import com.lucky.game.core.component.ext.hibernate.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 订单表
 *
 * @author conan
 *         2018/1/4 16:28
 **/
@Entity
@Table(name = "T_ORDER")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class OrderEntity extends UUID implements Serializable {
    private static final long serialVersionUID = 6743566467342671039L;


    /**
     * 账户ID
     */
    private String accountId;

    /**
     * 汇率表id
     */
    private String rateChangeId;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 交易对
     */
    private String symbol;
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
     * 订单状态 pre-submitted 准备提交, submitting , submitted 已提交, partial-filled 部分成交, partial-canceled 部分成交撤销, filled 完全成交,         *  canceled 已撤销,selling 挂单售卖中,saled 售卖已成交
     */
    private String state;

    /**
     * 是否已完成
     */
    private String isFinish;

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
     * 订单来源
     */
    private String source;

    /**
     * 买单订单id(卖单时保存对应买单id)
     * */
    private String buyOrderId;

    /**
     * 用户id
     */
    private String userId;

    private String symbolTradeConfigId;

    /**
     * 转换成usdt实时的总额
     */
    private BigDecimal totalToUsdt;


    /**
     * 实时单 real /限价单 limit
     */
    private String model;

    /**
     * hb/zb
     */
    private String marketType;

    private long createdAt;

    private long canceledAt;

    private long finishedAt;

    private Timestamp createTime;

    private Timestamp updateTime;
}
