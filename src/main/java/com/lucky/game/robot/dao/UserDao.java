package com.lucky.game.robot.dao;

import com.lucky.game.robot.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author conan
 *         2018/3/21 17:20
 **/
public interface UserDao extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {

    UserEntity findByPhone(String phone);

    List<UserEntity> findAllByStatus(String status);
}
