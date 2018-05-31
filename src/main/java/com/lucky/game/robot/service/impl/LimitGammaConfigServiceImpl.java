package com.lucky.game.robot.service.impl;

import com.lucky.game.robot.entity.LimitGammaConfigEntity;
import com.lucky.game.robot.service.LimitGammaConfigService;
import com.lucky.game.robot.dao.LimitGammaConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:43
 **/
@Service
public class LimitGammaConfigServiceImpl implements LimitGammaConfigService {

    @Autowired
    private LimitGammaConfigDao limitGammaConfigDao;

    @Override
    public List<LimitGammaConfigEntity> findByUserIdAndMarketType(String userId, String marketType) {
        return limitGammaConfigDao.findByUserIdAndMarketType(userId, marketType);
    }

    @Override
    public List<LimitGammaConfigEntity> findByUserIdAndSymbolAndMarketType(String userId, String symbol, String marketType) {
        return limitGammaConfigDao.findByUserIdAndSymbolAndMarketType(userId,symbol,marketType);
    }

    @Override
    public List<LimitGammaConfigEntity> findByUserId(String userId) {
        return limitGammaConfigDao.findByUserId(userId);
    }

    @Override
    public LimitGammaConfigEntity save(LimitGammaConfigEntity entity) {
        return limitGammaConfigDao.save(entity);
    }

    @Override
    public LimitGammaConfigEntity findById(String oid) {
        return limitGammaConfigDao.findOne(oid);
    }
}
