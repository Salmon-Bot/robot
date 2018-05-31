package com.lucky.game.robot.entity;

import com.lucky.game.core.component.ext.hibernate.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author conan
 *         2018/3/21 16:49
 **/
@Entity
@Table(name = "T_USER")
@lombok.Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class UserEntity extends UUID implements Serializable {
    /**
     * normal freeze
     */
    private String status;

    private String phone;

    private String password;

    private String salt;

    /**
     * 通知手机号,可多个,通过逗号分隔
     */
    private String notifyPhone;

    /**
     * 通知邮箱,可多个,通过逗号分隔
     */
    private String notifyEmail;

    private Timestamp createTime;

    private Timestamp updateTime;
}
