package com.lucky.game.robot.biz;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.dto.huobi.CreateOrderDto;
import com.lucky.game.robot.entity.AccountEntity;
import com.lucky.game.robot.entity.FcoinLimitConfigEntity;
import com.lucky.game.robot.entity.OrderEntity;
import com.lucky.game.robot.fcoin.FCoinApi;
import com.lucky.game.robot.fcoin.vo.FCoinOrderDetailVo;
import com.lucky.game.robot.fcoin.vo.FCoinTickerVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * conan
 * 2018/7/3 下午5:42
 **/
@Component
@Slf4j
public class FcoinTransBiz {


    @Autowired
    private FCoinApi fCoinApi;

    @Autowired
    private OrderBiz orderBiz;

    @Autowired
    private FcoinLimitConfigBiz fcoinLimitConfigBiz;

    @Autowired
    private AccountBiz accountBiz;


    /**
     * 检查需要交易的配置
     */
    public void checkToTrade() {
        List<FcoinLimitConfigEntity> list = fcoinLimitConfigBiz.findAllInUse();
        for (FcoinLimitConfigEntity entity : list) {
            OrderEntity order = orderBiz.findLastByConfigId(entity.getOid());
            if (order != null) {
                Calendar calender = Calendar.getInstance();
                calender.setTime(order.getCreateTime());
                calender.add(Calendar.SECOND, entity.getWaitTime());
                if (calender.getTime().after(new Date())) {
                    log.info("交易间隔未到,waitTime={},createTime={}", entity.getWaitTime(), order.getCreateTime());
                    continue;
                }
            }
            autoTrade(entity);
        }
    }

    /**
     * 自动交易
     */
    public void autoTrade(FcoinLimitConfigEntity entity) {

        FCoinTickerVo tickerVo = fCoinApi.getTicker(entity.getSymbol());
        BigDecimal price = tickerVo.getTicker().get(0);
        BigDecimal salePrice = getRandomPrice(price, DictEnum.FCOIN_ORDER_TRADE_TYPE_BUY.getCode());
        BigDecimal saleAmount = getRandomAmount(entity.getAmount());
        //创建卖单
        OrderEntity saleOrder = this.createOrder(entity.getSymbol(), entity.getUserId(),
                saleAmount, salePrice, entity.getOid(), DictEnum.FCOIN_ORDER_TRADE_TYPE_SELL.getCode());
        //创建买单
        OrderEntity buyOrder = this.createOrder(entity.getSymbol(), entity.getUserId(), saleAmount,
                salePrice, entity.getOid(), DictEnum.FCOIN_ORDER_TRADE_TYPE_BUY.getCode());

        saleOrder.setBuyOrderId(buyOrder.getOrderId());
        orderBiz.saveOrder(saleOrder);
    }

    /**
     * 检查更新订单状态
     */
    public void checkOrderStatus() {
        List<OrderEntity> list = orderBiz.findFCoinNotFinishOrder();
        for (OrderEntity entity : list) {
            AccountEntity account = accountBiz.getByUserIdAndType(entity.getUserId(), DictEnum.MARKET_TYPE_FCOIN.getCode());
            FCoinOrderDetailVo detailVo = fCoinApi.getDetail(entity.getOrderId(), account);
            if (!detailVo.getState().equals(entity.getState())) {
                entity.setState(detailVo.getState());
                orderBiz.saveOrder(entity);
            }
        }

    }

    /**
     * 创建fcoin订单
     */
    private OrderEntity createOrder(String symbol, String userId, BigDecimal amount, BigDecimal buyPrice, String configId, String orderType) {
        log.info("创建fcoin订单,symbol={},amount={},buyPrice={},configId={},orderType={}", symbol, amount, buyPrice, configId, orderType);
        CreateOrderDto buyOrderDto = new CreateOrderDto();
        buyOrderDto.setSymbol(symbol);
        buyOrderDto.setPrice(buyPrice);
        buyOrderDto.setOrderType(orderType);
        buyOrderDto.setUserId(userId);
        buyOrderDto.setAmount(amount);
        AccountEntity account = accountBiz.getByUserIdAndType(userId, DictEnum.MARKET_TYPE_FCOIN.getCode());
        //创建限价买单
        String orderId = fCoinApi.createOrder(symbol, DictEnum.ORDER_MODEL_LIMIT.getCode(), orderType, amount, buyPrice, account);
        return orderBiz.saveFcoinOrder(orderId, userId, configId,account);
    }

    /**
     * 价格随机值 原有价格基础随机0.01%~0.1%波动
     */
    private BigDecimal getRandomPrice(BigDecimal price, String orderType) {
        BigDecimal randomPrice;
        double random = Math.random() / 1000;
        //买价
        if (DictEnum.FCOIN_ORDER_TRADE_TYPE_BUY.getCode().equals(orderType)) {
            randomPrice = price.multiply(new BigDecimal(1 - random));
        } else {
            //卖价
            randomPrice = price.multiply(new BigDecimal(1 + random));
        }
        return randomPrice;
    }

    /**
     * 数量随机值 原有数量除以100的基础波动 1000+1000/1000*随机0.1~1
     */
    private static BigDecimal getRandomAmount(BigDecimal amount) {
        double random = Math.random() * amount.divide(new BigDecimal(100), BigDecimal.ROUND_HALF_DOWN).doubleValue();
        return amount.add(new BigDecimal(random));
    }
}
