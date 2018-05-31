package com.lucky.game.robot.biz;

import com.lucky.game.robot.entity.KlineInfoEntity;
import com.lucky.game.robot.entity.UserEntity;
import com.lucky.game.robot.market.HuobiApi;
import com.lucky.game.robot.vo.huobi.MarketDetailVo;
import com.lucky.game.robot.vo.huobi.SymBolsDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author conan
 *         2018/5/11 14:20
 **/
@Component
@Slf4j
public class HbSocketBiz {

    @Autowired
    private HuobiApi huobiApi;

    @Autowired
    private UserBiz userBiz;

    @Autowired
    private KlineInfoBiz klineInfoBiz;

    @Autowired
    private HbMarketMonitorBiz hbMarketMonitorBiz;

    public List<String> getAllSymbol() {
        List<String> allSymbol = new ArrayList<>();
        List<SymBolsDetailVo> list = huobiApi.getSymbolsInfo();
        for (SymBolsDetailVo vo : list) {
            allSymbol.add(vo.getSymbols());
        }
        return allSymbol;
    }

    /**
     * 接收k线数据
     */
    public void reciveKLine(List<MarketDetailVo> marketDetailList) {
        List<UserEntity> userList = userBiz.findAllHbByNormal();
        //前一分钟数据
        Long lastKlineId;
        for (MarketDetailVo marketDetail : marketDetailList) {
            log.info("marketDetail={}", marketDetail);
            try {
                KlineInfoEntity klineInfo = new KlineInfoEntity();
                BeanUtils.copyProperties(marketDetail, klineInfo);
                klineInfo.setKlineId(marketDetail.getId());
                klineInfoBiz.syncKlineInsert(klineInfo);
                for (UserEntity user : userList) {
                    lastKlineId = Long.valueOf(klineInfo.getKlineId()) - 60;
                    KlineInfoEntity oldKline = klineInfoBiz.findByKlineId(String.valueOf(lastKlineId));
                    MarketDetailVo lastMarketDetail = new MarketDetailVo();
                    BeanUtils.copyProperties(oldKline,lastMarketDetail);
                    // 1min monitor
                    hbMarketMonitorBiz.oneMinMonitor(klineInfo.getSymbol(), marketDetail, marketDetail, user);
                }
            }catch (Exception e){
                log.error("real monitor do fail.marketDetail={},e={}",marketDetail,e);
            }

        }
    }
}
