package com.lucky.game.robot.dto.huobi;

import lombok.Data;

/**
 * @author conan
 *         2018/3/15 15:45
 **/
@Data
public class DepthDto extends HuobiBaseDto{

    private String symbol;

    /**
     * step0, step1, step2, step3, step4, step5（合并深度0-5）；step0时，不合并深度
     */
    private String type = "step0";
}
