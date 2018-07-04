package com.lucky.game.robot.service.impl;

import com.lucky.game.robot.dao.FcoinLimitConfigDao;
import com.lucky.game.robot.entity.FcoinLimitConfigEntity;
import com.lucky.game.robot.service.FcoinLimitConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:43
 **/
@Service
public class FcoinLimitConfigServiceImpl implements FcoinLimitConfigService {

    @Autowired
    private FcoinLimitConfigDao fcoinLimitConfigDao;

    @Override
    public FcoinLimitConfigEntity save(FcoinLimitConfigEntity entity) {
        return fcoinLimitConfigDao.save(entity);
    }

    @Override
    public List<FcoinLimitConfigEntity> findByUserId(String userId) {
        return fcoinLimitConfigDao.findByUserId(userId);
    }

    @Override
    public List<FcoinLimitConfigEntity> findAll() {
        return fcoinLimitConfigDao.findAll();
    }

    @Override
    public FcoinLimitConfigEntity findById(String id) {
        return fcoinLimitConfigDao.getOne(id);
    }
}
