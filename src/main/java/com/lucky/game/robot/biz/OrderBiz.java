package com.lucky.game.robot.biz;

import com.lucky.game.core.constant.ResponseData;
import com.lucky.game.core.util.DateUtils;
import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.constant.ErrorEnum;
import com.lucky.game.robot.vo.OrderVo;
import com.lucky.game.robot.dto.client.OrderDto;
import com.lucky.game.robot.dto.huobi.HuobiBaseDto;
import com.lucky.game.robot.dto.zb.ZbOrderDetailDto;
import com.lucky.game.robot.entity.AccountEntity;
import com.lucky.game.robot.entity.OrderEntity;
import com.lucky.game.robot.exception.BizException;
import com.lucky.game.robot.huobi.response.OrdersDetail;
import com.lucky.game.robot.market.HuobiApi;
import com.lucky.game.robot.service.OrderService;
import com.lucky.game.robot.vo.StatisticsVo;
import com.lucky.game.robot.vo.huobi.MarketInfoVo;
import com.lucky.game.robot.zb.api.ZbApi;
import com.lucky.game.robot.zb.vo.ZbOrderDetailVo;
import com.lucky.game.robot.zb.vo.ZbTickerVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author conan
 *         2018/3/20 10:38
 **/
@Component
@Slf4j
public class OrderBiz {

    @Autowired
    private TradeBiz tradeBiz;

    @Autowired
    private OrderService orderService;

    @Autowired
    private HuobiApi huobiApi;

    @Autowired
    private ZbApi zbApi;

    @Autowired
    private AccountBiz accountBiz;


