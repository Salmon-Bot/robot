package com.lucky.game.robot.dto.zb;

import lombok.Data;

/**
 * @author conan
 *         2018/3/30 10:07
 **/
@Data
public class ZbCreateOrderDto extends BaseZbDto{

    private String price;

    private String amount;

    /**
     * 交易类型1/0[buy/sell]
     */
    private String tradeType;

    private String currency;

    /**
     * 杠杠 1/0[杠杠/现货](可选参数,默认为: 0 现货)
     */
    private String acctType;
}
