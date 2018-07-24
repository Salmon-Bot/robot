package com.lucky.game.robot.biz;

import com.lucky.game.core.util.StrRedisUtil;
import com.lucky.game.core.util.StringUtil;
import com.lucky.game.robot.entity.KlineInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * 趋势
 * conan
 * 2018/7/23 下午5:25
 **/
@Component
@Slf4j
public class TrendBizz {


    @Autowired
    private KlineInfoBiz klineInfoBiz;

    @Autowired
    private RedisTemplate<String, String> redis;

    private final static String KDJ_K = "KDJ_K_";

    private final static String KDJ_D = "KDJ_D_";

    private final static String KDJ_J = "KDJ_J_";

    public void oneHoursKdj(){
        String[] symbols = klineInfoBiz.klineSymbols.split(",");
        for (String symbol : symbols){
            kdj(symbol);
        }
    }

    public void kdj(String symbol) {
        try {
            long currHours = getCurrHoursTime();
            KlineInfoEntity kline = klineInfoBiz.findByKlineIdAndSymbol(String.valueOf(currHours), symbol);
            //    n日RSV=（Cn－Ln）/（Hn－Ln）×100,公式中，Cn为第n日收盘价；Ln为n日内的最低价；Hn为n日内的最高价。
            BigDecimal rsv = (kline.getClose().subtract(kline.getLow())).divide(kline.getHigh().subtract(kline.getLow()), 8, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

            //当日K值=2/3×前一日K值+1/3×当日RSV
            BigDecimal yesterdayK = getKdj(KDJ_K + symbol + "_" + (currHours - 3600));
            if (yesterdayK == null) {
                yesterdayK = new BigDecimal(50);
            }
//            当日K值=2/3×前一日K值+1/3×当日RSV
//            当日D值=2/3×前一日D值+1/3×当日K值
//            若无前一日K 值与D值，则可分别用50来代替。
//            J值=3*当日K值-2*当日D值
            BigDecimal todayK = new BigDecimal(2.0 / 3).multiply(yesterdayK).add((new BigDecimal(1.0 / 3).multiply(rsv)));

            BigDecimal yesterdayD = getKdj(KDJ_D + symbol + "_" + (currHours - 3600));
            if (yesterdayD == null) {
                yesterdayD = new BigDecimal(50);
            }
            BigDecimal todayD = new BigDecimal(2.0 / 3).multiply(yesterdayD).add((new BigDecimal(1.0 / 3).multiply(todayK)));
            BigDecimal todayJ = new BigDecimal(3).multiply(todayK).subtract(new BigDecimal(2).multiply(todayD));

            StrRedisUtil.set(redis, KDJ_K + symbol + "_" + currHours, todayK);
            StrRedisUtil.set(redis, KDJ_D + symbol + "_" + +currHours, todayD);
            StrRedisUtil.set(redis, KDJ_J + symbol + "_" + +currHours, todayJ);

            doKdj(todayK, todayD, todayJ);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doKdj(BigDecimal k, BigDecimal d, BigDecimal j) {
        log.info("k={},d={},j={}", k, d, j);
    }

    public BigDecimal getKdj(String kdjKey) {
        String value = StrRedisUtil.get(redis, kdjKey);
        if (StringUtil.isEmpty(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    public long getCurrHoursTime() {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(new Date().getTime());
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        return c1.getTimeInMillis() / 1000;
    }

}
