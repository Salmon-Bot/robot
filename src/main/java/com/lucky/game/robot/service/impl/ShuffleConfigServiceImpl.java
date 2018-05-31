package com.lucky.game.robot.service.impl;

import com.lucky.game.robot.dao.ShuffleConfigDao;
import com.lucky.game.robot.entity.ShuffleConfigEntity;
import com.lucky.game.robot.service.ShuffleConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author conan
 *         2018/4/11 16:35
 **/
@Service
public class ShuffleConfigServiceImpl implements ShuffleConfigService {

    @Autowired
    private ShuffleConfigDao shuffleConfigDao;

    @Override
    public List<ShuffleConfigEntity> findByUserId(String userId) {
        return shuffleConfigDao.findByUserId(userId);
    }

    @Override
    public List<ShuffleConfigEntity> findByUserIdWithOpen(String userId) {
        return shuffleConfigDao.findByUserIdWithOpen(userId);
    }
}
