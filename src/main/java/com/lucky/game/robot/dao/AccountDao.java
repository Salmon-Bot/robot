package com.lucky.game.robot.dao;

import com.lucky.game.robot.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author conan
 *         2018/3/30 15:10
 **/
public interface AccountDao extends JpaRepository<AccountEntity, String>, JpaSpecificationExecutor<AccountEntity> {

    AccountEntity findByUserIdAndType(String userId, String type);

    List<AccountEntity> findByTypeAndStatus(String type,String status);
}
