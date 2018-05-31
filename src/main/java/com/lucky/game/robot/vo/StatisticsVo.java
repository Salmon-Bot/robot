package com.lucky.game.robot.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author conan
 *         2018/4/13 15:40
 **/
@Data
public class StatisticsVo {

    private BigDecimal realBuyTotal;

    private BigDecimal realSellTotal;

    private BigDecimal limitAlphaBuyTotal;

    private BigDecimal limitAlphaSellTotal;

    private BigDecimal limitBetaBuyTotal;

    private BigDecimal limitBetaSellTotal;

    private BigDecimal shuffleBuyTotal;

    private BigDecimal shuffleSellTotal;
}
