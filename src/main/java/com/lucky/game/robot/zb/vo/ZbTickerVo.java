package com.lucky.game.robot.zb.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author conan
 *         2018/4/3 17:57
 **/
@Data
public class ZbTickerVo {
    /**
     * 最高价
     */
    private BigDecimal high;
    /**
     * 最低价
     */
    private BigDecimal low;

    /**
     * 买一价
     */
    private BigDecimal buy;

    /**
     * 卖一价
     */
    private BigDecimal sell;

    /**
     *  最新成交价
     */
    private BigDecimal last;

    /**
     * 成交量(最近的24小时)
     */
    private BigDecimal vol;
}
