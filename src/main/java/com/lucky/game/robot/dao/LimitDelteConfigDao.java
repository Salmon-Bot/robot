package com.lucky.game.robot.dao;

import com.lucky.game.robot.entity.LimitDelteConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author conan
 *         2018/4/16 10:42
 **/
public interface LimitDelteConfigDao extends JpaRepository<LimitDelteConfigEntity, String>, JpaSpecificationExecutor<LimitDelteConfigEntity> {

    List<LimitDelteConfigEntity> findByUserIdAndMarketType(String userId, String marketType);

    List<LimitDelteConfigEntity> findByUserIdAndSymbolAndMarketType(String userId, String symbol, String marketType);

    List<LimitDelteConfigEntity> findByUserId(String userId);
}
