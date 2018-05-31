package com.lucky.game.robot.biz;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.dto.huobi.DepthDto;
import com.lucky.game.robot.bian.OkexApi;
import com.lucky.game.robot.dto.huobi.CreateOrderDto;
import com.lucky.game.robot.dto.huobi.MaInfoDto;
import com.lucky.game.robot.entity.LimitDelteConfigEntity;
import com.lucky.game.robot.entity.OrderEntity;
import com.lucky.game.robot.entity.UserEntity;
import com.lucky.game.robot.huobi.response.Depth;
import com.lucky.game.robot.market.HuobiApi;
import com.lucky.game.robot.vo.huobi.MarketDetailVo;
import com.lucky.game.robot.vo.huobi.MarketInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author conan
 *         2018/5/3 10:27
 **/
@Component
@Slf4j
public class DelteTransBiz {

    @Autowired
    private HuobiApi huobiApi;

    @Autowired
    private UserBiz userBiz;

    @Autowired
    private LimitDelteConfigBiz limitDelteConfigBiz;

    @Autowired
    private OrderBiz orderBiz;

    @Autowired
    private MarketRuleBiz marketRuleBiz;

    @Autowired
    private AccountBiz accountBiz;

    @Autowired
    private TradeBiz tradeBiz;

    @Autowired
    private MailBiz mailBiz;

    @Autowired
    private MarketBiz marketBiz;

    @Autowired
    private OkexApi okexApi;

    @Value("${delte.rate:0.005}")
    private Double delteRate;

    @Value("${retry.time:3}")
    private Integer retryTime;

