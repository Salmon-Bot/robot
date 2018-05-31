package com.lucky.game.robot.dto.client;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author conan
 *         2018/3/28 10:50
 **/
@Data
public class UserLoginDto {
    @NotBlank(message = "手机号不能为空！")
    private String phone;

    /** 密码 */
    private String userPwd;

    /** 验证码 */
    private String vericode;
}
