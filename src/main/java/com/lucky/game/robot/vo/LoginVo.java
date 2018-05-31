package com.lucky.game.robot.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author conan
 *         2018/3/28 11:12
 **/
@Data
public class LoginVo implements Serializable{

    private String userId;

    private String phone;

}
