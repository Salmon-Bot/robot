package com.lucky.game.robot.service;

import com.lucky.game.robot.entity.LimitBetaConfigEntity;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:43
 **/
public interface LimitBetaConfigService {

    List<LimitBetaConfigEntity> findByUserIdAndMarketType(String userId, String marketType);

    List<LimitBetaConfigEntity> findByUserId(String userId);

    LimitBetaConfigEntity save(LimitBetaConfigEntity entity);

    LimitBetaConfigEntity findById(String oid);

    List<LimitBetaConfigEntity> findByUserIdAndSymbolAndMarketType(String userId, String symbol, String marketType);
}
