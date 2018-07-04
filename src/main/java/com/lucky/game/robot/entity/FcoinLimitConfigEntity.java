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
@Table(name = "T_FCOIN_LIMIT_CONFIG")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class FcoinLimitConfigEntity extends UUID{


    private String userId;

    private String symbol;

    /**
     * 买单浮动比例,不填默认最新价
     */
    private BigDecimal buyFluctuate;

    /**
     * 卖单浮动比例,不填默认最新价
     */
    private BigDecimal sellFluctuate;
    /**
     * 挂单时实时价格
     */
    private BigDecimal realPrice;

    /**
     * 挂单数量
     */
    private BigDecimal amount;

    /**
     * zb/hb/fcoin
     */
    private String marketType;


    /**
     * 间隔时间 秒
     */
    private Integer waitTime;

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
