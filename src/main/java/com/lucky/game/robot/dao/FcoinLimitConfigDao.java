package com.lucky.game.robot.dao;

import com.lucky.game.robot.entity.FcoinLimitConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:42
 **/
public interface FcoinLimitConfigDao extends JpaRepository<FcoinLimitConfigEntity, String>, JpaSpecificationExecutor<FcoinLimitConfigEntity> {

    List<FcoinLimitConfigEntity> findByUserIdAndMarketType(String userId, String marketType);

    List<FcoinLimitConfigEntity> findByUserId(String userId);

    List<FcoinLimitConfigEntity> findByUserIdAndSymbolAndMarketType(String userId, String symbol, String marketType);
}
