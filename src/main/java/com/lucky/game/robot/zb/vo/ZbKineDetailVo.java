package com.lucky.game.robot.zb.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author conan
 *         2018/3/29 17:34
 **/
@Data
public class ZbKineDetailVo {

    /**
     * 时间戳
     */
    private long timestamp;


    /**
     * 开盘价
     */
    private BigDecimal open;

    /**
     * 最高价
     */
    private BigDecimal high;

    /**
     * 最低价
     */
    private BigDecimal low;

    /**
     * 收盘价,当K线为最晚的一根时，是最新成交价
     */
    private BigDecimal close;

    /**
     * 交易量
     */
    private BigDecimal vol;


}
