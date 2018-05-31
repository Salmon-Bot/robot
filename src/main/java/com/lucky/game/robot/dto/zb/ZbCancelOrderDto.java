package com.lucky.game.robot.dto.zb;

import lombok.Data;

/**
 * @author conan
 *         2018/3/30 10:26
 **/
@Data
public class ZbCancelOrderDto extends BaseZbDto{

    private String orderId;

    private String currency;
}
