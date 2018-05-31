package com.lucky.game.robot.biz;

import com.lucky.game.core.util.DateUtils;
import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.dto.huobi.DepthDto;
import com.lucky.game.robot.entity.*;
import com.lucky.game.robot.service.RateChangeService;
import com.lucky.game.robot.dto.huobi.CreateOrderDto;
import com.lucky.game.robot.dto.huobi.HuobiBaseDto;
import com.lucky.game.robot.dto.huobi.MaInfoDto;
import com.lucky.game.robot.dto.zb.ZbCancelOrderDto;
import com.lucky.game.robot.exception.BizException;
import com.lucky.game.robot.huobi.request.CreateOrderRequest;
import com.lucky.game.robot.huobi.response.Accounts;
import com.lucky.game.robot.huobi.response.Depth;
import com.lucky.game.robot.mail.MailQQ;
import com.lucky.game.robot.market.HuobiApi;
import com.lucky.game.robot.vo.huobi.MarketDetailVo;
import com.lucky.game.robot.vo.huobi.MarketInfoVo;
import com.lucky.game.robot.vo.huobi.RateChangeVo;
import com.lucky.game.robot.zb.api.ZbApi;
import com.lucky.game.robot.zb.vo.ZbOrderDepthVo;
import com.lucky.game.robot.zb.vo.ZbTickerVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author conan
 *         2018/3/15 10:27
 **/
@Slf4j
@Component
public class TransBiz {

    @Autowired
    private MarketBiz marketBiz;

    @Autowired
    private TradeBiz tradeBiz;

    @Autowired
    private AccountBiz accountBiz;

    @Autowired
    private OrderBiz orderBiz;

    @Autowired
    private RateChangeService rateChangeService;

    @Autowired
    private RateChangeBiz rateChangeBiz;

    @Autowired
    private SymbolTradeConfigBiz symbolTradeConfigBiz;

    @Autowired
    private HuobiApi huobiApi;

    @Autowired
    private ZbApi zbApi;

    @Autowired
    private UserBiz userBiz;

    @Autowired
    private LimitTradeConfgBiz limitTradeConfgBiz;

    @Autowired
    private MarketRuleBiz marketRuleBiz;

    @Autowired
    private LimitBetaConfigBiz limitBetaConfigBiz;

    @Autowired
    private LimitGammaConfigBiz limitGammaConfigBiz;

    @Autowired
    private MailBiz mailBiz;

    @Autowired
    private DelteTransBiz delteTransBiz;


    @Value("${time.out.minute:3600}")
    private int timeOutMinute;


    @Value("${beta.and.game.rate:0.03}")
    private Double betaAndGameRate;



    /**
     * hbToBuy
     */
    public boolean hbToBuy(RateChangeVo rateChangeVo, SymbolTradeConfigEntity symbolTradeConfig) {
        log.info("hbToBuy,rateChangeVo={}", rateChangeVo);
        boolean result = false;
        List<String> orderIds;
        if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
            boolean checkResult = hbCheckNeedToBuy(rateChangeVo, symbolTradeConfig.getOid());
            if (checkResult) {
                log.info("存在未完成的订单,不再继续购买,rateChangeVo={}", rateChangeVo);
                return false;
            }
            //下单操作
            orderIds = hbToBuyOrder(rateChangeVo, symbolTradeConfig);
            if (orderIds != null && !orderIds.isEmpty()) {
                RateChangeEntity rateChangeEntity = rateChangeBiz.save(rateChangeVo);
                //保存下单结果
                for (String orderId : orderIds) {
                    orderBiz.saveHbOrder(orderId, rateChangeEntity.getOid(), null, symbolTradeConfig.getOid(), symbolTradeConfig.getUserId(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_REAL.getCode());
                }
                result = true;
            }
        }
        return result;
    }


