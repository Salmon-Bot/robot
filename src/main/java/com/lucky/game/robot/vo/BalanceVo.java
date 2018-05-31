package com.lucky.game.robot.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author conan
 *         2018/4/10 15:33
 **/
@Data
public class BalanceVo {

    /** hb 账号**/
    private BigDecimal hbUsdtTradeBalance;

    private BigDecimal hbBtcTradeBalance;

    private BigDecimal hbEthTradeBalance;

    private BigDecimal hbUsdtFrozenBalance;

    private BigDecimal hbBtcFrozenBalance;

    private BigDecimal hbEthFrozenBalance;

    /** zb 账号**/
    private BigDecimal zbUsdtTradeBalance;

    private BigDecimal zbBtcTradeBalance;

    private BigDecimal zbQcTradeBalance;

    private BigDecimal zbUsdtFrozenBalance;

    private BigDecimal zbBtcFrozenBalance;

    private BigDecimal zbQcFrozenBalance;
}
