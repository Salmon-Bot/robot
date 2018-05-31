package com.lucky.game.robot.entity;

import com.lucky.game.core.component.ext.hibernate.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 交易对波动监控配置项
 *
 * @author conan
 *         2018/3/21 16:53
 **/
@Entity
@Table(name = "T_SYMBOL_TRADE_CONFIG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class SymbolTradeConfigEntity extends UUID {

    private String userId;

    /**
     * 交易所 hb,zb
     */
    private String marketType;

    /**
     * 阈值类型(1min,5min)
     */
    private String thresholdType;

    /**
     * 不同交易对之间的差异比率(交易参考值,当两个主对之间差异超过次此值是交易)
     */
    private BigDecimal currencyAbsolute;


    /**
     * 触发交易购买逻辑后,购买价与卖一价之间允许的最大比率差,例如此值是0.01,某交易对欲购买的价格是1usdt,则如果卖一价低于1.1usdt,则直接购买
     * 此值是为了在一定波段内更快成交,当卖一价不符时,已实际欲购买价挂单
     */
    private BigDecimal asksBlunder;

    /**
     * 下单可浮动价格(在计算好的价格基础上加减此值,更容易成交)
     */
    private BigDecimal buyIncreasePrice = new BigDecimal(0.00000002);

    /**
     * 下单等待时间,超时未成功则撤单
     */
    private Integer buyWaitTime;

    /**
     * 单笔订单,usdt最多下单值
     */
    private BigDecimal usdtMaxUse;

    /**
     * 单笔订单,btc最多下单值
     */
    private BigDecimal btcMaxUse;

    /**
     * 单笔订单,eth最多下单值
     */
    private BigDecimal ethMaxUse;

    /**
     * 单笔下单,qc最多下单值
     */
    private BigDecimal qcMaxUse;

    /**
     * 启用open/停用stop
     */
    private String status;

    /**
     * no:未删除/yes:已删除
     */
    private String isDelete;

    private Timestamp createTime;

    private Timestamp updateTime;

}
