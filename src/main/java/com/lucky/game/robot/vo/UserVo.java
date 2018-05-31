package com.lucky.game.robot.vo;

import lombok.Data;

/**
 * @author conan
 *         2018/3/28 13:27
 **/
@Data
public class UserVo {
    /**
     * normal freeze
     */
    private String status;

    private String phone;

    /**
     * 通知手机号,可多个,通过逗号分隔
     */
    private String notifyPhone;

    /**
     * 通知邮箱,可多个,通过逗号分隔
     */
    private String notifyEmail;


    private String hbApiKey;

    private String hbApiSecret;


    private String zbApiKey;

    private String zbApiSecret;
}
