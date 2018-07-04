package com.lucky.game.robot.fcoin.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * conan
 * 2018/7/3 下午5:00
 **/
@Data
public class FCoinOrderDetailVo {

//  "id": "9d17a03b852e48c0b3920c7412867623",
//            "symbol": "string",
//            "type": "limit",
//            "side": "buy",
//            "price": "string",
//            "amount": "string",
//            "state": "submitted",
//            "executed_value": "string",
//            "fill_fees": "string",
//            "filled_amount": "string",
//            "created_at": 0,
//            "source": "web"

    private String id;

    private String symbol;

    private String type;

    private String side;

    private BigDecimal price;

    private BigDecimal amount;

    private String state;

    private String executedValue;

    private BigDecimal fillFees;

    private BigDecimal filledAmount;

    private String createAt;

    private String source;

}
