package com.lucky.game.robot.vo.huobi;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 不同交易对之间汇率比较返回结果
 *
 * @author conan
 *         2018/3/15 16:12
 **/
@Data
public class RateChangeVo implements Serializable {


    /**
     * 需要购买的交易对代码(为空则不买)
     */
    private String buyerSymbol;

    /**
     * 买入价格
     */
    private BigDecimal buyPrice;
    /**
     * 欲购买的最新一条行情信息
     */
    private MarketDetailVo nowMarketDetailVo;

    /**
     * 可操作的最大差异比率,example: eosbtc increase 8%,btcusdt decrease -4%,最大比率为4%;eosbtc decrease -8%,btcusdt decrease -2%,最大比率为10%
     */
    private BigDecimal rateValue;

    /**
     * 要购买的主对
     */
    private String baseCurrency;

    /**
     * 交易币
     */
    private String quoteCurrency;

    /**
     * 需要售出的交易对代码
     */
    private String saleSymbol;

    /**
     * 售出价格
     */
    private BigDecimal salePrice;

    /**
     * 原交易对
     */
    private String originSymbol;

    /**
     * 交易所类型 hb,hb
     */
    private String marketType;

    /**
     * 交易对指定时间内变动是否超过阈值
     */
    private boolean isOperate = false;

    /**
     * 短信和邮件通知的内容
     */
    private String context;

    /**
     * 存在其他主对交易区,例如eos存在btc,eth,usdt交易区
     */
    private boolean hasOtherBase = false;
}
