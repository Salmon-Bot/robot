package com.lucky.game.robot.entity;

import com.lucky.game.core.component.ext.hibernate.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @author conan
 *         2018/3/30 15:08
 **/
@Entity
@Table(name = "T_ACCOUNT")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class AccountEntity extends UUID {

    private String apiKey;

    private String apiSecret;

    /**
     * 账号类型 hb/zb
     */
    private String type;

    /**
     * 账号id
     */
    private String accountId;

    /**
     * normal freeze
     */
    private String status;

    private String userId;

    private Timestamp createTime;

    private Timestamp updateTime;

}
