package com.lucky.game.robot.service.impl;

import com.lucky.game.robot.service.AccountService;
import com.lucky.game.robot.dao.AccountDao;
import com.lucky.game.robot.entity.AccountEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author conan
 *         2018/3/30 15:14
 **/
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountDao accountDao;

    @Override
    public AccountEntity findByUserIdAndType(String userId, String type) {
        return accountDao.findByUserIdAndType(userId, type);
    }

    @Override
    public List<AccountEntity> findByTypeAndStatus(String type, String status) {
        return accountDao.findByTypeAndStatus(type,status);
    }

    @Override
    public AccountEntity save(AccountEntity entity) {
        return accountDao.save(entity);
    }
}
