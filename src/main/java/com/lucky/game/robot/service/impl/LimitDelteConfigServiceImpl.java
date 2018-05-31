package com.lucky.game.robot.service.impl;

import com.lucky.game.robot.dao.LimitDelteConfigDao;
import com.lucky.game.robot.entity.LimitDelteConfigEntity;
import com.lucky.game.robot.service.LimitDelteConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:43
 **/
@Service
public class LimitDelteConfigServiceImpl implements LimitDelteConfigService {

    @Autowired
    private LimitDelteConfigDao limitDelteConfigDao;


    @Override
    public List<LimitDelteConfigEntity> findByUserIdAndMarketType(String userId, String marketType) {
        return limitDelteConfigDao.findByUserIdAndMarketType(userId,marketType);
    }

    @Override
    public List<LimitDelteConfigEntity> findByUserIdAndSymbolAndMarketType(String userId, String symbol, String marketType) {
        return limitDelteConfigDao.findByUserIdAndSymbolAndMarketType(userId,symbol,marketType);
    }

    @Override
    public List<LimitDelteConfigEntity> findByUserId(String userId) {
        return limitDelteConfigDao.findByUserId(userId);
    }

    @Override
    public LimitDelteConfigEntity save(LimitDelteConfigEntity entity) {
        return limitDelteConfigDao.save(entity);
    }

    @Override
    public LimitDelteConfigEntity findById(String oid) {
        return limitDelteConfigDao.findOne(oid);
    }
}
