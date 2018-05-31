package com.lucky.game.robot.zb.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * @author conan
 *         2018/3/30 14:38
 **/
@Data
public class ZbAccountVo {

    /**
     * 币种信息
     */
    private List<ZbAccountDetailVo> coins;

    /**
     * 账号基本信息 json
     */
    private JSONObject base;

}
