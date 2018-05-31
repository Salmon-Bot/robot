package com.lucky.game.robot.dao;

import com.lucky.game.robot.entity.KlineInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author conan
 *         2018/5/31 14:13
 **/
public interface KlineInfoDao  extends JpaRepository<KlineInfoEntity, String>, JpaSpecificationExecutor<KlineInfoEntity> {

    KlineInfoEntity findByKlineId(String klineId);

}
