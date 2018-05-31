package com.lucky.game.robot.huobi.request;

import lombok.Data;

/**
 * @author conan
 *         2018/4/12 17:14
 **/
@Data
public class WithDrawRequest {

    private String address;

    private String amount;

    private String currency;

}
