package com.lucky.game.robot.service;

import com.lucky.game.robot.entity.ShuffleConfigEntity;

import java.util.List;

/**
 * @author conan
 *         2018/4/11 16:34
 **/
public interface ShuffleConfigService {

    List<ShuffleConfigEntity> findByUserId(String userId);

    List<ShuffleConfigEntity> findByUserIdWithOpen(String userId);
}
