package com.lucky.game.robot.dao;

import com.lucky.game.robot.entity.LimitGammaConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:42
 **/
public interface LimitGammaConfigDao extends JpaRepository<LimitGammaConfigEntity, String>, JpaSpecificationExecutor<LimitGammaConfigEntity> {

    List<LimitGammaConfigEntity> findByUserIdAndMarketType(String userId, String marketType);

    List<LimitGammaConfigEntity> findByUserIdAndSymbolAndMarketType(String userId,String symbol, String marketType);

    List<LimitGammaConfigEntity> findByUserId(String userId);
}
