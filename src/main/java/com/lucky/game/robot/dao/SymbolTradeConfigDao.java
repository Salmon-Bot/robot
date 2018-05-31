package com.lucky.game.robot.dao;

import com.lucky.game.robot.entity.SymbolTradeConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author conan
 *         2018/3/21 17:20
 **/
public interface SymbolTradeConfigDao extends JpaRepository<SymbolTradeConfigEntity, String>, JpaSpecificationExecutor<SymbolTradeConfigEntity> {

    List<SymbolTradeConfigEntity> findByUserId(String userId);

    SymbolTradeConfigEntity findByUserIdAndThresholdType(String userId, String thresholdType);

    SymbolTradeConfigEntity findByUserIdAndThresholdTypeAndMarketType(String userId, String thresholdType, String marketType);
}
