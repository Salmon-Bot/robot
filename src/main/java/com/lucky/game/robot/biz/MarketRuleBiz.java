package com.lucky.game.robot.biz;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.entity.SymbolTradeConfigEntity;
import com.lucky.game.robot.entity.UserEntity;
import com.lucky.game.robot.mail.MailQQ;
import com.lucky.game.robot.sms.Sms;
import com.lucky.game.robot.vo.huobi.RateChangeVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author conan
 *         2018/3/30 17:48
 **/
@Component
@Slf4j
public class MarketRuleBiz {


    @Value("${need.sms:false}")
    private boolean needSms;

    /**
     * 初始化监控信息,判断是否有可操作的汇率差
     *
     * @param symbol            交易对
     * @param nowPrice          最新价格
     * @param otherMinPrice     要对比的时间价格(一分钟之前或者5分钟之前)
     * @param symbolTradeConfig 配置
     * @param user              用户信息
     */
    public RateChangeVo initMonitor(String symbol, BigDecimal nowPrice, BigDecimal otherMinPrice, SymbolTradeConfigEntity symbolTradeConfig, UserEntity user) {
        RateChangeVo rateChangeVo = new RateChangeVo();
        BigDecimal increase = (nowPrice.subtract(otherMinPrice)).divide(otherMinPrice, 9, BigDecimal.ROUND_HALF_UP);
        BigDecimal hundredIncrease = increase.multiply(new BigDecimal(100));
        boolean isToOperate = false;
        String content = "";
        //指定时间段内价格降低超过阈值
        if (increase.compareTo(BigDecimal.ZERO) < 0 && (BigDecimal.ZERO.subtract(symbolTradeConfig.getCurrencyAbsolute())).compareTo(increase) >= 0) {
            if (DictEnum.TRADE_CONFIG_THRESHOLD_TYPE_ONE_MIN.getCode().equals(symbolTradeConfig.getThresholdType())) {
                content = symbol + " one min to lower " + hundredIncrease + "%";
            } else {
                content = symbol + " five min to lower " + hundredIncrease + "%";
            }
            isToOperate = true;
            log.info("nowPrice={},otherMinPrice={},thresholdType={},currencyAbsolute={},content={}", nowPrice, otherMinPrice, symbolTradeConfig.getThresholdType(), symbolTradeConfig.getCurrencyAbsolute(), content);
        }
        //指定时间段内价格升高超过阈值
        if (increase.compareTo(BigDecimal.ZERO) > 0 && symbolTradeConfig.getCurrencyAbsolute().compareTo(increase) <= 0) {
            if (DictEnum.TRADE_CONFIG_THRESHOLD_TYPE_ONE_MIN.getCode().equals(symbolTradeConfig.getThresholdType())) {
                content = symbol + " one min to hoist " + hundredIncrease + "%";
            } else {
                content = symbol + " five min to hoist " + hundredIncrease + "%";
            }
            isToOperate = true;
            log.info("nowPrice={},otherMinPrice={},thresholdType={},currencyAbsolute={},content={}", nowPrice, otherMinPrice, symbolTradeConfig.getThresholdType(), symbolTradeConfig.getCurrencyAbsolute(), content);
        }
        rateChangeVo.setOperate(isToOperate);
        rateChangeVo.setRateValue(increase);
        if (isToOperate) {
            // send eamil
//            sendNotifyEmail(content, user.getNotifyEmail());
            //设置通知内容
            rateChangeVo.setContext(content);
        }
        return rateChangeVo;
    }


