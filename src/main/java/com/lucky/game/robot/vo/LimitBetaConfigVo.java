package com.lucky.game.robot.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author conan
 *         2018/3/26 13:22
 **/
@Data
public class LimitBetaConfigVo implements Serializable {

    private String oid;

    private String userId;

    private String symbol;

    /**
     * 波动
     */
    private BigDecimal fluctuate;


    /**
     * 挂单时实时价格
     */
    private BigDecimal realPrice;

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


    private Timestamp createTime;

}
