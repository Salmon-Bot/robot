package com.lucky.game.robot.service.impl;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.service.LimitTradeConfigService;
import com.lucky.game.robot.dao.LimitTrdeConfigDao;
import com.lucky.game.robot.entity.LimitTradeConfigEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author conan
 *         2018/3/26 13:51
 **/
@Service
public class LimitTradeConfigServiceImpl implements LimitTradeConfigService {

    @Autowired
    private LimitTrdeConfigDao limitTrdeConfigDao;

    @Override
    public List<LimitTradeConfigEntity> findAllByUserIdAndMarketType(String userId, String marketType) {
        List<LimitTradeConfigEntity> list = limitTrdeConfigDao.findAllByUserIdAndMarketType(userId, marketType);
        List<LimitTradeConfigEntity> newList = new ArrayList<>();
        for (LimitTradeConfigEntity entity : list) {
            if (DictEnum.IS_DELETE_NO.getCode().equals(entity.getIsDelete()) && DictEnum.STATUS_OPEN.getCode().equals(entity.getStatus())) {
                newList.add(entity);
            }
        }
        return newList;
    }

    @Override
    public List<LimitTradeConfigEntity> findAllByUserId(String userId) {
        return limitTrdeConfigDao.findAllByUserId(userId);
    }

    @Override
    public LimitTradeConfigEntity findById(String oid) {
        return limitTrdeConfigDao.findOne(oid);
    }

    @Override
    public LimitTradeConfigEntity save(LimitTradeConfigEntity entity) {
        return limitTrdeConfigDao.save(entity);
    }
}
