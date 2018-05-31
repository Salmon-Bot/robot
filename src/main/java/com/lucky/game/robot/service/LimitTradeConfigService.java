package com.lucky.game.robot.service;

import com.lucky.game.robot.entity.LimitTradeConfigEntity;

import java.util.List;

/**
 * @author conan
 *         2018/3/26 13:49
 **/
public interface LimitTradeConfigService {

    List<LimitTradeConfigEntity> findAllByUserIdAndMarketType(String userId,String marketType);

    List<LimitTradeConfigEntity> findAllByUserId(String userId);

    LimitTradeConfigEntity findById(String oid);

    LimitTradeConfigEntity save(LimitTradeConfigEntity entity);
}
