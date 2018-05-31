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
 * 交易对之间汇率差异信息
 *
 * @author conan
 *         2018/1/4 16:28
 **/
@Entity
@Table(name = "T_RATE_CHANGE")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class RateChangeEntity extends UUID implements Serializable {
    private static final long serialVersionUID = 6743566467342671039L;

    /**
     * 需要购买的交易对代码
     */
    private String buyerSymbol;

    /**
     * 买入价格
     */
    private BigDecimal buyPrice;


    /**
     * 可操作的最大差异比率,example: eosbtc increase 8%,btcusdt decrease -4%,最大比率为4%;eosbtc decrease -8%,btcusdt decrease -2%,最大比率为10%
     */
    private BigDecimal rateValue;

    /**
     * 要购买的主对
     */
    private String baseCurrency;

    /**
     * 交易币种
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

    private Timestamp createTime;

    private Timestamp updateTime;
}