    /**
     * 检查hb是否有需要卖出的实时订单
     */
    public void hbToSale() {
        List<String> saleOrdes;
        List<OrderEntity> list = orderBiz.findHbRealNoFilledBuyOrder();
        for (OrderEntity buyOrderEntity : list) {
            try {
                buyOrderEntity = orderBiz.updateHbOrderState(buyOrderEntity);
                SymbolTradeConfigEntity symbolTradeConfig = symbolTradeConfigBiz.findById(buyOrderEntity.getSymbolTradeConfigId());
                //完全成交或者部分成交撤销,售出成交部分
                if (DictEnum.ORDER_DETAIL_STATE_FILLED.getCode().equals(buyOrderEntity.getState()) || DictEnum.ORDER_DETAIL_STATE_PARTIAL_CANCELED.getCode().equals(buyOrderEntity.getState())) {
                    log.info("买单已成交,可以挂单售出.orderEntity={}", buyOrderEntity);
                    RateChangeEntity rateChangeEntity = rateChangeService.findOne(buyOrderEntity.getRateChangeId());
                    saleOrdes = hbBeginSaleOrder(rateChangeEntity, buyOrderEntity.getFieldAmount(), symbolTradeConfig);
                    for (String orderId : saleOrdes) {
                        //保存卖单
                        orderBiz.saveHbOrder(orderId, rateChangeEntity.getOid(), buyOrderEntity.getOrderId(), symbolTradeConfig.getOid(),
                                symbolTradeConfig.getUserId(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.ORDER_MODEL_REAL.getCode());
                    }
                    //更新原买单状态
                    buyOrderEntity.setState(DictEnum.ORDER_DETAIL_STATE_SELL.getCode());
                    orderBiz.saveOrder(buyOrderEntity);
                    //发送成交邮件通知
                    transToEmailNotify(buyOrderEntity);
                }
                //部分成交
                else if (DictEnum.ORDER_DETAIL_STATE_PARTIAL_FILLED.getCode().equals(buyOrderEntity.getState()) || DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode().equals(buyOrderEntity.getState())) {
                    //买单超时则撤销
                    Integer buyOrderWaitTime = (symbolTradeConfig != null && symbolTradeConfig.getBuyWaitTime() != null) ? symbolTradeConfig.getBuyWaitTime() : 10;
                    if (DateUtils.addMinute(buyOrderEntity.getCreateTime(), buyOrderWaitTime).before(DateUtils.getCurrDateMmss())) {
                        log.info("买单超时撤销,buyOrderEntity={}", buyOrderEntity);
                        HuobiBaseDto dto = new HuobiBaseDto();
                        dto.setOrderId(buyOrderEntity.getOrderId());
                        dto.setUserId(buyOrderEntity.getUserId());
                        tradeBiz.hbCancelOrder(dto);
                        orderBiz.updateHbOrderState(buyOrderEntity);
                    }
                }
            } catch (Exception e) {
                log.error("buyOrderEntity={},e={}", buyOrderEntity, e);
            }
        }
    }

    /**
     * 检查hb是否有完成的实时卖单
     */
    public void hbCheckSaleFinish() {
        List<OrderEntity> saleOrderList = orderBiz.findHbRealNoFilledSaleOrder();
        for (OrderEntity saleOrder : saleOrderList) {
            try {
                HuobiBaseDto dto = new HuobiBaseDto();
                dto.setOrderId(saleOrder.getOrderId());
                dto.setUserId(saleOrder.getUserId());
                orderBiz.updateHbOrderState(saleOrder);
                //卖单已成交或撤销成交
                if (DictEnum.ORDER_DETAIL_STATE_FILLED.getCode().equals(saleOrder.getState()) || DictEnum.ORDER_DETAIL_STATE_PARTIAL_CANCELED.getCode().equals(saleOrder.getState())) {
                    log.info("卖单已成交,交易完成.saleOrderId={}", saleOrder.getOrderId());
                    //发送成交邮件通知
                    transToEmailNotify(saleOrder);

                }
            } catch (Exception e) {
                log.error("saleOrder={},e={}", saleOrder, e);
            }
        }
    }

    /**
     * 监控hb限价单
     */
    public void hbTransModelLimitOrder() {

        List<AccountEntity> accountList = accountBiz.findByType(DictEnum.MARKET_TYPE_HB.getCode());
        for (AccountEntity accountEntity : accountList) {
            List<LimitTradeConfigEntity> configList = limitTradeConfgBiz.findAllByUserIdAndMarketType(accountEntity.getUserId(), DictEnum.MARKET_TYPE_HB.getCode());
            for (LimitTradeConfigEntity config : configList) {
                hbCreateModelLimitOrder(config, accountEntity);
            }
        }

    }

    /**
     * zbToBuy
     */
    public boolean zbToBuy(RateChangeVo rateChangeVo, SymbolTradeConfigEntity symbolTradeConfig) {
        log.info("zbToBuy,rateChangeVo={}", rateChangeVo);
        boolean result = false;
        List<String> orderIds;
        if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
            boolean checkResult = zbCheckNeedToBuy(rateChangeVo, symbolTradeConfig.getOid());
            if (checkResult) {
                log.info("存在未完成的订单,不再继续购买,rateChangeVo={}", rateChangeVo);
                return false;
            }
            //下单操作
            orderIds = zbToBuyOrder(rateChangeVo, symbolTradeConfig);
            if (orderIds != null && !orderIds.isEmpty()) {
                RateChangeEntity rateChangeEntity = rateChangeBiz.save(rateChangeVo);
                //保存下单结果
                for (String orderId : orderIds) {
                    orderBiz.saveZbOrder(orderId, rateChangeVo.getBuyerSymbol(), rateChangeEntity.getOid(), null, symbolTradeConfig.getOid(),
                            symbolTradeConfig.getUserId(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_REAL.getCode());
                }
                result = true;
            }
        }
        return result;
    }

    /**
     * 检查zb是否有需要卖出的实时订单
     */
    public void zbToSale() {
        List<String> saleOrdes;
        List<OrderEntity> list = orderBiz.findZbNoFilledBuyOrder();
        for (OrderEntity buyOrderEntity : list) {
            try {
                buyOrderEntity = orderBiz.updateZbOrderState(buyOrderEntity);

                SymbolTradeConfigEntity symbolTradeConfig = symbolTradeConfigBiz.findById(buyOrderEntity.getSymbolTradeConfigId());
                //完全成交,售出成交部分
                if (DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode().equals(buyOrderEntity.getState())) {
                    log.info("买单已成交,可以挂单售出.orderEntity={}", buyOrderEntity);
                    RateChangeEntity rateChangeEntity = rateChangeService.findOne(buyOrderEntity.getRateChangeId());
                    saleOrdes = zbBeginSaleOrder(rateChangeEntity, buyOrderEntity.getFieldAmount(), symbolTradeConfig);
                    for (String orderId : saleOrdes) {
                        //保存卖单
                        orderBiz.saveZbOrder(orderId, rateChangeEntity.getSaleSymbol(), rateChangeEntity.getOid(), buyOrderEntity.getOrderId(), symbolTradeConfig.getOid(),
                                symbolTradeConfig.getUserId(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.ORDER_MODEL_REAL.getCode());
                    }
                    //更新原买单状态
                    buyOrderEntity.setState(DictEnum.ZB_ORDER_DETAIL_STATE_4.getCode());
                    orderBiz.saveOrder(buyOrderEntity);
                    //发送成交邮件通知
                    transToEmailNotify(buyOrderEntity);
                }
                //未成交
                else if (DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode().equals(buyOrderEntity.getState()) || DictEnum.ZB_ORDER_DETAIL_STATE_0.getCode().equals(buyOrderEntity.getState())) {
                    //买单超时则撤销
                    Integer buyOrderWaitTime = (symbolTradeConfig != null && symbolTradeConfig.getBuyWaitTime() != null) ? symbolTradeConfig.getBuyWaitTime() : 10;
                    if (DateUtils.addMinute(buyOrderEntity.getCreateTime(), buyOrderWaitTime).before(DateUtils.getCurrDateMmss())) {
                        log.info("买单超时撤销,buyOrderEntity={}", buyOrderEntity);
                        ZbCancelOrderDto dto = new ZbCancelOrderDto();
                        dto.setOrderId(buyOrderEntity.getOrderId());
                        dto.setCurrency(buyOrderEntity.getSymbol());
                        AccountEntity accountEntity = accountBiz.getByUserIdAndType(buyOrderEntity.getUserId(), DictEnum.MARKET_TYPE_ZB.getCode());
                        dto.setAccessKey(accountEntity.getApiKey());
                        dto.setSecretKey(accountEntity.getApiSecret());
                        tradeBiz.zbCancelOrder(buyOrderEntity.getOrderId(), buyOrderEntity.getSymbol(), buyOrderEntity.getUserId());
                        orderBiz.updateZbOrderState(buyOrderEntity);
                    }
                }
            } catch (Exception e) {
                log.error("order={},e={}", buyOrderEntity, e);
            }

        }
    }

    /**
     * 检查zb是否有完成的卖单
     */
    public void zbCheckSaleFinish() {
        List<OrderEntity> saleOrderList = orderBiz.findZbNoFilledSaleOrder();
        for (OrderEntity saleOrder : saleOrderList) {
            try {
                //同步状态
                orderBiz.updateZbOrderState(saleOrder);
                //卖单已成交或撤销成交
                if (DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode().equals(saleOrder.getState())) {
                    log.info("卖单已成交,交易完成.saleOrderId={}", saleOrder.getOrderId());
                    //发送成交邮件通知
                    transToEmailNotify(saleOrder);
                }
            } catch (Exception e) {
                log.error("saleOrder={},e={}", saleOrder, e);
            }
        }
    }


    /**
     * hb限价单
     */
    public void hbCreateModelLimitOrder(LimitTradeConfigEntity config, AccountEntity accountEntity) {
        try {
            //买单状态更新
            List<OrderEntity> buyList = orderBiz.findHbByUserIdAndModel(accountEntity.getUserId(), DictEnum.ORDER_MODEL_LIMIT.getCode(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), config.getSymbol(), config.getOid());
            Iterator<OrderEntity> buyIt = buyList.iterator();
            while (buyIt.hasNext()) {
                OrderEntity buyOrder = buyIt.next();
                buyOrder = orderBiz.updateHbOrderState(buyOrder);
                //买单已完成
                if (DictEnum.filledOrderStates.contains(buyOrder.getState())) {
                    log.info("买单已完结,buyOrder={}", buyOrder);
                    buyIt.remove();
                    if (DictEnum.ORDER_DETAIL_STATE_FILLED.getCode().equals(buyOrder.getState())) {
                        transToEmailNotify(buyOrder);
                    }
                }
            }
            //卖单状态更新
            List<OrderEntity> saleList = orderBiz.findHbByUserIdAndModel(accountEntity.getUserId(), DictEnum.ORDER_MODEL_LIMIT.getCode(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), config.getSymbol(), config.getOid());
            Iterator<OrderEntity> saleIt = saleList.iterator();
            while (saleIt.hasNext()) {
                OrderEntity saleOrder = saleIt.next();
                saleOrder = orderBiz.updateHbOrderState(saleOrder);
                //卖单已完成
                if (DictEnum.filledOrderStates.contains(saleOrder.getState())) {
                    log.info("卖单已完结,saleOrder={}", saleOrder);
                    saleIt.remove();
                    if (DictEnum.ORDER_DETAIL_STATE_FILLED.getCode().equals(saleOrder.getState())) {
                        transToEmailNotify(saleOrder);
                    }
                }
            }
            boolean result = this.checkNeedToCreateLimitOrder(buyList, saleList, config, accountEntity.getUserId());
            if (!result) {
                return;
            }
            log.info("创建限价单开始,symbols={},userId={}", config.getSymbol(), accountEntity.getUserId());

            //卖一价
            BigDecimal saleOnePrice = saleOnePrice(config.getSymbol(), config.getUserId());
            //买一价
            BigDecimal buyOnePrice = buyOnePrice(config.getSymbol(), config.getUserId());

            BigDecimal buyPrice = (new BigDecimal(1).subtract(config.getDecrease())).multiply(saleOnePrice);

            String baseCurrency = marketRuleBiz.getHbBaseCurrency(config.getSymbol());
            //基对余额
            BigDecimal balanceMax = accountBiz.getHuobiQuoteBalance(accountEntity.getUserId(), baseCurrency);
            //总成交额
            BigDecimal totalAmount = config.getTotalAmount().compareTo(balanceMax) <= 0 ? config.getTotalAmount() : balanceMax;
            //购买数量
            BigDecimal amount = totalAmount.divide(buyPrice, 4, BigDecimal.ROUND_DOWN);

            BigDecimal salePrice = (new BigDecimal(1).add(config.getIncrease())).multiply(buyOnePrice);
            CreateOrderDto buyOrderDto = new CreateOrderDto();
            buyOrderDto.setSymbol(config.getSymbol());
            buyOrderDto.setPrice(buyPrice);
            buyOrderDto.setOrderType(DictEnum.ORDER_TYPE_BUY_LIMIT.getCode());
            buyOrderDto.setAccountId(accountEntity.getAccountId());
            buyOrderDto.setUserId(accountEntity.getUserId());

            buyOrderDto.setAmount(amount);
            //创建限价买单
            String buyOrderId = tradeBiz.hbCreateOrder(buyOrderDto);
            orderBiz.saveHbOrder(buyOrderId, null, null, config.getOid(), accountEntity.getUserId(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT.getCode());
            CreateOrderDto saleOrderDto = new CreateOrderDto();
            BeanUtils.copyProperties(buyOrderDto, saleOrderDto);
            saleOrderDto.setOrderType(DictEnum.ORDER_TYPE_SELL_LIMIT.getCode());
            saleOrderDto.setPrice(salePrice);
            saleOrderDto.setUserId(accountEntity.getUserId());
            String quoteCurrency = marketRuleBiz.getHbQuoteCurrency(config.getSymbol());
            //业务对余额
            balanceMax = accountBiz.getHuobiQuoteBalance(accountEntity.getUserId(), quoteCurrency);
            //验证余额
            amount = amount.compareTo(balanceMax) <= 0 ? amount : balanceMax;
            saleOrderDto.setAmount(amount);
            //创建限价卖单
            String saleOrderId = tradeBiz.hbCreateOrder(saleOrderDto);
            orderBiz.saveHbOrder(saleOrderId, null, buyOrderId, config.getOid(), accountEntity.getUserId(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT.getCode());
            log.info("创建限价单结束,symbols={},userId={},buyOrderId={},saleOrderId={}", config.getSymbol(), accountEntity.getUserId(), buyOrderId, saleOrderId);
        } catch (Exception e) {
            log.error("限价单处理失败,config={},e={}", config, e);
        }
    }


    /**
     * hb检查是否需要去交易
     */
    private boolean hbCheckNeedToBuy(RateChangeVo rateChangeVo, String symbolConfigId) {
        boolean result = orderBiz.hbExistNotFinishOrder(rateChangeVo.getBuyerSymbol(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), symbolConfigId);
        if (!result) {
            result = orderBiz.hbExistNotFinishOrder(rateChangeVo.getSaleSymbol(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), symbolConfigId);
        }
        return result;
    }


    /**
     * 检查交易深度是否符合下单需求
     */
    private boolean checkSaleDept(RateChangeVo rateChangeVo, SymbolTradeConfigEntity symbolTradeConfig, List<List<BigDecimal>> bids, List<List<BigDecimal>> asks) {
        //同一交易对下单不检查
        if (rateChangeVo.getBuyerSymbol().equals(rateChangeVo.getSaleSymbol())) {
            return true;
        }
        boolean result = false;
        DepthDto dto = new DepthDto();
        dto.setSymbol(rateChangeVo.getSaleSymbol());
        dto.setUserId(symbolTradeConfig.getUserId());
        //买一价
        BigDecimal buyOnePrice = bids.get(0).get(0);
        //卖一价
        BigDecimal saleOnePrice = asks.get(0).get(0);
        //buyOne >= salePrice*(1-askBlunder)
        if (buyOnePrice.compareTo(rateChangeVo.getSalePrice().multiply(new BigDecimal(1).subtract(symbolTradeConfig.getAsksBlunder()))) >= 0) {
            result = true;
        }
        //saleOnePirce >= salePrice*(1-askbunder)
        else if (saleOnePrice.compareTo(rateChangeVo.getSalePrice().multiply(new BigDecimal(1).subtract(symbolTradeConfig.getAsksBlunder()))) >= 0) {
            result = true;
        }
        log.info("检查交易深度是否符合下单需求,result={},buyOnePrice={},saleOnePrice={},salePrice={},asksBlunder={}", result, buyOnePrice, saleOnePrice, rateChangeVo.getSalePrice(), String.valueOf(symbolTradeConfig.getAsksBlunder()));
        return result;
    }

    /**
     * HB交易下单
     */
    private List<String> hbToBuyOrder(RateChangeVo rateChangeVo, SymbolTradeConfigEntity symbolTradeConfig) {
        //所有成交的订单id
        List<String> orderIds = new ArrayList<>();
        DepthDto dto = new DepthDto();
        dto.setSymbol(rateChangeVo.getSaleSymbol());
        dto.setUserId(symbolTradeConfig.getUserId());
        //获取卖单交易深度
        Depth saleDepth = marketBiz.HbDepth(dto);
        //buy list
        List<List<BigDecimal>> saleBids = saleDepth.getBids();
        //sale list
        List<List<BigDecimal>> saleAsks = saleDepth.getAsks();
        //检查卖单深度是否符合要求
        boolean result = checkSaleDept(rateChangeVo, symbolTradeConfig, saleBids, saleAsks);
        if (result) {
            dto.setSymbol(rateChangeVo.getBuyerSymbol());
            //获取买单交易深度
            Depth buyDepth = marketBiz.HbDepth(dto);
            //buy list
            List<List<BigDecimal>> bids = buyDepth.getBids();
            //sale list
            List<List<BigDecimal>> asks = buyDepth.getAsks();
            //需要购买的数量
            BigDecimal amount = getHbBuyAmount(rateChangeVo.getBuyerSymbol(), rateChangeVo.getBuyPrice(), symbolTradeConfig, rateChangeVo.getBaseCurrency());
            //欲购买的下单信息
            Map<BigDecimal, BigDecimal> map = checkDeptAndBeginCreate(rateChangeVo.getBuyerSymbol(), rateChangeVo.getBuyPrice(), amount, rateChangeVo.getBaseCurrency(), symbolTradeConfig, bids, asks);
            for (BigDecimal key : map.keySet()) {
                String orderId = hbCreateBuyOrder(rateChangeVo.getBuyerSymbol(), key, map.get(key), symbolTradeConfig.getUserId());
                orderIds.add(orderId);

            }
        }

        log.info("orderIds={}", orderIds);
        return orderIds;
    }

    /**
     * 限价买
     */
    public String hbCreateBuyOrder(String symbol, BigDecimal price, BigDecimal amount, String userId) {
        log.info("hbCreateBuyOrder,symbols={},price={},amount={}", symbol, price, amount);
        CreateOrderDto dto = new CreateOrderDto();
        dto.setSymbol(symbol);
        dto.setOrderType(CreateOrderRequest.OrderType.BUY_LIMIT);

        HuobiBaseDto baseDto = new HuobiBaseDto();
        baseDto.setUserId(userId);
        Accounts accounts = accountBiz.getHuobiSpotAccounts(baseDto);
        dto.setAmount(amount);
        dto.setAccountId(String.valueOf(accounts.getId()));
        dto.setPrice(price);
        dto.setUserId(userId);
        return tradeBiz.hbCreateOrder(dto);
    }


    /**
     * 创建hb卖单
     */
    private List<String> hbBeginSaleOrder(RateChangeEntity rateChangeEntity, BigDecimal amount, SymbolTradeConfigEntity symbolTradeConfig) {
        log.info("hbCreateSaleOrder,rateChangeVo={},amount={}", rateChangeEntity, amount);
        DepthDto dto = new DepthDto();
        dto.setSymbol(rateChangeEntity.getSaleSymbol());
        dto.setUserId(symbolTradeConfig.getUserId());
        //获取交易深度
        Depth depth = marketBiz.HbDepth(dto);
        //所有成交的订单id
        List<String> orderIds = new ArrayList<>();
        //buy list
        List<List<BigDecimal>> bids = depth.getBids();
        //sale list
        List<List<BigDecimal>> asks = depth.getAsks();


        Map<BigDecimal, BigDecimal> map = checkDeptAndBeginSale(rateChangeEntity, amount, symbolTradeConfig, bids, asks);
        for (BigDecimal key : map.keySet()) {
            String orderId = hbCreateSaleOrder(rateChangeEntity.getSaleSymbol(), key, map.get(key), rateChangeEntity.getQuoteCurrency(), symbolTradeConfig.getUserId());
            orderIds.add(orderId);
        }
        log.info("orderIds={}", orderIds);
        return orderIds;
    }

    /**
     * 监控zb限价单
     */
    public void zbTransModelLimitOrder() {

        List<AccountEntity> accountEntityList = accountBiz.findByType(DictEnum.MARKET_TYPE_ZB.getCode());
        for (AccountEntity accountEntity : accountEntityList) {
            List<LimitTradeConfigEntity> configList = limitTradeConfgBiz.findAllByUserIdAndMarketType(accountEntity.getUserId(), DictEnum.MARKET_TYPE_ZB.getCode());
            for (LimitTradeConfigEntity config : configList) {
                zbCreateModelLimitOrder(config, accountEntity);
            }
        }

    }

    /**
     * zb限价单
     */
    public void zbCreateModelLimitOrder(LimitTradeConfigEntity config, AccountEntity accountEntity) {
        try {
            //买单状态更新
            List<OrderEntity> buyList = orderBiz.findZbByUserIdAndModel(accountEntity.getUserId(), DictEnum.ORDER_MODEL_LIMIT.getCode(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), config.getSymbol(), config.getOid());
            Iterator<OrderEntity> buyIt = buyList.iterator();
            while (buyIt.hasNext()) {
                OrderEntity buyOrder = buyIt.next();
                buyOrder = orderBiz.updateZbOrderState(buyOrder);
                //买单已完成
                if (DictEnum.ZB_ORDER_DETAIL_STATE_1.getCode().equals(buyOrder.getState()) || DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode().equals(buyOrder.getState())) {
                    log.info("买单已完结,buyOrder={}", buyOrder);
                    buyIt.remove();
                    if (DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode().equals(buyOrder.getState())) {
                        transToEmailNotify(buyOrder);
                    }
                }
            }
            //卖单状态更新
            List<OrderEntity> saleList = orderBiz.findZbByUserIdAndModel(accountEntity.getUserId(), DictEnum.ORDER_MODEL_LIMIT.getCode(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), config.getSymbol(), config.getOid());
            Iterator<OrderEntity> saleIt = saleList.iterator();
            while (saleIt.hasNext()) {
                OrderEntity saleOrder = saleIt.next();
                saleOrder = orderBiz.updateZbOrderState(saleOrder);
                //卖单已完成
                if (DictEnum.ZB_ORDER_DETAIL_STATE_1.getCode().equals(saleOrder.getState()) || DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode().equals(saleOrder.getState())) {
                    log.info("卖单已完结,saleOrder={}", saleOrder);
                    saleIt.remove();
                    if (DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode().equals(saleOrder.getState())) {
                        transToEmailNotify(saleOrder);
                    }
                }
            }

            boolean result = this.checkNeedToCreateLimitOrder(buyList, saleList, config, accountEntity.getUserId());
            if (!result) {
                return;
            }
            ZbTickerVo zbTickerVo = zbApi.getTicker(config.getSymbol());
            if (zbTickerVo == null) {
                log.info("获取行情失败");
                return;
            }
            log.info("创建限价单开始,symbols={},userId={}", config.getSymbol(), accountEntity.getUserId());

            BigDecimal buyPrice = (new BigDecimal(1).subtract(config.getDecrease())).multiply(zbTickerVo.getLast());

            String baseCurrency = marketRuleBiz.getZbBaseCurrency(config.getSymbol());
            //基对余额
            BigDecimal balanceMax = accountBiz.getZbBalance(accountEntity.getUserId(), baseCurrency);
            //总成交额
            BigDecimal totalAmount = config.getTotalAmount().compareTo(balanceMax) <= 0 ? config.getTotalAmount() : balanceMax;
            //购买数量
            BigDecimal amount = totalAmount.divide(buyPrice, 4, BigDecimal.ROUND_DOWN);
            BigDecimal salePrice = (new BigDecimal(1).add(config.getIncrease())).multiply(zbTickerVo.getLast());
            CreateOrderDto buyOrderDto = new CreateOrderDto();
            buyOrderDto.setSymbol(config.getSymbol());
            buyOrderDto.setPrice(buyPrice);
            buyOrderDto.setOrderType(DictEnum.ORDER_TYPE_BUY_LIMIT.getCode());
            buyOrderDto.setAccountId(accountEntity.getAccountId());
            buyOrderDto.setUserId(accountEntity.getUserId());

            buyOrderDto.setAmount(amount);
            //创建限价买单
            String buyOrderId = tradeBiz.zbCreateOrder(config.getSymbol(), buyPrice, amount, DictEnum.ZB_ORDER_TRADE_TYPE_BUY.getCode(), config.getUserId());
            //保存
            orderBiz.saveZbOrder(buyOrderId, config.getSymbol(), null, null, config.getOid(), config.getUserId(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT.getCode());

            String quoteCurrency = marketRuleBiz.getZbQuoteCurrency(config.getSymbol());
            //业务对余额
            balanceMax = accountBiz.getZbBalance(config.getUserId(), quoteCurrency);
            //验证余额
            amount = amount.compareTo(balanceMax) <= 0 ? amount : balanceMax;
            //创建限价卖单
            String saleOrderId = tradeBiz.zbCreateOrder(config.getSymbol(), salePrice, amount, DictEnum.ZB_ORDER_TRADE_TYPE_SELL.getCode(), config.getUserId());
            //保存
            orderBiz.saveZbOrder(saleOrderId, config.getSymbol(), null, buyOrderId, config.getOid(), config.getUserId(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT.getCode());
            log.info("创建限价单结束,symbols={},userId={},buyOrderId={},saleOrderId={}", config.getSymbol(), accountEntity.getUserId(), buyOrderId, saleOrderId);
        } catch (Exception e) {
            log.error("限价单处理失败,config={},e={}", config, e);
        }
    }


    /**
     * zb检查是否需要去交易
     */
    private boolean zbCheckNeedToBuy(RateChangeVo rateChangeVo, String symbolConfigId) {
        boolean result = orderBiz.zbExistNotFinishOrder(rateChangeVo.getBuyerSymbol(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), symbolConfigId);
        if (!result) {
            result = orderBiz.zbExistNotFinishOrder(rateChangeVo.getSaleSymbol(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), symbolConfigId);
        }
        return result;
    }

    /**
     * ZB交易下单
     */
    private List<String> zbToBuyOrder(RateChangeVo rateChangeVo, SymbolTradeConfigEntity symbolTradeConfig) {

        //所有成交的订单id
        List<String> orderIds = new ArrayList<>();
        DepthDto dto = new DepthDto();
        dto.setSymbol(rateChangeVo.getSaleSymbol());
        dto.setUserId(symbolTradeConfig.getUserId());
        //获取卖单交易深度
        ZbOrderDepthVo saleDepth = zbApi.orderDepth(rateChangeVo.getSaleSymbol(), 2);
        //buy list
        List<List<BigDecimal>> saleBids = saleDepth.getBids();
        //sale list
        List<List<BigDecimal>> saleAsks = saleDepth.getAsks();
        //检查卖单深度是否符合要求
        boolean result = checkSaleDept(rateChangeVo, symbolTradeConfig, saleBids, saleAsks);
        if (result) {

            //获取交易深度
            ZbOrderDepthVo depth = zbApi.orderDepth(rateChangeVo.getBuyerSymbol(), 5);
            //buy list
            List<List<BigDecimal>> buyBids = depth.getBids();
            //hbToSale list
            List<List<BigDecimal>> buyAsks = depth.getAsks();

            BigDecimal amount = getZbBuyAmount(rateChangeVo.getBuyerSymbol(), rateChangeVo.getBuyPrice(), symbolTradeConfig, rateChangeVo.getBaseCurrency());
            //欲购买的下单信息
            Map<BigDecimal, BigDecimal> map = checkDeptAndBeginCreate(rateChangeVo.getBuyerSymbol(), rateChangeVo.getBuyPrice(), amount, rateChangeVo.getBaseCurrency(), symbolTradeConfig, buyBids, buyAsks);
            for (BigDecimal key : map.keySet()) {
                String orderId = tradeBiz.zbCreateOrder(rateChangeVo.getBuyerSymbol(), key, map.get(key), DictEnum.ZB_ORDER_TRADE_TYPE_BUY.getCode(), symbolTradeConfig.getUserId());
                orderIds.add(orderId);
            }
        }
        log.info("orderIds={}", orderIds);
        return orderIds;
    }

    /**
     * 检查交易深度是否满足创建买单条件
     */
    private Map<BigDecimal, BigDecimal> checkDeptAndBeginCreate(String symbol, BigDecimal buyPrice, BigDecimal amount, String baseQuote, SymbolTradeConfigEntity symbolTradeConfig, List<List<BigDecimal>> bids, List<List<BigDecimal>> asks) {
        log.info("checkDeptAndBeginCreate,symbols={},buyPrice={},amount={},baseCurrency={}", symbol, buyPrice, amount, baseQuote);
        Map<BigDecimal, BigDecimal> buyMap = new HashMap<>();
        //判断卖单是否足够
        for (List<BigDecimal> ask : asks) {
            //卖单价格
            BigDecimal salePrice = ask.get(0);
            //(a(卖一)-b(最新成交价))/b <= c(阈值)
            if ((salePrice.subtract(buyPrice)).divide(buyPrice, 4, BigDecimal.ROUND_HALF_UP).compareTo(symbolTradeConfig.getAsksBlunder()) <= 0) {
                //卖单足够
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    //卖一+上浮=购买价
                    buyPrice = salePrice.add(symbolTradeConfig.getBuyIncreasePrice());
                    BigDecimal saleAmount = ask.get(1);
                    amount = amount.compareTo(saleAmount) > 0 ? saleAmount : amount;
                    buyMap.put(buyPrice, amount);
                    log.info("卖单价购买,buyPrice={},saleOnePrice={},remainAmount={},saleOneAmount={},orderId={}", buyPrice, salePrice, amount, saleAmount);
                    amount = amount.subtract(saleAmount);
                } else {
                    log.info("卖单数量已满足购买需求,订单完成委托");
                    break;
                }
            } else {
                log.info("卖单价格比例已超过阈值,不再已卖单价购买,salePrice={},buyPrice={},asksBlunder={}", salePrice, buyPrice, symbolTradeConfig.getBuyIncreasePrice());
                break;
            }
        }
        //未已卖单价成交所有,参考买一价购买
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            //买一价
            BigDecimal buyOnePrice = bids.get(0).get(0);
            //买单价格高于买一
            if (buyPrice.compareTo(buyOnePrice) > 0) {
                buyPrice = buyOnePrice.add(symbolTradeConfig.getBuyIncreasePrice());
            } else {
                buyPrice = buyPrice.add(symbolTradeConfig.getBuyIncreasePrice());
            }
            buyMap.put(buyPrice, amount);
            log.info("买单价购买,remainAmount={},buyPrice={},nowPrice={},orderId={}", amount, buyPrice, buyPrice);
        }
        log.info("欲购买的订单价格信息,buyMap={}", buyMap);
        return buyMap;
    }

    /**
     * 创建zb买单
     */
    private List<String> zbBeginSaleOrder(RateChangeEntity rateChangeEntity, BigDecimal amount, SymbolTradeConfigEntity symbolTradeConfig) {
        log.info("zbCreateSaleOrder,rateChangeVo={},amount={}", rateChangeEntity, amount);
        //获取交易深度
        ZbOrderDepthVo depth = zbApi.orderDepth(rateChangeEntity.getSaleSymbol(), 5);
        //所有成交的订单id
        List<String> orderIds = new ArrayList<>();
        //buy list
        List<List<BigDecimal>> bids = depth.getBids();
        //sale list
        List<List<BigDecimal>> asks = depth.getAsks();
        Map<BigDecimal, BigDecimal> map = checkDeptAndBeginSale(rateChangeEntity, amount, symbolTradeConfig, bids, asks);
        for (BigDecimal key : map.keySet()) {
            String orderId = zbCreateSaleOrder(rateChangeEntity.getSaleSymbol(), key, map.get(key), rateChangeEntity.getQuoteCurrency(), symbolTradeConfig.getUserId());
            orderIds.add(orderId);
        }
        log.info("orderIds={}", orderIds);
        return orderIds;
    }

    /**
     * 限价卖
     */
    public String zbCreateSaleOrder(String symbol, BigDecimal price, BigDecimal amount, String quoteCurrency, String userId) {
        log.info("zbCreateSaleOrder,symbols={},price={},amount={},quoteCurrency={},userId={}", symbol, price, amount, quoteCurrency, userId);
        //判断是否超过可使用的上限
        BigDecimal balanceMax = accountBiz.getZbBalance(userId, quoteCurrency);
        amount = balanceMax.compareTo(amount) < 0 ? balanceMax : amount;
        return tradeBiz.zbCreateOrder(symbol, price, amount, DictEnum.ZB_ORDER_TRADE_TYPE_SELL.getCode(), userId);
    }

    /**
     * 检查是否需要创建新的订单
     */
    private boolean checkNeedToCreateLimitOrder(List<OrderEntity> buyList, List<OrderEntity> saleList, LimitTradeConfigEntity config, String userId) {
        boolean result = true;
        //买单大于最大数量 || 买单不为空且买单等于卖单 ||买单有成交,卖单未成交
        if (buyList.size() >= config.getMaxTradeCount() || (!buyList.isEmpty() && saleList.size() == buyList.size()) || (buyList.size() == (config.getMaxTradeCount() - 1) && saleList.size() > buyList.size())) {
            log.info("挂单已满,symbol={},userId={},configId={}", config.getSymbol(), userId, config.getOid());
            result = false;
        } else if (buyList.isEmpty() && saleList.size() >= config.getMaxTradeCount()) {
            log.info("买单成交,卖单未成交,symbol={},userId={}", config.getSymbol(), userId);
            result = false;
        }
        return result;
    }


    /**
     * 限价单交易成功邮件通知
     */
    private void transToEmailNotify(OrderEntity orderEntity) {
        UserEntity userEntity = userBiz.findById(orderEntity.getUserId());
        if (StringUtils.isEmpty(userEntity.getNotifyEmail())) {
            log.info("email address is empty...");
            return;
        }
        String subject = orderEntity.getMarketType() + " " + orderEntity.getModel() + " model " + orderEntity.getSymbol() + " " + orderEntity.getType() + " success notify";
        String content = orderEntity.getSymbol() + " " + orderEntity.getType() + " success. price is " + orderEntity.getPrice().setScale(8, BigDecimal.ROUND_DOWN)
                + ",amount is " + orderEntity.getAmount().setScale(8, BigDecimal.ROUND_DOWN) + " and totalToUsdt is " + orderEntity.getTotalToUsdt().setScale(8, BigDecimal.ROUND_DOWN) + ".";
        MailQQ.sendEmail(subject, content, userEntity.getNotifyEmail());
    }


    /**
     * 根据交易深度创建卖单
     */
    private Map<BigDecimal, BigDecimal> checkDeptAndBeginSale(RateChangeEntity rateChangeEntity, BigDecimal amount, SymbolTradeConfigEntity symbolTradeConfig, List<List<BigDecimal>> bids, List<List<BigDecimal>> asks) {
        log.info("checkDeptAndBeginSale,rateChangeVo={},amount={}", rateChangeEntity, amount);
        Map<BigDecimal, BigDecimal> map = new HashMap<>();
        //剩余需要售卖的数量
        BigDecimal remainAmount = amount;
        //卖的价格
        BigDecimal salePrice = rateChangeEntity.getSalePrice();

        //判断买单是否足够
        for (List<BigDecimal> bid : bids) {
            //买单价格
            BigDecimal buyPrice = bid.get(0);

            //(a(卖一)-b(最新成交价))/b <= c(阈值)
            if ((salePrice.subtract(buyPrice)).divide(buyPrice, 4, BigDecimal.ROUND_HALF_UP).compareTo(symbolTradeConfig.getAsksBlunder()) <= 0) {
                //买单足够
                if (remainAmount.compareTo(BigDecimal.ZERO) > 0) {
                    //买一+上浮=购买价
                    salePrice = salePrice.subtract(symbolTradeConfig.getBuyIncreasePrice());
                    BigDecimal saleAmount = bid.get(1);
                    //比较要卖的数量是否超过买一
                    saleAmount = remainAmount.compareTo(saleAmount) < 0 ? remainAmount : saleAmount;
                    map.put(salePrice, saleAmount);
                    log.info("买单价售出,salePrice={},remainAmount={},saleAmount={},buyPrice={}", salePrice, remainAmount, saleAmount, buyPrice);
                    remainAmount = remainAmount.subtract(saleAmount);
                } else {
                    log.info("买单数量已满足出售需求,订单完成委托");
                    break;
                }
            } else {
                break;
            }
            //还原卖价
            salePrice = rateChangeEntity.getSalePrice();
        }
        //未已买单价成交所有,参考卖一价售出
        if (remainAmount.compareTo(BigDecimal.ZERO) > 0) {
            //卖一价
            BigDecimal saleOnePrice = asks.get(0).get(0);
            //卖价高于卖一
            if (salePrice.compareTo(saleOnePrice) > 0) {
                salePrice = salePrice.subtract(symbolTradeConfig.getBuyIncreasePrice());
            } else {
                salePrice = saleOnePrice.subtract(symbolTradeConfig.getBuyIncreasePrice());
            }

            map.put(salePrice, remainAmount);
            log.info("买单价卖出,remainAmount={},salePrice={}", remainAmount, salePrice);
        }
        log.info("map={}", map);
        return map;
    }

    /**
     * 限价卖
     */
    public String hbCreateSaleOrder(String symbol, BigDecimal price, BigDecimal amount, String quoteCurrency, String userId) {
        log.info("hb create sale order,symbols={},price={},amount={},quoteCurrency={},userId={}", symbol, price, amount, quoteCurrency, userId);
        //判断是否超过可使用的上限
        CreateOrderDto dto = new CreateOrderDto();
        dto.setSymbol(symbol);
        dto.setOrderType(CreateOrderRequest.OrderType.SELL_LIMIT);
        dto.setPrice(price);

        HuobiBaseDto baseDto = new HuobiBaseDto();
        baseDto.setUserId(userId);
        Accounts accounts = accountBiz.getHuobiSpotAccounts(baseDto);
        BigDecimal balanceMax = accountBiz.getHuobiQuoteBalance(userId, quoteCurrency);
        amount = balanceMax.compareTo(amount) < 0 ? balanceMax : amount;
        dto.setAccountId(String.valueOf(accounts.getId()));
        dto.setAmount(amount);
        dto.setUserId(userId);
        return tradeBiz.hbCreateOrder(dto);
    }

    /**
     * hb配置最大购买数量
     * newPrice 最新价格
     */
    private BigDecimal getHbBuyAmount(String symbol, BigDecimal newPrice, SymbolTradeConfigEntity symbolTradeConfig, String baseQuote) {
        BigDecimal amount;
        BigDecimal maxBalance = accountBiz.getHuobiQuoteBalance(symbolTradeConfig.getUserId(), baseQuote);
        if (symbol.endsWith(DictEnum.HB_MARKET_BASE_USDT.getCode())) {
            maxBalance = maxBalance.compareTo(symbolTradeConfig.getUsdtMaxUse()) < 0 ? maxBalance : symbolTradeConfig.getUsdtMaxUse();
            amount = maxBalance.divide(newPrice, 4, BigDecimal.ROUND_FLOOR);
        } else if (symbol.endsWith(DictEnum.HB_MARKET_BASE_BTC.getCode())) {
            maxBalance = maxBalance.compareTo(symbolTradeConfig.getBtcMaxUse()) < 0 ? maxBalance : symbolTradeConfig.getBtcMaxUse();
            amount = maxBalance.divide(newPrice, 8, BigDecimal.ROUND_FLOOR);
        } else {
            maxBalance = maxBalance.compareTo(symbolTradeConfig.getEthMaxUse()) < 0 ? maxBalance : symbolTradeConfig.getEthMaxUse();
            amount = maxBalance.divide(newPrice, 8, BigDecimal.ROUND_FLOOR);
        }
        log.info("getHbBuyAmount,symbols={},newPrice={},amount={},symbolTradeConfig={}", symbol, newPrice, amount, symbolTradeConfig);
        return amount;
    }


    /**
     * zb配置最大购买数量
     * newPrice 最新价格
     */
    private BigDecimal getZbBuyAmount(String symbol, BigDecimal newPrice, SymbolTradeConfigEntity symbolTradeConfig, String baseQuote) {
        BigDecimal amount;
        BigDecimal maxBalance = accountBiz.getZbBalance(symbolTradeConfig.getUserId(), baseQuote);
        if (symbol.endsWith(DictEnum.HB_MARKET_BASE_USDT.getCode())) {
            maxBalance = maxBalance.compareTo(symbolTradeConfig.getUsdtMaxUse()) < 0 ? maxBalance : symbolTradeConfig.getUsdtMaxUse();
            amount = maxBalance.divide(newPrice, 4, BigDecimal.ROUND_FLOOR);
        } else if (symbol.endsWith(DictEnum.HB_MARKET_BASE_BTC.getCode())) {
            maxBalance = maxBalance.compareTo(symbolTradeConfig.getBtcMaxUse()) < 0 ? maxBalance : symbolTradeConfig.getBtcMaxUse();
            amount = maxBalance.divide(newPrice, 8, BigDecimal.ROUND_FLOOR);
        } else {
            maxBalance = maxBalance.compareTo(symbolTradeConfig.getQcMaxUse()) < 0 ? maxBalance : symbolTradeConfig.getQcMaxUse();
            amount = maxBalance.divide(newPrice, 8, BigDecimal.ROUND_FLOOR);
        }
        log.info("getBuyAmount,symbols={},newPrice={},amount={},symbolTradeConfig={}", symbol, newPrice, amount, symbolTradeConfig);
        return amount;
    }

    /**
     * hb beta 订单检查
     */
    public void hbCheckLimitBetaOrder() {
        List<UserEntity> userList = userBiz.findAllHbByNormal();
        for (UserEntity user : userList) {
            List<LimitBetaConfigEntity> betaList = limitBetaConfigBiz.findByUserIdAndMarketTypeInUse(user.getOid(), DictEnum.MARKET_TYPE_HB.getCode());
            for (LimitBetaConfigEntity beta : betaList) {
                try {
                    OrderEntity order = orderBiz.findHbLimitOrder(user.getOid(), beta.getSymbol(), beta.getOid(), DictEnum.ORDER_MODEL_LIMIT_BETA.getCode());
                    BigDecimal amount;
                    BigDecimal balanceMax;
                    //不存在beta订单
                    if (order == null) {
                        hbCreateBetaLimitBuyOrder(beta);
                    } else {
                        order = orderBiz.updateHbOrderState(order);
                        //订单已完成
                        if (DictEnum.ORDER_DETAIL_STATE_FILLED.getCode().equals(order.getState()) || DictEnum.ORDER_DETAIL_STATE_PARTIAL_CANCELED.getCode().equals(order.getState())) {
                            //买单完成,创建卖单
                            if (DictEnum.ORDER_TYPE_BUY_LIMIT.getCode().equals(order.getType())) {
                                log.info("beta买单结束,orderId={}", order.getOrderId());
                                CreateOrderDto saleOrderDto = new CreateOrderDto();
                                saleOrderDto.setSymbol(beta.getSymbol());
                                saleOrderDto.setOrderType(DictEnum.ORDER_TYPE_SELL_LIMIT.getCode());
                                BigDecimal salePrice = beta.getRealPrice().multiply(new BigDecimal(1).add(beta.getFluctuate()));
                                saleOrderDto.setPrice(salePrice);
                                saleOrderDto.setUserId(user.getOid());
                                String quoteCurrency = marketRuleBiz.getHbQuoteCurrency(beta.getSymbol());
                                //业务对余额
                                balanceMax = accountBiz.getHuobiQuoteBalance(user.getOid(), quoteCurrency);
                                amount = order.getAmount();
                                //验证余额
                                amount = amount.compareTo(balanceMax) <= 0 ? amount : balanceMax;
                                saleOrderDto.setAmount(amount);
                                //创建限价卖单
                                String saleOrderId = tradeBiz.hbCreateOrder(saleOrderDto);
                                orderBiz.saveHbOrder(saleOrderId, null, order.getOrderId(), beta.getOid(), user.getOid(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT_BETA.getCode());
                                //更新买单为已挂单售卖
                                order.setState(DictEnum.ORDER_DETAIL_STATE_SELL.getCode());
                                orderBiz.saveOrder(order);
                            }
                            //卖单完成,创建买单
                            else {
                                log.info("beta卖单结束,orderId={}", order);
                                hbCreateBetaLimitBuyOrder(beta);
                            }
                            if (DictEnum.ORDER_DETAIL_STATE_FILLED.getCode().equals(order.getState()) || DictEnum.ORDER_DETAIL_STATE_SELL.getCode().equals(order.getState())) {
                                log.info("beta订单已完结,order={}", order);
                                transToEmailNotify(order);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("beta订单处理失败,beta={},e={}", beta, e);
                }

            }

        }
    }

    /**
     * 单笔策略beta限价买单
     */
    private void hbCreateBetaLimitBuyOrder(LimitBetaConfigEntity beta) {
        CreateOrderDto buyOrderDto = new CreateOrderDto();
        buyOrderDto.setSymbol(beta.getSymbol());
        //卖一价
        BigDecimal saleOnePrice = saleOnePrice(beta.getSymbol(), beta.getUserId());
        //买一价
        BigDecimal buyOnePrice = buyOnePrice(beta.getSymbol(), beta.getUserId());

        BigDecimal buyPrice = (new BigDecimal(1).subtract(beta.getFluctuateDecrease())).multiply(saleOnePrice);
        buyOrderDto.setPrice(buyPrice);
        buyOrderDto.setOrderType(DictEnum.ORDER_TYPE_BUY_LIMIT.getCode());
        buyOrderDto.setUserId(beta.getUserId());

        String baseCurrency = marketRuleBiz.getHbBaseCurrency(beta.getSymbol());
        //基对余额
        BigDecimal balanceMax = accountBiz.getHuobiQuoteBalance(beta.getUserId(), baseCurrency);
        //总成交额
        BigDecimal totalAmount = beta.getTotalAmount().compareTo(balanceMax) <= 0 ? beta.getTotalAmount() : balanceMax;
        //购买数量
        BigDecimal amount = totalAmount.divide(buyPrice, 4, BigDecimal.ROUND_DOWN);
        buyOrderDto.setAmount(amount);
        //创建限价买单
        String buyOrderId = tradeBiz.hbCreateOrder(buyOrderDto);
        orderBiz.saveHbOrder(buyOrderId, null, null, beta.getOid(), beta.getUserId(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT_BETA.getCode());
        beta.setRealPrice(buyOnePrice);
        limitBetaConfigBiz.save(beta);
    }

    /**
     * 单笔策略限价卖单
     */
    private void hbCreateLimitSellOrder(LimitGammaConfigEntity gamma) {
        log.info("创建gamma卖单,gamma={}", gamma);
        CreateOrderDto saleOrderDto = new CreateOrderDto();
        //卖一价
        BigDecimal saleOnePrice = saleOnePrice(gamma.getSymbol(), gamma.getUserId());
        //买一价
        BigDecimal buyOnePrice = buyOnePrice(gamma.getSymbol(), gamma.getUserId());

        BigDecimal salePrice = (new BigDecimal(1).add(gamma.getFluctuate())).multiply(buyOnePrice);
        saleOrderDto.setSymbol(gamma.getSymbol());
        saleOrderDto.setOrderType(DictEnum.ORDER_TYPE_SELL_LIMIT.getCode());
        saleOrderDto.setPrice(salePrice);
        saleOrderDto.setUserId(gamma.getUserId());
        String quoteCurrency = marketRuleBiz.getHbQuoteCurrency(gamma.getSymbol());
        //业务对余额
        BigDecimal balanceMax = accountBiz.getHuobiQuoteBalance(gamma.getUserId(), quoteCurrency);
        BigDecimal amount = gamma.getTotalAmount().divide(salePrice, 2, BigDecimal.ROUND_DOWN);
        //验证余额
        if (amount.compareTo(balanceMax) > 0) {
            log.info("余额不足,售卖数量不足,amount={},balanceMax={}", amount, balanceMax);
            mailBiz.balanceToEmailNotify(gamma.getUserId(), quoteCurrency, DictEnum.MARKET_TYPE_HB.getCode());
            amount = balanceMax;
        }
        saleOrderDto.setAmount(amount);
        //创建限价卖单
        String saleOrderId = tradeBiz.hbCreateOrder(saleOrderDto);
        orderBiz.saveHbOrder(saleOrderId, null, null, gamma.getOid(), gamma.getUserId(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT_GAMMA.getCode());
        gamma.setRealPrice(saleOnePrice);
        limitGammaConfigBiz.save(gamma);
    }

    /**
     * 检查超时订单
     */
    public void checkTimeOutOrder() {
        List<OrderEntity> orderEntityList = orderBiz.findNoFillLimit();
        for (OrderEntity entity : orderEntityList) {
            cancelTimeOutOrder(entity);
        }
    }

    /**
     * 撤销超时订单
     */
    private void cancelTimeOutOrder(OrderEntity orderEntity) {
        try {
            if (DateUtils.addMinute(orderEntity.getCreateTime(), timeOutMinute).before(DateUtils.getCurrDateMmss())) {
                //撤销之前先同步最新状态，逻辑得优化,下面有判断市场
                if (DictEnum.MARKET_TYPE_HB.getCode().equals(orderEntity.getMarketType())) {
                    orderBiz.updateHbOrderState(orderEntity);
                } else {
                    orderBiz.updateZbOrderState(orderEntity);
                }
                //买单超过指定未成交
                if (orderEntity.getType().equals(DictEnum.ORDER_TYPE_BUY_LIMIT.getCode())) {
                    //beta 单
                    if (orderEntity.getModel().equals(DictEnum.ORDER_MODEL_LIMIT_BETA.getCode())) {
                        //hb 未成交
                        if (orderEntity.getState().equals(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode())) {
                            log.info("hb beta限价单超时未成交,撤销订单,orderId={}", orderEntity.getOrderId());
                            HuobiBaseDto dto = new HuobiBaseDto();
                            dto.setOrderId(orderEntity.getOrderId());
                            dto.setUserId(orderEntity.getUserId());
                            tradeBiz.hbCancelOrder(dto);
                            orderBiz.updateHbOrderState(orderEntity);
                        }
                        //zb 未成交
                        else if (orderEntity.getState().equals(DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode())) {
                            log.info("zb beta限价单超时未成交,撤销订单,orderId={}", orderEntity.getOrderId());
                            tradeBiz.zbCancelOrder(orderEntity.getOrderId(), orderEntity.getSymbol(), orderEntity.getUserId());
                            orderBiz.updateZbOrderState(orderEntity);
                        }
                    }
                    //alpha 单
                    else if (orderEntity.getModel().equals(DictEnum.ORDER_MODEL_LIMIT.getCode())) {
                        OrderEntity sellOrder = orderBiz.findByBuyOrderId(orderEntity.getOrderId());
                        //卖单未成交
                        if (sellOrder != null && sellOrder.getState().equals(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode())) {
                            //hb
                            if (orderEntity.getState().equals(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode())) {
                                log.info("hb alpha限价买单和卖单均超时未成交,撤销订单,orderId={}", orderEntity.getOrderId());
                                //撤销买单
                                HuobiBaseDto dto = new HuobiBaseDto();
                                dto.setOrderId(orderEntity.getOrderId());
                                dto.setUserId(orderEntity.getUserId());
                                tradeBiz.hbCancelOrder(dto);
                                orderBiz.updateHbOrderState(orderEntity);
                                //撤销卖单
                                dto.setOrderId(sellOrder.getOrderId());
                                dto.setUserId(sellOrder.getUserId());
                                tradeBiz.hbCancelOrder(dto);
                                orderBiz.updateHbOrderState(sellOrder);
                            }
                            //zb
                            else if (orderEntity.getState().equals(DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode())) {
                                log.info("zb alpha限价买单和卖单均超时未成交,撤销订单,orderId={}", orderEntity.getOrderId());
                                //撤销买单
                                tradeBiz.zbCancelOrder(orderEntity.getOrderId(), orderEntity.getSymbol(), orderEntity.getUserId());
                                orderBiz.updateZbOrderState(orderEntity);
                                //撤销卖单
                                tradeBiz.zbCancelOrder(sellOrder.getOrderId(), sellOrder.getSymbol(), sellOrder.getUserId());
                                orderBiz.updateZbOrderState(sellOrder);
                            }
                        }
                    }
                } else {
                    //卖单未成交
                    //gamma 单
                    if (orderEntity.getModel().equals(DictEnum.ORDER_MODEL_LIMIT_GAMMA.getCode())) {
                        //hb 未成交
                        if (orderEntity.getState().equals(DictEnum.ORDER_DETAIL_STATE_SUBMITTED.getCode())) {
                            log.info("hb gamma限价单超时未成交,撤销订单,orderId={}", orderEntity.getOrderId());
                            HuobiBaseDto dto = new HuobiBaseDto();
                            dto.setOrderId(orderEntity.getOrderId());
                            dto.setUserId(orderEntity.getUserId());
                            tradeBiz.hbCancelOrder(dto);
                            orderBiz.updateHbOrderState(orderEntity);
                        }
                        //zb 未成交
                        else if (orderEntity.getState().equals(DictEnum.ZB_ORDER_DETAIL_STATE_3.getCode())) {
                            log.info("zb beta限价单超时未成交,撤销订单,orderId={}", orderEntity.getOrderId());
                            tradeBiz.zbCancelOrder(orderEntity.getOrderId(), orderEntity.getSymbol(), orderEntity.getUserId());
                            orderBiz.updateZbOrderState(orderEntity);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("限价单超时撤销失败,orderEntity={},e={}", orderEntity, e);
        }

    }

    /**
     * zb beta 订单检查
     */
    public void zbCheckLimitBetaOrder() {
        List<UserEntity> userList = userBiz.findAllZbByNormal();
        for (UserEntity user : userList) {
            List<LimitBetaConfigEntity> betaList = limitBetaConfigBiz.findByUserIdAndMarketTypeInUse(user.getOid(), DictEnum.MARKET_TYPE_ZB.getCode());
            for (LimitBetaConfigEntity beta : betaList) {
                try {
                    OrderEntity order = orderBiz.findZbBetaOrder(user.getOid(), beta.getSymbol(), beta.getOid());
                    BigDecimal amount;
                    BigDecimal balanceMax;
                    //不存在beta订单
                    if (order == null) {
                        zbCreateBetaLimitOrder(beta);
                    } else {
                        order = orderBiz.updateZbOrderState(order);
                        //订单已完成
                        if (DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode().equals(order.getState())) {
                            //买单完成,创建卖单
                            if (DictEnum.ORDER_TYPE_BUY_LIMIT.getCode().equals(order.getType())) {
                                log.info("beta买单结束");
                                BigDecimal salePrice = beta.getRealPrice().multiply(new BigDecimal(1).add(beta.getFluctuate()));
                                String quoteCurrency = marketRuleBiz.getZbQuoteCurrency(beta.getSymbol());
                                //业务对余额
                                balanceMax = accountBiz.getZbBalance(user.getOid(), quoteCurrency);
                                amount = order.getAmount();
                                //验证余额
                                amount = amount.compareTo(balanceMax) <= 0 ? amount : balanceMax;
                                //创建限价卖单
                                String saleOrderId = tradeBiz.zbCreateOrder(beta.getSymbol(), salePrice, amount, DictEnum.ZB_ORDER_TRADE_TYPE_SELL.getCode(), user.getOid());
                                orderBiz.saveZbOrder(saleOrderId, beta.getSymbol(), null, order.getOrderId(), beta.getOid(), user.getOid(),
                                        DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT_BETA.getCode());
                                //更新买单为已挂单售卖
                                order.setState(DictEnum.ZB_ORDER_DETAIL_STATE_4.getCode());
                                orderBiz.saveOrder(order);
                            }
                            //卖单完成,创建买单
                            else {
                                log.info("beta卖单结束");
                                zbCreateBetaLimitOrder(beta);
                            }
                            if (DictEnum.ZB_ORDER_DETAIL_STATE_4.getCode().equals(order.getState()) || DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode().equals(order.getState())) {
                                log.info("beta订单已完结,order={}", order);
                                transToEmailNotify(order);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("beta限价单处理失败,beta={},e={}", beta, e);
                }

            }

        }
    }


    private void zbCreateBetaLimitOrder(LimitBetaConfigEntity beta) {
        ZbTickerVo zbTickerVo = zbApi.getTicker(beta.getSymbol());
        if (zbTickerVo == null) {
            log.info("获取行情失败");
            return;
        }
        log.info("创建限价beta单开始,symbols={},userId={}", beta.getSymbol(), beta.getUserId());

        BigDecimal buyPrice = (new BigDecimal(1).subtract(beta.getFluctuate())).multiply(zbTickerVo.getLast());

        String baseCurrency = marketRuleBiz.getZbBaseCurrency(beta.getSymbol());
        //基对余额
        BigDecimal balanceMax = accountBiz.getZbBalance(beta.getUserId(), baseCurrency);
        //总成交额
        BigDecimal totalAmount = beta.getTotalAmount().compareTo(balanceMax) <= 0 ? beta.getTotalAmount() : balanceMax;
        //购买数量
        BigDecimal amount = totalAmount.divide(buyPrice, 4, BigDecimal.ROUND_DOWN);
        CreateOrderDto buyOrderDto = new CreateOrderDto();
        buyOrderDto.setSymbol(beta.getSymbol());
        buyOrderDto.setPrice(buyPrice);
        buyOrderDto.setOrderType(DictEnum.ORDER_TYPE_BUY_LIMIT.getCode());
        buyOrderDto.setUserId(beta.getUserId());
        buyOrderDto.setAmount(amount);
        //创建限价买单
        String buyOrderId = tradeBiz.zbCreateOrder(beta.getSymbol(), buyPrice, amount, DictEnum.ZB_ORDER_TRADE_TYPE_BUY.getCode(), beta.getUserId());
        //保存
        orderBiz.saveZbOrder(buyOrderId, beta.getSymbol(), null, null, beta.getOid(), beta.getUserId(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT_BETA.getCode());
        beta.setRealPrice(zbTickerVo.getLast());
        limitBetaConfigBiz.save(beta);
    }


    /**
     * hb gamma 订单检查
     */
    public void hbCheckLimitGammaOrder() {
        List<UserEntity> userList = userBiz.findAllHbByNormal();
        for (UserEntity user : userList) {
            List<LimitGammaConfigEntity> gammaList = limitGammaConfigBiz.findByUserIdAndMarketTypeInUse(user.getOid(), DictEnum.MARKET_TYPE_HB.getCode());
            for (LimitGammaConfigEntity gamma : gammaList) {
                try {
                    OrderEntity order = orderBiz.findHbLimitOrder(user.getOid(), gamma.getSymbol(), gamma.getOid(), DictEnum.ORDER_MODEL_LIMIT_GAMMA.getCode());
                    BigDecimal amount;
                    BigDecimal balanceMax;
                    //不存在gamma订单,创建卖单
                    if (order == null) {
                        hbCreateLimitSellOrder(gamma);
                    } else {
                        order = orderBiz.updateHbOrderState(order);
                        //订单已完成
                        if (order.getState().equals(DictEnum.ORDER_DETAIL_STATE_FILLED.getCode()) || order.getState().equals(DictEnum.ORDER_DETAIL_STATE_PARTIAL_CANCELED.getCode())) {
                            //卖单完成,创建买单
                            if (DictEnum.ORDER_TYPE_SELL_LIMIT.getCode().equals(order.getType())) {
                                log.info("gamma卖单结束,开始创建买单,orderId={}", order.getOrderId());
                                CreateOrderDto buyOrderDto = new CreateOrderDto();
                                buyOrderDto.setSymbol(gamma.getSymbol());
                                buyOrderDto.setOrderType(DictEnum.ORDER_TYPE_BUY_LIMIT.getCode());
                                BigDecimal buyPrice = gamma.getRealPrice().multiply(new BigDecimal(1).subtract(gamma.getFluctuateDecrease()));
                                buyOrderDto.setPrice(buyPrice);
                                buyOrderDto.setUserId(user.getOid());

                                String baseCurrency = marketRuleBiz.getHbBaseCurrency(gamma.getSymbol());
                                //基对余额
                                balanceMax = accountBiz.getHuobiQuoteBalance(gamma.getUserId(), baseCurrency);
                                //欲购买数量
                                amount = order.getAmount();
                                //总成交额
                                BigDecimal totalAmount = amount.multiply(buyPrice);
                                if (balanceMax.compareTo(totalAmount) < 0) {
                                    log.info("买单余额不足,totalAmount={},balanceMax={}", totalAmount, balanceMax);
                                    amount = balanceMax.divide(buyPrice, 2, BigDecimal.ROUND_DOWN);
                                    mailBiz.balanceToEmailNotify(gamma.getUserId(), baseCurrency, DictEnum.MARKET_TYPE_HB.getCode());
                                }
                                buyOrderDto.setAmount(amount);
                                //创建限价买单
                                String buyOrderId = tradeBiz.hbCreateOrder(buyOrderDto);
                                orderBiz.saveHbOrder(buyOrderId, null, null, gamma.getOid(), user.getOid(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT_GAMMA.getCode());
                                //更新卖单为已挂单购买
                                order.setState(DictEnum.ORDER_DETAIL_STATE_BUY.getCode());
                                order.setBuyOrderId(buyOrderId);
                                orderBiz.saveOrder(order);
                            } else {
                                //买单完成,创建卖单
                                log.info("gamma买单结束,orderId={}", order.getOrderId());
                                hbCreateLimitSellOrder(gamma);
                            }
                            if (DictEnum.ORDER_DETAIL_STATE_FILLED.getCode().equals(order.getState()) || DictEnum.ORDER_DETAIL_STATE_BUY.getCode().equals(order.getState())) {
                                log.info("gamma订单已完结,order={}", order);
                                transToEmailNotify(order);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("gamma订单处理失败,beta={},e={}", gamma, e);
                }

            }

        }
    }

    /**
     * 检查gamma类型趋势
     */
    public void checkGammaTrend() {
        List<UserEntity> userList = userBiz.findAllHbByNormal();
        for (UserEntity user : userList) {

            List<LimitGammaConfigEntity> gammaList = limitGammaConfigBiz.findByUserIdAndMarketType(user.getOid(), DictEnum.MARKET_TYPE_HB.getCode());
            for (LimitGammaConfigEntity gamma : gammaList) {
                try {
                    Boolean trendUp = calcTrend(gamma.getSymbol());
                    //趋势下降,启动gamma,停止beta
                    if (trendUp != null && !trendUp) {
                        if (!DictEnum.IS_USER_YES.getCode().equals(gamma.getIsUse())) {
                            log.info("趋势下降,启动gamma,停止beta,symbol={}", gamma.getSymbol());
                            gamma.setIsUse(DictEnum.IS_USER_YES.getCode());
                            limitGammaConfigBiz.save(gamma);
                        }
                        stopBeta(gamma.getUserId(), gamma.getSymbol(), gamma.getMarketType());
                    }
                    //hb对行情接口有频率限制
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.info("检查趋势失败,gamma={},e={}", gamma, e);
                }
            }
        }
    }

    /**
     * 停止beta策略并还原订单状态
     */
    private void stopBeta(String userId, String symbol, String marketType) {
        List<LimitBetaConfigEntity> list = limitBetaConfigBiz.findByUserIdAndSymbolAndMarketType(userId, symbol, marketType);
        for (LimitBetaConfigEntity beta : list) {
            try {
                if (!beta.getIsUse().equals(DictEnum.IS_USER_NO.getCode())) {
                    log.info("停止beta策略并还原订单状态,symbol={}", symbol);
                    beta.setIsUse(DictEnum.IS_USER_NO.getCode());
                    limitBetaConfigBiz.save(beta);
                    OrderEntity order = orderBiz.findOneByParam(userId, DictEnum.ORDER_MODEL_LIMIT_BETA.getCode(), symbol, beta.getOid());
                    if (order != null) {
                        //撤销
                        HuobiBaseDto dto = new HuobiBaseDto();
                        dto.setOrderId(order.getOrderId());
                        dto.setUserId(userId);
                        tradeBiz.hbCancelOrder(dto);
                        //卖单未成交,撤销完再卖出
                        if (DictEnum.ORDER_TYPE_SELL_LIMIT.getCode().equals(order.getType())) {
                            log.info("beta卖单未成交,撤销后挂单卖出,orderId={}", order.getOrderId());
                            CreateOrderDto saleOrderDto = new CreateOrderDto();
                            //买一价
                            BigDecimal buyOnePrice = buyOnePrice(beta.getSymbol(), beta.getUserId());
                            saleOrderDto.setSymbol(symbol);
                            saleOrderDto.setOrderType(DictEnum.ORDER_TYPE_SELL_LIMIT.getCode());
                            saleOrderDto.setPrice(buyOnePrice.multiply(new BigDecimal(0.9995)));
                            saleOrderDto.setUserId(userId);
                            String quoteCurrency = marketRuleBiz.getHbQuoteCurrency(symbol);
                            //业务对余额
                            BigDecimal balanceMax = accountBiz.getHuobiQuoteBalance(userId, quoteCurrency);
                            BigDecimal amount = order.getAmount();
                            //验证余额
                            if (amount.compareTo(balanceMax) > 0) {
                                log.info("余额不足,售卖数量不足,amount={},balanceMax={}", amount, balanceMax);
                                mailBiz.balanceToEmailNotify(userId, quoteCurrency, DictEnum.MARKET_TYPE_HB.getCode());
                                amount = balanceMax;
                            }
                            saleOrderDto.setAmount(amount);
                            String orderId = tradeBiz.hbCreateOrder(saleOrderDto);
                            orderBiz.saveHbOrder(orderId, null, order.getBuyOrderId(), beta.getOid(), userId, DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT_BETA.getCode());
                        }
                        //更新原始订单状态
                        orderBiz.updateHbOrderState(order);
                    }
                }
            } catch (BizException e) {
                log.info("停止beta策略失败,symbol={},e={}", symbol, e);
            }

        }
    }


    /**
     * 检查beta类型趋势
     */
    public void checkBetaTrend() {
        List<UserEntity> userList = userBiz.findAllHbByNormal();
        for (UserEntity user : userList) {
            List<LimitBetaConfigEntity> betaList = limitBetaConfigBiz.findByUserIdAndMarketType(user.getOid(), DictEnum.MARKET_TYPE_HB.getCode());
            for (LimitBetaConfigEntity beta : betaList) {
                try {
                    Boolean trendUp = calcTrend(beta.getSymbol());
                    //趋势上升,启动beta,停止gamma
                    if (trendUp != null && trendUp) {
                        if (!beta.getIsUse().equals(DictEnum.IS_USER_YES.getCode())) {
                            log.info("趋势上升,启动beta,停止gamma,symbol={}", beta.getSymbol());
                            beta.setIsUse(DictEnum.IS_USER_YES.getCode());
                            limitBetaConfigBiz.save(beta);
                        }
                        stopGamma(beta.getUserId(), beta.getSymbol(), beta.getMarketType());
                    }
                    //hb对行情接口有频率限制
                    Thread.sleep(3000);
                } catch (Exception e) {
                    log.info("检查趋势失败,beta={},e={}", beta, e);
                }
            }
        }
    }

    /**
     * 停止gamma策略并还原订单状态
     */
    private void stopGamma(String userId, String symbol, String marketType) {
        List<LimitGammaConfigEntity> list = limitGammaConfigBiz.findByUserIdAndSymbolAndMarketType(userId, symbol, marketType);
        for (LimitGammaConfigEntity gamma : list) {
            try {
                if (!DictEnum.IS_USER_NO.getCode().equals(gamma.getIsUse())) {
                    log.info("停止gamma策略并还原订单状态,symbol={}", symbol);
                    gamma.setIsUse(DictEnum.IS_USER_NO.getCode());
                    limitGammaConfigBiz.save(gamma);
                    OrderEntity order = orderBiz.findOneByParam(userId, DictEnum.ORDER_MODEL_LIMIT_GAMMA.getCode(), symbol, gamma.getOid());
                    if (order != null) {
                        //撤销
                        HuobiBaseDto dto = new HuobiBaseDto();
                        dto.setOrderId(order.getOrderId());
                        dto.setUserId(userId);
                        tradeBiz.hbCancelOrder(dto);
                        //买单未成交,撤销完再买入
                        if (DictEnum.ORDER_TYPE_BUY_LIMIT.getCode().equals(order.getType())) {
                            log.info("gamma买单未成交,撤销后挂单买入,orderId={}", order.getOrderId());
                            CreateOrderDto buyOrderDto = new CreateOrderDto();
                            buyOrderDto.setSymbol(gamma.getSymbol());
                            buyOrderDto.setOrderType(DictEnum.ORDER_TYPE_BUY_LIMIT.getCode());
                            //卖一价
                            BigDecimal saleOnePrice = saleOnePrice(gamma.getSymbol(), gamma.getUserId());
                            //略高买
                            buyOrderDto.setPrice(saleOnePrice.multiply(new BigDecimal(1.0005)));
                            buyOrderDto.setUserId(userId);
                            String baseCurrency = marketRuleBiz.getHbBaseCurrency(gamma.getSymbol());
                            //基对余额
                            BigDecimal balanceMax = accountBiz.getHuobiQuoteBalance(gamma.getUserId(), baseCurrency);
                            //欲购买数量
                            BigDecimal amount = order.getAmount();
                            //总成交额
                            BigDecimal totalAmount = amount.multiply(saleOnePrice);
                            if (balanceMax.compareTo(totalAmount) < 0) {
                                log.info("买单余额不足,totalAmount={},balanceMax={}", totalAmount, balanceMax);
                                amount = balanceMax.divide(saleOnePrice, 2, BigDecimal.ROUND_DOWN);
                                mailBiz.balanceToEmailNotify(gamma.getUserId(), baseCurrency, DictEnum.MARKET_TYPE_HB.getCode());
                            }
                            buyOrderDto.setAmount(amount);
                            //创建限价买单
                            String buyOrderId = tradeBiz.hbCreateOrder(buyOrderDto);
                            orderBiz.saveHbOrder(buyOrderId, null, null, gamma.getOid(), userId, DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_LIMIT_GAMMA.getCode());
                            //更新卖单记录的buyOrderId
                            OrderEntity saleOrder = orderBiz.findByBuyOrderId(order.getOrderId());
                            saleOrder.setBuyOrderId(buyOrderId);
                            log.info("更新卖单,saleOrder={},buyOrderId={}", saleOrder, buyOrderId);
                            orderBiz.saveOrder(saleOrder);
                        }
                        //更新原始订单状态
                        orderBiz.updateHbOrderState(order);
                    }
                }
            } catch (BizException e) {
                log.info("停止gamma策略失败,symbol={},e={}", symbol, e);
            }

        }
    }

    private Boolean calcTrend(String symbol) {
        MaInfoDto ma = this.getMaInfo(symbol, DictEnum.MARKET_PERIOD_60MIN.getCode());
        Boolean maUp = null;
        //up
        if (ma.getMa7Middle().compareTo(ma.getMa30Middle()) > 0  && ma.getRate().compareTo(betaAndGameRate) <= 0) {
            log.info("ma4H趋势结果上升,symbol={},ma={}", symbol, ma);
            maUp = true;
        }
        //down
        if (ma.getMa7Middle().compareTo(ma.getMa30Middle()) < 0 && ma.getRate().compareTo(betaAndGameRate) <= 0) {
            log.info("ma4H趋势结果下降,symbol={},ma={}", symbol, ma);
            maUp = false;
        }
        return maUp;
    }

    /**
     * 获取ma趋势数据，hb
     */
    public MaInfoDto getMaInfo(String symbol, String period) {
        MaInfoDto maInfoDto = new MaInfoDto();
        MarketInfoVo marketInfoVo = huobiApi.getMarketInfo(period, 120, symbol);
        if (marketInfoVo == null || marketInfoVo.getData() == null) {
            log.info("获取K线失败");
            return maInfoDto;
        }
        List<MarketDetailVo> detailVoList = marketInfoVo.getData();
        BigDecimal oneTotal = BigDecimal.ZERO;
        BigDecimal twoTotal = BigDecimal.ZERO;
        BigDecimal oneMiddle;
        BigDecimal twoMiddle;
        for (int i = 0; i < detailVoList.size(); i++) {
            if (i >= 0 && i < 28 && i%4==0) {
                oneTotal = oneTotal.add(detailVoList.get(i).getClose());
            }
            if (i >= 0 && i < 120 && i%4==0) {
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
