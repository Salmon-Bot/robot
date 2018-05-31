package com.lucky.game.robot.dto.client;

import lombok.Data;

/**
 * @author conan
 *         2018/3/28 13:27
 **/
@Data
public class ModifyUserInfoDto {

    private String hbApiKey;

    private String hbApiSecret;

    private String zbApiKey;

    private String zbApiSecret;

    private String phone;

    private String password;

    /**
     * 通知手机号,可多个,通过逗号分隔
     */
    private String notifyPhone;

    /**
     * 通知邮箱,可多个,通过逗号分隔
     */
    private String notifyEmail;
}
