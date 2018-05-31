package com.lucky.game.robot.service.impl;

import com.lucky.game.robot.service.UserService;
import com.lucky.game.robot.dao.UserDao;
import com.lucky.game.robot.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author conan
 *         2018/3/21 17:23
 **/

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public List<UserEntity> findAllByStatus(String status) {
        return userDao.findAllByStatus(status);
    }

    @Override
    public UserEntity save(UserEntity userEntity) {
        return userDao.save(userEntity);
    }

    @Override
    public UserEntity findOne(String id) {
        return userDao.findOne(id);
    }

    @Override
    public UserEntity findByPhone(String phone) {
        return userDao.findByPhone(phone);
    }
}
