package com.lucky.game.robot.service.impl;

import com.lucky.game.robot.dao.RateChangeDao;
import com.lucky.game.robot.entity.RateChangeEntity;
import com.lucky.game.robot.service.RateChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author conan
 *         2018/3/20 11:15
 **/
@Service
public class RateChangeServiceImpl implements RateChangeService {

    @Autowired
    private RateChangeDao rateChangeDao;

    @Override
    public RateChangeEntity findOne(String id) {
        return rateChangeDao.findOne(id);
    }

    @Override
    public RateChangeEntity save(RateChangeEntity entity) {
        return rateChangeDao.save(entity);
    }
}
