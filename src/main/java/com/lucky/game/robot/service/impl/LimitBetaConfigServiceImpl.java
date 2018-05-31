package com.lucky.game.robot.service.impl;

import com.lucky.game.robot.dao.LimitBetaConfigDao;
import com.lucky.game.robot.entity.LimitBetaConfigEntity;
import com.lucky.game.robot.service.LimitBetaConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:43
 **/
@Service
public class LimitBetaConfigServiceImpl implements LimitBetaConfigService {

    @Autowired
    private LimitBetaConfigDao limitBetaConfigDao;

    @Override
    public List<LimitBetaConfigEntity> findByUserIdAndMarketType(String userId, String marketType) {
        return limitBetaConfigDao.findByUserIdAndMarketType(userId, marketType);
    }

    @Override
    public List<LimitBetaConfigEntity> findByUserId(String userId) {
        return limitBetaConfigDao.findByUserId(userId);
    }

    @Override
    public LimitBetaConfigEntity save(LimitBetaConfigEntity entity) {
        return limitBetaConfigDao.save(entity);
    }

    @Override
    public LimitBetaConfigEntity findById(String oid) {
        return limitBetaConfigDao.findOne(oid);
    }

    @Override
    public List<LimitBetaConfigEntity> findByUserIdAndSymbolAndMarketType(String userId, String symbol, String marketType) {
        return limitBetaConfigDao.findByUserIdAndSymbolAndMarketType(userId,symbol,marketType);
    }
}
