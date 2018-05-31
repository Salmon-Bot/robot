package com.lucky.game.robot.service;

import com.lucky.game.robot.entity.RateChangeEntity;

/**
 * @author conan
 *         2018/3/20 11:14
 **/
public interface RateChangeService {

    RateChangeEntity findOne(String id);

    RateChangeEntity save(RateChangeEntity entity);

}
