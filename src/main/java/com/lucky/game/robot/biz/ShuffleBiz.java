package com.lucky.game.robot.biz;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.dto.huobi.DepthDto;
import com.lucky.game.robot.dto.zb.ZbOrderDetailDto;
import com.lucky.game.robot.entity.AccountEntity;
import com.lucky.game.robot.entity.OrderEntity;
import com.lucky.game.robot.entity.ShuffleConfigEntity;
import com.lucky.game.robot.entity.UserEntity;
import com.lucky.game.robot.huobi.response.Depth;
import com.lucky.game.robot.service.ShuffleConfigService;
import com.lucky.game.robot.vo.huobi.DepthOneVo;
import com.lucky.game.robot.zb.api.ZbApi;
import com.lucky.game.robot.zb.vo.ZbOrderDetailVo;
import com.lucky.game.robot.dto.huobi.HuobiBaseDto;
import com.lucky.game.robot.huobi.response.OrdersDetail;
import com.lucky.game.robot.zb.vo.ZbOrderDepthVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author conan
 *         2018/4/11 10:51
 **/
@Component
@Slf4j
public class ShuffleBiz {

    @Autowired
    private MarketBiz marketBiz;

    @Autowired
    private ZbApi zbApi;

    @Autowired
    private TransBiz transBiz;

    @Autowired
    private OrderBiz orderBiz;

    @Autowired
    private TradeBiz tradeBiz;

    @Autowired
    private AccountBiz accountBiz;

    @Autowired
    private UserBiz userBiz;

    @Autowired
    private MailBiz mailBiz;

    @Autowired
    private ShuffleConfigService shuffleConfigService;


    @Async("shuffleMonitor")
    public void shuffleMonitor() {
        log.info("shuffleMonitor begin");
        while (true) {
            List<UserEntity> userList = userBiz.findAllByNormal();
            for (UserEntity userEntity : userList) {
                List<ShuffleConfigEntity> shuffleList = shuffleConfigService.findByUserIdWithOpen(userEntity.getOid());
                for (ShuffleConfigEntity shuffle : shuffleList) {
                    try {
                        Thread.sleep(5000);
                        checkDepth(shuffle);
                    } catch (Exception e) {
                        log.error("shuffle=={},e={}", shuffle, e);
                    }
                }
            }
        }
    }

    /**
     * 检查hb搬砖订单状态
     */
    public void checkHbShuffleOrder() {
        List<OrderEntity> hbShuffleOrder = orderBiz.findHbShuffleNoFillOrder();
        for (OrderEntity order : hbShuffleOrder) {
            HuobiBaseDto dto = new HuobiBaseDto();
            dto.setOrderId(order.getOrderId());
            dto.setUserId(order.getUserId());
            OrdersDetail ordersDetail = tradeBiz.getHbOrderDetail(dto);
            //卖单已成交或撤销成交
            if (DictEnum.ORDER_DETAIL_STATE_FILLED.getCode().equals(ordersDetail.getState()) || DictEnum.ORDER_DETAIL_STATE_PARTIAL_CANCELED.getCode().equals(ordersDetail.getState())) {
                log.info("hb搬砖订单交易完成.orderId={}", order.getOrderId());
                order.setState(ordersDetail.getState());
                order.setFieldAmount(ordersDetail.getFieldAmount());
                order.setFieldCashAmount(ordersDetail.getFieldCashAmount());
                order.setFieldFees(ordersDetail.getFieldFees());
                orderBiz.saveOrder(order);
                UserEntity userEntity = userBiz.findById(order.getUserId());
                //发送成交邮件通知
                mailBiz.transToEmailNotify(order, userEntity);

            }
        }
    }

