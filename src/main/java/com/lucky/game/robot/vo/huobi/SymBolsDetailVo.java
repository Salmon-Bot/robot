package com.lucky.game.robot.vo.huobi;

import lombok.Data;

import java.io.Serializable;

/**
 * @author conan
 *         2018/3/8 17:57
 **/
@Data
public class SymBolsDetailVo implements Serializable{

    /**
     * 完整交易对
     */
    private String symbols;

    /**
     * 主对代码 btc/eth/usdt
     */
    private String baseCurrency;

    /**
     * 应用对
     */
    private String quoteCurrency;

    private String pricePrecision;

    private String amountPrecision;

    /**
     * 所属区 主区/创新区
     */
    private String symbolPartition;
}
