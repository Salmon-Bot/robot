package com.lucky.game.robot.entity;

import com.lucky.game.core.component.ext.hibernate.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author conan
 *         2018/5/31 14:06
 **/
@Entity
@Table(name = "T_KLINE_INFO")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class KlineInfoEntity  extends UUID {


    /**
     * k线id(时间戳)
     */
    private String klineId;

    /**
     * 交易对
     */
    private String symbol;

    /**
     * k线类型
     */
    private String period;

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
