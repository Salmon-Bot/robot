package com.lucky.game.robot.zb.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author conan
 *         2018/3/30 14:41
 **/
@Data
public class ZbAccountDetailVo {

    /**
     * 冻结资产
     */
    private BigDecimal freez;

    /**
     * 币种英文名
     */
    private String enName;

    /**
     * 保留小数位
     */
    private Integer unitDecimal;

    /**
     * 币种中文名
     */
    private String cnName;

    /**
     * 币种符号
     */
    private String unitTag;

    /**
     * 可用资产
     */
    private BigDecimal available;

    /**
     * 币种
     */
    private String key;

}
