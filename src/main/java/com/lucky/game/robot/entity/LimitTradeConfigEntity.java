package com.lucky.game.robot.entity;

import com.lucky.game.core.component.ext.hibernate.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author conan
 *         2018/3/26 13:22
 **/
@Entity
@Table(name = "T_LIMIT_TRADE_CONFIG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class LimitTradeConfigEntity extends UUID implements Serializable {

    private String userId;

    private String symbol;

    /**
     * 增长百分比
     */
    private BigDecimal increase;

    /**
     * 降低百分比
     */
    private BigDecimal decrease;

    /**
     * 最多允许同时交易挂单数(一笔买单一笔卖单算一笔)
     */
    private Integer maxTradeCount;

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

    /**
     * no:未删除/yes:已删除
     */
    private String isDelete;


    private Timestamp createTime;

    private Timestamp updateTime;

}
