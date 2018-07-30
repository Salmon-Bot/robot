package com.lucky.game.robot.huobi.response;

import lombok.Data;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 16:23
 */

@Data
public class BalanceBean {
    /**
     * currency : usdt
     * type : trade
     * balance : 500009195917.4362872650
     */

    private String currency;
    private String type;
    private String balance;


}
