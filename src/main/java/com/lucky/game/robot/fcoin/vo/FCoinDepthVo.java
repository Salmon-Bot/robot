package com.lucky.game.robot.fcoin.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * conan
 * 2018/7/3 下午4:44
 **/
@Data
public class FCoinDepthVo {


    private String type;
    /**
     * 卖方深度
     */
    private List<BigDecimal> asks;

    /**
     * 买方深度
     */
    private List<BigDecimal> bids;

    private Long ts;

    private String seq;
}
