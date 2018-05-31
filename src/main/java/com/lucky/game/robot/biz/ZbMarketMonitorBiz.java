package com.lucky.game.robot.biz;

import com.lucky.game.core.util.StrRedisUtil;
import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.zb.vo.ZbSymbolInfoVo;
import com.lucky.game.robot.entity.AccountEntity;
import com.lucky.game.robot.entity.SymbolTradeConfigEntity;
import com.lucky.game.robot.entity.UserEntity;
import com.lucky.game.robot.huobi.api.ApiException;
import com.lucky.game.robot.vo.huobi.RateChangeVo;
import com.lucky.game.robot.zb.api.ZbApi;
import com.lucky.game.robot.zb.vo.ZbKineDetailVo;
import com.lucky.game.robot.zb.vo.ZbKineVo;
import com.lucky.game.robot.zb.vo.ZbTickerVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 中币行情监控
 *
 * @author conan
 *         2018/3/8 15:58
 **/
@Component
@Slf4j
public class ZbMarketMonitorBiz {


    @Value("${need.sms:false}")
    private boolean needSms;

    @Value("${zb.monitor.real.symbol:false}")
    private boolean zbMonitorRealSymbol;


    @Autowired
    private ZbApi zbApi;
    @Autowired
    private TransBiz transBiz;

    @Autowired
    private UserBiz userBiz;

    @Autowired
    private AccountBiz accountBiz;

    @Autowired
    private SymbolTradeConfigBiz symbolTradeConfigBiz;

    @Autowired
    private MarketRuleBiz marketRuleBiz;

    @Autowired
    private RedisTemplate<String, String> redis;


    public void zbMonitor(String symbol) {
        ZbKineVo info = zbApi.getKline(symbol, DictEnum.MARKET_PERIOD_1MIN.getCode(), 6);
        if (info != null && info.getData() != null && info.getData().size() > 5) {
            List<ZbKineDetailVo> kineDetailVoList = info.getData();
            // 倒序排列,zb最新数据在最后面
            Collections.reverse(kineDetailVoList);
            List<UserEntity> userList = userBiz.findAllZbByNormal();

            ZbKineDetailVo nowVo = kineDetailVoList.get(0);
            for (UserEntity user : userList) {
                AccountEntity account = accountBiz.getByUserIdAndType(user.getOid(), DictEnum.MARKET_TYPE_ZB.getCode());
                if (account != null && StringUtils.isNotEmpty(account.getApiKey())) {
                    // 1min monitor
                    oneMinMonitor(symbol, nowVo, kineDetailVoList, user);
                    // 5min monitor
//                    fiveMinMonitor(symbol, nowVo, kineDetailVoList, user);
                }
            }
        }

    }

    /**
     * 初始化zb各交易对小数位并开启监控(项目启动时加载)
     */
    @Async("initScaleToRedisAndMonitor")
    public void initScaleToRedisAndMonitor() {
        List<ZbSymbolInfoVo> list = zbApi.getSymbolInfo();
        for (ZbSymbolInfoVo vo : list) {
            StrRedisUtil.set(redis, DictEnum.ZB_CURRENCY_KEY_PRICE.getCode() + vo.getCurrency(), vo.getPriceScale());
            StrRedisUtil.set(redis, DictEnum.ZB_CURRENCY__KEY_AMOUNT.getCode() + vo.getCurrency(), vo.getAmountScale());
        }
        zbAllSymBolsMonitor(list);
    }

    /**
     * 监控交易对,由于zb行情接口每秒只能请求一次,无法多线程，直接项目启动时监控
     */
    private void zbAllSymBolsMonitor(List<ZbSymbolInfoVo> list) {
        while (zbMonitorRealSymbol) {
            log.info("zb symbol monitor start...");
            for (ZbSymbolInfoVo zbSymbolInfoVo : list) {
                try {
                    this.zbMonitor(zbSymbolInfoVo.getCurrency());
                } catch (Exception e) {
                    log.error("zbSymbolInfoVo={},e={}", zbSymbolInfoVo, e);
                }
            }
            log.info("zb symbol monitor end...");
        }

    }

