package com.lucky.game.robot.dao;

import com.lucky.game.robot.entity.ShuffleConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author conan
 *         2018/3/30 15:10
 **/
public interface ShuffleConfigDao extends JpaRepository<ShuffleConfigEntity, String>, JpaSpecificationExecutor<ShuffleConfigEntity> {

    @Query(value = "select * from T_SHUFFLE_CONFIG  where isDelete = 'no' and userId=?1", nativeQuery = true)
    List<ShuffleConfigEntity> findByUserId(String userId);

    @Query(value = "select * from T_SHUFFLE_CONFIG  where status = 'open' and isDelete = 'no' and userId=?1", nativeQuery = true)
    List<ShuffleConfigEntity> findByUserIdWithOpen(String userId);

}
