package com.lucky.game.robot.service.impl;

import com.lucky.game.robot.dao.KlineInfoDao;
import com.lucky.game.robot.entity.KlineInfoEntity;
import com.lucky.game.robot.service.KlineInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author conan
 *         2018/5/31 14:23
 **/
@Service
public class KlineInfoServiceImpl implements KlineInfoService{

    @Autowired
    private KlineInfoDao klineInfoDao;

    @Override
    public void insert(KlineInfoEntity entity) {
        klineInfoDao.save(entity);
    }

    @Override
    public KlineInfoEntity findByKlineId(String klineId) {
        return klineInfoDao.findByKlineId(klineId);
    }
}
