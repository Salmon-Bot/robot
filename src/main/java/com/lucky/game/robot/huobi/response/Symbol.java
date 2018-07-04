package com.lucky.game.robot.huobi.response;

import lombok.Data;

@Data
public class Symbol {

    private String baseCurrency;
    private String quoteCurrency;
    private String symbol;
    private Integer priceDecimal;
    private Integer amountDecimal;

}
