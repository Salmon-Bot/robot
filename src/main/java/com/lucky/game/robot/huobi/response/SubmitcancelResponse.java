package com.lucky.game.robot.huobi.response;

import lombok.Data;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 17:15
 */

@Data
public class SubmitcancelResponse {


    /**
     * state : ok
     * data : 59378
     */

    private String status;
    public String errCode;
    public String errMsg;
    private String data;

}
