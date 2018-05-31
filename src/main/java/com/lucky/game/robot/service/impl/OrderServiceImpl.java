package com.lucky.game.robot.service.impl;

import com.lucky.game.robot.dao.OrderDao;
import com.lucky.game.robot.entity.OrderEntity;
import com.lucky.game.robot.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author conan
 *         2018/1/5 11:24
 **/
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Override
    public OrderEntity findByOrderId(String orderId) {
        return orderDao.findByOrderId(orderId);
    }

    @Override
    public OrderEntity findOne(String oid) {
        return orderDao.findOne(oid);
    }

    @Override
    public OrderEntity save(OrderEntity entity) {
        return orderDao.save(entity);
    }

    @Override
    public List<OrderEntity> findByState(List<String> states, String type, String marketType) {
        return orderDao.findByState(states, type, marketType);
    }

    @Override
    public List<OrderEntity> findBySymbolAndType(String symbol, String orderType, String symbolConfigId, List<String> states) {
        return orderDao.findBySymbolAndType(symbol, orderType, symbolConfigId, states);
    }

    @Override
    public List<OrderEntity> findByParam(String userId, String model, String orderType, String symbol, String symbolTradeConfigId, String marketType, List<String> states) {
        return orderDao.findByParam(userId, model, orderType, symbol, symbolTradeConfigId, marketType, states);
    }

    @Override
    public OrderEntity findOneByParam(String userId, String model, String symbol, String symbolTradeConfigId, String marketType, List<String> states) {
        return orderDao.findOneByParam(userId, model, symbol, symbolTradeConfigId, marketType, states);
    }

    @Override
    public List<OrderEntity> findByMarket(String model, String marketType, List<String> states) {
        return orderDao.findByMarket(model, marketType, states);
    }

    @Override
    public List<OrderEntity> findNofinishDelteByMarket(String model, String marketType,String isFinish, List<String> states) {
        return orderDao.findNofinishDelteByMarket(model,marketType,isFinish,states);
    }

    @Override
    public List<OrderEntity> findByTypeAndState(String type, List<String> states) {
        return orderDao.findByTypeAndState(type, states);
    }

    @Override
    public List<OrderEntity> findByUserIdAndModel(String userId, String model) {
        return orderDao.findByUserIdAndModel(userId, model);
    }

    @Override
    public Page<OrderEntity> findAll(Specification<OrderEntity> spec, Pageable pageable) {
        return orderDao.findAll(spec, pageable);
    }

    @Override
    public BigDecimal findByTypeBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime) {
        return orderDao.findByTypeBuyTotalAmount(userId, model, hbState, zbState, startTime, endTime);
    }

    @Override
    public BigDecimal findByTypeSellTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime) {
        return orderDao.findByTypeSellTotalAmount(userId, model, hbState, zbState, startTime, endTime);
    }

    @Override
    public BigDecimal findRealBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime) {
        return orderDao.findRealBuyTotalAmount(userId, model, hbState, zbState, startTime, endTime);
    }

    @Override
    public BigDecimal findLimitBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime) {
        return orderDao.findLimitBuyTotalAmount(userId, model, hbState, zbState, startTime, endTime);
    }

    @Override
    public BigDecimal findLimitSellTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime) {
        return orderDao.findLimitSellTotalAmount(userId, model, hbState, zbState, startTime, endTime);
    }

    @Override
    public BigDecimal findLimitBetaBuyTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime) {
        return orderDao.findLimitBetaBuyTotalAmount(userId, model, hbState, zbState, startTime, endTime);
    }

    @Override
    public BigDecimal findLimitBetaSellTotalAmount(String userId, String model, String hbState, String zbState, Date startTime, Date endTime) {
        return orderDao.findLimitBetaSellTotalAmount(userId, model, hbState, zbState, startTime, endTime);
    }

    @Override
    public OrderEntity findByBuyOrderId(String buyOrderId) {
        return orderDao.findByBuyOrderId(buyOrderId);
    }
}