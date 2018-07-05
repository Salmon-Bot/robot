package com.lucky.game.robot.dto.fcoin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * conan
 * 2018/7/3 下午5:31
 **/
@ApiModel
@Data
public class FcoinLimitConfigDto{

    private String oid;

    @ApiModelProperty("交易对")
    private String symbol;


    /**
     * 挂单数量
     */
    @ApiModelProperty("挂单数量")
    private BigDecimal amount;

    /**
     * 间隔时间秒
     */
    @ApiModelProperty("每次挂单间隔时间,单位秒")
    private Integer waitTime;


    /**
     * 价格波动倍数
     */
    @ApiModelProperty("价格波动倍数")
    private BigDecimal priceMultiple;


    /**
     * 价格斜率
     */
    @ApiModelProperty("价格斜率")
    private BigDecimal priveSlope;

    /**
     * 数量波动倍数
     */
    @ApiModelProperty("数量波动倍数")
    private BigDecimal amountMultiple;

    /**
     * 启用open/停用stop
     */
    @ApiModelProperty("启用open/停用stop")
    private String status;
    /**
     * no:未删除/yes:已删除
     */
    @ApiModelProperty("no:未删除/yes:已删除")
    private String isDelete;

    /**
     * 备注
     *
     *价格随机值 原有价格基础随机*-0.1%~0.1% * 倍数波动+斜率波动
     *
     * 例如: 当前价格为10,随机数为-0.05,倍数为5,斜率为0.6% 则 10*((1+(-0.5/1000)*5)+0.6/100)=10.035 为挂单价格
     * 即 挂单价格=交易对市场最新价格+(交易对市场最新价格*随机数(默认-0.1%~0.1%之间)*倍数+斜率(默认为0)/100)
     *
     * 数量随机值  指定数量+ 指定数量 *-0.1%~0.1% * 倍数波动
     *例如: 当前数量为100,随机数为0.2,倍数为10, 则 100*((1+0.5/1000*5)=100.25 为挂单价格
     */
}
