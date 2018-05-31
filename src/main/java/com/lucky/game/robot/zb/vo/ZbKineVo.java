package com.lucky.game.robot.zb.vo;

import lombok.Data;

import java.util.List;

/**
 * @author conan
 *         2018/3/29 17:33
 **/
@Data
public class ZbKineVo {

    private String symbol;

    private String moneyType;

    private List<ZbKineDetailVo> data;
}