    /**
     * hb该交易对是否存在未完成的买单/卖单
     */
    public boolean hbExistNotFinishOrder(String symbol, String type, String symbolConfigId) {
        boolean result = false;
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_PRE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTING.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_PARTIAL_FILLED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_PARTIAL_CANCELED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_FILLED.getCode());
        List<OrderEntity> orderEntityList = orderService.findBySymbolAndType(symbol, type, symbolConfigId, states);
        for (OrderEntity orderEntity : orderEntityList) {
            if (DateUtils.addDay(orderEntity.getCreateTime(), 1).after(DateUtils.getCurrDateMmss())) {
                log.info("一天之内已有未完成的订单,orderEntity={}", orderEntity);
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 保存hb下单记录
     */
    public OrderEntity saveHbOrder(String orderId, String rateChangeId, String buyOrderId, String symbolTradeConfigId, String userId, String orderType, String model) {
        HuobiBaseDto dto = new HuobiBaseDto();
        dto.setOrderId(orderId);
        dto.setUserId(userId);
        OrdersDetail ordersDetail = tradeBiz.getHbOrderDetail(dto);
        OrderEntity orderEntity = orderService.findByOrderId(orderId);
        if (orderEntity == null) {
            orderEntity = new OrderEntity();
            orderEntity.setCreateTime(DateUtils.getSqlCurrentDate());
        }
        BeanUtils.copyProperties(ordersDetail, orderEntity);
        orderEntity.setRateChangeId(rateChangeId);
        orderEntity.setOrderId(ordersDetail.getId());
        orderEntity.setBuyOrderId(buyOrderId);
        BigDecimal totalToUsdt = getTotalToUsdt(orderEntity.getSymbol(), orderEntity.getPrice(), orderEntity.getAmount());
        orderEntity.setTotalToUsdt(totalToUsdt);
        orderEntity.setSymbolTradeConfigId(symbolTradeConfigId);
        orderEntity.setUserId(userId);
        orderEntity.setType(orderType);
        orderEntity.setModel(model);
        orderEntity.setMarketType(DictEnum.MARKET_TYPE_HB.getCode());
        return this.saveOrder(orderEntity);
    }

    /**
     * 同步hb订单最新状态
     */
    public OrderEntity updateHbOrderState(OrderEntity orderEntity) {
        HuobiBaseDto dto = new HuobiBaseDto();
        dto.setOrderId(orderEntity.getOrderId());
        dto.setUserId(orderEntity.getUserId());

        OrdersDetail ordersDetail = tradeBiz.getHbOrderDetail(dto);
        //订单状态或者成交数量有变动
        if (ordersDetail != null && (!ordersDetail.getState().equals(orderEntity.getState()) || !ordersDetail.getFieldAmount().stripTrailingZeros().equals(orderEntity.getFieldAmount().stripTrailingZeros()))) {
            BeanUtils.copyProperties(ordersDetail, orderEntity);
            if (DictEnum.ORDER_DETAIL_STATE_FILLED.getCode().equals(orderEntity.getState()) || DictEnum.ORDER_DETAIL_STATE_PARTIAL_CANCELED.getCode().equals(orderEntity.getState())) {
                BigDecimal totalToUsdt = getTotalToUsdt(orderEntity.getSymbol(), ordersDetail.getPrice(), ordersDetail.getFieldAmount());
                orderEntity.setAmount(ordersDetail.getFieldAmount());
                orderEntity.setTotalToUsdt(totalToUsdt);
            }
            orderEntity.setState(ordersDetail.getState());
            orderEntity.setFieldAmount(ordersDetail.getFieldAmount());
            orderEntity.setFieldCashAmount(ordersDetail.getFieldCashAmount());
            orderEntity.setFieldFees(ordersDetail.getFieldFees());
            orderEntity = this.saveOrder(orderEntity);
        }
        return orderEntity;
    }

    /**
     * zb该交易对是否存在未完成的买单/卖单
     */
    public boolean zbExistNotFinishOrder(String symbol, String type, String symbolConfigId) {
        boolean result = false;
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_0.getCode());
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode());
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode());
        List<OrderEntity> orderEntityList = orderService.findBySymbolAndType(symbol, type, symbolConfigId, states);
        for (OrderEntity orderEntity : orderEntityList) {
            if (DateUtils.addDay(orderEntity.getCreateTime(), 1).after(DateUtils.getCurrDateMmss())) {
                log.info("一天之内已有未完成的订单,orderEntity={}", orderEntity);
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 保存zb下单记录
     */
    public OrderEntity saveZbOrder(String orderId, String symbol, String rateChangeId, String buyOrderId, String symbolTradeConfigId, String userId, String orderType, String model) {

        ZbOrderDetailDto dto = new ZbOrderDetailDto();
        dto.setOrderId(orderId);
        dto.setCurrency(symbol);
        AccountEntity accountEntity = accountBiz.getByUserIdAndType(userId, DictEnum.MARKET_TYPE_ZB.getCode());
        dto.setSecretKey(accountEntity.getApiSecret());
        dto.setAccessKey(accountEntity.getApiKey());

        ZbOrderDetailVo detailVo = zbApi.orderDetail(dto);
        OrderEntity orderEntity = orderService.findByOrderId(orderId);
        if (orderEntity == null) {
            orderEntity = new OrderEntity();
        }
        BeanUtils.copyProperties(detailVo, orderEntity);
        orderEntity.setRateChangeId(rateChangeId);
        orderEntity.setOrderId(detailVo.getId());
        orderEntity.setBuyOrderId(buyOrderId);
        BigDecimal totalToUsdt = getTotalToUsdt(orderEntity.getSymbol(), orderEntity.getPrice(), orderEntity.getAmount());
        orderEntity.setSymbolTradeConfigId(symbolTradeConfigId);
        orderEntity.setTotalToUsdt(totalToUsdt);
        orderEntity.setUserId(userId);
        orderEntity.setType(orderType);
        orderEntity.setModel(model);
        orderEntity.setMarketType(DictEnum.MARKET_TYPE_ZB.getCode());
        return this.saveOrder(orderEntity);
    }

    /**
     * 同步zb订单最新状态
     */
    public OrderEntity updateZbOrderState(OrderEntity orderEntity) {
        ZbOrderDetailDto dto = new ZbOrderDetailDto();
        dto.setOrderId(orderEntity.getOrderId());
        dto.setCurrency(orderEntity.getSymbol());
        AccountEntity accountEntity = accountBiz.getByUserIdAndType(orderEntity.getUserId(), DictEnum.MARKET_TYPE_ZB.getCode());
        dto.setSecretKey(accountEntity.getApiSecret());
        dto.setAccessKey(accountEntity.getApiKey());

        ZbOrderDetailVo detailVo = zbApi.orderDetail(dto);
        //订单状态或者成交数量有变动
        if (detailVo != null && (!detailVo.getState().equals(orderEntity.getState()) || detailVo.getFieldAmount().compareTo(orderEntity.getFieldAmount()) != 0)) {
            orderEntity.setFieldAmount(detailVo.getFieldAmount());
            orderEntity.setFieldCashAmount(detailVo.getFieldCashAmount());
            BigDecimal totalToUsdt = getTotalToUsdt(orderEntity.getSymbol(), detailVo.getPrice(), detailVo.getFieldAmount());
            orderEntity.setTotalToUsdt(totalToUsdt);
            orderEntity.setState(detailVo.getState());
            //zb 部分成交时订单取消时更新状态为已成交，数量为部分成交的数量
            if (DictEnum.ZB_ORDER_DETAIL_STATE_1.getCode().equals(detailVo.getState()) && detailVo.getFieldAmount().compareTo(BigDecimal.ZERO) > 0) {
                orderEntity.setAmount(detailVo.getFieldAmount());
                orderEntity.setState(DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode());
            } else if (DictEnum.ZB_ORDER_DETAIL_STATE_1.getCode().equals(detailVo.getState())) {
                //更新为撤销状态
                orderEntity.setState(DictEnum.ZB_ORDER_DETAIL_STATE_1.getCode());
            }
            orderEntity = this.saveOrder(orderEntity);
        }
        return orderEntity;
    }

    public OrderEntity saveOrder(OrderEntity entity) {
        return orderService.save(entity);
    }

    /**
     * 获取未完成的hb实时买单(部分成交,部分成交撤销，完全成交)
     */
    public List<OrderEntity> findHbRealNoFilledBuyOrder() {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_PARTIAL_FILLED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_PARTIAL_CANCELED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_FILLED.getCode());
        return orderService.findByState(states, DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.MARKET_TYPE_HB.getCode());
    }

    /**
     * 获取未完成的hb实时卖单(已提交,部分成交,部分成交撤销)
     */
    public List<OrderEntity> findHbRealNoFilledSaleOrder() {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTING.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_PARTIAL_FILLED.getCode());
        return orderService.findByState(states, DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.MARKET_TYPE_HB.getCode());

    }


