package com.lucky.game.robot.dto.client;

import lombok.Data;

/**
 * @author conan
 *         2018/3/28 17:04
 **/
@Data
public class OrderDto {

    private Integer pageSize = 100;

    private Integer currentPage = 1;

    private String orderId;

}
