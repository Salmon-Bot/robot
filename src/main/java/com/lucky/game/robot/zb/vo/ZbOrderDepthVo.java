package com.lucky.game.robot.zb.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author conan
 *         2018/3/30 14:06
 **/
@Data
public class ZbOrderDepthVo {

    /**
     * 卖方深度
     */
    private List<List<BigDecimal>> asks;

    /**
     * 买方深度
     */
    private List<List<BigDecimal>> bids;

    private Long timestamp;
}
