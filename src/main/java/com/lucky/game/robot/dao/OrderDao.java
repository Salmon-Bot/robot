package com.lucky.game.robot.dao;

import com.lucky.game.robot.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author conan
 *         2017/10/26 13:41
 **/
public interface OrderDao extends JpaRepository<OrderEntity, String>, JpaSpecificationExecutor<OrderEntity> {

    OrderEntity findByOrderId(String orderId);

    OrderEntity findByBuyOrderId(String buyOrderId);

    @Query(value = "select * from T_ORDER  where state in (?1) and type = ?2  and marketType=?3 and model = 'real'", nativeQuery = true)
    List<OrderEntity> findByState(List<String> states, String orderType, String marketType);

    @Query(value = "select * from T_ORDER  where symbol =?1  and type = ?2 and symbolTradeConfigId=?3 and state in (?4) and model = 'real'", nativeQuery = true)
    List<OrderEntity> findBySymbolAndType(String symbol, String orderType, String symbolConfigId, List<String> states);

    @Query(value = "select * from T_ORDER  where userId =?1  and model = ?2 and type = ?3 and symbol = ?4 and symbolTradeConfigId=?5 and marketType=?6 and state in (?7) and isFinish='no'", nativeQuery = true)
    List<OrderEntity> findByParam(String userId, String model, String orderType, String symbol, String symbolTradeConfigId, String marketType, List<String> states);

    @Query(value = "select * from T_ORDER  where userId =?1  and model = ?2 and symbol = ?3 and symbolTradeConfigId=?4 and marketType=?5 and state in (?6)", nativeQuery = true)
    OrderEntity findOneByParam(String userId, String model, String symbol, String symbolTradeConfigId, String marketType, List<String> states);


    @Query(value = "select * from T_ORDER  where  model = ?1 and marketType=?2 and state in (?3)", nativeQuery = true)
    List<OrderEntity> findByMarket(String model, String marketType, List<String> states);

    @Query(value = "select * from T_ORDER  where  model = ?1 and marketType=?2 and isFinish = ?3 and state in (?4)", nativeQuery = true)
    List<OrderEntity> findNofinishDelteByMarket(String model, String marketType,String isFinish, List<String> states);



    @Query(value = "select * from T_ORDER  where  type = ?1 and state in (?2) and (model = 'limit' or model='limitBeta' or model='limitGamma') ", nativeQuery = true)
    List<OrderEntity> findByTypeAndState(String type, List<String> states);

    List<OrderEntity> findByUserIdAndModel(String userId, String model);

    @Query(value = "SELECT sum(totalToUsdt) FROM T_ORDER where userId = ?1 " +
            "and model=?2 and type = 'buy-limit' and (state = ?3 or state=?4)" +
            "and orderId  in (SELECT buyOrderId FROM T_ORDER where userId = ?1 \n" +
            "and model=?2 and type = 'sell-limit' and (state = ?3 or state=?4) and updateTime >= ?5 and updateTime <= ?6) ", nativeQuery = true)
    BigDecimal findByTypeBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    @Query(value = "SELECT SUM(totalToUsdt) FROM T_ORDER where userId = ?1 " +
            "and model=?2 and type = 'sell-limit' and (state = ?3 or state=?4) " +
            "and updateTime >= ?5 and updateTime <= ?6 ", nativeQuery = true)
    BigDecimal findByTypeSellTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    @Query(value = "SELECT sum(totalToUsdt) FROM T_ORDER where userId = ?1 " +
            "and model=?2 and type = 'buy-limit' and (state = 'sell' or state='4')" +
            "and orderId  in (SELECT buyOrderId FROM T_ORDER where userId = ?1 \n" +
            "and model=?2 and type = 'sell-limit' and (state = ?3 or state=?4) and updateTime >= ?5 and updateTime <= ?6) ", nativeQuery = true)
    BigDecimal findRealBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    @Query(value = "SELECT sum(totalToUsdt) FROM T_ORDER where userId = ?1 \n" +
            "and model=?2 and type = 'buy-limit' and (state = ?3 or state=?4) \n" +
            "and updateTime >= ?5 and updateTime <= ?6 " +
            "and orderId in \n" +
            "(SELECT buyOrderId FROM T_ORDER where userId = ?1 " +
            "and model=?2 and type = 'sell-limit' and (state = ?3 or state=?4) \n" +
            "and createTime >= ?5 and createTime <= ?6)", nativeQuery = true)
    BigDecimal findLimitBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    @Query(value = "SELECT sum(totalToUsdt) FROM T_ORDER where userId = ?1 \n" +
            "and model=?2 and type = 'sell-limit' and (state = ?3 or state=?4) \n" +
            "and createTime >= ?5 and createTime <= ?6 and buyOrderId in \n" +
            "(SELECT orderId FROM T_ORDER where userId = ?1 \n" +
            "and model=?2 and type = 'buy-limit' and (state = ?3 or state=?4) \n" +
            "and updateTime >= ?5 and updateTime <= ?6)", nativeQuery = true)
    BigDecimal findLimitSellTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    @Query(value = "SELECT sum(totalToUsdt) FROM T_ORDER where userId = ?1 " +
            "and model=?2 and type = 'buy-limit' and (state = 'sell' or state=4) " +
            "and updateTime >= ?5 and updateTime <= ?6 " +
            "and orderId in " +
            "(SELECT buyOrderId FROM T_ORDER where userId = ?1 " +
            "and model=?2 and type = 'sell-limit' and (state = ?3 or state= ?4) " +
            "and createTime >= ?5 and createTime <= ?6)", nativeQuery = true)
    BigDecimal findLimitBetaBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    @Query(value = "SELECT sum(totalToUsdt) FROM T_ORDER where userId = ?1 " +
            "and model=?2 and type = 'sell-limit' and (state = ?3 or state=?4) " +
            "and createTime >= ?5 and createTime <= ?6 and buyOrderId in " +
            "(SELECT orderId FROM T_ORDER where userId = ?1 \n" +
            "and model=?2 and type = 'buy-limit' and (state = 'sell' or state=4 ) \n" +
            "and updateTime >= ?5 and updateTime <= ?6)", nativeQuery = true)
    BigDecimal findLimitBetaSellTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);
}


