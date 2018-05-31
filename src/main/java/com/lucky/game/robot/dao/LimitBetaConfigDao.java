package com.lucky.game.robot.dao;

import com.lucky.game.robot.entity.LimitBetaConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:42
 **/
public interface LimitBetaConfigDao extends JpaRepository<LimitBetaConfigEntity, String>, JpaSpecificationExecutor<LimitBetaConfigEntity> {

    List<LimitBetaConfigEntity> findByUserIdAndMarketType(String userId, String marketType);

    List<LimitBetaConfigEntity> findByUserId(String userId);

    List<LimitBetaConfigEntity> findByUserIdAndSymbolAndMarketType(String userId,String symbol,String marketType);
}
