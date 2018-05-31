package com.lucky.game.robot.entity;

import com.lucky.game.core.component.ext.hibernate.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 *DELTE策略限价单
 * @author conan
 *         2018/4/16 10:30
 **/
@Entity
@Table(name = "T_LIMIT_DELTE_CONFIG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class LimitDelteConfigEntity extends UUID{


    private String userId;

    private String symbol;
    /**
     * 卖出波动(上升)
     */
    private BigDecimal fluctuate;


    /**
     * 买入波动（下降）
     */
    private BigDecimal fluctuateDecrease;

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
