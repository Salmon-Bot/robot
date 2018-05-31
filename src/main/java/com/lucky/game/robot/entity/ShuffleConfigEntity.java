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
 * @author conan
 *         2018/4/10 15:20
 **/
@Entity
@Table(name = "T_SHUFFLE_CONFIG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class ShuffleConfigEntity extends UUID{

    private String userId;

    /**
     * 业务对
     */
    private String quoteCurrency;

    /**
     * 主对
     */
    private String baseCurrency = "usdt";

    private String marketOne;

    private String marketTwo;

    /**
     * 差异率
     */
    private BigDecimal rateValue;

    /**
     *
     */
    private BigDecimal buyIncreasePrice;

    /**
     * 挂单总金额(usdt)
     */
    private BigDecimal totalAmount;

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
