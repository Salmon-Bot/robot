package com.lucky.game.robot.dto.zb;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author conan
 *         2018/4/12 15:31
 **/
@Data
public class ZbWithDrawDto extends BaseZbDto{

    private String receiveAddr;

    private BigDecimal fees;

    private String currency;

    private BigDecimal amount;

    private String itransfer = "0";

    private String safePwd;
}
