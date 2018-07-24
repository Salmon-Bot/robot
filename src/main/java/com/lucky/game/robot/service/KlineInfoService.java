package com.lucky.game.robot.service;

import com.lucky.game.robot.entity.KlineInfoEntity;

/**
 * @author conan
 *         2018/5/31 14:23
 **/
public interface KlineInfoService {

    void insert(KlineInfoEntity entity);

    KlineInfoEntity findByKlineId(String klineId);

    KlineInfoEntity findByKlineIdAndSymbol(String klineId,String symbol);
}
