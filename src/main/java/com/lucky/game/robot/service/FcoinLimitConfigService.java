package com.lucky.game.robot.service;

import com.lucky.game.robot.entity.FcoinLimitConfigEntity;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:43
 **/
public interface FcoinLimitConfigService {

    FcoinLimitConfigEntity save(FcoinLimitConfigEntity entity);

    List<FcoinLimitConfigEntity> findByUserId(String userId);

    List<FcoinLimitConfigEntity> findAll();

    FcoinLimitConfigEntity findById(String id);
}
