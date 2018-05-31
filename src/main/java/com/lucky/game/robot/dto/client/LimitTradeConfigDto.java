package com.lucky.game.robot.dto.client;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author conan
 *         2018/3/26 13:22
 **/
@Data
public class LimitTradeConfigDto implements Serializable {

    private String oid;

    private String symbol;

    /**
     * 增长百分比
     */
    private BigDecimal increase;

    /**
     * 降低百分比
     */
    private BigDecimal decrease;

    /**
     * 最多允许同时交易挂单数(一笔买单一笔卖单算一笔)
     */
    private Integer maxTradeCount;

    /**
     * 挂单总金额
     */
    private BigDecimal totalAmount;

    /**
     * hb/zb
     */
    private String marketType;


}
