package com.lucky.game.robot.service;

import com.lucky.game.robot.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author conan
 *         2018/1/5 11:23
 **/
public interface OrderService {


    OrderEntity findByOrderId(String orderId);

    OrderEntity findOne(String oid);

    OrderEntity save(OrderEntity entity);

    List<OrderEntity> findByState(List<String> states, String type, String marketType);

    List<OrderEntity> findBySymbolAndType(String symbol, String orderType, String symbolConfigId, List<String> states);

    List<OrderEntity> findByParam(String userId, String model, String orderType, String symbol, String symbolTradeConfigId, String marketType, List<String> states);

    OrderEntity findOneByParam(String userId, String model,String symbol, String symbolTradeConfigId, String marketType, List<String> states);

    List<OrderEntity> findByMarket(String model, String marketType, List<String> states);

    List<OrderEntity> findNofinishDelteByMarket(String model, String marketType,String isFinish, List<String> states);

    List<OrderEntity> findByTypeAndState(String type, List<String> states);

    List<OrderEntity> findByUserIdAndModel(String userId, String model);

    Page<OrderEntity> findAll(Specification<OrderEntity> spec, Pageable pageable);

    BigDecimal findByTypeBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    BigDecimal findByTypeSellTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    BigDecimal findRealBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    BigDecimal findLimitBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    BigDecimal findLimitSellTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    BigDecimal findLimitBetaBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    BigDecimal findLimitBetaSellTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime);

    OrderEntity findByBuyOrderId(String buyOrderId);
}
