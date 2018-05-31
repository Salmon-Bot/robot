package com.lucky.game.robot.service;

import com.lucky.game.robot.entity.LimitDelteConfigEntity;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:43
 **/
public interface LimitDelteConfigService {

    List<LimitDelteConfigEntity> findByUserIdAndMarketType(String userId, String marketType);

    List<LimitDelteConfigEntity> findByUserIdAndSymbolAndMarketType(String userId, String symbol, String marketType);

    List<LimitDelteConfigEntity> findByUserId(String userId);

    LimitDelteConfigEntity save(LimitDelteConfigEntity entity);

    LimitDelteConfigEntity findById(String oid);
}
