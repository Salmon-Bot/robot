package com.lucky.game.robot.biz;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.entity.UserEntity;
import com.lucky.game.robot.vo.huobi.MarketDetailVo;
import com.lucky.game.robot.entity.SymbolTradeConfigEntity;
import com.lucky.game.robot.huobi.api.ApiException;
import com.lucky.game.robot.market.HuobiApi;
import com.lucky.game.robot.vo.huobi.MarketInfoVo;
import com.lucky.game.robot.vo.huobi.RateChangeVo;
import com.lucky.game.robot.vo.huobi.SymBolsDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * hb行情监控
 *
 * @author conan
 *         2018/3/8 15:58
 **/
@Component
@Slf4j
public class HbMarketMonitorBiz {


    @Value("${need.sms:false}")
    private boolean needSms;

    @Autowired
    private HuobiApi huobiApi;
    @Autowired
    private TransBiz transBiz;

    @Autowired
    private UserBiz userBiz;


    @Autowired
    private SymbolTradeConfigBiz symbolTradeConfigBiz;

    @Autowired
    private MarketRuleBiz marketRuleBiz;

    @Async("marketMonitor")
    public void asyncDoMonitor(List<SymBolsDetailVo> list) {
        for (SymBolsDetailVo detailVo : list) {
            huoBiMonitor(detailVo.getSymbols());
        }
    }

    public void huoBiMonitor(String symbol) {
        MarketInfoVo info = huobiApi.getMarketInfo(DictEnum.MARKET_PERIOD_1MIN.getCode(), 6, symbol);
        if (info != null && info.getData().size() > 5) {
            List<UserEntity> userList = userBiz.findAllHbByNormal();
            for (UserEntity user : userList) {
                MarketDetailVo nowVo = info.getData().get(0);
                // 1min monitor
                oneMinMonitor(symbol, nowVo, info.getData().get(1), user);
                // 5min monitor
//            fiveMinMonitor(symbol, nowVo, info.getData(), userList);
            }
        }

    }

    /**
     * 1分钟行情监控
     */
    public void oneMinMonitor(String symbol, MarketDetailVo nowVo, MarketDetailVo lastMinVo, UserEntity user) {
        //查询用户一分钟hb交易配置
        SymbolTradeConfigEntity symbolTradeConfig = symbolTradeConfigBiz.findByUserIdAndThresholdTypeAndMarketType(user.getOid(), DictEnum.TRADE_CONFIG_THRESHOLD_TYPE_ONE_MIN.getCode(), DictEnum.MARKET_TYPE_HB.getCode());
        if (symbolTradeConfig != null) {
            checkMinMoitor(symbol, nowVo, lastMinVo, user, symbolTradeConfig);
        }
    }

    private void checkMinMoitor(String symbol, MarketDetailVo nowVo, MarketDetailVo lastMinVo, UserEntity user, SymbolTradeConfigEntity symbolTradeConfig) {
        RateChangeVo rateChangeVo = marketRuleBiz.initMonitor(symbol, nowVo.getClose(), lastMinVo.getClose(), symbolTradeConfig, user);
        if (rateChangeVo.isOperate()) {
            //check buy
            boolean transResult = checkToHbTrans(symbol, nowVo.getClose(), rateChangeVo.getRateValue(), symbolTradeConfig);
            //trans success to send sms
            if (transResult) {
                marketRuleBiz.sendSms(rateChangeVo.getContext(), symbol, user.getNotifyPhone());
            }
        }
    }

