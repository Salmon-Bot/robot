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
 *beta策略限价单
 * @author conan
 *         2018/4/16 10:30
 **/
@Entity
@Table(name = "T_LIMIT_GAMM_CONFIG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class LimitGammaConfigEntity extends UUID{


    private String userId;

    private String symbol;

    /**
     * 波动
     */
    private BigDecimal fluctuate;


    /**
     * 波动（下降）
     */
    private BigDecimal fluctuateDecrease;



    /**
     * 挂单时实时价格
     */
    private BigDecimal realPrice;

    /**
     * 挂单总金额
     */
    private BigDecimal totalAmount;

    /**
     * zb/hb
     */
    private String marketType;

    /**
     * 是否启用 yes/no
     */
    private String isUse;

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
