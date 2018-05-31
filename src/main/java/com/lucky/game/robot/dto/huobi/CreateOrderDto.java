package com.lucky.game.robot.dto.huobi;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author conan
 *         2018/3/14 16:46
 **/
@Data
public class CreateOrderDto extends HuobiBaseDto {
    private String accountId;

    private BigDecimal amount;

    private BigDecimal price;

    private String symbol;

    private String orderType;
}
