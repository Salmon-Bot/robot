package com.lucky.game.robot.dto.client;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author conan
 *         2018/3/28 14:00
 **/
@Data
public class SymbolTradeConfigDto {

    private String oid;
    /**
     * 阈值类型(1min,5min)
     */
    private String thresholdType;

    /**
     * 不同交易对之间的差异比率(交易参考值,当两个主对之间差异超过次此值时交易)
     */
    private BigDecimal currencyAbsolute;


    /**
     * 触发交易购买逻辑后,购买价与卖一价之间允许的最大比率差,例如此值是0.01,某交易对欲购买的价格是1usdt,则如果卖一价低于1.1usdt,则直接购买
     * 此值是为了在一定波段内更快成交,当卖一价不符时,已实际欲购买价挂单
     */
    private BigDecimal asksBlunder;

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
     * hb/zb
     */
    private String marketType;

}