    /**
     * 计算增加率
     *
     * @param originSymbol            原对
     * @param originNowPrice          原对当前价格
     * @param otherSymbol             现对
     * @param increase                原对增长率
     * @param symbolTradeConfigEntity 配置
     * @param otherNowPrice           要比较的对当前价格
     * @param otherMinPrice           要比较的对之前(1min,5min)类型的价格
     */
    public RateChangeVo getRateChangeVo(String originSymbol, BigDecimal originNowPrice, String otherSymbol,
                                        BigDecimal increase, SymbolTradeConfigEntity symbolTradeConfigEntity, BigDecimal otherNowPrice, BigDecimal otherMinPrice) {
        RateChangeVo rateChangeVo = new RateChangeVo();
        BigDecimal otherMinIncrease = (otherNowPrice.subtract(otherMinPrice)).divide(otherMinPrice, 9, BigDecimal.ROUND_HALF_UP);
        //上升
        if (increase.compareTo(BigDecimal.ZERO) >= 0) {
            // |a-b| > 0.05 两个交易对之间差异过大
            if (new BigDecimal(Math.abs(increase.subtract(otherMinIncrease).doubleValue())).compareTo(symbolTradeConfigEntity.getCurrencyAbsolute()) > 0) {
                rateChangeVo.setBuyerSymbol(otherSymbol);
                rateChangeVo.setBuyPrice(otherNowPrice);
                rateChangeVo.setSaleSymbol(originSymbol);
                rateChangeVo.setBuyPrice(otherNowPrice);
                rateChangeVo.setRateValue(increase.subtract(otherMinIncrease));
            }
        }
        //下降
        if (increase.compareTo(BigDecimal.ZERO) < 0) {
            // |b-a| > 0.05 两个交易对之间差异过大
            if (new BigDecimal(Math.abs(otherMinIncrease.subtract(increase).doubleValue())).compareTo(symbolTradeConfigEntity.getCurrencyAbsolute()) > 0) {
                rateChangeVo.setBuyerSymbol(originSymbol);
                rateChangeVo.setSaleSymbol(otherSymbol);
                rateChangeVo.setBuyPrice(originNowPrice);
                rateChangeVo.setRateValue(otherMinIncrease.subtract(increase));
            }
        }
        //存在其他主区交易对
        rateChangeVo.setHasOtherBase(true);
        log.info("不同交易对比较结果,originSymbol={},otherSymbol={},increase={},nowPrice={},otherMinPrice={},otherMinIncrease={},rateChangeVo={}", originSymbol, otherSymbol, increase, otherNowPrice, otherMinPrice, otherMinIncrease, rateChangeVo);
        return rateChangeVo;
    }

    public void sendSms(String content, String symbol, String phones) {
        if (!needSms) {
            log.info("do not need sms...");
            return;
        }
        if (StringUtils.isNotEmpty(symbol)) {
            Sms.smsSend(content, phones);
        }
    }

    private void sendNotifyEmail(String content, String email) {
        String subject = "market info notify";
        MailQQ.sendEmail(subject, content, email);
    }


    /**
     * 相对主对相乘汇率
     *
     * @param buyPrice  买价
     * @param rateValue 两个主对之间可操作的汇率差
     */
    public BigDecimal getMultiplySalePrice(BigDecimal buyPrice, BigDecimal baseCurrencyPrice, BigDecimal rateValue) {
        BigDecimal salePrice = buyPrice.multiply(baseCurrencyPrice).multiply((new BigDecimal(1).add(new BigDecimal(Math.abs(rateValue.doubleValue())))));
        log.info("getMultiplySalePrice,buyPrice={},baseCurrencyPrice={},rateValue={},salePrice={}", buyPrice, baseCurrencyPrice, rateValue, salePrice);
        return salePrice;
    }


    /**
     * 相对主对相除汇率
     * * @param buyPrice          买价
     *
     * @param rateValue 两个主对之间可操作的汇率差
     */
    public BigDecimal getDivideSalePrice(BigDecimal buyPrice, BigDecimal baseCurrencyPrice, BigDecimal rateValue) {
        BigDecimal salePrice = buyPrice.divide(baseCurrencyPrice, 8, BigDecimal.ROUND_FLOOR).multiply((new BigDecimal(1).add(new BigDecimal(Math.abs(rateValue.doubleValue())))));
        log.info("getMultiplySalePrice,buyPrice={},baseCurrencyPrice={},rateValue={},salePrice={}", buyPrice, baseCurrencyPrice, rateValue, salePrice);
        return salePrice;
    }

    public String getHbBaseCurrency(String symbol) {
        String baseCurrency;
        if (symbol.endsWith(DictEnum.HB_MARKET_BASE_BTC.getCode()) || symbol.endsWith(DictEnum.HB_MARKET_BASE_ETH.getCode())) {
            baseCurrency = symbol.substring(symbol.length() - 3, symbol.length());
        } else {
            baseCurrency = symbol.substring(symbol.length() - 4, symbol.length());
        }
        return baseCurrency;
    }

    public String getHbQuoteCurrency(String symbol) {
        String quoteCurrency;
        if (symbol.endsWith(DictEnum.HB_MARKET_BASE_BTC.getCode()) || symbol.endsWith(DictEnum.HB_MARKET_BASE_ETH.getCode())) {
            quoteCurrency = symbol.substring(0, symbol.length() - 3);
        } else {
            quoteCurrency = symbol.substring(0, symbol.length() - 4);
        }
        return quoteCurrency;
    }


    public String getZbQuoteCurrency(String symbol) {
        String[] quoteCurrencys = symbol.split("_");
        return quoteCurrencys[0];
    }

    public String getZbBaseCurrency(String symbol) {
        String[] quoteCurrencys = symbol.split("_");
        return quoteCurrencys[1];
    }
}
