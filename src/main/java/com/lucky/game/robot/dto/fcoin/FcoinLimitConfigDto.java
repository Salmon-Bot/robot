package com.lucky.game.robot.dto.fcoin;

import lombok.Data;

import java.math.BigDecimal;

/**
 * conan
 * 2018/7/3 下午5:31
 **/
@Data
public class FcoinLimitConfigDto{

    private String oid;

    private String symbol;


    /**
     * 挂单数量
     */
    private BigDecimal amount;

    /**
     * 间隔时间秒
     */
    private Integer waitTime;

    /**
     * 启用open/停用stop
     */
    private String status;
    /**
     * no:未删除/yes:已删除
     */
    private String isDelete;
}