    private void oneMinMonitor(String symbol, ZbKineDetailVo nowVo, List<ZbKineDetailVo> detailVos, UserEntity user) {
        //查询用户一分钟zb交易配置
        SymbolTradeConfigEntity symbolTradeConfig = symbolTradeConfigBiz.findByUserIdAndThresholdTypeAndMarketType(user.getOid(), DictEnum.TRADE_CONFIG_THRESHOLD_TYPE_ONE_MIN.getCode(), DictEnum.MARKET_TYPE_ZB.getCode());
        if (symbolTradeConfig != null) {
            ZbKineDetailVo lastMinVo = detailVos.get(1);
            checkMinMoitor(symbol, nowVo, lastMinVo, user, symbolTradeConfig);
        }

    }

    private void fiveMinMonitor(String symbol, ZbKineDetailVo nowVo, List<ZbKineDetailVo> detailVos, UserEntity user) {
        //查询用户五分钟交易配置
        SymbolTradeConfigEntity symbolTradeConfig = symbolTradeConfigBiz.findByUserIdAndThresholdTypeAndMarketType(user.getOid(), DictEnum.TRADE_CONFIG_THRESHOLD_TYPE_FIVE_MIN.getCode(), DictEnum.MARKET_TYPE_ZB.getCode());
        if (symbolTradeConfig != null) {
            ZbKineDetailVo lastMinVo = detailVos.get(5);
            checkMinMoitor(symbol, nowVo, lastMinVo, user, symbolTradeConfig);
        }
    }

    private void checkMinMoitor(String symbol, ZbKineDetailVo nowVo, ZbKineDetailVo lastMinVo, UserEntity user, SymbolTradeConfigEntity symbolTradeConfig) {
        RateChangeVo rateChangeVo = marketRuleBiz.initMonitor(symbol, nowVo.getClose(), lastMinVo.getClose(), symbolTradeConfig, user);
        if (rateChangeVo.isOperate()) {
            //check buy
            boolean transResult = checkToZbTrans(symbol, nowVo.getClose(), rateChangeVo.getRateValue(), symbolTradeConfig);
            //trans success to send sms
            if (transResult) {
                marketRuleBiz.sendSms(rateChangeVo.getContext(), symbol, user.getNotifyPhone());
            }
        }
    }

    /**
     * 检查是否可交易
     */
    private boolean checkToZbTrans(String symbol, BigDecimal nowPrice, BigDecimal increase, SymbolTradeConfigEntity symbolTradeConfig) {
        log.info("checkToZbTrans,symbol={},increase={}", symbol, increase);
        RateChangeVo rateChangeVo;
        BigDecimal salePrice;
        //应用对
        String quoteCurrency = marketRuleBiz.getZbQuoteCurrency(symbol);

        boolean tranResult = false;
        //检查是否直接购买当前交易对
        rateChangeVo = checkOneQuoteCanTrade(symbol, nowPrice, increase);
        if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
            //卖单价=买单价*(1-(-增长率))
            salePrice = rateChangeVo.getBuyPrice().multiply((new BigDecimal(1).subtract(increase)));
            tranResult = checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
            if (tranResult) {
                return true;
            }
        }

