package com.lucky.game.robot.dto.huobi;

import lombok.Data;

import java.util.List;

/**
 * @author conan
 *         2018/3/14 16:57
 **/
@Data
public class BatchCancelDto extends HuobiBaseDto {

    private List<String> orderIds;
}
