package com.lucky.game.robot.service;

import com.lucky.game.robot.entity.AccountEntity;

import java.util.List;

/**
 * @author conan
 *         2018/3/30 15:12
 **/
public interface AccountService {

    AccountEntity findByUserIdAndType(String userId, String type);

    List<AccountEntity> findByTypeAndStatus(String type,String status);

    AccountEntity save(AccountEntity entity);
}