        String otherSymbol;
        //is btc
        if (symbol.endsWith(DictEnum.ZB_MARKET_BASE_BTC.getCode())) {
            //compare to qc
            otherSymbol = zbGroupCurrency(quoteCurrency, DictEnum.ZB_MARKET_BASE_QC.getCode());
            rateChangeVo = zbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
            if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                //原对增长
                if (increase.compareTo(BigDecimal.ZERO) > 0) {
                    salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_BTC.getCode(), DictEnum.ZB_MARKET_BASE_QC.getCode(), rateChangeVo.getRateValue());
                }
                //原对下降
                else {
                    salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_BTC.getCode(), DictEnum.ZB_MARKET_BASE_QC.getCode(), rateChangeVo.getRateValue());
                }
                //验证是否成功创建订单
                tranResult = checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
            }
            //compare to usdt
            if (!tranResult) {
                otherSymbol = zbGroupCurrency(quoteCurrency, DictEnum.ZB_MARKET_BASE_USDT.getCode());
                rateChangeVo = zbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
                if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                    //原对增长
                    if (increase.compareTo(BigDecimal.ZERO) > 0) {
                        salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_BTC.getCode(), DictEnum.ZB_MARKET_BASE_USDT.getCode(), rateChangeVo.getRateValue());
                    }
                    //原对下降
                    else {
                        salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_BTC.getCode(), DictEnum.ZB_MARKET_BASE_USDT.getCode(), rateChangeVo.getRateValue());
                    }
                    //验证是否成功创建订单
                    checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
                }
            }
        }
        //is QC
        else if (symbol.endsWith(DictEnum.ZB_MARKET_BASE_QC.getCode())) {
            //compare to btc
            otherSymbol = zbGroupCurrency(quoteCurrency, DictEnum.ZB_MARKET_BASE_BTC.getCode());
            rateChangeVo = zbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
            if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                //原对增长
                if (increase.compareTo(BigDecimal.ZERO) > 0) {
                    salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_QC.getCode(), DictEnum.ZB_MARKET_BASE_BTC.getCode(), rateChangeVo.getRateValue());
                }
                //原对下降
                else {
                    salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_QC.getCode(), DictEnum.ZB_MARKET_BASE_BTC.getCode(), rateChangeVo.getRateValue());
                }
                //验证是否成功创建订单
                tranResult = checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
            }
            //compare to usdt
            if (!tranResult) {
                otherSymbol = zbGroupCurrency(quoteCurrency, DictEnum.HB_MARKET_BASE_USDT.getCode());
                rateChangeVo = zbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
                if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                    //原对增长
                    if (increase.compareTo(BigDecimal.ZERO) > 0) {
                        salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_QC.getCode(), DictEnum.ZB_MARKET_BASE_USDT.getCode(), rateChangeVo.getRateValue());
                    }
                    //原对下降
                    else {
                        salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_QC.getCode(), DictEnum.ZB_MARKET_BASE_USDT.getCode(), rateChangeVo.getRateValue());
                    }
                    //验证是否成功创建订单
                    checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
                }
            }
        }
        //is usdt
        else {
            //compare to btc
            otherSymbol = zbGroupCurrency(quoteCurrency, DictEnum.ZB_MARKET_BASE_BTC.getCode());
            rateChangeVo = zbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
            if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                //原对增长
                if (increase.compareTo(BigDecimal.ZERO) > 0) {
                    salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_USDT.getCode(), DictEnum.ZB_MARKET_BASE_BTC.getCode(), rateChangeVo.getRateValue());
                }
                //原对下降
                else {
                    salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_USDT.getCode(), DictEnum.ZB_MARKET_BASE_BTC.getCode(), rateChangeVo.getRateValue());
                }
                //验证是否成功创建订单
                tranResult = checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
            }
            //compare to qc
            if (!tranResult) {
                otherSymbol = zbGroupCurrency(quoteCurrency, DictEnum.ZB_MARKET_BASE_QC.getCode());
                rateChangeVo = zbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
                if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                    //原对增长
                    if (increase.compareTo(BigDecimal.ZERO) > 0) {
                        salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_USDT.getCode(), DictEnum.ZB_MARKET_BASE_QC.getCode(), rateChangeVo.getRateValue());
                    }
                    //原对下降
                    else {
                        salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.ZB_MARKET_BASE_USDT.getCode(), DictEnum.ZB_MARKET_BASE_QC.getCode(), rateChangeVo.getRateValue());
                    }
                    //验证是否成功创建订单
                    checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
                }
            }
        }
        return tranResult;
    }

    /**
     * 只有单个交易对的下降趋势检查是否购买
     */
    private RateChangeVo checkOneQuoteCanTrade(String symbol, BigDecimal nowPrice, BigDecimal increase) {
        RateChangeVo rateChangeVo = new RateChangeVo();
        //交易对下降
        if (increase.compareTo(BigDecimal.ZERO) < 0) {
            ZbKineVo info = zbApi.getKline(symbol, DictEnum.MARKET_PERIOD_1MIN.getCode(), 6);
            BigDecimal otherMinPrice;
            BigDecimal otherMinIncrease;
            ZbKineDetailVo oneMinVo = info.getData().get(5);
            otherMinPrice = oneMinVo.getClose();
            //当前价格与5分钟之前的比较
            otherMinIncrease = (nowPrice.subtract(otherMinPrice)).divide(otherMinPrice, 9, BigDecimal.ROUND_HALF_UP);
            //比较降低幅度是否符合购买条件,例如当前价格一分钟内跌幅-5%,但是与5分钟前比较,当前价格跌幅小与-5%,则有可能是几分钟之内拉高又迅速回落，这种情况不购买
            if (otherMinIncrease.compareTo(increase.add(new BigDecimal(0.02))) <= 0) {
                //5分钟符合再比较15分钟结果
                oneMinVo = info.getData().get(15);
                otherMinPrice = oneMinVo.getClose();
                otherMinIncrease = (nowPrice.subtract(otherMinPrice)).divide(otherMinPrice, 9, BigDecimal.ROUND_HALF_UP);
                log.info("5分钟比较符合,比较15分钟结果,otherMinPrice={},otherMinIncrease={}", otherMinPrice, otherMinIncrease);
                if (otherMinIncrease.compareTo(increase.add(new BigDecimal(0.03))) <= 0) {
                    rateChangeVo.setBuyerSymbol(symbol);
                    rateChangeVo.setSaleSymbol(symbol);
                    rateChangeVo.setBuyPrice(nowPrice);
                    rateChangeVo.setRateValue(increase);
                    rateChangeVo.setMarketType(DictEnum.MARKET_TYPE_ZB.getCode());
                }
            }
            log.info("单个交易对下降趋势检查是否需要购买,rateChangeVo={},otherMinPrice={},otherMinIncrease={}", rateChangeVo, otherMinPrice, otherMinIncrease);
        }
        return rateChangeVo;
    }

    private boolean checkTransResult(RateChangeVo rateChangeVo, String quoteCurrency, BigDecimal salePrice, SymbolTradeConfigEntity symbolTradeConfig) {
        log.info("checkTransResult,rateChangeVo={},quoteCurrency={},salePrice={}", rateChangeVo, quoteCurrency, salePrice);
        boolean tranResult = false;
        try {
            rateChangeVo.setQuoteCurrency(quoteCurrency);
            //set sale realPrice
            rateChangeVo.setSalePrice(salePrice);
            //要购买的交易对主对
            String baseCurrency = marketRuleBiz.getZbBaseCurrency(rateChangeVo.getBuyerSymbol());
            rateChangeVo.setBaseCurrency(baseCurrency);
            //交易
            tranResult = transBiz.zbToBuy(rateChangeVo, symbolTradeConfig);
        } catch (ApiException e) {
            log.warn("buy fail.errCode={},errMsg={}", e.getErrCode(), e.getMessage());
        }
        return tranResult;
    }


    /**
     * ZB 不同交易对之间涨跌幅比较
     */
    private RateChangeVo zbCompareToOtherCurrency(String originSymbol, BigDecimal nowPrice, String otherSymbol, BigDecimal increase, SymbolTradeConfigEntity symbolTradeConfigEntity) {
        RateChangeVo rateChangeVo = new RateChangeVo();
        ZbKineVo info = zbApi.getKline(otherSymbol, DictEnum.MARKET_PERIOD_1MIN.getCode(), 6);
        if (info == null) {
            log.info(otherSymbol + " currency not found.");
            return rateChangeVo;
        }
        ZbKineDetailVo zbKineDetailVo = info.getData().get(0);
        BigDecimal otherNowPrice = zbKineDetailVo.getClose();
        BigDecimal otherMinPrice;
        BigDecimal otherMinIncrease;
        if (DictEnum.TRADE_CONFIG_THRESHOLD_TYPE_ONE_MIN.getCode().equals(symbolTradeConfigEntity.getThresholdType())) {
            ZbKineDetailVo oneMinVo = info.getData().get(1);
            otherMinPrice = oneMinVo.getClose();
            otherMinIncrease = (otherNowPrice.subtract(otherMinPrice)).divide(otherMinPrice, 9, BigDecimal.ROUND_HALF_UP);
        } else {
            ZbKineDetailVo oneMinVo = info.getData().get(5);
            otherMinPrice = oneMinVo.getClose();
            otherMinIncrease = (otherNowPrice.subtract(otherMinPrice)).divide(otherMinPrice, 9, BigDecimal.ROUND_HALF_UP);
        }
        rateChangeVo = marketRuleBiz.getRateChangeVo(originSymbol, nowPrice, otherSymbol, increase, symbolTradeConfigEntity, otherNowPrice, otherMinPrice);
        rateChangeVo.setMarketType(DictEnum.MARKET_TYPE_ZB.getCode());
        log.info("compare to other currency. otherSymbol={},increase={},nowPrice={},otherMinPrice={},otherMinIncrease={},rateChangeVo={}", otherSymbol, increase, otherNowPrice, otherMinPrice, otherMinIncrease, rateChangeVo);
        return rateChangeVo;
    }


    /**
     * 相对主对相乘汇率
     *
     * @param buyPrice 买价
     * @param base1    主对1
     * @param base2    主对2
     */
    private BigDecimal getMultiplySalePrice(BigDecimal buyPrice, String base1, String base2, BigDecimal rateValue) {
        String baseCurrencyGroup = twoBaseCurrencyGroup(base1, base2);
        ZbTickerVo info = zbApi.getTicker(baseCurrencyGroup);
        return marketRuleBiz.getMultiplySalePrice(buyPrice, info.getLast(), rateValue);
    }

    /**
     * 相对主对相除汇率
     */
    private BigDecimal getDivideSalePrice(BigDecimal buyPrice, String base1, String base2, BigDecimal rateValue) {
        String baseCurrencyGroup = twoBaseCurrencyGroup(base1, base2);
        ZbTickerVo info = zbApi.getTicker(baseCurrencyGroup);
        return marketRuleBiz.getDivideSalePrice(buyPrice, info.getLast(), rateValue);
    }


    /**
     * 两个主对组合顺序
     */
    private String twoBaseCurrencyGroup(String base1, String base2) {
        String baseSymbol = null;
        //is qc
        if (DictEnum.ZB_MARKET_BASE_QC.getCode().equals(base1)) {
            baseSymbol = base2 + "_" + base1;
        } else if (DictEnum.ZB_MARKET_BASE_QC.getCode().equals(base2)) {
            baseSymbol = base1 + "_" + base2;
        } else if (DictEnum.ZB_MARKET_BASE_USDT.getCode().equals(base1)) {
            baseSymbol = base2 + "_" + base1;
        } else if (DictEnum.ZB_MARKET_BASE_USDT.getCode().equals(base2)) {
            baseSymbol = base1 + "_" + base2;
        }
        log.info("baseSymbol={}", baseSymbol);
        return baseSymbol;
    }


    private String zbGroupCurrency(String quote, String base) {
        return quote + "_" + base;
    }
}
