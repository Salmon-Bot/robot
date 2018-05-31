package com.lucky.game.robot.dto.huobi;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ma趋势数据
 * @author conan
 *         2018/5/3 18:08
 **/

@Data
public class MaInfoDto {

    /**
     * 查询类型
     */
    private String period;
    /**
     * 最新时间的ma7平均数
     */
    BigDecimal ma7Middle = BigDecimal.ZERO;

    /**
     * 最新时间的ma30平均数
     */
    BigDecimal ma30Middle = BigDecimal.ZERO;


    /**
     * 平均数差异比率
     */
    private Double rate;

//    BigDecimal threeMiddle = BigDecimal.ZERO;
//
//    BigDecimal fourMiddle = BigDecimal.ZERO;
}