    /**
     * * 检查zb搬砖订单状态
     */
    public void checkZbShuffleOrder() {
        List<OrderEntity> orderList = orderBiz.findZbShuffleNoFillOrder();
        for (OrderEntity order : orderList) {
            ZbOrderDetailDto dto = new ZbOrderDetailDto();
            dto.setOrderId(order.getOrderId());
            dto.setCurrency(order.getSymbol());
            AccountEntity accountEntity = accountBiz.getByUserIdAndType(order.getUserId(), DictEnum.MARKET_TYPE_ZB.getCode());
            dto.setAccessKey(accountEntity.getApiKey());
            dto.setSecretKey(accountEntity.getApiSecret());
            ZbOrderDetailVo ordersDetail = zbApi.orderDetail(dto);
            //卖单已成交或撤销成交
            if (DictEnum.ZB_ORDER_DETAIL_STATE_2.getCode().equals(ordersDetail.getState())) {
                log.info("zb搬砖订单交易完成.orderId={}", order.getOrderId());
                order.setState(ordersDetail.getState());
                order.setFieldAmount(ordersDetail.getFieldAmount());
                order.setFieldCashAmount(ordersDetail.getFieldCashAmount());
                orderBiz.saveOrder(order);
                UserEntity userEntity = userBiz.findById(order.getUserId());
                //发送成交邮件通知
                mailBiz.transToEmailNotify(order, userEntity);
            }
        }
    }

    private void checkDepth(ShuffleConfigEntity shuffle) {
        String hbSymbol = groupCurrency(shuffle.getBaseCurrency(), shuffle.getQuoteCurrency(), DictEnum.MARKET_TYPE_HB.getCode());
        DepthOneVo hbDepth = hbGetDepth(hbSymbol, shuffle.getUserId());
        if (hbDepth != null) {
            String zbSymbol = groupCurrency(shuffle.getBaseCurrency(), shuffle.getQuoteCurrency(), DictEnum.MARKET_TYPE_ZB.getCode());
            DepthOneVo zbDepth = zbGetDepth(zbSymbol, shuffle.getUserId());
            if (zbDepth != null) {
                //(saleOne*(1+rateValue) <= buyOne zb or hb
                if (hbDepth.getSaleOne().multiply(new BigDecimal(1).add(shuffle.getRateValue())).compareTo(zbDepth.getBuyOne()) <= 0) {
                    log.info("hb卖一比zb买一低,hbDepth={},zbDepth={},shuffle={}", hbDepth, zbDepth, shuffle);
                    BigDecimal buyPrice = hbDepth.getSaleOne().add(shuffle.getBuyIncreasePrice());
                    BigDecimal salePrice = zbDepth.getBuyOne().subtract(shuffle.getBuyIncreasePrice());
                    //购买数量
                    BigDecimal amount = buyAmount(shuffle.getTotalAmount(), buyPrice, shuffle.getUserId(), shuffle.getBaseCurrency(), DictEnum.MARKET_TYPE_HB.getCode());
                    //检验两个市场余额
                    boolean result = this.checkBalance(shuffle.getUserId(), shuffle.getQuoteCurrency(), shuffle.getBaseCurrency(), shuffle.getTotalAmount(), amount, DictEnum.MARKET_TYPE_HB.getCode());
                    if (result) {
                        this.createShuffleOrder(hbSymbol, zbSymbol, buyPrice, salePrice, amount, shuffle, DictEnum.MARKET_TYPE_HB.getCode());
                    }

                } else if (zbDepth.getSaleOne().multiply(new BigDecimal(1).add(shuffle.getRateValue())).compareTo(hbDepth.getBuyOne()) <= 0) {
                    log.info("zb卖一比hb买一低,hbDepth={},zbDepth={},shuffle={}", hbDepth, zbDepth, shuffle);
                    BigDecimal buyPrice = zbDepth.getSaleOne().add(shuffle.getBuyIncreasePrice());
                    BigDecimal salePrice = hbDepth.getBuyOne().subtract(shuffle.getBuyIncreasePrice());
                    //购买数量
                    BigDecimal amount = buyAmount(shuffle.getTotalAmount(), buyPrice, shuffle.getUserId(), shuffle.getBaseCurrency(), DictEnum.MARKET_TYPE_ZB.getCode());
                    //检验两个市场余额
                    boolean result = this.checkBalance(shuffle.getUserId(), shuffle.getQuoteCurrency(), shuffle.getBaseCurrency(), shuffle.getTotalAmount(), amount, DictEnum.MARKET_TYPE_ZB.getCode());
                    if (result) {
                        this.createShuffleOrder(hbSymbol, zbSymbol, buyPrice, salePrice, amount, shuffle, DictEnum.MARKET_TYPE_ZB.getCode());
                    }
                }
            }
        }
    }

