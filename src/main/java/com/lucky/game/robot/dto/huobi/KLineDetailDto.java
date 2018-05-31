package com.lucky.game.robot.dto.huobi;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author conan
 *         2018/1/5 16:53
 **/
@Data
public class KLineDetailDto implements Serializable{

    private static final long serialVersionUID = 3954843808584586490L;

    private String id;

    private String symbol;

    /**
     * 成交量
     */
    private BigDecimal amount;

    /**
     * 成交笔数
     */
    private Integer count;

    /**
     * 开盘价
     */
    private BigDecimal open;

    /**
     * 收盘价,当K线为最晚的一根时，是最新成交价
     */
    private BigDecimal close;

    /**
     * 最高价
     */
    private BigDecimal high;

    /**
     * 最低价
     */
    private BigDecimal low;

    /**
     * 成交额, 即 sum(每一笔成交价 * 该笔的成交量)
     */
    private BigDecimal vol;
}
