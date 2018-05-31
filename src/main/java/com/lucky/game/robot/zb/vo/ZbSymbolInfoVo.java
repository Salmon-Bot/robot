package com.lucky.game.robot.zb.vo;

import lombok.Data;

/**
 * @author conan
 *         2018/3/30 11:07
 **/
@Data
public class ZbSymbolInfoVo {

    /**
     * 交易对
     */
    private String currency;

    private Integer amountScale;

    private Integer priceScale;


}