    private void createShuffleOrder(String hbSymbol, String zbSymbol, BigDecimal buyPrice, BigDecimal salePrice, BigDecimal amount, ShuffleConfigEntity shuffle, String buyMarketType) {
        log.info("创建shuffle订单,hbSymbol={},zbSymbol={},buyPrice={},salePrice={},amount={},buyMarketType={}", hbSymbol, zbSymbol, buyPrice, salePrice, amount, buyMarketType);
        if (DictEnum.MARKET_TYPE_HB.getCode().equals(buyMarketType)) {
            //create hb buy
            String hbOrderId = transBiz.hbCreateBuyOrder(hbSymbol, buyPrice, amount, shuffle.getUserId());
            orderBiz.saveHbOrder(hbOrderId, null, null, shuffle.getOid(), shuffle.getUserId(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_SHUFFLE.getCode());
            //create zb sell
            String zbOrderId = transBiz.zbCreateSaleOrder(zbSymbol, salePrice, amount, shuffle.getQuoteCurrency(), shuffle.getUserId());
            orderBiz.saveZbOrder(zbOrderId, zbSymbol, null, hbOrderId, shuffle.getOid(),
                    shuffle.getUserId(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.ORDER_MODEL_SHUFFLE.getCode());
        } else if (DictEnum.MARKET_TYPE_ZB.getCode().equals(buyMarketType)) {
            //create zb buy
            String zbOrderId = tradeBiz.zbCreateOrder(zbSymbol, buyPrice, amount, DictEnum.ZB_ORDER_TRADE_TYPE_BUY.getCode(), shuffle.getUserId());
            orderBiz.saveZbOrder(zbOrderId, zbSymbol, null, null, shuffle.getOid(),
                    shuffle.getUserId(), DictEnum.ORDER_TYPE_BUY_LIMIT.getCode(), DictEnum.ORDER_MODEL_SHUFFLE.getCode());
            //create hb sell
            String hbOrderId = transBiz.hbCreateSaleOrder(hbSymbol, salePrice, amount, shuffle.getQuoteCurrency(), shuffle.getUserId());
            orderBiz.saveHbOrder(hbOrderId, null, zbOrderId, shuffle.getOid(), shuffle.getUserId(), DictEnum.ORDER_TYPE_SELL_LIMIT.getCode(), DictEnum.ORDER_MODEL_SHUFFLE.getCode());

        }
    }

    /**
     * 校验余额
     *
     * @param userId        usreId
     * @param quoteCurrency 业务对
     * @param baseCurrency  基对
     * @param totalAmount   总金额
     * @param amount        购买数量
     * @param buyMarketType 市场类型
     */
    private boolean checkBalance(String userId, String quoteCurrency, String baseCurrency, BigDecimal totalAmount, BigDecimal amount, String buyMarketType) {
        boolean result = true;
        BigDecimal hbMaxBalance;
        BigDecimal zbMaxBalance;
        //hb to buy check
        if (DictEnum.MARKET_TYPE_HB.getCode().equals(buyMarketType)) {
            hbMaxBalance = accountBiz.getHuobiQuoteBalance(userId, baseCurrency);
            if (totalAmount.compareTo(hbMaxBalance) > 0) {
                log.info("hb " + baseCurrency + " 余额不足,totalAmount={},maxBalance={}", totalAmount, hbMaxBalance);
                mailBiz.balanceToEmailNotify(userId, baseCurrency, DictEnum.MARKET_TYPE_HB.getCode());
                result = false;
            } else {
                zbMaxBalance = accountBiz.getZbBalance(userId, quoteCurrency);
                if (amount.compareTo(zbMaxBalance) > 0) {
                    log.info("zb " + quoteCurrency + " 余额不足,amount={},maxBalance={}", amount, hbMaxBalance);
                    mailBiz.balanceToEmailNotify(userId, baseCurrency, DictEnum.MARKET_TYPE_ZB.getCode());
                    result = false;
                }
            }
        } else if (DictEnum.MARKET_TYPE_ZB.getCode().equals(buyMarketType)) {
            zbMaxBalance = accountBiz.getZbBalance(userId, baseCurrency);
            if (totalAmount.compareTo(zbMaxBalance) > 0) {
                log.info("zb " + baseCurrency + " 余额不足,totalAmount={},maxBalance={}", totalAmount, zbMaxBalance);
                mailBiz.balanceToEmailNotify(userId, baseCurrency, DictEnum.MARKET_TYPE_ZB.getCode());
                result = false;
            } else {
                hbMaxBalance = accountBiz.getHuobiQuoteBalance(userId, quoteCurrency);
                if (amount.compareTo(hbMaxBalance) > 0) {
                    log.info("hb " + quoteCurrency + " 余额不足,amount={},maxBalance={}", amount, hbMaxBalance);
                    mailBiz.balanceToEmailNotify(userId, baseCurrency, DictEnum.MARKET_TYPE_HB.getCode());
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * 买入数量
     */
    private BigDecimal buyAmount(BigDecimal totalAmount, BigDecimal buyPrice, String userId, String quote, String buyMarketType) {
        BigDecimal amount;
        BigDecimal balance;
        if (DictEnum.MARKET_TYPE_HB.getCode().equals(buyMarketType)) {
            balance = accountBiz.getHuobiQuoteBalance(userId, quote);
        } else {
            balance = accountBiz.getZbBalance(userId, quote);
        }
        totalAmount = totalAmount.compareTo(balance) < 0 ? totalAmount : balance;
        amount = totalAmount.divide(buyPrice, 1, BigDecimal.ROUND_DOWN);
        return amount;
    }


    /**
     * hb买一、卖一深度
     */
    private DepthOneVo hbGetDepth(String symbol, String userId) {
        DepthOneVo vo = null;
        DepthDto dto = new DepthDto();
        dto.setSymbol(symbol);
        dto.setUserId(userId);
        //获取卖单交易深度
        Depth depth = marketBiz.HbDepth(dto);
        if (depth != null && depth.getBids() != null && depth.getAsks() != null) {
            vo = getDepth(depth.getBids(), depth.getAsks());
        }
        return vo;
    }

    /**
     * zb 买一、卖一深度
     */
    private DepthOneVo zbGetDepth(String symbol, String userId) {
        DepthOneVo vo = null;
        DepthDto dto = new DepthDto();
        dto.setSymbol(symbol);
        dto.setUserId(userId);
        //获取卖单交易深度
        ZbOrderDepthVo depth = zbApi.orderDepth(symbol, 2);
        if (depth != null && depth.getBids() != null && depth.getAsks() != null) {
            vo = getDepth(depth.getBids(), depth.getAsks());
        }
        return vo;
    }

    private DepthOneVo getDepth(List<List<BigDecimal>> bids, List<List<BigDecimal>> asks) {
        DepthOneVo vo = new DepthOneVo();
        vo.setBuyOne(bids.get(0).get(0));
        vo.setSaleOne(asks.get(0).get(0));
        return vo;
    }

    private String groupCurrency(String base, String quote, String marketType) {
        String currency;
        if (DictEnum.MARKET_TYPE_HB.getCode().equals(marketType)) {
            currency = quote + base;
        } else {
            currency = quote + "_" + base;
        }
        return currency;
    }
}
