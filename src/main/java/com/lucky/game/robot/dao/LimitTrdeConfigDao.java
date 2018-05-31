package com.lucky.game.robot.dao;

import com.lucky.game.robot.entity.LimitTradeConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author conan
 *         2018/3/26 13:37
 **/
public interface LimitTrdeConfigDao extends JpaRepository<LimitTradeConfigEntity, String>, JpaSpecificationExecutor<LimitTradeConfigEntity> {


    @Query(value = "SELECT * FROM T_LIMIT_TRADE_CONFIG where userId = ?1 order by status ASC,createTime desc", nativeQuery = true)
    List<LimitTradeConfigEntity> findAllByUserId(String userId);

    List<LimitTradeConfigEntity> findAllByUserIdAndMarketType(String userId, String marketType);

}
