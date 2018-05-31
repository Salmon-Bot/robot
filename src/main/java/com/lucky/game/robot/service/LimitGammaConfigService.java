package com.lucky.game.robot.service;

import com.lucky.game.robot.entity.LimitGammaConfigEntity;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:43
 **/
public interface LimitGammaConfigService {

    List<LimitGammaConfigEntity> findByUserIdAndMarketType(String userId, String marketType);

    List<LimitGammaConfigEntity> findByUserIdAndSymbolAndMarketType(String userId,String symbol, String marketType);

    List<LimitGammaConfigEntity> findByUserId(String userId);

    LimitGammaConfigEntity save(LimitGammaConfigEntity entity);

    LimitGammaConfigEntity findById(String oid);
}