    /**
     * 更新已完成但是状态还未同步的detle订单
     */
    public void checkDelteStatus() {
        List<OrderEntity> orderEntityList = orderBiz.hbDetleNotFillOrder();
        for (OrderEntity orderEntity : orderEntityList) {
            try {
                orderBiz.updateHbOrderState(orderEntity);
                if (DictEnum.ORDER_DETAIL_STATE_FILLED.getCode().equals(orderEntity.getState())) {
                    mailBiz.transToEmailNotify(orderEntity, orderEntity.getUserId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新delte订单状态并判断是否可操作原订单
     */
    public void checkDelteTodo() {
        List<OrderEntity> orderEntityList = orderBiz.hbDetleNotFinishOrder();
        for (OrderEntity orderEntity : orderEntityList) {
            try {
                orderBiz.updateHbOrderState(orderEntity);
                if (orderEntity.getState().equals(DictEnum.ORDER_DETAIL_STATE_FILLED.getCode())) {
                    //是否该操作订单
                    boolean toDo = calcMaToWin(orderEntity.getSymbol(), orderEntity.getType());
                    if (toDo) {
                        log.info("趋势构成处理delte订单,orderId={}", orderEntity.getOrderId());
                        //买单检查是否有足够利润去卖出
                        if (DictEnum.ORDER_TYPE_BUY_LIMIT.getCode().equals(orderEntity.getType())) {
                            LimitDelteConfigEntity delte = limitDelteConfigBiz.findById(orderEntity.getSymbolTradeConfigId());
                            BigDecimal buyOnePrice = buyOnePrice(orderEntity.getSymbol(), orderEntity.getUserId());
                            BigDecimal salePrice = orderEntity.getPrice().multiply(new BigDecimal(1).add(delte.getFluctuate()));
                            //买一价高于卖价
                            if (salePrice.compareTo(buyOnePrice) <= 0) {
                                log.info("利润足够,挂单卖出,salePrice={},buyOnePrice={}", salePrice, buyOnePrice);
                                salePrice = buyOnePrice;
                                //创建卖单标记完成并更新buyOrderId
                                hbCreateLimitSellOrderWithBuyOrder(orderEntity, salePrice);
                            }

                        } else {
                            //卖单价是否有足够的利润买入
                            LimitDelteConfigEntity delte = limitDelteConfigBiz.findById(orderEntity.getSymbolTradeConfigId());
                            BigDecimal saleOnePrice = saleOnePrice(orderEntity.getSymbol(), orderEntity.getUserId());
                            BigDecimal buyPrice = orderEntity.getPrice().multiply(new BigDecimal(1).subtract(delte.getFluctuateDecrease()));
                            //卖一低于买价
                            if (saleOnePrice.compareTo(buyPrice) <= 0) {
                                log.info("利润足够,挂单买入,buyPrice={},saleOnePrice={}", buyPrice, saleOnePrice);
                                buyPrice = saleOnePrice;
                                //创建买单并标记完成
                                this.hbCreateDelteLimitBuyOrderWithSaleOrder(orderEntity, buyPrice);
                            }

                        }
                    }
                }
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 检查MA趋势delte单
     */
    public void checkByMa() {
        List<UserEntity> userList = userBiz.findAllHbByNormal();
        for (UserEntity user : userList) {
            List<LimitDelteConfigEntity> delteList = limitDelteConfigBiz.findByUserIdAndMarketType(user.getOid(), DictEnum.MARKET_TYPE_HB.getCode());
            for (LimitDelteConfigEntity delte : delteList) {
                try {
                    Boolean maUp = calcMaInfo(delte.getSymbol(), DictEnum.MARKET_PERIOD_60MIN.getCode());
                    //上升
                    if (maUp != null && maUp) {
                        OrderEntity buyOrder = orderBiz.findHbDelteBuyOrder(delte.getUserId(), delte.getSymbol(), delte.getOid(), DictEnum.ORDER_MODEL_LIMIT_DELTE.getCode());
                        //不存在买单
                        if (buyOrder == null) {
                            OrderEntity saleOrder = orderBiz.findHbDelteSellOrder(delte.getUserId(), delte.getSymbol(), delte.getOid(), DictEnum.ORDER_MODEL_LIMIT_DELTE.getCode());
                            //存在卖单,先处理原始卖单
                            if (saleOrder != null) {
                                hbCreateDelteLimitBuyOrderWithSaleOrder(saleOrder, null);
                            }
                            //创建买单
                            hbCreateDelteLimitBuyOrderWithDelte(delte);
                        } else {
                            log.info("已存在买单,orderId={}", buyOrder.getOrderId());
                        }
                    }
                    //下降不做，亏损率高
//                    else if (maUp != null) {
//                        OrderEntity saleOrder = orderBiz.findHbDelteSellOrder(delte.getUserId(), delte.getSymbol(), delte.getOid(), DictEnum.ORDER_MODEL_LIMIT_DELTE.getCode());
//                        //不存在卖单
//                        if (saleOrder == null) {
//                            OrderEntity buyOrder = orderBiz.findHbDelteBuyOrder(delte.getUserId(), delte.getSymbol(), delte.getOid(), DictEnum.ORDER_MODEL_LIMIT_DELTE.getCode());
//                            //存在买单,先处理买单
//                            if (buyOrder != null) {
//                                hbCreateLimitSellOrderWithBuyOrder(buyOrder, null);
//                            }
//                            //创建卖单
//                            hbCreateLimitSellOrderWithDelte(delte);
//                        } else {
//                            log.info("已存在卖单,orderId={}", saleOrder.getOrderId());
//                        }
//                    }
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("处理delte订单失败,delte={},e={}", delte, e);
                }
            }
        }
    }


    /**
     * 计算ma 5min 趋势是否构成买入卖出点
     */
    public Boolean calcMaInfo(String symbol, String period) {
        Boolean maUp = null;

        MaInfoDto ma = reTryGetMaInfo(symbol, period,retryTime);
        //up
        if (ma.getMa7Middle().compareTo(ma.getMa30Middle()) > 0 && ma.getRate().compareTo(delteRate) <= 0) {
            log.info("delte{}趋势结果上升,symbol={},ma={}", period, symbol, ma);
            maUp = true;
        }
        //down
        if (ma.getMa7Middle().compareTo(ma.getMa30Middle()) < 0 && ma.getRate().compareTo(delteRate) <= 0) {
            log.info("delte{}趋势结果下降,symbol={},ma={}", period, symbol, ma);
            maUp = false;
        }
        return maUp;
    }


    /**
     * 计算当前某种趋势是否趋向于成交订单盈利
     */
    public Boolean calcMaToWin(String symbol, String orderType) {
        boolean toDo = false;

        MaInfoDto ma = reTryGetMaInfo(symbol, DictEnum.MARKET_PERIOD_15MIN.getCode(),retryTime);
        //买单,判断上涨趋势减慢
        if (DictEnum.ORDER_TYPE_BUY_LIMIT.getCode().equals(orderType)) {
            //down
            if (ma.getMa7Middle().compareTo(ma.getMa30Middle()) < 0) {
                log.info("下跌跌穿,symbol={},ma={}", symbol, ma);
                //ma 跌穿
                toDo = true;
            }
        }
        if (DictEnum.ORDER_TYPE_SELL_LIMIT.getCode().equals(orderType)) {
            //up
            if (ma.getMa7Middle().compareTo(ma.getMa30Middle()) > 0) {
                log.info("上涨涨穿,symbol={},ma={}", symbol, ma);
                //ma 涨穿
                toDo = true;
            }
        }
        return toDo;
    }

    /**
     * 获取行情失败重试
     */
    public MaInfoDto reTryGetMaInfo(String symbol, String period, Integer retryTime) {
        MaInfoDto maInfoDto = getMaInfo(symbol, period);
        try {
            if (maInfoDto == null && retryTime > 0) {
                log.info("retry to getMaInfo,symbol={},retryTime={}", symbol, retryTime);
                Thread.sleep(1000);
                --retryTime;
                maInfoDto = reTryGetMaInfo(symbol, period, retryTime);
            } else if(maInfoDto == null && retryTime <= 0){
                maInfoDto = new MaInfoDto();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return maInfoDto;
    }

    /**
     * 获取ma趋势数据
     */
    public MaInfoDto getMaInfo(String symbol, String period) {
        MaInfoDto maInfoDto = new MaInfoDto();
        MarketInfoVo marketInfoVo = huobiApi.getMarketInfo(period, 30, symbol);
        if (marketInfoVo == null || marketInfoVo.getData() == null) {
            log.info("获取K线失败");
            return null;
        }
        List<MarketDetailVo> detailVoList = marketInfoVo.getData();
        BigDecimal oneTotal = BigDecimal.ZERO;
        BigDecimal twoTotal = BigDecimal.ZERO;
        BigDecimal oneMiddle;
        BigDecimal twoMiddle;
        for (int i = 0; i < detailVoList.size(); i++) {
            if (i >= 0 && i < 7) {
                oneTotal = oneTotal.add(detailVoList.get(i).getClose());
            }
            if (i >= 0 && i < 30) {
                twoTotal = twoTotal.add(detailVoList.get(i).getClose());
            }
        }
        //MA7
        oneMiddle = oneTotal.divide(new BigDecimal(7), 8, BigDecimal.ROUND_HALF_UP);
        //MA30
        twoMiddle = twoTotal.divide(new BigDecimal(30), 8, BigDecimal.ROUND_HALF_UP);
        maInfoDto.setMa7Middle(oneMiddle);
        maInfoDto.setMa30Middle(twoMiddle);
        maInfoDto.setPeriod(period);
        //差异比率
        maInfoDto.setRate(Math.abs(oneMiddle.divide(twoMiddle, 8, BigDecimal.ROUND_HALF_UP).subtract(new BigDecimal(1)).doubleValue()));
        return maInfoDto;
    }


    /**
     * 单笔策略delte限价买单,主动买入时使用
     */
    private OrderEntity hbCreateDelteLimitBuyOrderWithDelte(LimitDelteConfigEntity delte) {
        log.info("创建detle买单,delte={}", delte);
        BigDecimal saleOnePrice = saleOnePrice(delte.getSymbol(), delte.getUserId());
        //略高买
        BigDecimal buyPrice = (new BigDecimal(1.0005)).multiply(saleOnePrice);
        String baseCurrency = marketRuleBiz.getHbBaseCurrency(delte.getSymbol());
        //基对余额
        BigDecimal balanceMax = accountBiz.getHuobiQuoteBalance(delte.getUserId(), baseCurrency);
        //总成交额
        BigDecimal totalAmount = delte.getTotalAmount().compareTo(balanceMax) <= 0 ? delte.getTotalAmount() : balanceMax;
        //购买数量
        BigDecimal amount = totalAmount.divide(buyPrice, 4, BigDecimal.ROUND_DOWN);
        //创建限价买单
        return hbCreateDelteLimitBuyOrder(delte.getSymbol(), delte.getUserId(), amount, buyPrice, delte.getOid());
    }

    /**
     * 单笔策略delte限价买单,被动买入时使用(对应卖单买入)
     */
    private OrderEntity hbCreateDelteLimitBuyOrderWithSaleOrder(OrderEntity saleOrder, BigDecimal buyPrice) {
        log.info("创建delte买单,saleOrderId={}", saleOrder.getOrderId());
        //购买数量
        BigDecimal amount = saleOrder.getAmount();

        String baseCurrency = marketRuleBiz.getHbBaseCurrency(saleOrder.getSymbol());
        //基对余额
        BigDecimal balanceMax = accountBiz.getHuobiQuoteBalance(saleOrder.getUserId(), baseCurrency);
        if (balanceMax.compareTo(saleOrder.getTotalToUsdt()) < 0) {
            amount = balanceMax.divide(saleOrder.getPrice(), 4, BigDecimal.ROUND_DOWN);
        }
        if (buyPrice == null) {
            BigDecimal saleOnePrice = saleOnePrice(saleOrder.getSymbol(), saleOrder.getUserId());
            //略高买
            buyPrice = (new BigDecimal(1.0005)).multiply(saleOnePrice);
        }
        //创建限价买单
        OrderEntity buyOrder = hbCreateDelteLimitBuyOrder(saleOrder.getSymbol(), saleOrder.getUserId(), amount, buyPrice, saleOrder.getOid());
        buyOrder.setIsFinish(DictEnum.IS_FINISH_YES.getCode());
        orderBiz.saveOrder(buyOrder);
        saleOrder.setBuyOrderId(buyOrder.getOrderId());
        saleOrder.setIsFinish(DictEnum.IS_FINISH_YES.getCode());
        saleOrder.setState(DictEnum.ORDER_DETAIL_STATE_BUY.getCode());
        orderBiz.saveOrder(saleOrder);
        mailBiz.transToEmailNotify(saleOrder, saleOrder.getUserId());
        return buyOrder;
    }


    /**
     * 单笔策略delte限价买单
     */
    private OrderEntity hbCreateDelteLimitBuyOrder(String symbol, String userId, BigDecimal amount, BigDecimal buyPrice, String configId) {
        log.info("创建delte买单,symbol={},amount={},buyPrice={}", symbol, amount, buyPrice);
        CreateOrderDto buyOrderDto = new CreateOrderDto();
        buyOrderDto.setSymbol(symbol);
        buyOrderDto.setPrice(buyPrice);
        buyOrderDto.setOrderType(DictEnum.ORDER_TYPE_BUY_LIMIT.getCode());
        buyOrderDto.setUserId(userId);

        buyOrderDto.setAmount(amount);
        //创建限价买单
        String buyOrderId = tradeBiz.hbCreateOrder(buyOrderDto);
        return orderBiz.saveHbOrder(buyOrderId, null, null, configId, userId, DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT_DELTE.getCode());
    }


    /**
     * 单笔策略限价卖单,主动卖出时使用
     */
    private OrderEntity hbCreateLimitSellOrderWithDelte(LimitDelteConfigEntity delte) {
        log.info("创建delte卖单,delte={}", delte);
        //买一价
        BigDecimal buyOnePrice = buyOnePrice(delte.getSymbol(), delte.getUserId());
        //略低卖
        BigDecimal salePrice = (new BigDecimal(0.9995)).multiply(buyOnePrice);
        String quoteCurrency = marketRuleBiz.getHbQuoteCurrency(delte.getSymbol());
        //业务对余额
        BigDecimal balanceMax = accountBiz.getHuobiQuoteBalance(delte.getUserId(), quoteCurrency);
        BigDecimal amount = delte.getTotalAmount().divide(salePrice, 2, BigDecimal.ROUND_DOWN);
        //验证余额
        if (amount.compareTo(balanceMax) > 0) {
            log.info("余额不足,售卖数量不足,amount={},balanceMax={}", amount, balanceMax);
            mailBiz.balanceToEmailNotify(delte.getUserId(), quoteCurrency, DictEnum.MARKET_TYPE_HB.getCode());
            amount = balanceMax;
        }
        return hbCreateLimitSellOrder(delte.getSymbol(), delte.getUserId(), amount, salePrice, delte.getOid());
    }


    /**
     * 单笔策略限价卖单,对应买单使用
     */
    private OrderEntity hbCreateLimitSellOrderWithBuyOrder(OrderEntity orderEntity, BigDecimal salePrice) {
        log.info("创建delte卖单,buyOrderId={}", orderEntity.getOrderId());
        //卖出数量
        BigDecimal amount = orderEntity.getAmount();
        String quoteCurrency = marketRuleBiz.getHbQuoteCurrency(orderEntity.getSymbol());
        //业务对余额
        BigDecimal balanceMax = accountBiz.getHuobiQuoteBalance(orderEntity.getUserId(), quoteCurrency);
        amount = balanceMax.compareTo(amount) < 0 ? balanceMax : amount;
        if (salePrice == null) {
            //买一价
            BigDecimal buyOnePrice = buyOnePrice(orderEntity.getSymbol(), orderEntity.getUserId());
            salePrice = (new BigDecimal(0.9995)).multiply(buyOnePrice);
        }
        //更新卖单完成标记
        OrderEntity saleOrder = hbCreateLimitSellOrder(orderEntity.getSymbol(), orderEntity.getUserId(), amount, salePrice, orderEntity.getSymbolTradeConfigId());
        saleOrder.setIsFinish(DictEnum.IS_FINISH_YES.getCode());
        saleOrder.setBuyOrderId(orderEntity.getOrderId());
        orderBiz.saveOrder(saleOrder);
        //更新买单状态并标记处理完成
        orderEntity.setIsFinish(DictEnum.IS_FINISH_YES.getCode());
        orderEntity.setState(DictEnum.ORDER_DETAIL_STATE_SELL.getCode());
        orderBiz.saveOrder(orderEntity);
        mailBiz.transToEmailNotify(orderEntity, orderEntity.getUserId());
        return saleOrder;
    }

    /**
     * 单笔策略限价卖单
     */
    private OrderEntity hbCreateLimitSellOrder(String symbol, String userId, BigDecimal amount, BigDecimal salePrice, String configId) {
        log.info("创建delte卖单,symbol={},amount={},salePrice={}", symbol, amount, salePrice);
        CreateOrderDto saleOrderDto = new CreateOrderDto();
        saleOrderDto.setSymbol(symbol);
        saleOrderDto.setOrderType(DictEnum.ORDER_TYPE_SELL_LIMIT.getCode());
        saleOrderDto.setPrice(salePrice);
        saleOrderDto.setUserId(userId);
        saleOrderDto.setAmount(amount);
        //创建限价卖单
        String saleOrderId = tradeBiz.hbCreateOrder(saleOrderDto);
        return orderBiz.saveHbOrder(saleOrderId, null, null, configId, userId, DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT_DELTE.getCode());
    }


    /**
     * 买一价
     */
    private BigDecimal buyOnePrice(String symbol, String userId) {
        Depth depth = getDeth(symbol, userId);
        return depth.getBids().get(0).get(0);
    }

    /**
     * 卖一价
     */
    private BigDecimal saleOnePrice(String symbol, String userId) {
        Depth depth = getDeth(symbol, userId);
        return depth.getAsks().get(0).get(0);
    }

    private Depth getDeth(String symbol, String userId) {
        DepthDto dto = new DepthDto();
        dto.setSymbol(symbol);
        dto.setUserId(userId);
        return marketBiz.HbDepth(dto);
    }

}