    /**
     * 获取hb限价单
     */
    public List<OrderEntity> findHbByUserIdAndModel(String userId, String model, String orderType, String symbol, String symbolTradeConfigId) {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_PRE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTING.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_PARTIAL_FILLED.getCode());
        return orderService.findByParam(userId, model, orderType, symbol, symbolTradeConfigId, DictEnum.MARKET_TYPE_HB.getCode(), states);

    }


    /**
     * 根据条件获取单条未完成的订单记录
     */
    public OrderEntity findOneByParam(String userId, String model, String symbol, String symbolTradeConfigId) {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode());
        return orderService.findOneByParam(userId, model, symbol, symbolTradeConfigId, DictEnum.MARKET_TYPE_HB.getCode(), states);

    }


    /**
     * 获取未完成的zb买单(未成交,部分成交，完全成交)
     */
    public List<OrderEntity> findZbNoFilledBuyOrder() {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_0.getCode());
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode());
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode());
        return orderService.findByState(states, DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.MARKET_TYPE_ZB.getCode());
    }

    /**
     * 获取未完成的zb卖单(已提交,部分成交)
     */
    public List<OrderEntity> findZbNoFilledSaleOrder() {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_0.getCode());
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode());
        return orderService.findByState(states, DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.MARKET_TYPE_ZB.getCode());

    }

    /**
     * 获取zb限价单
     */
    public List<OrderEntity> findZbByUserIdAndModel(String userId, String model, String orderType, String symbol, String symbolTradeConfigId) {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_0.getCode());
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode());
        return orderService.findByParam(userId, model, orderType, symbol, symbolTradeConfigId, DictEnum.MARKET_TYPE_ZB.getCode(), states);
    }

    /**
     * 获取统计信息
     */
    public StatisticsVo getTotalStatistics(String userId, Date startTime, Date endTime) {
        if (startTime == null) {
            startTime = DateUtils.parse("2018-04-01");
        }
        if (endTime == null) {
            endTime = DateUtils.parse("2118-04-01");
        }
        //real buy total
        BigDecimal realBuyTotal = orderService.findRealBuyTotalAmount(userId, DictEnum.ORDER_MODEL_REAL.getCode(), DictEnum.ORDER_DETAIL_STATE_FILLED.getCode(), DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode(), startTime, endTime);
        //real sell total
        BigDecimal realSellTotal = orderService.findByTypeSellTotalAmount(userId, DictEnum.ORDER_MODEL_REAL.getCode(), DictEnum.ORDER_DETAIL_STATE_FILLED.getCode(), DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode(), startTime, endTime);
        //limit buy total
        BigDecimal limitBuyTotal = orderService.findLimitBuyTotalAmount(userId, DictEnum.ORDER_MODEL_LIMIT.getCode(), DictEnum.ORDER_DETAIL_STATE_FILLED.getCode(), DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode(), startTime, endTime);
        //limit sell total
        BigDecimal limitSellTotal = orderService.findLimitSellTotalAmount(userId, DictEnum.ORDER_MODEL_LIMIT.getCode(), DictEnum.ORDER_DETAIL_STATE_FILLED.getCode(), DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode(), startTime, endTime);

        //limit beta buy total
        BigDecimal limitBetaBuyTotal = orderService.findLimitBetaBuyTotalAmount(userId, DictEnum.ORDER_MODEL_LIMIT_BETA.getCode(), DictEnum.ORDER_DETAIL_STATE_FILLED.getCode(), DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode(), startTime, endTime);
        //limit beta sell total
        BigDecimal limitBetaSellTotal = orderService.findLimitBetaSellTotalAmount(userId, DictEnum.ORDER_MODEL_LIMIT_BETA.getCode(), DictEnum.ORDER_DETAIL_STATE_FILLED.getCode(), DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode(), startTime, endTime);
        //shuffle buy total
        BigDecimal shuffleBuyTotal = orderService.findByTypeBuyTotalAmount(userId, DictEnum.ORDER_MODEL_SHUFFLE.getCode(), DictEnum.ORDER_DETAIL_STATE_FILLED.getCode(), DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode(), startTime, endTime);
        //shuffle sell total
        BigDecimal shuffleSellTotal = orderService.findByTypeSellTotalAmount(userId, DictEnum.ORDER_MODEL_SHUFFLE.getCode(), DictEnum.ORDER_DETAIL_STATE_FILLED.getCode(), DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode(), startTime, endTime);
        StatisticsVo vo = new StatisticsVo();
        vo.setRealBuyTotal(realBuyTotal);
        vo.setRealSellTotal(realSellTotal);
        vo.setLimitAlphaBuyTotal(limitBuyTotal);
        vo.setLimitAlphaSellTotal(limitSellTotal);
        vo.setLimitBetaBuyTotal(limitBetaBuyTotal);
        vo.setLimitBetaSellTotal(limitBetaSellTotal);
        vo.setShuffleBuyTotal(shuffleBuyTotal);
        vo.setShuffleSellTotal(shuffleSellTotal);
        return vo;
    }

    /**
     * 实时单列表
     */
    public ResponseData findRealOrderList(OrderDto dto, String userId) {
        return findByModel(dto, userId, DictEnum.ORDER_MODEL_REAL.getCode());
    }

    /**
     * 限价单列表
     */
    public ResponseData findLimitOrderList(OrderDto dto, String userId) {
        return findByModel(dto, userId, DictEnum.ORDER_MODEL_LIMIT.getCode());
    }


    /**
     * beta限价单列表
     */
    public ResponseData findBetaLimitOrderList(OrderDto dto, String userId) {
        return findByModel(dto, userId, DictEnum.ORDER_MODEL_LIMIT_BETA.getCode());
    }


    /**
     * 根据订单表id撤销订单
     */
    public ResponseData cancelOrder(String oid) {
        ResponseData response = ResponseData.failure(ErrorEnum.CANCEL_ORDER_FAIL.getCode(), ErrorEnum.CANCEL_ORDER_FAIL.getValue());
        OrderEntity entity = orderService.findOne(oid);
        if (entity == null) {
            throw new BizException(ErrorEnum.RECOLD_NOT_FOUND);
        }
        HuobiBaseDto dto = new HuobiBaseDto();
        dto.setOrderId(entity.getOrderId());
        dto.setUserId(entity.getUserId());
        //暂时只撤销hb订单,zb逻辑待添加
        if (DictEnum.MARKET_TYPE_HB.getCode().equals(entity.getMarketType())) {
            tradeBiz.hbCancelOrder(dto);
            entity = this.updateHbOrderState(entity);
            //撤销成功
            if (DictEnum.ORDER_DETAIL_STATE_CANCELED.getCode().equals(entity.getState()) || DictEnum.ORDER_DETAIL_STATE_PARTIAL_CANCELED.getCode().equals(entity.getState())) {
                response = ResponseData.success();
            }
        }
        return response;
    }

    /**
     * 获取hb未完成的搬砖单
     */
    public List<OrderEntity> findHbShuffleNoFillOrder() {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_PRE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTING.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_PARTIAL_FILLED.getCode());
        return orderService.findByMarket(DictEnum.ORDER_MODEL_SHUFFLE.getCode(), DictEnum.MARKET_TYPE_HB.getCode(), states);
    }

    /**
     * 获取zb未完成的搬砖单
     */
    public List<OrderEntity> findZbShuffleNoFillOrder() {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_0.getCode());
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode());
        return orderService.findByMarket(DictEnum.ORDER_MODEL_SHUFFLE.getCode(), DictEnum.MARKET_TYPE_ZB.getCode(), states);
    }


    /**
     * 获取hb未完成的一条beta/gamma/detle限价单
     */
    public OrderEntity findHbLimitOrder(String userId, String symbol, String symbolTradeConfigId, String model) {
        OrderEntity orderEntity = null;
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_PRE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTING.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_PARTIAL_FILLED.getCode());
        //beta查询买单时,filled状态还未处理卖单,gamma查询买单时,只要是fulled状态就是已完成
        if (model.equals(DictEnum.ORDER_MODEL_LIMIT_BETA.getCode())) {
            states.add(DictEnum.ORDER_DETAIL_STATE_FILLED.getCode());
        }
        List<OrderEntity> list = orderService.findByParam(userId, model, DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), symbol, symbolTradeConfigId, DictEnum.MARKET_TYPE_HB.getCode(), states);
        //买单不存在,查询未完成的卖单
        if (list == null || list.isEmpty()) {
            states.remove(DictEnum.ORDER_DETAIL_STATE_FILLED.getCode());
            list = orderService.findByParam(userId, model, DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), symbol, symbolTradeConfigId,
                    DictEnum.MARKET_TYPE_HB.getCode(), states);
            //卖单存在
            if (list != null && !list.isEmpty()) {
                orderEntity = list.get(0);
            }
        } else {
            orderEntity = list.get(0);
        }
        return orderEntity;
    }

    /**
     * 未售出的delte 买单
     */
    public OrderEntity findHbDelteBuyOrder(String userId, String symbol, String symbolTradeConfigId, String model) {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_FILLED.getCode());
        List<OrderEntity> list = orderService.findByParam(userId, model, DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), symbol, symbolTradeConfigId, DictEnum.MARKET_TYPE_HB.getCode(), states);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /**
     * 未买入的delte 卖单
     */
    public OrderEntity findHbDelteSellOrder(String userId, String symbol, String symbolTradeConfigId, String model) {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_FILLED.getCode());
        List<OrderEntity> list = orderService.findByParam(userId, model, DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), symbol, symbolTradeConfigId, DictEnum.MARKET_TYPE_HB.getCode(), states);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /**
     * hb detle 未结束的订单
     */
    public List<OrderEntity> hbDetleNotFinishOrder() {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_PRE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTING.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_PARTIAL_FILLED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_FILLED.getCode());
        return  orderService.findNofinishDelteByMarket(DictEnum.ORDER_MODEL_LIMIT_DELTE.getCode(),DictEnum.MARKET_TYPE_HB.getCode(),DictEnum.IS_FINISH_NO.getCode(),states);
    }

    /**
     * hb detle 未完成的订单
     */
    public List<OrderEntity> hbDetleNotFillOrder() {
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_PRE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTING.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode());
        states.add(DictEnum.ORDER_DETAIL_STATE_PARTIAL_FILLED.getCode());
        return  orderService.findNofinishDelteByMarket(DictEnum.ORDER_MODEL_LIMIT_DELTE.getCode(),DictEnum.MARKET_TYPE_HB.getCode(),DictEnum.IS_FINISH_YES.getCode(),states);
    }

    /**
     * 获取zb未完成的一条beta限价单
     */
    public OrderEntity findZbBetaOrder(String userId, String symbol, String symbolTradeConfigId) {
        OrderEntity orderEntity = null;
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_0.getCode());
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode());
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode());
        List<OrderEntity> list = orderService.findByParam(userId, DictEnum.ORDER_MODEL_LIMIT_BETA.getCode(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), symbol, symbolTradeConfigId, DictEnum.MARKET_TYPE_ZB.getCode(), states);
        //买单不存在
        if (list == null || list.isEmpty()) {
            states.remove(DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode());
            list = orderService.findByParam(userId, DictEnum.ORDER_MODEL_LIMIT_BETA.getCode(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), symbol, symbolTradeConfigId,
                    DictEnum.MARKET_TYPE_ZB.getCode(), states);
            //卖单存在
            if (list != null && !list.isEmpty()) {
                orderEntity = list.get(0);
            }
        } else {
            orderEntity = list.get(0);
        }
        return orderEntity;
    }

    /**
     * 根据买单订单id查询卖单记录
     */
    public OrderEntity findByBuyOrderId(String buyOrderId) {
        return orderService.findByBuyOrderId(buyOrderId);
    }


    /**
     * 查询limit/betaLimit 所有未完成的买单
     */
    public List<OrderEntity> findNoFillLimit() {
        List<OrderEntity> allList = new ArrayList<>();
        List<String> states = new ArrayList<>();
        states.add(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode());
        states.add(DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode());
        List<OrderEntity> buyOrderList = orderService.findByTypeAndState(DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), states);
        List<OrderEntity> saleOrderList = orderService.findByTypeAndState(DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), states);
        allList.addAll(buyOrderList);
        allList.addAll(saleOrderList);
        return allList;
    }


    private ResponseData findByModel(OrderDto dto, String userId, String model) {
        ResponseData responseData = ResponseData.success();
        Pageable pageable = new PageRequest(dto.getCurrentPage() - 1, dto.getPageSize(), new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
        Specification<OrderEntity> spec = (root, query, cb) -> {
            List<Predicate> bigList = new ArrayList<>();
            bigList.add(cb.equal(root.get("userId").as(String.class), userId));
            bigList.add(cb.equal(root.get("model").as(String.class), model));
            if (StringUtils.isNotEmpty(dto.getOrderId())) {
                bigList.add(cb.or(cb.equal(root.get("orderId").as(String.class), dto.getOrderId()), cb.equal(root.get("buyOrderId").as(String.class), dto.getOrderId())));
            }
            query.where(cb.and(bigList.toArray(new Predicate[bigList.size()])));
            query.orderBy(cb.desc(root.get("createTime")));
            // 条件查询
            return query.getRestriction();
        };
        List<OrderVo> voList = new ArrayList<>();
        Page<OrderEntity> pageList = orderService.findAll(spec, pageable);
        if (pageList != null && pageList.getContent() != null && pageList.getTotalElements() > 0) {
            for (OrderEntity entity : pageList) {
                OrderVo vo = new OrderVo();
                BeanUtils.copyProperties(entity, vo);
                if (entity.getState().equals(DictEnum.ZB_ORDER_DETAIL_STATE_0.getCode()) || entity.getState().equals(DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode())) {
                    vo.setState(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode());
                } else if (entity.getState().equals(DictEnum.ZB_ORDER_DETAIL_STATE_1.getCode())) {
                    vo.setState(DictEnum.ORDER_DETAIL_STATE_CANCELED.getCode());
                } else if (entity.getState().equals(DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode())) {
                    vo.setState(DictEnum.ORDER_DETAIL_STATE_FILLED.getCode());
                } else if (entity.getState().equals(DictEnum.ZB_ORDER_DETAIL_STATE_4.getCode())) {
                    vo.setState(DictEnum.ORDER_DETAIL_STATE_SELL.getCode());
                }
                voList.add(vo);
            }
            responseData = ResponseData.success(voList, dto.getCurrentPage(), dto.getPageSize(), pageList.getTotalElements());
        }
        return responseData;
    }

    /**
     * 下单总额转换成usdt值
     */
    private BigDecimal getTotalToUsdt(String symbol, BigDecimal price, BigDecimal amount) {
        BigDecimal totalToUsdt;
        MarketInfoVo marketInfoVo;
        if (symbol.endsWith(DictEnum.HB_MARKET_BASE_USDT.getCode())) {
            totalToUsdt = price.multiply(amount);
        } else if (symbol.endsWith(DictEnum.HB_MARKET_BASE_BTC.getCode())) {
            marketInfoVo = huobiApi.getMarketInfo(DictEnum.MARKET_PERIOD_1MIN.getCode(), 1, DictEnum.MARKET_HUOBI_SYMBOL_BTC_USDT.getCode());
            totalToUsdt = price.multiply(amount).multiply(marketInfoVo.getData().get(0).getClose());
        } else if (symbol.endsWith(DictEnum.HB_MARKET_BASE_ETH.getCode())) {
            marketInfoVo = huobiApi.getMarketInfo(DictEnum.MARKET_PERIOD_1MIN.getCode(), 1, DictEnum.MARKET_HUOBI_SYMBOL_ETH_USDT.getCode());
            totalToUsdt = price.multiply(amount).multiply(marketInfoVo.getData().get(0).getClose());
        } else if (symbol.endsWith(DictEnum.ZB_MARKET_BASE_QC.getCode())) {
            ZbTickerVo vo = zbApi.getTicker("usdt_qc");
            totalToUsdt = price.multiply(amount).divide(vo.getLast(), 2);
        } else {
            totalToUsdt = BigDecimal.ZERO;
            log.warn("未找到该主对兑换USDT信息symbol={},price={},amount={},totalToUsdt={}", symbol, price, amount, totalToUsdt);
        }
        log.info("getTotalToUsdt,symbol={},price={},amount={},totalToUsdt={}", symbol, price, amount, totalToUsdt);
        return totalToUsdt.setScale(8, BigDecimal.ROUND_DOWN);
    }
}