    /**
     * 检查是否可交易
     */
    private boolean checkToHbTrans(String symbol, BigDecimal nowPrice, BigDecimal increase, SymbolTradeConfigEntity symbolTradeConfig) {
        log.info("checkToHbTrans,symbol={},nowPrice={},increase={}", symbol, nowPrice, increase);
        RateChangeVo rateChangeVo;
        BigDecimal salePrice;
        //应用对
        String quoteCurrency = marketRuleBiz.getHbQuoteCurrency(symbol);

        boolean tranResult = false;

        //优先检查当前交易对是否是下降趋势,是则直接比较是否需要购买当前交易对
        rateChangeVo = checkHbOneQuoteCanTrade(symbol, nowPrice, increase);
        if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
            //卖单价=买单价*(1-(-增长率))
            salePrice = rateChangeVo.getBuyPrice().multiply((new BigDecimal(1).subtract(increase)));
            tranResult = checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
            //购买完成
            if (tranResult) {
                return true;
            }
        }
        String otherSymbol;
        //is btc
        if (symbol.endsWith(DictEnum.HB_MARKET_BASE_BTC.getCode())) {
            //compare to eth
            otherSymbol = quoteCurrency + DictEnum.HB_MARKET_BASE_ETH.getCode();
            rateChangeVo = hbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
            if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                //原对增长
                if (increase.compareTo(BigDecimal.ZERO) > 0) {
                    salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_BTC.getCode(), DictEnum.HB_MARKET_BASE_ETH.getCode(), rateChangeVo.getRateValue());
                }
                //原对下降
                else {
                    salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_BTC.getCode(), DictEnum.HB_MARKET_BASE_ETH.getCode(), rateChangeVo.getRateValue());
                }
                //验证是否成功创建订单
                tranResult = checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
            }
            //compare to usdt
            if (!tranResult) {
                otherSymbol = quoteCurrency + DictEnum.HB_MARKET_BASE_USDT.getCode();
                rateChangeVo = hbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
                if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                    //原对增长
                    if (increase.compareTo(BigDecimal.ZERO) > 0) {
                        salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_BTC.getCode(), DictEnum.HB_MARKET_BASE_USDT.getCode(), rateChangeVo.getRateValue());
                    }
                    //原对下降
                    else {
                        salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_BTC.getCode(), DictEnum.HB_MARKET_BASE_USDT.getCode(), rateChangeVo.getRateValue());
                    }
                    //验证是否成功创建订单
                    tranResult = checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
                }
            }
        }
        //is eth
        else if (symbol.endsWith(DictEnum.HB_MARKET_BASE_ETH.getCode())) {
            //compare to btc
            otherSymbol = quoteCurrency + DictEnum.HB_MARKET_BASE_BTC.getCode();
            rateChangeVo = hbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
            if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                //原对增长
                if (increase.compareTo(BigDecimal.ZERO) > 0) {
                    salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_ETH.getCode(), DictEnum.HB_MARKET_BASE_BTC.getCode(), rateChangeVo.getRateValue());
                }
                //原对下降
                else {
                    salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_ETH.getCode(), DictEnum.HB_MARKET_BASE_BTC.getCode(), rateChangeVo.getRateValue());
                }
                //验证是否成功创建订单
                tranResult = checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
            }
            //compare to usdt
            if (!tranResult) {
                otherSymbol = quoteCurrency + DictEnum.HB_MARKET_BASE_USDT.getCode();
                rateChangeVo = hbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
                if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                    //原对增长
                    if (increase.compareTo(BigDecimal.ZERO) > 0) {
                        salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_ETH.getCode(), DictEnum.HB_MARKET_BASE_USDT.getCode(), rateChangeVo.getRateValue());
                    }
                    //原对下降
                    else {
                        salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_ETH.getCode(), DictEnum.HB_MARKET_BASE_USDT.getCode(), rateChangeVo.getRateValue());
                    }
                    //验证是否成功创建订单
                    tranResult = checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
                }
            }
        }
        //is usdt
        else {
            //compare to btc
            otherSymbol = quoteCurrency + DictEnum.HB_MARKET_BASE_BTC.getCode();
            rateChangeVo = hbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
            if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                //原对增长
                if (increase.compareTo(BigDecimal.ZERO) > 0) {
                    salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_USDT.getCode(), DictEnum.HB_MARKET_BASE_BTC.getCode(), rateChangeVo.getRateValue());
                }
                //原对下降
                else {
                    salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_USDT.getCode(), DictEnum.HB_MARKET_BASE_BTC.getCode(), rateChangeVo.getRateValue());
                }
                //验证是否成功创建订单
                tranResult = checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
            }
            //compare to eth
            if (!tranResult) {
                otherSymbol = quoteCurrency + DictEnum.HB_MARKET_BASE_ETH.getCode();
                rateChangeVo = hbCompareToOtherCurrency(symbol, nowPrice, otherSymbol, increase, symbolTradeConfig);
                if (StringUtils.isNotEmpty(rateChangeVo.getBuyerSymbol())) {
                    //原对增长
                    if (increase.compareTo(BigDecimal.ZERO) > 0) {
                        salePrice = getMultiplySalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_USDT.getCode(), DictEnum.HB_MARKET_BASE_ETH.getCode(), rateChangeVo.getRateValue());
                    }
                    //原对下降
                    else {
                        salePrice = getDivideSalePrice(rateChangeVo.getBuyPrice(), DictEnum.HB_MARKET_BASE_USDT.getCode(), DictEnum.HB_MARKET_BASE_ETH.getCode(), rateChangeVo.getRateValue());
                    }
                    //验证是否成功创建订单
                    tranResult = checkTransResult(rateChangeVo, quoteCurrency, salePrice, symbolTradeConfig);
                }
            }
        }

        return tranResult;
    }


    /**
     * 只有单个交易对的下降趋势检查是否购买
     */
    private RateChangeVo checkHbOneQuoteCanTrade(String symbol, BigDecimal nowPrice, BigDecimal increase) {
        RateChangeVo rateChangeVo = new RateChangeVo();
        //交易对下降
        if (increase.compareTo(BigDecimal.ZERO) < 0) {
            MarketInfoVo info = huobiApi.getMarketInfo(DictEnum.MARKET_PERIOD_1MIN.getCode(), 16, symbol);
            BigDecimal otherMinPrice;
            BigDecimal otherMinIncrease;
            MarketDetailVo oneMinVo = info.getData().get(5);
            otherMinPrice = oneMinVo.getClose();
            //当前价格与5分钟之前的比较
            otherMinIncrease = (nowPrice.subtract(otherMinPrice)).divide(otherMinPrice, 9, BigDecimal.ROUND_HALF_UP);
            //比较降低幅度是否符合购买条件,例如当前价格一分钟内跌幅-5%,但是与5分钟前比较,当前价格跌幅小与-5%,则有可能是几分钟之内拉高又迅速回落，这种情况不购买,0.02表示允许2%的误差
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
                    rateChangeVo.setMarketType(DictEnum.MARKET_TYPE_HB.getCode());
                }
            }
            log.info("单个交易对下降趋势检查是否需要购买,rateChangeVo={},otherMinPrice={}otherMinIncrease={}", rateChangeVo, otherMinPrice, otherMinIncrease);
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
            String baseCurrency = marketRuleBiz.getHbBaseCurrency(rateChangeVo.getBuyerSymbol());
            rateChangeVo.setBaseCurrency(baseCurrency);
            //交易
            tranResult = transBiz.hbToBuy(rateChangeVo, symbolTradeConfig);
        } catch (ApiException e) {
            log.warn("buy fail.errCode={},errMsg={}", e.getErrCode(), e.getMessage());
        }
        return tranResult;
    }

    /**
     * HB 不同交易对之间涨跌幅比较
     */
    private RateChangeVo hbCompareToOtherCurrency(String originSymbol, BigDecimal nowPrice, String otherSymbol, BigDecimal increase, SymbolTradeConfigEntity symbolTradeConfigEntity) {
        RateChangeVo rateChangeVo = new RateChangeVo();
        MarketInfoVo info = huobiApi.getMarketInfo(DictEnum.MARKET_PERIOD_1MIN.getCode(), 6, otherSymbol);
        if (info == null) {
            log.info(otherSymbol + " currency not found.");
            return rateChangeVo;
        }
        log.info("marketInfo={}", info);
        MarketDetailVo marketDetailVo = info.getData().get(0);
        BigDecimal otherNowPrice = marketDetailVo.getClose();
        BigDecimal otherMinPrice;
        BigDecimal otherMinIncrease;
        if (DictEnum.TRADE_CONFIG_THRESHOLD_TYPE_ONE_MIN.getCode().equals(symbolTradeConfigEntity.getThresholdType())) {
            MarketDetailVo oneMinVo = info.getData().get(1);
            otherMinPrice = oneMinVo.getClose();
            otherMinIncrease = (otherNowPrice.subtract(otherMinPrice)).divide(otherMinPrice, 9, BigDecimal.ROUND_HALF_UP);
        } else {
            MarketDetailVo oneMinVo = info.getData().get(5);
            otherMinPrice = oneMinVo.getClose();
            otherMinIncrease = (otherNowPrice.subtract(otherMinPrice)).divide(otherMinPrice, 9, BigDecimal.ROUND_HALF_UP);
        }
        rateChangeVo = marketRuleBiz.getRateChangeVo(originSymbol, nowPrice, otherSymbol, increase, symbolTradeConfigEntity, otherNowPrice, otherMinPrice);
        rateChangeVo.setMarketType(DictEnum.MARKET_TYPE_HB.getCode());
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
        MarketInfoVo info = huobiApi.getMarketInfo(DictEnum.MARKET_PERIOD_1MIN.getCode(), 1, baseCurrencyGroup);
        return marketRuleBiz.getMultiplySalePrice(buyPrice, info.getData().get(0).getClose(), rateValue);
    }

    /**
     * 相对主对相除汇率
     */
    private BigDecimal getDivideSalePrice(BigDecimal buyPrice, String base1, String base2, BigDecimal rateValue) {
        String baseCurrencyGroup = twoBaseCurrencyGroup(base1, base2);
        MarketInfoVo info = huobiApi.getMarketInfo(DictEnum.MARKET_PERIOD_1MIN.getCode(), 1, baseCurrencyGroup);
        return marketRuleBiz.getDivideSalePrice(buyPrice, info.getData().get(0).getClose(), rateValue);
    }


    /**
     * 两个主对组合顺序
     */
    private String twoBaseCurrencyGroup(String base1, String base2) {
        String baseSymbol = null;
        //is usdt
        if (DictEnum.HB_MARKET_BASE_USDT.getCode().equals(base1)) {
            baseSymbol = base2 + base1;
        } else if (DictEnum.HB_MARKET_BASE_USDT.getCode().equals(base2)) {
            baseSymbol = base1 + base2;
        } else if (DictEnum.HB_MARKET_BASE_BTC.getCode().equals(base1)) {
            baseSymbol = base2 + base1;
        } else if (DictEnum.HB_MARKET_BASE_BTC.getCode().equals(base2)) {
            baseSymbol = base1 + base2;
        }
        log.info("baseSymbol={}", baseSymbol);
        return baseSymbol;
    }

}
