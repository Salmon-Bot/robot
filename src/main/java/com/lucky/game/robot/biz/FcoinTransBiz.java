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
        BigDecimal salePrice = getRandomPrice(price, entity.getPriceMultiple(), entity.getPriveSlope());
        BigDecimal saleAmount = getRandomAmount(entity.getAmount(), entity.getAmountMultiple());
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
        return orderBiz.saveFcoinOrder(orderId, userId, configId, account);
    }

    /**
     * 价格随机值 原有价格基础随机*-0.1%~0.1% * 倍数波动+斜率波动
     * 例如: 当前价格为10,随机数为-0.05,倍数为5,斜率为0.6% 则 10*((1+(-0.5/1000)*5)+0.6/100)=10.035 为挂单价格
     */
    private static BigDecimal getRandomPrice(BigDecimal price, BigDecimal priceMultiple, BigDecimal priceSlope) {
        //默认一倍波动
        if (priceMultiple == null || priceMultiple.compareTo(BigDecimal.ZERO) == 0) {
            priceMultiple = BigDecimal.ONE;
        }
        if (priceSlope == null) {
            priceSlope = BigDecimal.ZERO;
        }
        double random = (Math.random() / 1000) * priceMultiple.doubleValue();
        //随机正负
        double plusMinus = Math.random();
        if (plusMinus < 0.5) {
            random = 0 - random;
        }
        random += priceSlope.doubleValue() / 100;
        return price.multiply(new BigDecimal(1 + random));
    }

    /**
     * /**
     * 随机值 原有价格基础随机*-0.1%~0.1% * 倍数波动
     */
    private static BigDecimal getRandomAmount(BigDecimal amount, BigDecimal amountMulitple) {
        //默认一倍波动
        if (amountMulitple == null || amountMulitple.compareTo(BigDecimal.ZERO) == 0) {
            amountMulitple = BigDecimal.ONE;
        }
        double random = Math.random() / 100 * amountMulitple.doubleValue();
        //随机正负
        double plusMinus = Math.random();
        if (plusMinus < 0.5) {
            random = 0 - random;
        }
        return amount.multiply(new BigDecimal(1 + random));
    }

    public static void main(String[] s) {
        BigDecimal result = getRandomPrice(new BigDecimal(10), new BigDecimal(5), new BigDecimal(0.6));
        System.err.println(result);
       BigDecimal amount = getRandomAmount(new BigDecimal(10), new BigDecimal(5));
        System.err.println(amount);
    }
}
