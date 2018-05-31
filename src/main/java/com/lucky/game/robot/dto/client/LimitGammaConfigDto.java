package com.lucky.game.robot.dto.client;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author conan
 *         2018/3/26 13:22
 **/
@Data
public class LimitGammaConfigDto implements Serializable {

    private String oid;

    private String symbol;

    /**
     * 波动
     */
    private BigDecimal fluctuate;


    /**
     * 挂单总金额
     */
    private BigDecimal totalAmount;

    /**
     * zb/hb
     */
    private String marketType;

    /**
     * 启用open/停用stop
     */
    private String status;

}
