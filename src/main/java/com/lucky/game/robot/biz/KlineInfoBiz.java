package com.lucky.game.robot.biz;

import com.lucky.game.robot.constant.DictEnum;
import com.lucky.game.robot.entity.KlineInfoEntity;
import com.lucky.game.robot.exception.BizException;
import com.lucky.game.robot.market.HuobiApi;
import com.lucky.game.robot.service.KlineInfoService;
import com.lucky.game.robot.vo.huobi.MarketDetailVo;
import com.lucky.game.robot.vo.huobi.MarketInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author conan
 *         2018/5/31 14:28
 **/
@Component
@Slf4j
public class KlineInfoBiz {

    @Autowired
    private KlineInfoService klineInfoService;

    @Autowired
    private HuobiApi huobiApi;

    @Value("${kline.symbols:eosusdt}")
    private String klineSymbols;


    /**
     * 异步保存行情数据
     */
    @Async("syncKlineInsert")
    public void syncKlineInsert(KlineInfoEntity entity) {
        KlineInfoEntity old = klineInfoService.findByKlineId(entity.getKlineId());
        if (old == null) {
            klineInfoService.insert(entity);
        }
    }


    /**
     *保存行情数据
     */
    public void saveOneHourKline() {
        log.info("保存{}{}行情数据",klineSymbols,DictEnum.MARKET_PERIOD_60MIN.getValue());
        String[] symbols = klineSymbols.split(",");
        for (String symbol : symbols) {
            saveKline(DictEnum.MARKET_PERIOD_60MIN.getCode(), 24, symbol);
        }
    }

    /**
     * 保存k线数据
     */
    public void saveKline(String period, Integer size, String symbol) {
        MarketInfoVo marketInfoVo = huobiApi.getMarketInfo(period, size, symbol);
        List<MarketDetailVo> vos = marketInfoVo.getData();
        for (MarketDetailVo vo : vos) {
            KlineInfoEntity old = klineInfoService.findByKlineId(vo.getId());
            if (old == null) {
                KlineInfoEntity entity = new KlineInfoEntity();
                BeanUtils.copyProperties(vo, entity);
                entity.setPeriod(period);
                entity.setKlineId(vo.getId());
                entity.setSymbol(symbol);
                klineInfoService.insert(entity);
            }
        }
    }


    /**
     * 获取指定行情的k线记录
     */
    public KlineInfoEntity findByKlineId(String klineId) {
        KlineInfoEntity klineInfoEntity = klineInfoService.findByKlineId(klineId);
        if (klineInfoEntity == null) {
            throw new BizException("未找到指定id的k线,klineId=" + klineId);
        }
        return klineInfoEntity;
    }
}
